# NaturalTracker

[![JitPack](https://jitpack.io/v/Vovari2/NaturalTracker.svg)](https://jitpack.io/#Vovari2/NaturalTracker)

Плагин для Paper, который помнит, что в последний раз происходило с блоком: его создал мир, его поставили или его уничтожили.

```
Location location = block.getLocation();

if (NaturalTrackerAPI.wasGenerated(location)) {
    // блок создан миром
} else if (NaturalTrackerAPI.wasPlaced(location)) {
    // последним действием блок поставили
} else {
    // последним действием блок уничтожили
}
```

- три состояния блока: `generated`, `placed`, `destroyed`, для каждого блока верно ровно одно
- API для других плагинов и плейсхолдеры PlaceholderAPI
- предмет-инспектор, который показывает состояние блока кликом
- данные хранятся в SQLite, MySQL или PostgreSQL, по одной сжатой строке на чанк

## Для чего это нужно

Плагинам, которым важно, поставил ли блок игрок: защита от дюпа и фарма, награды за добычу руды, ограничения на естественные блоки.

- **Состояния** - сгенерирован миром, поставлен или уничтожен. Блок, с которым ничего не делали, считается сгенерированным.
- **Скорость** - ответ берётся из памяти, без обращения к БД. База работает в отдельном потоке и не нагружает главный.
- **Хранение** - данные чанка подгружаются при его загрузке и сохраняются при выгрузке и по таймеру. Если включён лимит, давно не обновлявшиеся чанки удаляются.
- **Что отслеживается** - установка и разрушение блоков игроками и работа поршней. Остальные источники (взрывы, жидкости, огонь, рост растений) пока не учитываются.

## Установка

Требуется Paper 1.20+ и Java 17. Положите `NaturalTracker-<версия>.jar` в папку `plugins` и перезапустите сервер.

NaturalTracker нужно использовать **как плагин**, а не как библиотеку: он должен стоять в `plugins`, потому что именно он хранит данные и отвечает на запросы других плагинов.

## Команды

Команда `/naturaltracker`, алиас `/nt`. Нужно право `naturaltracker.admin`.

| Команда | Описание |
|---|---|
| `/nt reload` | Перезагружает настройки, сообщения и подключение к БД |
| `/nt inspect` | Выдаёт предмет-инспектор |

Инспектор - морской фонарь с особой меткой. Клик по блоку показывает его состояние, правый клик проверяет место рядом с кликнутой гранью (туда ставится блок). Если данные чанка ещё грузятся, инспектор попросит повторить.

## Плейсхолдеры

Нужен [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) (необязательная зависимость).

| Плейсхолдер | `true`, если |
|---|---|
| `%naturaltracker_generated_<мир>_<x>_<y>_<z>%` | блок создан миром |
| `%naturaltracker_placed_<мир>_<x>_<y>_<z>%` | последним действием блок поставили |
| `%naturaltracker_destroyed_<мир>_<x>_<y>_<z>%` | последним действием блок уничтожили |

Результат - значения `true` / `false` из настроек PlaceholderAPI. Координаты могут быть дробными, они округляются до блока. В названии мира допустим `_`. Внутри работают вложенные плейсхолдеры в фигурных скобках:

```
%naturaltracker_placed_{player_world}_{player_x}_{player_y}_{player_z}%
```

Плейсхолдеры работают только в главном потоке. При асинхронном вызове, неверном мире, координатах или названии состояния плейсхолдер не раскроется и останется в тексте.

## Настройки

Файл `plugins/NaturalTracker/settings.yml`, недостающие ключи дописываются сами. Применить изменения: `/nt reload`.

| Ключ | По умолчанию | Описание |
|---|---|---|
| `changes.autosave` | `300` | Как часто (в секундах) сохранять изменённые чанки; `0` - только при выгрузке |
| `database.type` | `sqlite` | `sqlite`, `mysql` или `postgresql` |
| `database.url` | пусто | sqlite: путь к файлу (пусто = `plugins/NaturalTracker/data.db`); mysql/postgresql: `host:port/database` |
| `database.user`, `database.password` | `root`, пусто | Данные для входа (mysql/postgresql) |
| `database.limit` | `true` | Ограничивать ли число чанков в таблице |
| `database.limit_size` | `100_000` | Максимум чанков, лишние (давно не обновлявшиеся) удаляются |
| `database.pool.*` | `2`, `10`, `10`, `10` | `min_size`, `max_size`, `timeout_time`, `timeout_query` |

Тексты сообщений лежат в `plugins/NaturalTracker/messages.json` (MiniMessage).

## API

Библиотека нужна только чтобы компилировать ваш плагин. На сервере всё равно должен стоять сам NaturalTracker.

### Как подключить

Требуется Java 17. API публикуется через [JitPack](https://jitpack.io/#Vovari2/NaturalTracker): `Tag` ниже - тег [релиза](https://github.com/Vovari2/NaturalTracker/releases), последний указан на бейдже выше.

Используйте `compileOnly` (в Maven - `provided`): не упаковывайте API в свой плагин, иначе в вашем jar окажется копия кода, которая может сломаться после обновления NaturalTracker.

#### Gradle (Kotlin)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.Vovari2.NaturalTracker:NaturalTracker:Tag")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Vovari2.NaturalTracker:NaturalTracker:Tag'
}
```

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Vovari2.NaturalTracker</groupId>
    <artifactId>NaturalTracker</artifactId>
    <version>Tag</version>
    <scope>provided</scope>
</dependency>
```

#### plugin.yml

Чтобы NaturalTracker загрузился раньше вашего плагина:

```yaml
depend: [NaturalTracker]      # без NaturalTracker плагин не работает
softdepend: [NaturalTracker]  # или: может работать и без него
```

### Как пользоваться

```java
import me.vovari2.naturaltracker.NaturalTrackerAPI;

boolean generated = NaturalTrackerAPI.wasGenerated(location);
boolean placed = NaturalTrackerAPI.wasPlaced(location);
boolean destroyed = NaturalTrackerAPI.wasDestroyed(location);
```

| Метод | `true`, если |
|---|---|
| `wasGenerated(Location)` | блок создан миром |
| `wasPlaced(Location)` | последним действием блок поставили |
| `wasDestroyed(Location)` | последним действием блок уничтожили |

### Как это работает

- Для каждого загруженного чанка в памяти лежит один набор битов, по два бита на блок: «изменён» и «поставлен/уничтожен». Блока в наборе нет - он считается сгенерированным.
- Методы читают только эту память, без запросов к БД, и отвечают сразу.
- Вызывать методы нужно **только из главного потока**. Из другого потока вызов игнорируется, в консоль идёт предупреждение, а блок считается сгенерированным: `wasGenerated` вернёт `true`, остальные - `false`.
- Сразу после загрузки чанка данные из БД подгружаются асинхронно. Пока они не пришли, блок, который на самом деле поставили или уничтожили, может ненадолго считаться сгенерированным.
- В `Location` должен быть задан мир. Блок в выгруженном чанке считается сгенерированным.