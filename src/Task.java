import java.util.UUID;
import java.util.concurrent.Callable;

public record Task<T> (
        UUID taskUUID,
        TaskGroup taskGroup,
        TaskType taskType,
        Callable<T> taskAction
){
    public Task {
        if(taskUUID == null || taskGroup == null || taskType == null || taskAction == null) {
            throw new IllegalArgumentException("All parameters must not be null");
        }
    }
}
