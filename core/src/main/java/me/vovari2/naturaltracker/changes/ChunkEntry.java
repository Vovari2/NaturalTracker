package me.vovari2.naturaltracker.changes;

import me.vovari2.naturaltracker.ChangeAge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/** Изменённые блоки одного чанка. Живой кэш используется только из главного потока, копии — также в воркере. */
public final class ChunkEntry {
    private static final int FORMAT = 0x4E544301; // NTC, версия 1; прежний формат без заголовка не поддерживается
    private static final ChangeAge[] AGES = ChangeAge.values();
    // Индекс блока: ((y - minY) << 8) | (z << 4) | x, где x и z — координаты внутри чанка.
    // На блок четыре бита: изменён, поставлен (1) или уничтожен (0), два бита давности.
    private final BitSet bits = new BitSet();
    private final Map<Integer, Long> recentChanges = new HashMap<>();

    boolean requested; // Загрузка из БД уже запрошена (повторно не запрашиваем)
    boolean loaded; // Данные из БД уже подмешены
    boolean dirty; // Есть изменения, которых ещё нет в БД

    public BlockState state(int index) {
        int bit = index << 2;
        if (!bits.get(bit)) return BlockState.GENERATED;
        return bits.get(bit + 1) ? BlockState.PLACED : BlockState.DESTROYED;
    }
    public void setPlaced(int index) {
        setChanged(index, true, System.currentTimeMillis());
    }
    public void setDestroyed(int index) {
        setChanged(index, false, System.currentTimeMillis());
    }
    private void setChanged(int index, boolean placed, long now) {
        int bit = index << 2;
        bits.set(bit);
        bits.set(bit + 1, placed);
        bits.clear(bit + 2, bit + 4);
        recentChanges.put(index, now);
        dirty = true;
    }
    public boolean isLoaded() { return loaded; }

    public @Nullable ChangeAge age(int index, long now) {
        int bit = index << 2;
        if (!bits.get(bit)) return null;

        Long changedAt = recentChanges.get(index);
        if (changedAt != null) {
            ChangeAge age = updateAge(index, changedAt, now);
            if (age == ChangeAge.TWENTY_MINUTES_OR_MORE) recentChanges.remove(index);
            return age;
        }
        return AGES[(bits.get(bit + 2) ? 1 : 0) | (bits.get(bit + 3) ? 2 : 0)];
    }
    void updateAges(long now) {
        var iterator = recentChanges.entrySet().iterator();
        while (iterator.hasNext()) {
            var change = iterator.next();
            if (updateAge(change.getKey(), change.getValue(), now) == ChangeAge.TWENTY_MINUTES_OR_MORE)
                iterator.remove();
        }
    }
    private ChangeAge updateAge(int index, long changedAt, long now) {
        ChangeAge age = ChangeAge.fromElapsed(now - changedAt);
        int bit = (index << 2) + 2;
        boolean low = (age.ordinal() & 1) != 0;
        boolean high = (age.ordinal() & 2) != 0;
        if (bits.get(bit) != low || bits.get(bit + 1) != high) {
            bits.set(bit, low);
            bits.set(bit + 1, high);
            dirty = true;
        }
        return age;
    }

    // Блоки, изменённые пока шла загрузка, новее данных из БД и не затираются ими
    public void merge(@NotNull ChunkEntry stored) {
        stored.recentChanges.forEach((index, time) -> {
            if (!bits.get(index << 2)) recentChanges.put(index, time);
        });
        BitSet merged = overlay(bits, stored.bits);
        bits.clear();
        bits.or(merged);
        dirty |= stored.dirty;
        updateAges(System.currentTimeMillis());
    }
    public byte[] toBytes() {
        updateAges(System.currentTimeMillis());
        if (bits.isEmpty()) return new byte[0];

        byte[] raw = bits.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(12 + raw.length + recentChanges.size() * 12);
        buffer.putInt(FORMAT).putInt(raw.length).put(raw).putInt(recentChanges.size());
        recentChanges.forEach((index, time) -> buffer.putInt(index).putLong(time));
        return buffer.array();
    }
    public static ChunkEntry fromBytes(byte[] raw) throws DataFormatException {
        ChunkEntry entry = new ChunkEntry();
        if (raw.length == 0) return entry;
        if (raw.length < 12) throw new DataFormatException("Неполные данные чанка");

        ByteBuffer buffer = ByteBuffer.wrap(raw);
        if (buffer.getInt() != FORMAT) throw new DataFormatException("Неподдерживаемый формат данных чанка");
        int length = buffer.getInt();
        if (length < 0 || length > buffer.remaining() - 4)
            throw new DataFormatException("Неверная длина состояний чанка");
        byte[] states = new byte[length];
        buffer.get(states);
        entry.bits.or(BitSet.valueOf(states));
        int count = buffer.getInt();
        if (count < 0 || (long) count * 12 != buffer.remaining())
            throw new DataFormatException("Неверное число временных отметок чанка");
        for (int i = 0; i < count; i++) {
            int index = buffer.getInt();
            long time = buffer.getLong();
            if (index < 0 || index > (Integer.MAX_VALUE >>> 2) || !entry.bits.get(index << 2)
                    || time < 0 || entry.recentChanges.put(index, time) != null)
                throw new DataFormatException("Неверная временная отметка чанка");
        }
        entry.updateAges(System.currentTimeMillis());
        return entry;
    }

    /** Состояния блоков из newer перекрывают состояния тех же блоков из older. */
    public static BitSet overlay(BitSet newer, BitSet older) {
        BitSet result = (BitSet) older.clone();
        for (int i = newer.nextSetBit(0); i >= 0; i = newer.nextSetBit((i | 3) + 1))
            result.clear(i & ~3, (i & ~3) + 4);
        result.or(newer);
        return result;
    }

    public static int index(int x, int y, int z, int minY, int maxY) {
        if (y < minY || y >= maxY) return -1;
        return ((y - minY) << 8) | ((z & 15) << 4) | (x & 15);
    }

    // Сжатие и распаковка данных чанка
    public static byte[] encode(byte[] raw) {
        if (raw.length == 0) return raw;

        Deflater deflater = new Deflater();
        deflater.setInput(raw);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 4 + 16);
        byte[] buffer = new byte[1_024];
        while (!deflater.finished())
            out.write(buffer, 0, deflater.deflate(buffer));
        deflater.end();
        return out.toByteArray();
    }
    public static ChunkEntry decode(byte[] data) throws DataFormatException {
        if (data.length == 0) return new ChunkEntry();

        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
        byte[] buffer = new byte[1_024];
        try {
            while (!inflater.finished()) {
                int n = inflater.inflate(buffer);
                if (n == 0 && (inflater.needsInput() || inflater.needsDictionary()))
                    throw new DataFormatException("Неполные данные чанка");
                out.write(buffer, 0, n);
            }
        } finally {
            inflater.end();
        }
        return fromBytes(out.toByteArray());
    }
}
