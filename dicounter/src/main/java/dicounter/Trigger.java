package dicounter;

import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString(exclude = "countingCallback")
@EqualsAndHashCode(exclude = "countingCallback")
@RequiredArgsConstructor
@AllArgsConstructor
class Trigger {

    private final UUID triggerId;

    @Setter
    private Consumer<Long> countingCallback;


    static Trigger create() {
        return new Trigger(UUID.randomUUID());
    }

    static Trigger create(@NonNull final Consumer<Long> countingCallback) {
        return new Trigger(UUID.randomUUID(), countingCallback);
    }
}
