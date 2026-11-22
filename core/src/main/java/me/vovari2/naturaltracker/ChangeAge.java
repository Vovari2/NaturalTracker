package me.vovari2.naturaltracker;

/** Давность последнего отслеживаемого изменения. Порядок значений соответствует двум битам в БД. */
public enum ChangeAge {
    LESS_THAN_MINUTE,
    ONE_TO_FIVE_MINUTES,
    FIVE_TO_TWENTY_MINUTES,
    TWENTY_MINUTES_OR_MORE;

    /** Интервал по прошедшему времени в миллисекундах; отрицательное время считается нулевым. */
    public static ChangeAge fromElapsed(long millis) {
        if (millis < 60_000) return LESS_THAN_MINUTE;
        if (millis < 300_000) return ONE_TO_FIVE_MINUTES;
        if (millis < 1_200_000) return FIVE_TO_TWENTY_MINUTES;
        return TWENTY_MINUTES_OR_MORE;
    }
}
