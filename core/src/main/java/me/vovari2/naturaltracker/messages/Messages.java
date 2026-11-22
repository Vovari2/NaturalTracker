package me.vovari2.naturaltracker.messages;

import me.vovari2.naturaltracker.Console;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public enum Messages {
    DONT_HAVE_PERMISSION("<red>Недостаточно прав!"),
    NOT_ENOUGH_ARGUMENTS("<red>Недостаточно аргументов!"),
    INVALID_COMMAND_ARGUMENT("<red>Неверный аргумент команды!"),
    ONLY_FOR_PLAYERS("<red>Команда доступна только игрокам!"),

    RELOAD_SUCCESS("<#54B435>Плагин был перезагружен! ({time} ms)"),
    INSPECT_SUCCESS("<gradient:#54B435:#82CD47>Выдан инспектор для блоков плагина!"),
    INSPECT_INVENTORY_FULL("<red>Не удалось выдать инспектор, инвентарь заполнен!"),
    INSPECT_CHUNK_LOADING("<yellow>Данные чанка ещё загружаются, попробуйте снова!"),
    INSPECT_GENERATED("<green>Блок был сгенерирован!"),
    INSPECT_PLACED("<red>Блок был поставлен!"),
    INSPECT_DESTROYED("<red>Блок был уничтожен!"),
    INSPECT_AGE_LESS_THAN_MINUTE("<yellow>Последнее изменение: менее минуты назад."),
    INSPECT_AGE_ONE_TO_FIVE_MINUTES("<yellow>Последнее изменение: от 1 до 5 минут назад."),
    INSPECT_AGE_FIVE_TO_TWENTY_MINUTES("<yellow>Последнее изменение: от 5 до 20 минут назад."),
    INSPECT_AGE_TWENTY_MINUTES_OR_MORE("<yellow>Последнее изменение: 20 минут назад или более.");

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
        return this.message.append(message);
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

    public static void initialize(){
        try { new Loader(); }
        catch(Exception e){ Console.error("Не удалось загрузить сообщения!", e); }
    }
}
