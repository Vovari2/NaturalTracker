package me.vovari2.naturaltracker.messages;

import me.vovari2.naturaltracker.Console;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public enum Messages {
    DONT_HAVE_PERMISSION("<red>Недостаточно прав!"),
    NOT_ENOUGH_ARGUMENTS("<red>Недостаточно аргументов!"),
    INVALID_FIRST_ARGUMENT("<red>Неверный первый аргумент!"),
    INVALID_PAYLOAD_TYPE("<red>Неверный тип пейлоада!"),
    INVALID_PAYLOAD_FORMAT("<red>Неверный формат пейлоада!"),
    NOT_ENOUGH_PAYLOAD_ARGUMENTS("<red>Недостаточно аргументов для пейлоада!"),

    RELOAD_SUCCESS("<#54B435>Плагин был перезагружен! ({time} ms)"),
    RELOAD_FAILED("<#FACF68>Плагин не был перезагружен из-за ошибки! <newline><#FACF68>{error}"),

    NEW_SUCCESS("<#54B435>Статистика была записана!"),
    NEW_FAILED("<#FACF68>Статистика не была записана из-за ошибки! <newline><#FACF68>{error}"),

    LOOKUP_RESULT_NOT_FOUND("<#6FAF4F>StrixStats <white>- Результаты не найдены."),
    LOOKUP_PAGE_MUST_BE_POSITIVE("<#FACF68>Номер страницы должен быть положительным!"),
    LOOKUP_FAILED("<#FACF68>Статистика не была получена из-за ошибки! <newline><#FACF68>{error}"),
    LOOKUP_HEADER("<white>----- <#6FAF4F>StrixStats <white>| <#6FAF4F>Результаты (<color:white>{type}</color>) <white>-----"),
    LOOKUP_FOOTER("<white>------- <hover:show_text:'<gray>Предыдущая страница'><click:run_command:'sxstats lookup {type} {condition} page={prev_page}'>◀</click></hover> <color:#6FAF4F>Страница</color> <white>{current_page} <hover:show_text:'<gray>Следующая страница'><click:run_command:'sxstats lookup {type} {condition} page={next_page}'><white>▶</click></hover> -------"),
    LOOKUP_PLAYER_BUY_LINE_1("<gray>{time_hours}/h назад - <#6FAF4F>{player} <white>купил x{amount} <#6FAF4F>{product}"),
    LOOKUP_PLAYER_BUY_LINE_2("            <gray>^ Сервер: {server} Цена: {cost} Причина: {reason}"),
    LOOKUP_PLAYER_JOIN_LINE("<gray>{time_hours}/h назад - <#6FAF4F>{player} <white>зашёл на сервер <#6FAF4F>{server}"),
    LOOKUP_PLAYER_QUIT_LINE_1("<gray>{time_hours}/h назад - <#6FAF4F>{player} <white>вышел с сервера <#6FAF4F>{server}"),
    LOOKUP_PLAYER_QUIT_LINE_2("            <gray>^ Время сессии: {duration} ч.");

    private Message message;
    Messages(String message){
        this.message = new Message(message);
    }
    public void set(@NotNull Message message){
        this.message = message;
    }

    public String string(){
        return message.string();
    }
    public Message message(){
        return message;
    }

    public Message silent(boolean silent){
        return message.silent(silent);
    }
    public Message newline(){
        return message.newline();
    }
    public Message append(@NotNull Message message){
        return message.append(message);
    }

    public Message replace(@NotNull String placeholder, int replacement){
        return message.replace(placeholder, replacement);
    }
    public Message replace(@NotNull String placeholder, @NotNull String replacement){
        return message.replace(placeholder, replacement);
    }
    public boolean send(@NotNull Audience audience){
        return message.send(audience);
    }

    public static void enable(){
        try { new Loader(); }
        catch(Exception e){ Console.error("Failed to load messages: %s".formatted(e.getMessage())); }
    }
}
