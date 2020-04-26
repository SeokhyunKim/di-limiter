package dicounter.overlaynet.utils;

import static dicounter.overlaynet.utils.Exceptions.logError;

import dicounter.overlaynet.node.NodeAddress;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

    public static NodeAddress createRandomTestNodeAddress() {
        return createTestNodeAddress(ThreadLocalRandom.current().nextInt(2000, 3000));
    }

    public static NodeAddress createTestNodeAddress(int port) {
        return NodeAddress.builder()
                          .ipAddress("127.0.0.1")
                          .port(port)
                          .build();
    }

    /**
     * Get reflection Method of an object for testing.
     *
     * @param clz        Class instance of an object
     * @param methodName method name.
     * @param paramTypes list of parameter types
     * @return Found method which is changed to be accessible.
     */
    public static Method getMethod(@NonNull final Class<?> clz, @NonNull final String methodName, Class<?>... paramTypes) {
        try {
            return clz.getDeclaredMethod(methodName, paramTypes);
        } catch (final NoSuchMethodException e) {
            throw logError(new IllegalStateException("Failed to get a method " + methodName, e));
        }
    }



    /**
     * Call method of an object. Always set accessible as true, so this call call private method.
     *
     * @param method reflection Method.
     * @param object an object of the Method.
     * @param args   arguments of the method.
     * @return method return object.
     */
    public static Object callMethod(@Nullable final Object object, @NonNull final Method method, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw logError(new IllegalStateException("Failed to call a method " + method.getName(), e));
        }
    }

    public static Object getMemberVariable(@NonNull final Object object, @NonNull final String fieldName) {
        try {
            final Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw logError(new IllegalStateException("Failed to get a field " + fieldName, e));
        }
    }

    public static void setMemberVariable(@NonNull final Object object, @NonNull final String fieldName, @NonNull final Object value) {
        try {
            final Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (final NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            throw logError(new IllegalStateException("Failed to get a field " + fieldName, e));
        }
    }

    public static Object getStaticVariable(@NonNull final Class<?> clz, @NonNull final String fieldName) {
        try {
            final Field field = clz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw logError(new IllegalStateException("Failed to get a field " + fieldName, e));
        }
    }
}
