package dicounter.overlaynet.communication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.testing.NullPointerTester;
import dicounter.overlaynet.exception.NetworkException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

// happy cases are covered by SocketNodeTest. This is mainly checking code paths throwing exceptions and etc
// many cases are missed because mocking final method of library class like DataOutputStream is not allowed.
public class ExchangeMessageTest {

    @Test
    public void constructor_throwException_whenSocketThrowsException() throws IOException {
        Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenThrow(IOException.class);
        Assertions.assertThrows(NetworkException.class,
                                () -> ExchangeMessage.create(socket));
    }

    @Test
    public void nullTest() throws IOException {
        NullPointerTester npt = new NullPointerTester();
        OutputStream os = mock(OutputStream.class);
        InputStream is = mock(InputStream.class);
        Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(os);
        when(socket.getInputStream()).thenReturn(is);
        ExchangeMessage em = ExchangeMessage.create(mock(Socket.class));
        npt.testAllPublicInstanceMethods(em);
        npt.testAllPublicStaticMethods(ExchangeMessage.class);
    }
}
