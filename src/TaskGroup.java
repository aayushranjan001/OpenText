import java.util.UUID;

public record TaskGroup(
        UUID groupId
) {
    public TaskGroup {
        if(groupId == null) {
            throw new IllegalArgumentException("All parameters must not be null");
        }
    }
}
