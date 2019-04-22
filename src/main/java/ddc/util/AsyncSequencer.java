package ddc.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class AsyncSequencer<T2> {
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Общее количество вызовов run()
     */
    private AtomicLong total = new AtomicLong(0L);

    private AtomicLong next = new AtomicLong(0L);

    @Getter
    private Map<String, T2> runningThreads = new ConcurrentHashMap<>();

    /**
     * Асинхронный запуск с учетом порядка вызова данного метода.
     * Для разделения долгой операции на две части:
     *    1. Синхронной: для соблюдения порядка вызовов (Включение транзакции в мемпул с инкрементом nonce)
     *    2. Ассинхронной: для ожидания результата, когда порядок неважен (Включение транзакции в блок)
     * @param callable
     * @param dto
     * @param <T>
     * @return
     */
    public <T> CompletableFuture<T> run(Callable<T> callable, T2 dto) {
        long current = total.getAndIncrement();
        CompletableFuture<T> result = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            String threadName = getClass().getSimpleName() + current;
            Thread.currentThread().setName(threadName);
            runningThreads.put(threadName, dto);
            waitForAhead(current);
            try {
                result.complete(callable.call());
            } catch (Exception e) {
                log.error("Transaction #{} error: {}", dto, e.getMessage());
                result.completeExceptionally(e);
            }
            runningThreads.remove(threadName);
        }, executor);

        return result;
    }

    /**
     * Ожидание своей очереди
     * @param current
     */
    private synchronized void waitForAhead(long current) {
        if (!runningThreads.containsKey(Thread.currentThread().getName())) {
            log.warn("Вызов ожидания вне {}", this.getClass().getSimpleName());
            return;
        }
        while (current != next.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Позволяет следующему потоку отправить транзакцию в мемпул
     */
    public synchronized void allowNext() {
        if (!runningThreads.containsKey(Thread.currentThread().getName())) {
            log.warn("Вызов уведомления вне {}", this.getClass().getSimpleName());
            return;
        }
        next.getAndIncrement();
        notifyAll();
    }
}
