> **Note:** This documentation is written in German. Your browser should have no trouble translating it into your preferred language. :)

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
	    <version>Tag</version>
	</dependency>
```

## Entwicklung

*   **Build:** `mvn clean package`
*   **Tests:** `mvn test`

## Lizenz & Autor

Copyright © Martin Kreutz.
Dieses Projekt ist unter der Apache 2.0 Lizenz veröffentlicht.
Co-Autor: Jules AI.
