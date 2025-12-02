# Performance Analyse

Die folgende Tabelle zeigt die Performance der verschiedenen Implementierungen im Vergleich.
Als Referenz (100%) dient jeweils die Standard-Implementierung der Java Class Library (JCL), welche `null` unterstützt, aber nicht Thread-Safe ist (z.B. `HashMap`, `ArrayList`).
Verglichen werden diese mit den Thread-Safe Varianten der JCL (z.B. `ConcurrentHashMap`, `CopyOnWriteArrayList`, die meist kein `null` unterstützen) und den Implementierungen dieses Projekts (`Nullable...`, `RobustValueIteratorList`).

Die Werte geben die relative Laufzeit an. Ein Wert von 200% bedeutet, dass die Operation doppelt so lange dauert (doppelt so langsam ist) wie die Referenz.
Gemessen wurde mit JMH (Java Microbenchmark Harness) auf einer Standard-Umgebung.

## Map (HashMap vs. ConcurrentHashMap vs. NullableConcurrentMap)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Nullable) |
| :--- | :---: | :---: | :---: |
| **Get (Lesen)** | 100% | 112% | 120% |
| **Put (Schreiben)** | 100% | 272% | 277% |
| **Iterieren** | 100% | 158% | 331% |

*   **Analyse:** Der Overhead durch den Wrapper (`NullableConcurrentMap`) ist beim Lesen und Schreiben minimal gegenüber der `ConcurrentHashMap`. Beim Iterieren ist der Overhead höher, da `Entry`-Objekte gewrappt werden müssen.

## Sorted Map (TreeMap vs. ConcurrentSkipListMap vs. NullableSortedConcurrentMap)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Nullable) |
| :--- | :---: | :---: | :---: |
| **Get (Lesen)** | 100% | 190% | 228% |
| **Put (Schreiben)** | 100% | 171% | 257% |
| **Iterieren** | 100% | 21%* | 100% |

*   **Anmerkung:** `ConcurrentSkipListMap` iteriert hier extrem schnell (schneller als `TreeMap`). Unsere Implementierung liegt durch den Wrapper-Overhead etwa auf dem Niveau der `TreeMap`.

## List (ArrayList vs. CopyOnWriteArrayList vs. RobustValueIteratorList)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Robust) |
| :--- | :---: | :---: | :---: |
| **Add (Schreiben)** | 100% | 14894% | 1058% |
| **Get (Lesen)** | 100% | 922% | 12200% |
| **Iterieren** | 100% | 100% | 4184% |

*   **Analyse:**
    *   `CopyOnWriteArrayList` ist beim Schreiben extrem langsam (kopiert Array). Unsere `RobustValueIteratorList` ist hier deutlich schneller, da sie Locking verwendet statt zu kopieren.
    *   Beim Lesen und Iterieren ist unsere Implementierung jedoch signifikant langsamer. Dies ist eine bewusste Designentscheidung: Die Liste verwendet einen "intelligenten" Iterator, der auch bei konkurrierenden Änderungen versucht, die Position zu halten oder wiederzufinden ("Robustness over Performance"), sowie ein `ReadWriteLock`.

## Queue (LinkedList vs. ConcurrentLinkedQueue vs. NullableConcurrentQueue)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Nullable) |
| :--- | :---: | :---: | :---: |
| **Offer/Poll** | 100% | 185% | 193% |
| **Iterieren** | 100% | 169% | 151% |

*   **Analyse:** Die Performance ist vergleichbar mit der `ConcurrentLinkedQueue`. Der Overhead für die Null-Unterstützung ist vernachlässigbar.

---
*Messwerte basieren auf JMH Benchmarks (Avg Time, 1000 Elemente).*
