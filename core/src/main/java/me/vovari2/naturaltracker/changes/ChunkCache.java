package me.vovari2.naturaltracker.changes;

import me.vovari2.naturaltracker.Console;
import me.vovari2.naturaltracker.Database;
import me.vovari2.naturaltracker.NaturalTracker;
import me.vovari2.naturaltracker.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Изменённые блоки по чанкам. Чанк живёт в кэше, пока загружен: данные подгружаются из БД при загрузке чанка
 * и сохраняются при его выгрузке и периодически.
 * <p>
 * Все методы, работающие с кэшем (log, has, isReady, события чанков), вызываются ТОЛЬКО из главного потока:
 * ни карта чанков, ни {@link ChunkEntry} не потокобезопасны. С БД работает отдельный воркер, которому
 * передаются уже готовые копии данных.
 */
public class ChunkCache {
    private static ChunkCache IMP;
    private static boolean ASYNC_WARNED;

    private final Map<ChunkKey, ChunkEntry> map = new HashMap<>();
    private final SerialWorker worker;

    private BukkitTask autosaveTask;
    public ChunkCache() {
        this.worker = new SerialWorker("natural-tracker-worker", 1_000, t -> Console.error("Не получилось обработать задачу в очереди задач!", t));
    }

    /** Только главный поток. */
    public static void onBlockChange(Location loc) {
        ChunkCache c = IMP;
        if (c == null) {
            Console.error("Не получилось записать изменение блока, кэш не инициализирован! (location=%s)".formatted(loc));
            return;
        }
        if (!isPrimaryThread("log")) return;

        World world = loc.getWorld();
        int index = ChunkEntry.index(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), world.getMinHeight(), world.getMaxHeight());
        if (index < 0) return;

        c.map.computeIfAbsent(ChunkKey.of(loc), k -> new ChunkEntry()).set(index);
    }
    /**
     * Только главный поток (из других потоков вернёт false).
     * Смотрит только кэш, без обращения к БД: пока данные чанка не подгрузились ({@link #changeIsAccurate}), ответ может быть неточным.
     */
    public static boolean wasChanged(Location loc) {
        final ChunkCache c = IMP;
        if (c == null) return false;
        if (!isPrimaryThread("has")) return false;

        World world = loc.getWorld();
        int index = ChunkEntry.index(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), world.getMinHeight(), world.getMaxHeight());
        if (index < 0) return false;

        ChunkEntry entry = c.map.get(ChunkKey.of(loc));
        return entry != null && entry.has(index);
    }
    /** Только главный поток. true, если данные чанка из БД уже подгружены и has() точен. */
    public static boolean changeIsAccurate(Location location) {
        ChunkCache c = IMP;
        if (c == null) return false;
        if (!isPrimaryThread("isReady")) return false;

        ChunkEntry entry = c.map.get(ChunkKey.of(location));
        return entry != null && entry.isLoaded();
    }
    private static boolean isPrimaryThread(String method) {
        if (Bukkit.isPrimaryThread()) return true;

        if (!ASYNC_WARNED) {
            ASYNC_WARNED = true;
            Console.warn("ChangesCache.%s вызван не из главного потока, вызов проигнорирован!".formatted(method), new IllegalStateException());
        }
        return false;
    }

    public static void onChunkLoad(Chunk chunk, boolean isNew) {
        ChunkCache c = IMP;
        if (c == null) return;

        ChunkKey key = ChunkKey.of(chunk);
        ChunkEntry entry = c.map.computeIfAbsent(key, k -> new ChunkEntry());

        if (entry.requested) return;
        entry.requested = true;

        if (isNew) {
            entry.loaded = true; // Новый чанк: в БД его быть не может
            return;
        }

        c.worker.execute(() -> {
            BitSet stored = null;
            boolean success = true;
            try {
                stored = Database.loadChunk(key);
            } catch (Exception e) {
                success = false;
                Console.error("Не получилось загрузить чанк %s из БД!".formatted(key), e);
            }

            // Плагин выключился, пока шла загрузка
            NaturalTracker plugin = NaturalTracker.getInstance();
            if (!plugin.isEnabled()) return;

            BitSet finalStored = stored;
            boolean finalSuccess = success;
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Чанк уже выгрузился, пока шла загрузка
                if (c.map.get(key) != entry) return;

                if (finalStored != null) entry.merge(finalStored);
                if (finalSuccess) entry.loaded = true;
            });
        });
    }
    public static void onChunkUnload(Chunk chunk) {
        ChunkCache c = IMP;
        if (c == null) return;

        ChunkKey key = ChunkKey.of(chunk);
        ChunkEntry entry = c.map.remove(key);
        if (entry == null || !entry.dirty) return;

        List<ChunkSnapshot> snapshots = List.of(ChunkSnapshot.of(key, entry));
        c.worker.execute(() -> Database.saveChunks(snapshots));
    }

    public static synchronized void enable() {
        disable();

        Database.enable();
        ChunkCache c = new ChunkCache();
        IMP = c;

        // Чанки, загруженные до включения кэша (например, после /reload сервера)
        for (World world : Bukkit.getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                c.onChunkLoad(chunk, false);

        c.startAutosave();
    }
    public static void reload() {
        ChunkCache c = IMP;
        if (c == null) {
            Console.warn("Не получилось перезагрузить кэш, кэш не инициализирован! Инициализация нового кэша!");
            enable();
            return;
        }

        c.worker.execute(Database::reload);
        c.startAutosave();
    }
    public static synchronized void disable(){
        ChunkCache c = IMP;
        if (c == null) return;

        IMP = null;
        if (c.autosaveTask != null)
            c.autosaveTask.cancel();

        c.flushDirty();
        c.worker.close();

        // Закрытие пула соединений с БД
        Database.disable();
    }

    private void startAutosave() {
        if (autosaveTask != null) autosaveTask.cancel();

        int seconds = Settings.CHANGES.AUTOSAVE;
        if (seconds <= 0) {
            autosaveTask = null;
            return;
        }
        long ticks = seconds * 20L;
        autosaveTask = Bukkit.getScheduler().runTaskTimer(NaturalTracker.getInstance(), this::flushDirty, ticks, ticks);
    }
    private void flushDirty() {
        List<ChunkSnapshot> snapshots = new ArrayList<>();
        map.forEach((key, entry) -> {
            if (entry.dirty)
                snapshots.add(ChunkSnapshot.of(key, entry));
        });
        if (snapshots.isEmpty()) return;

        worker.execute(() -> Database.saveChunks(snapshots));
    }
}
