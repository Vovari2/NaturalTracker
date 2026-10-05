# NaturalTracker

Плагин для Paper (Minecraft 1.20+), который отслеживает, был ли блок **сгенерирован миром** или **изменён игроком/механизмом**.
Изменённые блоки хранятся по чанкам: `BitSet` на чанк в in-memory кэше (`ChunkCache`, только пока чанк загружен) и в БД (SQLite / MySQL / PostgreSQL через пул Agroal, одна сжатая строка на чанк).
Другие плагины используют `NaturalTrackerAPI.wasGenerated(Location)`.

Автор: Vovari2. Язык сообщений, логов и комментариев — **русский**.

## Сборка

- Gradle, Java 17, модули `api` и `core`.
- `./gradlew :core:shadowJar` — собирает `NaturalTracker-<version>.jar` прямо в `D:\Minecraft\Servers\Paper 1.20.1\plugins\`.
- Версия задаётся в корневом `build.gradle` (`allprojects.version`) и подставляется в `plugin.yml`.
- Тестов нет; проверка — запуск на локальном сервере Paper.

## Структура (`core/src/main/java/me/vovari2/naturaltracker/`)

| Файл / пакет | Назначение |
|---|---|
| `NaturalTracker` | Главный класс `JavaPlugin`: `onEnable` / `onDisable` / `onReload`, регистрация слушателей |
| `NaturalTrackerCommand` | `BukkitCommand` `/naturaltracker` (алиас `/nt`), регистрируется через `getCommandMap()`; проверяет `naturaltracker.admin` и по `args[0]` создаёт подкоманду |
| `Console` | Статический логгер, принимает строки MiniMessage |
| `Database`, `DatabaseType` | Пул соединений; `loadChunk` / `saveChunks` (пачка в одной транзакции, вызываются только из воркера); необязательное ограничение числа чанков в таблице (`database.limit`), удаляются давно не обновлявшиеся; SQL для каждого типа БД лежит в enum |
| `changes/ChunkCache` | Синглтон-кэш `HashMap<ChunkKey, ChunkEntry>`. **`onBlockChange` / `wasChanged` / `changeIsAccurate` и события чанков — только из главного потока** (из других потоков игнорируются). Загрузка из БД при `ChunkLoadEvent` (асинхронно, результат подмешивается в главном потоке), сохранение при выгрузке и по таймеру `changes.autosave` (при неудачной записи `dirty` возвращается). Если загрузка из БД упала — ошибка в консоль, чанк считается новым |
| `changes/ChunkEntry` | `BitSet` изменённых блоков чанка (индекс `((y - minY) << 8) \| (z << 4) \| x`), флаги `loaded` / `dirty`; сжатие Deflate |
| `changes/ChunkKey` | Ключ чанка: UUID мира + cx/cz (в БД UUID пишется как `byte[16]`) |
| `changes/ChunkSnapshot` | Копия битов для записи; `merge = true`, если данные из БД не успели подгрузиться (воркер подмешает старые перед записью) |
| `changes/SerialWorker` | Один фоновый поток с ограниченной очередью задач (при переполнении ждёт место, `close()` дописывает очередь до конца) |
| `listeners/` | `BlockListener` (place/break/pistons → `ChunkCache.onBlockChange`), `ChunkListener` (load/unload чанков), `InspectorListener` (предмет-инспектор) |
| `commands/` | Подкоманды — наследники абстрактного `Command(instance, sender, args)` с `boolean execute()`; `ReloadCommand` (`reload`), `InspectorCommand` (`inspect`, выдаёт предмет-инспектор) |
| `messages/` | `Messages` — enum сообщений MiniMessage с плейсхолдерами `{name}` (`.replace(...).send(sender)`); `Loader` читает и дописывает `messages.json` в папке плагина |
| `settings/` | `Settings` — статические поля во вложенных классах; `Loader` читает `settings.yml` |
| `placeholders/` | `NaturalTrackerExpansion` — PlaceholderAPI (softdepend): `%naturaltracker_has_<world>_<x>_<y>_<z>%` → `wasChanged`; `{...}` внутри раскрываются как плейсхолдеры, разбор координат справа |
| `utils/` | `FileUtils` (YAML/JSON), `TextUtils.toComponent` (MiniMessage) |

Модуль `api` содержит копию `NaturalTrackerAPI` и зависит от `core` как `compileOnly`.

## Жизненный цикл

`onEnable`: `Messages.initialize()` → `Settings.initialize()` → `ChunkCache.enable()` (внутри `Database.enable()`, затем подключение уже загруженных чанков и автосохранение) → слушатели → регистрация `NaturalTrackerCommand`.
PDC-ключ предмета-инспектора — `NaturalTracker.getInspectorNamespacedKey()`.
`onReload`: `Messages.initialize()` → `Settings.initialize()` → `ChunkCache.reload()` (в воркере: `Database.reload()`; пересоздаётся таймер автосохранения) → перерегистрация слушателей.
`onDisable`: `ChunkCache.disable()` (сбрасывает все изменённые чанки в воркер, воркер дописывает очередь и закрывается, затем `Database.disable()`).

## Стиль кода

Писать в стиле существующего кода:

- **Статические менеджеры** с методами `enable()` / `reload()` / `disable()` вместо DI (у конфигов `Settings` / `Messages` — один `initialize()`, он же для reload). Синглтон — поле `private static ... IMP` / `INSTANCE`.
- **Именование**: статические поля и константы — `UPPER_SNAKE_CASE` (даже не final: `DATA_SOURCE`, `BUFFER`); поля экземпляра — `camelCase`. В `Settings` вложенные классы тоже капсом (`Settings.DATABASE.POOL.MAX_SIZE`).
- Отступ 4 пробела, открывающая скобка на той же строке, часто без пробела перед ней: `public void foo(){`.
- Однострочные `if` без фигурных скобок, ранний `return`:
  ```
  if (event.isCancelled())
      return;
  ```
  Короткие guard-ы допустимо в одну строку: `if (c == null) return;`.
- Короткие геттеры — в одну строку: `public int x() { return x; }`.
- Компактный try/catch для простых случаев: `try { new Loader(); } catch(Exception e){ Console.error(...); }`.
- Форматирование строк через `"...%s...".formatted(...)`, не конкатенацию и не `String.format`.
- Аннотации `@NotNull` / `@Nullable` из `org.jetbrains.annotations` — в утилитах и парсинге.
- Текст для игроков и консоли — MiniMessage (`<green>`, `<gradient:#54B435:#82CD47>`, `<!italic>`). Игрокам — через `Messages.X.send(sender)` (новый текст = новая константа в `Messages`), в консоль — `Console.*`.
- Сообщения об ошибках: `"Не получилось ... !"` / `"Не удалось ... !"`, исключение передаётся вторым аргументом в `Console.warn/error`.
- Комментарии редкие, на русском, объясняют «почему», а не «что».
- Java 17: records-подобные классы, switch-выражения с `->`, pattern matching в `instanceof`, text blocks для SQL.
- Числовые литералы с разделителями: `1_000_000`.
- Слушатели изменений — `EventPriority.MONITOR` с проверкой `isCancelled()`.

## Конфигурация (`settings.yml`)

Ключи, которые читает `Loader`: `changes.autosave`, `database.{type,url,user,password,limit,limit_size}`, `database.pool.{min_size,max_size,timeout_time,timeout_query}`.
При добавлении настройки: поле в `Settings`, чтение с дефолтом в `Loader`, ключ в `resources/settings.yml` (`FileUtils.loadYamlFile` сам допишет недостающие ключи в файл сервера).
