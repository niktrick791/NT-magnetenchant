# 🧲 MagnetEnchant (Purpur / Paper 26.2)

Ein modernes, performantes Purpur/Paper Plugin, das eine neue benutzerdefinierte Verzauberung namens **Magnet** hinzufügt.  
Mit dieser Verzauberung werden gedroppte Items und abgebaute Blöcke in einem konfigurierbaren Radius automatisch zum Spieler herangezogen!

---

## ✨ Features

- **Neues Custom Enchantment: Magnet**
  - Stufen: **Magnet I**, **Magnet II**, **Magnet III** (beliebig erweiterbar in der `config.yml`).
  - Funktioniert auf **Werkzeugen** (Spitzhacken, Äxte, Schaufeln, Hacken, Schwerter) und/oder **Rüstungen** (Stiefel, Brustpanzer, Helm, Hose).
- **📚 Alle Survival-Fundmöglichkeiten & Verzauberungsmethoden**:
  - **Am Verzauberungstisch:**
    - Wenn du Werkzeuge, Rüstungen oder Bücher am Verzauberungstisch verzauberst, kann die Verzauberung **Magnet I** (bei Level 30 sogar **Magnet II**) zusätzlich ganz natürlich entstehen!
  - **Dorfbewohner (Bibliothekare):**
    - Bibliothekare können verzauberte Bücher mit **Magnet** in ihren Tauschangeboten für Smaragde anbieten.
  - **Loot-Kisten & Dungeons:**
    - In Kisten von Minenschächten, Dungeons, Festungen, Antiken Städten, Wüstentempeln, Bastionen etc.
  - **Angeln:**
    - Schätze, die aus dem Wasser geangelt werden, können Magnet-Bücher sein.
  - **Piglin-Tausch:**
    - Beim Tauschen von Goldbarren mit Piglins im Nether.
  - **Survival-Crafting (Werkbank):**
    - 1 Buch in der Mitte + 4 Eisenbarren + 4 Redstone = **Verzaubertes Buch (Magnet I)**.
  - **Amboss & Schleifstein:**
    - Bücher auf Werkzeuge/Rüstung schmieden und gleiche Stufen verschmelzen (Magnet I + I = II, II + II = III).
    - Am Schleifstein entfernbar.
  - **Befehle (Admin):**
    - `/magnet book [Level]` – Sofortiges Buch ins Inventar.
    - `/magnet give <Spieler> [Level]` – Buch an Spieler vergeben.
- **Flüssiges Einsaugen & Animationen**
  - Zieht Items mit sanfter physikalischer Flugkurve zum Spieler.
  - Setzt die Pickup-Verzögerung sofort auf 0, damit abgebaute Items ohne Wartezeit aufgenommen werden.
  - Optionale Partikeleffekte (`PORTAL`, `ENCHANT`, etc.) und Pickup-Sounds.
- **Intelligente Schutzmechanismen**
  - **Inventar-Prüfung**: Zieht nur Items an, wenn der Spieler auch tatsächlich Platz dafür im Inventar hat.
  - **Sneak-Bypass**: Schleicht der Spieler (Shift), wird das Anziehen pausiert (ideal, um Items an Freunde weiterzugeben).
  - **Toggle-Modus**: Spieler können den Magneten mit `/magnet toggle` jederzeit selbst an- und ausschalten.
  - **Blockabbau-Unterstützung**: Gedroppte Blöcke werden beim Abbauen sofort in Richtung des Spielers gezogen oder optional direkt ins Inventar transferiert (`instant-block-break-pickup`).

---

## 🚀 Befehle & Permissions

| Befehl | Beschreibung | Permission | Standard |
|---|---|---|---|
| `/magnet toggle` | Schaltet das automatische Einsammeln für sich an/aus | `magnet.toggle` / `magnet.use` | Jeder Spieler (`true`) |
| `/magnet book [Level]` | Gibt dir direkt ein Verzaubertes Buch mit Magnet | `magnet.admin` | OP (`op`) |
| `/magnet give <Spieler> [Level]` | Gibt ein Verzaubertes Buch an einen Spieler | `magnet.admin` | OP (`op`) |
| `/magnet enchant [Level]` | Verzaubert das Item in der Hand mit Magnet | `magnet.admin` | OP (`op`) |
| `/magnet reload` | Lädt die `config.yml` im laufenden Betrieb neu | `magnet.admin` | OP (`op`) |

---

## ⚙️ Konfiguration (`config.yml`)

```yaml
magnet:
  # Wie oft der Magnet-Scan ausgeführt wird (in Server-Ticks, 20 Ticks = 1 Sekunde)
  tick-interval: 3

  # Deaktiviert das Anziehen, wenn der Spieler schleicht (Shift)
  disable-while-sneaking: true

  # Items nur anziehen, wenn Platz im Inventar vorhanden ist
  only-pull-if-inventory-space: true

  # Wo nach der Verzauberung gesucht wird
  check-main-hand: true
  check-off-hand: true
  check-armor: true

  # Beim Abbauen von Blöcken:
  pull-drops-on-block-break: true
  instant-block-break-pickup: false

# Stufen, Radien und Geschwindigkeiten
levels:
  1:
    radius: 5.0
    speed: 0.45
  2:
    radius: 8.0
    speed: 0.65
  3:
    radius: 12.0
    speed: 0.85

# Optik und Sounds
effects:
  sound:
    enabled: true
    type: "ENTITY_ITEM_PICKUP"
    volume: 0.4
    pitch: 1.3
  particles:
    enabled: true
    type: "PORTAL"
```

---

## 📦 Kompilierte JAR-Datei

Die fertige Datei befindet sich in:
`build/libs/MagnetEnchant-1.0.0.jar`

Einfach in den `plugins/`-Ordner deines Purpur-Servers kopieren und den Server starten bzw. `/reload confirm` ausführen!
