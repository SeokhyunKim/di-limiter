package dicounter.overlaynet.node;

import static dicounter.overlaynet.utils.Exceptions.logError;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dicounter.overlaynet.communication.Message;
import dicounter.overlaynet.exception.BadRequestException;
import dicounter.overlaynet.exception.NetworkException;
import dicounter.overlaynet.utils.Messages;
import dicounter.overlaynet.utils.ObjectMappers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class HttpNodeExchangeHandler implements HttpHandler {

    @NonNull
    private final HttpNode httpNodeImpl;

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            if ("POST".equals(httpExchange.getRequestMethod())) {
                final String requestBody = readRequestBody(httpExchange);
                final Message message = ObjectMappers.readValue(requestBody, Message.class);
                httpNodeImpl.processReceivedMessage(message);
                final Optional<Message> responseOpt = httpNodeImpl.createResponseMessage(message);
                responseOpt.ifPresent(response -> httpNodeImpl.sendMessage(message.getSender(), response));
                httpExchange.sendResponseHeaders(200, 0);
                log.debug("Http message handling is done. Message: {}", message);
            } else {
                final String response = ObjectMappers.writeValueAsString(Messages.createExceptionMessage("Only POST call is supported"));
                httpExchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
                final OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
                log.warn("Only accepting POST for exchanging messages between nodes. Request body: {}", httpExchange.getRequestBody());
            }
        } catch (final IOException e) {
            throw logError(new NetworkException("Caught an exception while responsing to a received message", e));
        }
    }

    private String readRequestBody(@NonNull final HttpExchange httpExchange) {
        final InputStream inputStream = httpExchange.getRequestBody();
        final StringBuilder stringBuilder = new StringBuilder();
        try (final Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char)c);
            }
        } catch (final IOException e) {
            throw logError(new BadRequestException("Caught an exception while reading request body", e));
        }
        return stringBuilder.toString();
    }
}
