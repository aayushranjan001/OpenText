import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class TaskExecutorImpl implements TaskExecutor{

    private final ExecutorService executorService;
    private final Map<UUID, BlockingQueue<Runnable>> groupQueues = new ConcurrentHashMap<>();
    private final Semaphore concurrencyLimiter;

    public TaskExecutorImpl(int maxConcurrency) {
        this.executorService = Executors.newFixedThreadPool(maxConcurrency);
        this.concurrencyLimiter = new Semaphore(maxConcurrency);
    }

    @Override
    public <T> Future<T> submitTask(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        UUID groupId = task.taskGroup().groupId();

        groupQueues.putIfAbsent(groupId, new LinkedBlockingQueue<>());
        BlockingQueue<Runnable> taskQueue = groupQueues.get(groupId);

        Runnable taskWrapper = () -> {
            try {
                concurrencyLimiter.acquire();
                T result = task.taskAction().call();
                future.complete(result);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            } finally {
                concurrencyLimiter.release();
                processNextTask(groupId);
            }
        };

        synchronized (taskQueue) {
            taskQueue.add(taskWrapper);
            if(taskQueue.size() == 1)
                executorService.submit(taskWrapper);
        }
        return future;
    }

    private void processNextTask(UUID groupId) {
        BlockingQueue<Runnable> queue = groupQueues.get(groupId);
        if(queue != null) {
            synchronized (queue) {
                queue.poll();
                Runnable nextTask = queue.peek();
                if(nextTask != null) {
                    executorService.submit(nextTask);
                }
            }
        }
    }

    public void shutDown() {
        executorService.shutdown();
    }
}
