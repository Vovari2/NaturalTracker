package me.vovari2.naturaltracker.changes;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.BitSet;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/** Изменённые блоки одного чанка. Используется только из главного потока. */
public final class ChunkEntry {
    // Индекс бита: ((y - minY) << 8) | (z << 4) | x, где x и z — координаты внутри чанка
    private final BitSet bits = new BitSet();

    boolean requested; // Загрузка из БД уже запрошена (повторно не запрашиваем)
    boolean loaded; // Данные из БД уже подмешены
    boolean dirty; // Есть изменения, которых ещё нет в БД

    public boolean has(int index) { return bits.get(index); }
    public void set(int index) {
        bits.set(index);
        dirty = true;
    }
    public boolean isLoaded() { return loaded; }

    // OR, чтобы не затереть изменения, залогированные пока шла загрузка
    void merge(@NotNull BitSet other) { bits.or(other); }
    byte[] toBytes() { return bits.toByteArray(); }

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
