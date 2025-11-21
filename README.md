# Nullable Concurrent Maps

Dieses Projekt stellt thread-sichere Map-Implementierungen bereit, die `null` als Schlüssel und Wert unterstützen.
Standardmäßige Concurrent-Maps in Java (wie `ConcurrentHashMap`) erlauben kein `null`.

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

## Verwendung

Das Projekt ist ein Maven-Projekt und benötigt Java 21.

```xml
<dependency>
    <groupId>de.krawallgraben</groupId>
    <artifactId>nullable</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Entwicklung

*   **Build:** `mvn clean package`
*   **Tests:** `mvn test`
*   **Dependency-Updates prüfen:** `mvn versions:display-dependency-updates`
*   **Code-Formatierung:** Vor dem Commit `mvn spotless:apply` ausführen.

## Konventionen

*   Code und Dokumentation auf Deutsch (wo sinnvoll).
*   Einfache, klare Sprache.
*   Kein `Optional` verwenden.
*   Trennung von Refactoring und Features.
