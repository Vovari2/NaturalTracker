package me.vovari2.naturaltracker.changes;

import me.vovari2.naturaltracker.Console;
import me.vovari2.naturaltracker.settings.Settings;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChangesCache {
    private volatile static ChangesCache IMP;

    private final ConcurrentHashMap<Key, Boolean> map = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Key> queue = new ConcurrentLinkedQueue<>();
    private final SerialWorker worker;

    private volatile int size;
    private volatile int maxSize;
    private int currentSize;

    public ChangesCache(int size, int buffer) {
        this.size = size;
        this.maxSize = size + buffer;
        this.worker = new SerialWorker("natural-tracker-worker", 1_000, t -> Console.error("Не получилось обработать задачу в очереди задач!", t));
    }

    private void doAdd(Key key) {
        if (map.putIfAbsent(key, Boolean.TRUE) == null) {
            queue.add(key);
            currentSize++;
            if (currentSize > maxSize) doEvict();
        }
    }
    private void doReload(int newSize, int newBuffer) {
        this.size = newSize;
        this.maxSize = newSize + newBuffer;
        if (currentSize > newSize) doEvict();
    }
    private void doEvict() {
        Key k;
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

        Key key = Key.of(location);
        c.worker.execute(() -> c.doAdd(key));
    }
    public static boolean has(Location location) {
        ChangesCache c = IMP;
        if (c == null) return false;
        return c.map.containsKey(Key.of(location));
    }

    public static synchronized void enable() {
        if (IMP != null)
            IMP.worker.close();

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
    }

    private record Key(UUID world, int x, int y, int z){
        static Key of(Location loc){
            return new Key(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }
}
