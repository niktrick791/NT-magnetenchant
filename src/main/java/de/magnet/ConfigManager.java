package de.magnet;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final MagnetPlugin plugin;

    private int tickInterval = 3;
    private boolean disableWhileSneaking = true;
    private boolean onlyPullIfInventorySpace = true;
    private boolean checkMainHand = true;
    private boolean checkOffHand = true;
    private boolean checkArmor = true;
    private boolean instantBlockBreakPickup = false;
    private boolean pullDropsOnBlockBreak = true;

    private boolean playSound = true;
    private Sound sound = Sound.ENTITY_ITEM_PICKUP;
    private float soundVolume = 0.5f;
    private float soundPitch = 1.2f;

    private boolean spawnParticles = true;
    private Particle particle = Particle.PORTAL;

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

    public record LevelSettings(double radius, double speed) {}

    public ConfigManager(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        tickInterval = config.getInt("magnet.tick-interval", 3);
        disableWhileSneaking = config.getBoolean("magnet.disable-while-sneaking", true);
        onlyPullIfInventorySpace = config.getBoolean("magnet.only-pull-if-inventory-space", true);
        checkMainHand = config.getBoolean("magnet.check-main-hand", true);
        checkOffHand = config.getBoolean("magnet.check-off-hand", true);
        checkArmor = config.getBoolean("magnet.check-armor", true);
        instantBlockBreakPickup = config.getBoolean("magnet.instant-block-break-pickup", false);
        pullDropsOnBlockBreak = config.getBoolean("magnet.pull-drops-on-block-break", true);

        playSound = config.getBoolean("effects.sound.enabled", true);
        String soundStr = config.getString("effects.sound.type", "entity.item.pickup").toLowerCase().replace('_', '.');
        try {
            org.bukkit.NamespacedKey soundKey = org.bukkit.NamespacedKey.minecraft(soundStr.replace("minecraft:", ""));
            Sound found = org.bukkit.Registry.SOUNDS.get(soundKey);
            sound = found != null ? found : Sound.ENTITY_ITEM_PICKUP;
        } catch (Exception e) {
            plugin.getLogger().warning("Unbekannter Sound in config: " + soundStr + ", nutze Fallback ENTITY_ITEM_PICKUP");
            sound = Sound.ENTITY_ITEM_PICKUP;
        }
        soundVolume = (float) config.getDouble("effects.sound.volume", 0.5);
        soundPitch = (float) config.getDouble("effects.sound.pitch", 1.2);

        spawnParticles = config.getBoolean("effects.particles.enabled", true);
        String particleStr = config.getString("effects.particles.type", "PORTAL");
        try {
            particle = Particle.valueOf(particleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unbekanntes Partikel in config: " + particleStr + ", nutze Fallback PORTAL");
            particle = Particle.PORTAL;
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

        maxLevel = levels.keySet().stream().max(Integer::compareTo).orElse(3);

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
    public boolean isCheckMainHand() { return checkMainHand; }
    public boolean isCheckOffHand() { return checkOffHand; }
    public boolean isCheckArmor() { return checkArmor; }
    public boolean isInstantBlockBreakPickup() { return instantBlockBreakPickup; }
    public boolean isPullDropsOnBlockBreak() { return pullDropsOnBlockBreak; }

    public boolean isPlaySound() { return playSound; }
    public Sound getSound() { return sound; }
    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch() { return soundPitch; }

    public boolean isSpawnParticles() { return spawnParticles; }
    public Particle getParticle() { return particle; }

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
