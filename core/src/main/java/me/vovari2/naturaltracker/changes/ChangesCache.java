package me.vovari2.naturaltracker.changes;

import me.vovari2.naturaltracker.Console;
import me.vovari2.naturaltracker.Database;
import me.vovari2.naturaltracker.settings.Settings;
import org.bukkit.Location;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChangesCache {
    private volatile static ChangesCache IMP;

    private final ConcurrentHashMap<Position, Boolean> map = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Position> queue = new ConcurrentLinkedQueue<>();
    private final SerialWorker worker;

    private int size;
    private int maxSize;
    private int currentSize;

    public ChangesCache(int size, int buffer) {
        this.size = size;
        this.maxSize = size + buffer;
        this.worker = new SerialWorker("natural-tracker-worker", 1_000, t -> Console.error("Не получилось обработать задачу в очереди задач!", t));
    }

    private void doAdd(Position position) {
        if (map.putIfAbsent(position, Boolean.TRUE) != null)
            return;

        queue.add(position);
        currentSize++;
        if (currentSize > maxSize) doEvict();

        Database.insertPosition(position);
    }
    private void doReload(int newSize, int newBuffer) {
        // Пересоздание пула соединений с БД
        Database.reload();

        this.size = newSize;
        this.maxSize = newSize + newBuffer;
        if (currentSize > newSize) doEvict();
    }
    private void doEvict() {
        Position k;
        while (currentSize > size && (k = queue.poll()) != null) {
            map.remove(k);
            currentSize--;
        }
    }

    public static void log(Location location) {
        ChangesCache c = IMP;
        if (c == null) {
            Console.error("Не получилось записать изменение блока, кэш не инициализирован! (location=%s)".formatted(location));
            return;
        }

        Position position = Position.of(location);
        c.worker.execute(() -> c.doAdd(position));
    }
    public static boolean has(Location location) {
        ChangesCache c = IMP;
        if (c == null) return false;

        // Только кэш, без обращения к БД — ради скорости ответа
        return c.map.containsKey(Position.of(location));
    }

    public static synchronized void enable() {
        // При повторном вызове закрываем старый воркер и пул со сбросом буфера
        disable();

        Database.enable();
        IMP = new ChangesCache(Settings.CHANGES.MAX_SIZE, Settings.CHANGES.BUFFER);
    }
    public static void reload() {
        ChangesCache c = IMP;
        if (c == null) {
            Console.warn("Не получилось перезагрузить кэш, кэш не инициализирован! Инициализация нового кэша!");
            enable();
            return;
        }

        final int maxSize = Settings.CHANGES.MAX_SIZE;
        final int buffer = Settings.CHANGES.BUFFER;
        c.worker.execute(() -> c.doReload(maxSize, buffer));
    }
    public static void disable(){
        ChangesCache c = IMP;
        if (c == null) return;

        IMP = null;
        c.worker.close();

        // Закрытие пула соединений с БД
        Database.disable();
    }
}
