> **Note:** This documentation is written in German. Your browser should have no trouble translating it into your preferred language. :)

# Nullable Concurrent Collections

Dieses Projekt stellt thread-sichere Collection-Implementierungen bereit, die `null` als Element, Schlüssel oder Wert unterstützen.
Standardmäßige Concurrent-Collections in Java (wie `ConcurrentHashMap` oder `ConcurrentLinkedQueue`) erlauben oft kein `null`.

## Philosophie

`null` ist ein vollwertiger Status, der fachlich "kein Wert" oder "abwesend" bedeutet. Dies ist ein Standardfall und kein Fehler.

*   **Keine UnsupportedOperationException:** Methoden sollten das tun, was von ihnen erwartet wird, oder `null` zurückgeben, wenn ein Wert nicht vorhanden ist. Das Werfen einer `UnsupportedOperationException` ist ein Anti-Pattern.
*   **Null > Optional:** `null` ist `Optional` vorzuziehen. `Optional` dient oft nur der Vermeidung von `NullPointerException`s, anstatt Code robust gegen `null` zu gestalten (KISS / YAGNI).
*   **Non-Blocking:** Blockierende Queues werden nicht unterstützt. Die Begrenzung von Ressourcen durch Blockieren deutet auf Designschwächen hin. Überlast und Ausfälle sind durch *Circuit Breaker* zu behandeln.

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

### `RobustValueIteratorList`

Eine thread-sichere Liste, die Robustheit der Iteration über Geschwindigkeit stellt.

**Eigenschaften:**
*   Thread-sicher durch Verwendung eines `ReentrantReadWriteLock`.
*   Unterstützt `null` als Element.
*   **Robuster Iterator:** Der Iterator versucht bei Änderungen an der Liste, die Position logisch wiederherzustellen (basierend auf dem zuletzt gelesenen Wert und dessen Vorkommenshäufigkeit).
*   **Performance:** Schreib-/Leseoperationen (`add`, `remove`, `get`) sind effizient, aber die Iteration ist teuer (O(N²)), da bei jedem Schritt der Kontext neu berechnet wird.
*   Wirft `ConcurrentModificationException` nur, wenn eine logische Wiederherstellung der Position unmöglich ist (z.B. wenn das aktuelle Element gelöscht wurde).

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
	<version>1.1.0</version>
</dependency>
```

## Entwicklung

*   **Build:** `mvn clean package`
*   **Tests:** `mvn test`

## Lizenz & Autor

Copyright © Martin Kreutz.
Dieses Projekt ist unter der Apache 2.0 Lizenz veröffentlicht.
Co-Autor: Jules AI.
