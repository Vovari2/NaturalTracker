package me.vovari2.naturaltracker.changes;

import me.vovari2.naturaltracker.Console;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public final class SerialWorker implements AutoCloseable {
    private final BlockingQueue<Runnable> tasks;
    private final Thread thread;
    private final Consumer<Throwable> errorHandler;
    private volatile boolean running = true;

    public SerialWorker(String name, int queueCapacity, Consumer<Throwable> errorHandler) {
        this.tasks = new LinkedBlockingQueue<>(queueCapacity);
        this.errorHandler = errorHandler;
        this.thread = new Thread(this::processLoop, name);
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void execute(Runnable task) {
        if (!running) {
            Console.warn("Не получилось добавить задачу в очередь, поток не запущен!");
            return;
        }

        if (tasks.offer(task))
            return;

        task.run();
    }
    public void close() {
        running = false;
        thread.interrupt();
    }
    private void processLoop() {
        while (running) {
            try {
                tasks.take().run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                errorHandler.accept(t);
            }
        }
    }
}