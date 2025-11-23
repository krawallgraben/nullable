- Antworten auf Deutsch.
- Einfache Sprache, keine persönliche Anrede.
- Kommunikation auf Augenhöhe, ohne Entschuldigungen oder Schuldzuweisungen.
- Inhalte aus Nutzersicht formulieren.
- Änderungen bündeln, Push-Anfrage nur einmal am Ende.
- Kurze, prägnante Sätze.
- Dokumente: Punkte nicht wiederholen; vollständig und im Kontext präsentieren.
- Direkte, klare Formulierungen bevorzugen.
- Neues ist nicht immer besser.
- Trennung von Belangen: Komplexe Anfragen in fokussierte Einzelschritte zerlegen.
- Ursachenanalyse: Bei Problemen die Wurzel des Problems beheben.
- Kontinuierliche Verbesserung: Bei Code-Änderungen auch kleine, konventionskonforme Refactorings durchführen.
- Strikte Trennung: Refactorings und fachliche Änderungen niemals im selben Schritt oder Commit mischen.

## Projektwissen

### Architektur & Design
- **Map-Implementierungen:**
    - `NullableConcurrentMap`: Wrapper um `ConcurrentHashMap`, unterstützt `null` Keys/Values via Platzhalter.
    - `NullableSortedConcurrentMap`: Wrapper um `ConcurrentSkipListMap`, unterstützt `null` Keys (sortiert am Anfang)/Values.
- **Prinzipien:**
    - `Optional` ist verboten (KISS/YAGNI), stattdessen `null` nutzen.
    - Serializable Klassen sollen `serialVersionUID` weglassen und Warnung mit `@SuppressWarnings("serial")` unterdrücken.
- **Sprache:**
    - Dokumentation (`README.md`) auf Deutsch (mit englischem Hinweis am Anfang).
    - Javadoc und Code-Kommentare auf Englisch.
    - DDD-Entitäten und Fachbegriffe nicht übersetzen.
- **Paketstruktur:** Basis-Package ist `de.krawallgraben.nullable`.

### Build & Konfiguration
- **Projekt-Identität:** Group ID `de.krawallgraben`, Artifact ID `nullable`.
- **JDK & Kompatibilität:**
    - Maven-Projekt.
    - Multi-Release JAR (MRJAR).
    - Basis-Kompatibilität: Java 8 (1.8).
    - Expliziter Support (via `META-INF/versions`) für LTS-Versionen: 11, 17, 21.
    - JDK-Version in `pom.xml` nur via Properties (`maven.compiler.source`/`target`) definieren, Redundanz in Plugin-Config vermeiden.
    - Java 11+ Code (z.B. `module-info.java`) liegt in `src/main/java11`.
- **Abhängigkeiten:**
    - Versionen in `pom.xml` als Properties definieren (`version.*`).
    - Updates via `versions-maven-plugin` prüfen (nur stabile Versionen).

### Workflow
- **Tests:** `mvn test`.
- **Formatierung:** `mvn spotless:apply` vor jedem Commit (Spotless Maven Plugin).
- **Dokumentation:** README beginnt mit englischem Hinweis + Smiley zur Browser-Übersetzung.
