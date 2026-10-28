package de.magnet;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final MagnetPlugin plugin;

    private String language = "en";
    private int tickInterval = 3;
    private boolean disableWhileSneaking = true;
    private boolean onlyPullIfInventorySpace = true;
    private boolean checkMainHand = true;
    private boolean checkOffHand = true;
    private boolean checkArmor = true;
    private boolean instantBlockBreakPickup = false;
    private boolean pullDropsOnBlockBreak = true;

    private boolean playSound = true;
    private float soundVolume = 0.5f;
    private float soundPitch = 1.2f;

    private boolean spawnParticles = true;
    private Object particle = null;

    private final Map<Integer, LevelSettings> levels = new HashMap<>();
    private int maxLevel = 3;

    private boolean craftingRecipeEnabled = true;
    private boolean lootChestsEnabled = true;
    private double lootChestChance = 0.15;
    private boolean enchantingTableEnabled = true;
    private double enchantingTableChance = 0.10;
    private boolean villagerTradesEnabled = true;
    private double villagerTradeChance = 0.15;
    private boolean fishingEnabled = true;
    private double fishingChance = 0.20;
    private boolean piglinBarterEnabled = true;
    private double piglinBarterChance = 0.15;

    public static class LevelSettings {
        private final double radius;
        private final double speed;

        public LevelSettings(double radius, double speed) {
            this.radius = radius;
            this.speed = speed;
        }

        public double radius() { return radius; }
        public double speed() { return speed; }
    }

    public ConfigManager(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        language = config.getString("language", "en").toLowerCase();
        if (!"de".equals(language) && !"en".equals(language)) {
            language = "en";
        }

        tickInterval = config.getInt("magnet.tick-interval", 3);
        disableWhileSneaking = config.getBoolean("magnet.disable-while-sneaking", true);
        onlyPullIfInventorySpace = config.getBoolean("magnet.only-pull-if-inventory-space", true);
        checkMainHand = config.getBoolean("magnet.check-main-hand", true);
        checkOffHand = config.getBoolean("magnet.check-off-hand", true);
        checkArmor = config.getBoolean("magnet.check-armor", true);
        instantBlockBreakPickup = config.getBoolean("magnet.instant-block-break-pickup", false);
        pullDropsOnBlockBreak = config.getBoolean("magnet.pull-drops-on-block-break", true);

        playSound = config.getBoolean("effects.sound.enabled", true);
        soundVolume = (float) config.getDouble("effects.sound.volume", 0.5);
        soundPitch = (float) config.getDouble("effects.sound.pitch", 1.2);

        spawnParticles = config.getBoolean("effects.particles.enabled", true);
        String particleStr = config.getString("effects.particles.type", "PORTAL");
        try {
            Class<?> pClass = Class.forName("org.bukkit.Particle");
            particle = Enum.valueOf((Class<Enum>) pClass, particleStr.toUpperCase());
        } catch (Throwable ignored) {
            particle = null;
        }

        levels.clear();
        ConfigurationSection levelsSection = config.getConfigurationSection("levels");
        if (levelsSection != null) {
            for (String key : levelsSection.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(key);
                    double radius = levelsSection.getDouble(key + ".radius", 4.0 + (lvl * 3.0));
                    double speed = levelsSection.getDouble(key + ".speed", 0.35 + (lvl * 0.15));
                    levels.put(lvl, new LevelSettings(radius, speed));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (levels.isEmpty()) {
            levels.put(1, new LevelSettings(5.0, 0.45));
            levels.put(2, new LevelSettings(8.0, 0.65));
            levels.put(3, new LevelSettings(12.0, 0.85));
        }

        int max = 3;
        for (int k : levels.keySet()) {
            if (k > max) max = k;
        }
        maxLevel = max;

        craftingRecipeEnabled = config.getBoolean("books.crafting-recipe.enabled", true);
        lootChestsEnabled = config.getBoolean("books.loot-chests.enabled", true);
        lootChestChance = config.getDouble("books.loot-chests.chance", 0.15);
        enchantingTableEnabled = config.getBoolean("books.enchanting-table.enabled", true);
        enchantingTableChance = config.getDouble("books.enchanting-table.chance", 0.10);
        villagerTradesEnabled = config.getBoolean("books.villager-trades.enabled", true);
        villagerTradeChance = config.getDouble("books.villager-trades.chance", 0.15);
        fishingEnabled = config.getBoolean("books.fishing.enabled", true);
        fishingChance = config.getDouble("books.fishing.chance", 0.20);
        piglinBarterEnabled = config.getBoolean("books.piglin-barter.enabled", true);
        piglinBarterChance = config.getDouble("books.piglin-barter.chance", 0.15);
    }

    public String getLanguage() { return language; }

    public void setLanguage(String lang) {
        String clean = (lang != null && lang.equalsIgnoreCase("de")) ? "de" : "en";
        this.language = clean;
        plugin.getConfig().set("language", clean);
        plugin.saveConfig();
        load();
    }

    public String getMessage(String key, String fallback) {
        boolean isDe = "de".equalsIgnoreCase(language);
        if ("help-toggle".equals(key)) return isDe ? "§e/magnet toggle [spieler] §7- Magnet für dich oder einen Spieler umschalten" : "§e/magnet toggle [player] §7- Toggle magnet for yourself or a player";
        if ("help-give".equals(key)) return isDe ? "§e/magnet give <spieler> [level] §7- Gibt ein Magnet-Verzauberungsbuch" : "§e/magnet give <player> [level] §7- Gives an enchanted magnet book";
        if ("help-enchant".equals(key)) return isDe ? "§e/magnet enchant [level] §7- Verzaubert das Item in deiner Hand" : "§e/magnet enchant [level] §7- Enchants the item in your hand";
        if ("help-lang".equals(key)) return isDe ? "§e/magnet lang <de|en> §7- Ändert die Sprache (Deutsch/Englisch)" : "§e/magnet lang <de|en> §7- Changes the plugin language (German/English)";
        if ("help-reload".equals(key)) return isDe ? "§e/magnet reload §7- Lädt die Konfiguration neu" : "§e/magnet reload §7- Reloads configuration";
        if ("toggle-on".equals(key)) return isDe ? "§aDein Magnet wurde aktiviert!" : "§aYour magnet has been enabled!";
        if ("toggle-off".equals(key)) return isDe ? "§cDein Magnet wurde deaktiviert." : "§cYour magnet has been disabled.";
        if ("toggle-other-on".equals(key)) return isDe ? "§aMagnet für §e{target} §aaktiviert." : "§aMagnet for §e{target} §aenabled.";
        if ("toggle-other-off".equals(key)) return isDe ? "§cMagnet für §e{target} §cdeaktiviert." : "§cMagnet for §e{target} §cdisabled.";
        if ("give-success".equals(key)) return isDe ? "§aMagnet-Buch Level {level} an §e{target} §agegeben." : "§aGave Magnet Book Level {level} to §e{target}§a.";
        if ("enchant-success".equals(key)) return isDe ? "§aItem erfolgreich mit §bMagnet {level} §averzaubert!" : "§aSuccessfully enchanted item with §bMagnet {level}§a!";
        if ("enchant-no-item".equals(key)) return isDe ? "§cDu musst ein Werkzeug oder Rüstungsteil in der Hand halten." : "§cYou must hold a tool or armor piece in your hand.";
        if ("no-permission".equals(key)) return isDe ? "§cDazu hast du keine Berechtigung." : "§cYou do not have permission for this.";
        if ("reload-success".equals(key)) return isDe ? "§aKonfiguration erfolgreich neu geladen!" : "§aConfiguration successfully reloaded!";
        if ("lang-changed".equals(key)) return isDe ? "§aSprache erfolgreich auf §eDeutsch §ageändert!" : "§aLanguage successfully set to §eEnglish§a!";
        if ("unknown-sub".equals(key)) return isDe ? "§cUnbekannter Unterbefehl. Nutze §e/magnet help§c." : "§cUnknown subcommand. Use §e/magnet help§c.";
        return fallback;
    }

    public LevelSettings getLevelSettings(int level) {
        if (levels.containsKey(level)) {
            return levels.get(level);
        }
        if (level > maxLevel) {
            LevelSettings maxSettings = levels.get(maxLevel);
            double extraRadius = (level - maxLevel) * 3.0;
            return new LevelSettings(maxSettings.radius() + extraRadius, Math.min(1.5, maxSettings.speed() + (level - maxLevel) * 0.1));
        }
        return new LevelSettings(4.0, 0.4);
    }

    public int getTickInterval() { return tickInterval; }
    public boolean isDisableWhileSneaking() { return disableWhileSneaking; }
    public boolean isOnlyPullIfInventorySpace() { return onlyPullIfInventorySpace; }
    public boolean isToolsAllowed() { return checkMainHand; }
    public boolean isHeldItemAllowed() { return checkMainHand; }
    public boolean isOffHandAllowed() { return checkOffHand; }
    public boolean isArmorAllowed() { return checkArmor; }
    public boolean isInventoryCharmAllowed() { return false; }
    public boolean isInstantBlockBreakPickup() { return instantBlockBreakPickup; }
    public boolean isPullDropsOnBlockBreak() { return pullDropsOnBlockBreak; }

    public boolean isPlaySound() { return playSound; }
    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch() { return soundPitch; }

    public boolean isSpawnParticles() { return spawnParticles; }
    public Object getParticle() { return particle; }

    public int getMaxLevel() { return maxLevel; }

    public boolean isCraftingRecipeEnabled() { return craftingRecipeEnabled; }
    public boolean isLootChestsEnabled() { return lootChestsEnabled; }
    public double getLootChestChance() { return lootChestChance; }
    public boolean isEnchantingTableEnabled() { return enchantingTableEnabled; }
    public double getEnchantingTableChance() { return enchantingTableChance; }
    public boolean isVillagerTradesEnabled() { return villagerTradesEnabled; }
    public double getVillagerTradeChance() { return villagerTradeChance; }
    public boolean isFishingEnabled() { return fishingEnabled; }
    public double getFishingChance() { return fishingChance; }
    public boolean isPiglinBarterEnabled() { return piglinBarterEnabled; }
    public double getPiglinBarterChance() { return piglinBarterChance; }
}
