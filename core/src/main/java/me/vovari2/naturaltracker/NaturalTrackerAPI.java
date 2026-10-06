package me.vovari2.naturaltracker;

import me.vovari2.naturaltracker.changes.BlockState;
import me.vovari2.naturaltracker.changes.ChunkCache;
import org.bukkit.Location;

/**
 * API для других плагинов: показывает, что в последний раз происходило с блоком — он создан миром,
 * его поставили или его уничтожили. Для каждого блока верно ровно одно из трёх состояний.
 * <p>
 * Все методы вызываются <b>только из главного потока</b>: из других потоков вызов игнорируется
 * и блок считается сгенерированным ({@link #wasGenerated} вернёт {@code true}, остальные методы — {@code false}).
 * Ответ берётся из кэша без обращения к БД, поэтому пока данные чанка не подгрузились из БД
 * (первые мгновения после его загрузки), он может быть неточным: блок, который на самом деле поставили
 * или уничтожили, может ещё считаться сгенерированным.
 */
public interface NaturalTrackerAPI {
    /**
     * Проверяет, что блок создан миром и с ним ничего не делали ни игроки, ни механизмы.
     *
     * @param location положение блока; дробные координаты округляются до блока, мир должен быть задан
     * @return {@code true}, если блок сгенерирован миром; {@code false}, если его поставили или уничтожили
     * (вне главного потока — {@code true})
     */
    static boolean wasGenerated(Location location){
        return ChunkCache.stateOf(location) == BlockState.GENERATED;
    }

    /**
     * Проверяет, что последним действием с блоком была его установка.
     *
     * @param location положение блока; дробные координаты округляются до блока, мир должен быть задан
     * @return {@code true}, если блок поставили (игрок или механизм); {@code false} в остальных случаях
     * (вне главного потока — {@code false})
     */
    static boolean wasPlaced(Location location){
        return ChunkCache.stateOf(location) == BlockState.PLACED;
    }

    /**
     * Проверяет, что последним действием с блоком было его уничтожение.
     *
     * @param location положение блока; дробные координаты округляются до блока, мир должен быть задан
     * @return {@code true}, если блок уничтожили (игрок или механизм); {@code false} в остальных случаях
     * (вне главного потока — {@code false})
     */
    static boolean wasDestroyed(Location location){
        return ChunkCache.stateOf(location) == BlockState.DESTROYED;
    }
}
