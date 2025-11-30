> **Note:** This documentation is written in German. Your browser should have no trouble translating it into your preferred language. :)

# Nullable Concurrent Collections

Dieses Projekt stellt thread-sichere Collection-Implementierungen bereit, die `null` als Element, Schlüssel oder Wert unterstützen.
Standardmäßige Concurrent-Collections in Java (wie `ConcurrentHashMap` oder `ConcurrentLinkedQueue`) erlauben oft kein `null`.

## Klassen

### `NullableConcurrentMap`

Eine Implementierung von `ConcurrentMap`, die intern eine `ConcurrentHashMap` nutzt.
`null`-Werte werden maskiert gespeichert.

**Eigenschaften:**
*   Thread-sicher.
*   Unterstützt `null` als Key und Value.
*   Methoden wie `put`, `get`, `containsKey` verhalten sich wie bei einer `HashMap`.
*   Sichten (`keySet`, `entrySet`, `values`) unterstützen `null`.

### `NullableSortedConcurrentMap`

Eine Erweiterung von `ConcurrentSkipListMap`.

**Eigenschaften:**
*   Thread-sicher und sortiert.
*   Unterstützt `null` als Key und Value.
*   `null`-Keys werden immer an den Anfang sortiert.
*   Verwendet intern einen Wrapper-Comparator.

### `NullableConcurrentQueue`

Eine Implementierung von `Queue`, die intern eine `ConcurrentLinkedQueue` nutzt.

**Eigenschaften:**
*   Thread-sicher (nicht blockierend).
*   Unterstützt `null` als Element.
*   `null`-Werte werden intern maskiert.

### `NullableConcurrentDeque`

Eine Implementierung von `Deque`, die intern eine `ConcurrentLinkedDeque` nutzt.

**Eigenschaften:**
*   Thread-sicher (nicht blockierend).
*   Unterstützt `null` als Element.
*   Kann als Queue oder Stack verwendet werden.

### `NullableConcurrentList`

Eine Implementierung von `List`, die intern eine `CopyOnWriteArrayList` nutzt.

**Eigenschaften:**
*   Thread-sicher.
*   Unterstützt `null` als Element.
*   **Wichtig:** Schreiboperationen sind teuer (blocking copy), Leseoperationen sind schnell und nicht blockierend.

## Verwendung

Das Projekt baut ein MRJAR und benötigt mindestens Java 1.8

```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

```xml
<dependency>
	<groupId>com.github.krawallgraben</groupId>
	<artifactId>nullable</artifactId>
	<version>1.0.0</version>
</dependency>
```

## Entwicklung

*   **Build:** `mvn clean package`
*   **Tests:** `mvn test`

## Lizenz & Autor

Copyright © Martin Kreutz.
Dieses Projekt ist unter der Apache 2.0 Lizenz veröffentlicht.
Co-Autor: Jules AI.
