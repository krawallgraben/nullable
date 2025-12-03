# Performance Analyse

Die folgende Tabelle zeigt die Performance der verschiedenen Implementierungen im Vergleich.
Als Referenz (1.0x) dient jeweils die Standard-Implementierung der Java Class Library (JCL), welche `null` unterstützt, aber nicht Thread-Safe ist (z.B. `HashMap`, `ArrayList`).
Verglichen werden diese mit den Thread-Safe Varianten der JCL (z.B. `ConcurrentHashMap`, `CopyOnWriteArrayList`, die meist kein `null` unterstützen) und den Implementierungen dieses Projekts (`Nullable...`, `RobustValueIteratorList`).

Die Werte geben den Faktor der Laufzeit an. Ein Wert von 2.0x bedeutet, dass die Operation doppelt so lange dauert (doppelt so langsam ist) wie die Referenz.
Gemessen wurde mit JMH (Java Microbenchmark Harness) auf einer Standard-Umgebung.

## Map (HashMap vs. ConcurrentHashMap vs. NullableConcurrentMap)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Nullable) |
| :--- | :---: | :---: | :---: |
| **Get (Lesen)** | 1.0x | 1.1x | 1.2x |
| **Put (Schreiben)** | 1.0x | 1.8x | 2.1x |
| **Iterieren** | 1.0x | 1.5x | 3.3x |
| **Delete (Löschen)** | 1.0x | 3.4x | 3.4x |

*   **Analyse:** Der Overhead durch den Wrapper (`NullableConcurrentMap`) ist beim Lesen und Schreiben minimal gegenüber der `ConcurrentHashMap`. Beim Iterieren ist der Overhead höher, da `Entry`-Objekte gewrappt werden müssen.

## Sorted Map (TreeMap vs. ConcurrentSkipListMap vs. NullableSortedConcurrentMap)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Nullable) |
| :--- | :---: | :---: | :---: |
| **Get (Lesen)** | 1.0x | 2.2x | 2.1x |
| **Put (Schreiben)** | 1.0x | 1.9x | 2.3x |
| **Iterieren** | 1.0x | 0.4x | 1.1x |
| **Delete (Löschen)** | 1.0x | 1.8x | 2.1x |

*   **Anmerkung:** `ConcurrentSkipListMap` iteriert hier extrem schnell (schneller als `TreeMap`). Unsere Implementierung liegt durch den Wrapper-Overhead etwa auf dem Niveau der `TreeMap`.

## List (ArrayList vs. CopyOnWriteArrayList vs. RobustValueIteratorList)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Robust) |
| :--- | :---: | :---: | :---: |
| **Add (Schreiben)** | 1.0x | 1408.9x | 10.6x |
| **Get (Lesen)** | 1.0x | 9.1x | 121.8x |
| **Iterieren** | 1.0x | 1.0x | 40.8x |
| **Delete (Löschen)** | 1.0x | 63.9x | 1.7x |

*   **Analyse:**
    *   `CopyOnWriteArrayList` ist beim Schreiben extrem langsam (kopiert Array). Unsere `RobustValueIteratorList` ist hier deutlich schneller, da sie Locking verwendet statt zu kopieren.
    *   Beim Lesen und Iterieren ist unsere Implementierung jedoch signifikant langsamer. Dies ist eine bewusste Designentscheidung: Die Liste verwendet einen "intelligenten" Iterator, der auch bei konkurrierenden Änderungen versucht, die Position zu halten oder wiederzufinden ("Robustness over Performance"), sowie ein `ReadWriteLock`.

## Queue (LinkedList vs. ConcurrentLinkedQueue vs. NullableConcurrentQueue)

| Operation | Standard (Ref) | Thread-Safe (JCL) | Project (Nullable) |
| :--- | :---: | :---: | :---: |
| **Offer/Poll** | 1.0x | 2.0x | 2.0x |
| **Iterieren** | 1.0x | 1.6x | 1.5x |
| **Delete (Löschen)** | 1.0x | 1.1x | 1.2x |

*   **Analyse:** Die Performance ist vergleichbar mit der `ConcurrentLinkedQueue`. Der Overhead für die Null-Unterstützung ist vernachlässigbar.

## Vergleich der Collection-Typen (Standard Implementierungen)

Dieser Vergleich zeigt die relativen Geschwindigkeiten der Standard-Implementierungen untereinander, normiert auf die schnellste Implementierung pro Kategorie (meist `ArrayList`).

| Operation | List (ArrayList) | Map (HashMap) | Sorted Map (TreeMap) | Queue (LinkedList) |
| :--- | :---: | :---: | :---: | :---: |
| **Schreiben (Add/Put/Offer)** | 1.0x (8.5ns) | 2255.0x | 14406.0x | 2.5x |
| **Lesen (Get/Poll)** | 1.0x (213.7ns)* | 28.6x | 514.5x | 0.1x** |
| **Iterieren** | 1.0x (1240.4ns) | 3.6x | 7.3x | 2.9x |
| **Löschen** | 1.0x (88.6ns) | 0.4x | 2.1x | 79.8x |

*   ***Anmerkung Lesen:** `ArrayList` Get ist Random Access (Index), Maps sind Key-Lookup.
*   ***Anmerkung Queue:** `LinkedList` Poll ist extrem schnell (20ns), da nur Referenzen umgebogen werden, während `ArrayList.get` (213ns) hier im Benchmark langsamer scheint (evtl. Cache Effects oder JIT Unterschied im Benchmark-Setup).

---
*Messwerte basieren auf JMH Benchmarks (Avg Time, 1000 Elemente).*
