# 🧲 NT-magnetenchant - Minecraft Plugin
[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C.svg?logo=paypal&logoColor=white)](https://paypal.me/niktrick)

[🇬🇧 Switch to English Version](README.md) | [🇩🇪 Deutsche Version](README_DE.md)

Ein modernes, performantes Spigot / Paper / Purpur Plugin (Minecraft 1.8.8 – 1.21.4+), das eine neue benutzerdefinierte Verzauberung namens **Magnet** hinzufügt.  
Mit dieser Verzauberung werden gedroppte Items und abgebaute Blöcke in einem konfigurierbaren Radius automatisch zum Spieler herangezogen!

---

## ✨ Features

- **Neues Custom Enchantment: Magnet**
  - Stufen: **Magnet I**, **Magnet II**, **Magnet III** (beliebig erweiterbar in der `config.yml`).
  - Funktioniert auf **Werkzeugen** (Spitzhacken, Äxte, Schaufeln, Hacken, Schwerter) und/oder **Rüstungen** (Stiefel, Brustpanzer, Helm, Hose, Elytren).
- **📚 Alle Survival-Fundmöglichkeiten & Verzauberungsmethoden**:
  - **Am Verzauberungstisch:**
    - Wenn du Werkzeuge, Rüstungen oder Bücher verzauberst, kann die Verzauberung **Magnet I** (bei Level 30 sogar **Magnet II**) zusätzlich ganz natürlich entstehen!
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
- **Flüssiges Einsaugen & Animationen**
  - Zieht Items mit sanfter physikalischer Flugkurve zum Spieler.
  - Setzt die Pickup-Verzögerung sofort auf 0, damit abgebaute Items ohne Wartezeit aufgenommen werden.
  - Optionale Partikeleffekte und Pickup-Sounds.
- **Intelligente Schutzmechanismen**
  - **Inventar-Prüfung**: Zieht nur Items an, wenn der Spieler auch tatsächlich Platz dafür im Inventar hat.
  - **Sneak-Bypass**: Schleicht der Spieler (Shift), wird das Anziehen pausiert (ideal, um Items an Freunde weiterzugeben).
  - **Toggle-Modus**: Spieler können den Magneten mit `/magnet toggle` jederzeit selbst an- und ausschalten.
  - **Blockabbau-Unterstützung**: Gedroppte Blöcke werden beim Abbauen sofort in Richtung des Spielers gezogen oder optional direkt ins Inventar transferiert (`instant-block-break-pickup`).
- **Zweisprachige Unterstützung (i18n)**:
  - Konfigurierbar in `config.yml`: `language: de` oder `language: en`.
  - In-Game umschaltbar: `/magnet lang <de|en>`.
- **Universelle Cross-Version-Kompatibilität**:
  - Kompiliert mit Java 8 Bytecode (`major version: 52`). Läuft auf Spigot, Paper und Purpur von 1.8.8 bis 1.21.4+.

---

## 🚀 Befehle & Permissions

| Befehl | Beschreibung | Permission | Standard |
|---|---|---|---|
| `/magnet toggle [spieler]` | Schaltet das automatische Einsammeln an/aus | `magnet.toggle` / `magnet.use` | Jeder Spieler (`true`) |
| `/magnet give <spieler> [level]` | Gibt ein Verzaubertes Buch an einen Spieler | `magnet.admin` | OP (`op`) |
| `/magnet enchant [level]` | Verzaubert das Item in der Hand mit Magnet | `magnet.admin` | OP (`op`) |
| `/magnet lang <de\|en>` | Ändert die Sprache des Plugins live im Spiel | `magnet.admin` | OP (`op`) |
| `/magnet reload` | Lädt die `config.yml` im laufenden Betrieb neu | `magnet.admin` | OP (`op`) |

---

## ⚙️ Konfiguration (`config.yml`)

```yaml
# Sprache: "de" (Deutsch) oder "en" (Englisch)
language: de

magnet:
  tick-interval: 3
  disable-while-sneaking: true
  only-pull-if-inventory-space: true
  check-main-hand: true
  check-off-hand: true
  check-armor: true
  pull-drops-on-block-break: true
  instant-block-break-pickup: false

levels:
  1:
    radius: 7.0
    speed: 0.50
  2:
    radius: 11.0
    speed: 0.70
  3:
    radius: 16.0
    speed: 0.95
```

---

## 📦 Kompilierung

```bash
.\gradlew.bat jar
```
Die fertige JAR befindet sich im Projektordner:
- `build/libs/MagnetEnchant-1.0.1.jar`
---

## 💖 Unterstützung & Spenden

Wenn dir dieses Plugin gefällt, freuen wir uns über deine freiwillige Unterstützung:
- [PayPal Spende](https://paypal.me/niktrick)
- Alle Spenden sind freiwillig und fließen direkt in die Wartung und Weiterentwicklung!
