package me.vovari2.naturaltracker.changes;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.BitSet;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/** Изменённые блоки одного чанка. Используется только из главного потока. */
public final class ChunkEntry {
    // Индекс блока: ((y - minY) << 8) | (z << 4) | x, где x и z — координаты внутри чанка.
    // На блок два соседних бита: 2 * index — изменён, 2 * index + 1 — поставлен (1) или уничтожен (0); второй бит значим только у изменённого
    private final BitSet bits = new BitSet();

    boolean requested; // Загрузка из БД уже запрошена (повторно не запрашиваем)
    boolean loaded; // Данные из БД уже подмешены
    boolean dirty; // Есть изменения, которых ещё нет в БД

    public BlockState state(int index) {
        int bit = index << 1;
        if (!bits.get(bit)) return BlockState.GENERATED;
        return bits.get(bit + 1) ? BlockState.PLACED : BlockState.DESTROYED;
    }
    public void setPlaced(int index) {
        int bit = index << 1;
        bits.set(bit, bit + 2);
        dirty = true;
    }
    public void setDestroyed(int index) {
        int bit = index << 1;
        bits.set(bit);
        bits.clear(bit + 1);
        dirty = true;
    }
    public boolean isLoaded() { return loaded; }

    // Блоки, изменённые пока шла загрузка, новее данных из БД и не затираются ими
    void merge(@NotNull BitSet stored) {
        BitSet merged = overlay(bits, stored);
        bits.clear();
        bits.or(merged);
    }
    byte[] toBytes() { return bits.toByteArray(); }

    /** Состояния блоков из newer перекрывают состояния тех же блоков из older. */
    public static BitSet overlay(BitSet newer, BitSet older) {
        BitSet result = (BitSet) older.clone();
        for (int i = newer.nextSetBit(0); i >= 0; i = newer.nextSetBit((i | 1) + 1))
            result.clear(i & ~1, (i & ~1) + 2);
        result.or(newer);
        return result;
    }

    public static int index(int x, int y, int z, int minY, int maxY) {
        if (y < minY || y >= maxY) return -1;
        return ((y - minY) << 8) | ((z & 15) << 4) | (x & 15);
    }

    // Сжатие и распаковка BitSet
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
    public static BitSet decode(byte[] data) throws DataFormatException {
        if (data.length == 0) return new BitSet();

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
        return BitSet.valueOf(out.toByteArray());
    }
}
