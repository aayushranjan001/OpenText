import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        TaskExecutor executor = new TaskExecutorImpl(3);
        TaskGroup group1 = new TaskGroup(UUID.randomUUID());
        TaskGroup group2 = new TaskGroup(UUID.randomUUID());

        Task<Integer> task1 = new Task<>(UUID.randomUUID(), group1, TaskType.READ, () -> {
            Thread.sleep(1000);
            return 10;
        });

        Task<Integer> task2 = new Task<>(UUID.randomUUID(), group1, TaskType.WRITE, () -> {
            Thread.sleep(500);
            return 20;
        });

        Task<Integer> task3 = new Task<>(UUID.randomUUID(), group2, TaskType.READ, () -> 30);

        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            try {
                return executor.submitTask(task1).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                return executor.submitTask(task2).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Integer> f3 = CompletableFuture.supplyAsync(() -> {
            try {
                return executor.submitTask(task3).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        f1.thenAccept(result -> System.out.println("Task 1 Result: " + result));
        f2.thenAccept(result -> System.out.println("Task 2 Result: " + result));
        f3.thenAccept(result -> System.out.println("Task 3 Result: " + result));
        CompletableFuture.allOf(f1, f2, f3).join();
        executor.shutDown();

    }

}