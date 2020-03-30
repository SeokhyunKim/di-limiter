package dicounter.overlaynet.utils;

import com.google.common.testing.NullPointerTester;
import org.junit.Test;

public class ObjectMappersTest {

    @Test
    public void nullTest() {
        NullPointerTester npt = new NullPointerTester();
        npt.testAllPublicStaticMethods(ObjectMappers.class);
    }
}
