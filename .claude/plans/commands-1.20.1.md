# План: перенос команд NaturalTracker на Paper 1.20.1

## Проблема

`Executor` регистрирует команды через Brigadier Lifecycle API
(`io.papermc.paper.command.brigadier.*`, `LifecycleEvents.COMMANDS`), которого в Paper 1.20.1 нет
(появилось в 1.20.6). `commands/ReloadCommand` и `commands/InspectorCommand` тоже завязаны на
`CommandContext<CommandSourceStack>`. Переносим на `BukkitCommand` + `CommandMap`, как в StrixStats.

## Изменения

### 1. `Executor` → наследник `BukkitCommand` (по образцу `StrixStatsCommand`)

Имя класса оставляю `Executor` (как в CLAUDE.md и как ты назвал). Внутри:

```java
public class Executor extends BukkitCommand {
    public final static String PERMISSION = "naturaltracker.*";

    private final NaturalTracker instance;
    public Executor(NaturalTracker instance){
        super("naturaltracker", "Команда для работы с плагином", "/<command>", List.of("ntracker"));
        this.instance = instance;
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args){
        if (!sender.hasPermission(PERMISSION)) return sendMessage(sender, "<red>У вас нет прав на эту команду!");
        if (args.length < 1) return sendMessage(sender, "<red>Недостаточно аргументов!");

        @Nullable Command command = switch(args[0]){
            case "reload" -> new ReloadCommand(instance, sender);
            case "inspect" -> new InspectorCommand(instance, sender);
            default -> null;
        };

        if (command == null) return sendMessage(sender, "<red>Неверный первый аргумент!");
        return command.execute();
    }

    public @NotNull List<String> tabComplete(...){
        if (!sender.hasPermission(PERMISSION) || args.length != 1) return List.of();
        return Stream.of("reload", "inspect").filter(s -> s.startsWith(args[0])).toList();
    }

    static void register(@NotNull NaturalTracker instance){
        instance.getServer().getCommandMap().register("naturaltracker", new Executor(instance));
    }
}
```

- В StrixStats после проверки прав нет `return` (команда выполняется даже без прав) — здесь сделаю с `return`.
- Таб-комплит добавлю (в Brigadier-версии он был бесплатно), в StrixStats он пустой.
- Алиас `ntracker` — под вопросом, можно без алиасов.
- Отдельного `Messages` как в StrixStats нет — сообщения строками MiniMessage через `TextUtils.toComponent`,
  как сейчас. Если хочешь — могу вынести в `Messages`-enum, но это больше кода.

### 2. `commands/Command` — новый абстрактный класс

Копия из StrixStats: поля `instance`, `sender`, `args`, абстрактный `boolean execute()`.

### 3. `commands/ReloadCommand extends Command`

Сохраняю текущее поведение (синхронный `instance.onReload()` + сообщение
`<gradient:#54B435:#82CD47>Плагин был перезагружен!`). Асинхронный вариант из StrixStats не нужен:
`ChangesCache.reload()` и так уходит в `SerialWorker`, а перерегистрация слушателей должна идти в основном потоке.
Можно добавить время из `onReload()` в сообщение: `Плагин был перезагружен! (%d ms)`.

### 4. `commands/InspectorCommand extends Command`

- Тот же код выдачи предмета; не-игроку — сообщение «Команда доступна только игроку!» вместо тихого выхода.
- `getBlockInspector()` остаётся `public static`.
- **`itemMeta.setEnchantmentGlintOverride(true)` нет в 1.20.1** (появилось в 1.20.5). Замена:
  `addEnchant(Enchantment.LUCK, 1, true)` + `addItemFlags(ItemFlag.HIDE_ENCHANTS)`.

### 5. `plugin.yml`

Ничего не нужно: `BukkitCommand` через `CommandMap` не требует секции `commands:`.
Опционально — секция `permissions:` для `naturaltracker.*` с `default: op`.

## Найденное попутно (вне команд) — исправить?

1. **Ключ инспектора не совпадает.** `InspectorCommand` ставит
   `NamespacedKey.fromString("natural_tracker.inspector")` → это `minecraft:natural_tracker.inspector`,
   а `InspectorListener` проверяет `NaturalTracker.getInspectorNamespacedKey()` = `natural_traker:inspector`
   (ещё и опечатка). Выданный предмет листенер не распознает. Предлагаю везде использовать один ключ
   `natural_tracker:inspector` (оставить один источник: либо `NamespacedKeyUtils`, либо поле в `NaturalTracker`).
2. **`PLUGIN_NAME` / `VERSION` в `NaturalTracker` нигде не присваиваются** — в логах выходит `null null`.
   Добавить в `onEnable` как в StrixStats: `getPluginMeta().getName()` / `getVersion()`.

## Проверка

`./gradlew :core:shadowJar`, запуск на Paper 1.20.1: `/naturaltracker reload`, `/naturaltracker inspect`,
таб-комплит, отказ без прав, клик инспектором по блоку.
