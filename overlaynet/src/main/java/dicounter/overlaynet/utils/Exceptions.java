package dicounter.overlaynet.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Exceptions {
    public static RuntimeException logError(@NonNull final RuntimeException re) {
        log.error("Caught an runtime exception.", re);
        return re;
    }
}
