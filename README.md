# 🧲 NT-magnetenchant - Minecraft Plugin

[🇩🇪 Zur deutschen Version wechseln](README_DE.md) | [🇬🇧 English Version](README.md)

A modern, high-performance Spigot / Paper / Purpur plugin (Minecraft 1.8.8 – 1.21.4+) that adds a custom enchantment called **Magnet**.  
With this enchantment, dropped items and mined blocks are smoothly drawn toward the player within a configurable radius!

---

## ✨ Features

- **Custom Enchantment: Magnet**
  - Levels: **Magnet I**, **Magnet II**, **Magnet III** (infinitely extensible in `config.yml`).
  - Works on **tools** (pickaxes, axes, shovels, hoes, swords) and/or **armor** (boots, leggings, chestplates, helmets, elytra).
- **📚 Complete Survival Ways to Obtain**:
  - **Enchanting Table:**
    - When enchanting tools, armor, or books at an enchanting table, **Magnet I** (at level 30 even **Magnet II**) can naturally appear as an extra enchantment!
  - **Villagers (Librarians):**
    - Librarians can offer enchanted books with **Magnet** in their trades for emeralds.
  - **Loot Chests & Dungeons:**
    - Found in mineshafts, dungeons, strongholds, ancient cities, desert temples, bastions, and end cities.
  - **Fishing:**
    - Can be reeled in as enchanted book treasure.
  - **Piglin Bartering:**
    - Can be obtained by bartering gold ingots with Piglins in the Nether.
  - **Survival Crafting (Workbench):**
    - 1 Book in the center + 4 Iron Ingots + 4 Redstone Dust = **Enchanted Book (Magnet I)**.
  - **Anvil & Grindstone:**
    - Apply books to tools/armor on an anvil, merge equal levels (I + I = II, II + II = III), or remove via grindstone.
- **Smooth Attraction & Physics**
  - Pulls items with smooth acceleration curves directly to the player.
  - Instantly resets item pickup delay to 0 so mined blocks are picked up immediately.
  - Optional particle trails and pickup chime sounds.
- **Smart Protection Features**
  - **Inventory Check**: Only pulls items if the player has available inventory space.
  - **Sneak Bypass**: Sneaking (Shift) temporarily pauses attraction (ideal for dropping items to friends).
  - **Toggle Command**: `/magnet toggle` lets players enable or disable the magnet effect at any time.
  - **Instant Block Pickup**: Optional `instant-block-break-pickup` directly places mined blocks into the inventory.
- **Bilingual & Multi-Language Support**:
  - Configurable language in `config.yml`: `language: en` or `language: de`.
  - In-game language switching: `/magnet lang <de|en>`.
- **Universal Cross-Version Support**:
  - Compiled targeting Java 8 bytecode (`major version: 52`). Compatible with Minecraft 1.8.8 through 1.21.4+.

---

## 🚀 Commands & Permissions

| Command | Permission | Default | Description |
|---|---|---|---|
| `/magnet toggle [player]` | `magnet.toggle` | Everyone (`true`) | Toggles the magnet effect on or off. |
| `/magnet give <player> [level]` | `magnet.admin` | OP (`op`) | Gives an enchanted Magnet book to a player. |
| `/magnet enchant [level]` | `magnet.admin` | OP (`op`) | Enchants the held item with Magnet. |
| `/magnet lang <de\|en>` | `magnet.admin` | OP (`op`) | Changes plugin language live. |
| `/magnet reload` | `magnet.admin` | OP (`op`) | Reloads `config.yml`. |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Language: "en" (English) or "de" (German)
language: en

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

## 📦 Building

```bash
.\gradlew.bat jar
```
The compiled JAR is generated at:
- `build/libs/MagnetEnchant-1.0.1.jar`
