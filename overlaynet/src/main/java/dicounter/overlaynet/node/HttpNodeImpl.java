package dicounter.overlaynet.node;

import static dicounter.overlaynet.utils.Exceptions.logError;

import com.google.common.collect.Sets;
import com.sun.net.httpserver.HttpServer;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.exception.BadRequestException;
import dicounter.overlaynet.exception.NetworkException;
import dicounter.overlaynet.utils.ObjectMappers;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpNodeImpl extends BaseNodeImpl {

    private static final int BACK_LOGGING = 0;
    private static final int HTTP_SERVER_STOP_DELAY_IN_SECONDS = 1;
    private static Set<Integer> successResponseCodes = Sets.newHashSet(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED);

    @NonNull
    private final ExecutorService executorService;

    private final HttpServer httpServer;
    private final String apiPath;

    public HttpNodeImpl(@NonNull final NodeAddress nodeAddress, @NonNull final String apiPath,
                        @NonNull final ExecutorService executorService) {
        super(nodeAddress);
        addKnownAddress(nodeAddress);
        this.apiPath = apiPath;
        this.executorService = executorService;
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(nodeAddress.getIpAddress(), nodeAddress.getPort()), BACK_LOGGING);
        } catch (final IOException e) {
            throw logError(new NetworkException("Failed to create http server with " + nodeAddress, e));
        }
    }

    @Override
    public void sendMessage(NodeAddress nodeAddress, Message message) {
        message.setSender(getNodeAddress());
        message.setReceiver(nodeAddress);
        final URL url;
        HttpURLConnection conn = null;
        try {
            url = new URL("http://" + nodeAddress.getIpAddress() + ":" + nodeAddress.getPort() + apiPath);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            final OutputStream os = conn.getOutputStream();
            os.write(ObjectMappers.writeValueAsString(message).getBytes(StandardCharsets.UTF_8));
            os.flush();

            log.debug("Made a POST call with a message {}", message);

            if (!successResponseCodes.contains(conn.getResponseCode())) {
                throw logError(new NetworkException("Failed to make post api call. url: " + conn.getURL().getPath() +
                                                    ", error code: " + conn.getResponseCode()));
            }
        } catch (final MalformedURLException e) {
            throw logError(new BadRequestException("Malformed url: " + apiPath, e));
        } catch (final IOException e) {
            throw logError(new BadRequestException("Failed to make connection. url: " + apiPath, e));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    public void startMessageListening() {
        super.startMessageListening();
        this.httpServer.createContext(apiPath, new NodeHttpHandler(this));
        this.httpServer.setExecutor(this.executorService);
        this.httpServer.start();
        log.info("HttpServer for message listening started on {} ", getNodeAddress());
    }

    @Override
    public void stopMessageListening() {
        super.stopMessageListening();
        this.httpServer.stop(HTTP_SERVER_STOP_DELAY_IN_SECONDS);
        log.info("HttpServer for message listening stopped on {}", getNodeAddress());
    }
}
