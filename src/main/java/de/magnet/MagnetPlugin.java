package de.magnet;

import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MagnetPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MagnetEnchantment magnetEnchantment;
    private final Set<UUID> disabledPlayers = new HashSet<>();
    private BukkitTask magnetTask;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.magnetEnchantment = new MagnetEnchantment(this);

        // Register commands via Bukkit Standard API
        PluginCommand cmd = getCommand("magnet");
        if (cmd != null) {
            MagnetCommand executor = new MagnetCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        } else {
            getLogger().warning("Befehl 'magnet' nicht in plugin.yml gefunden!");
        }

        // Register core listeners (1.8 bis 1.21.4+)
        getServer().getPluginManager().registerEvents(new AnvilGrindstoneListener(this), this);
        getServer().getPluginManager().registerEvents(new BookDiscoveryListener(this), this);

        // Register modern block drop listener (1.13+)
        try {
            Class.forName("org.bukkit.event.block.BlockDropItemEvent");
            getServer().getPluginManager().registerEvents(new BlockDropItemListener(this), this);
        } catch (Throwable ignored) {}

        // Register modern loot/trade/barter listeners (1.14+)
        try {
            Class.forName("org.bukkit.event.world.LootGenerateEvent");
            getServer().getPluginManager().registerEvents(new ModernDiscoveryListener(this), this);
        } catch (Throwable ignored) {}

        // Register crafting recipe
        registerRecipe();

        // Start scheduler task
        startTask();

        getLogger().info("=========================================");
        getLogger().info(" MagnetEnchant v" + getDescription().getVersion() + " wurde erfolgreich aktiviert!");
        getLogger().info(" Multi-Version Support: Minecraft 1.8 - 1.21.4+");
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        if (magnetTask != null) {
            magnetTask.cancel();
        }
        unregisterRecipe();
        disabledPlayers.clear();
        getLogger().info("MagnetEnchant deaktiviert.");
    }

    public void reload() {
        configManager.load();
        unregisterRecipe();
        registerRecipe();
        startTask();
    }

    private void registerRecipe() {
        if (!configManager.isCraftingRecipeEnabled()) {
            return;
        }
        try {
            ItemStack result = magnetEnchantment.createBook(1);
            ShapedRecipe recipe;

            // Try 1.12+ constructor with NamespacedKey
            try {
                Class<?> keyClass = Class.forName("org.bukkit.NamespacedKey");
                Constructor<?> keyCtor = keyClass.getConstructor(org.bukkit.plugin.Plugin.class, String.class);
                Object key = keyCtor.newInstance(this, "magnet_book_recipe");
                Constructor<ShapedRecipe> recipeCtor = ShapedRecipe.class.getConstructor(keyClass, ItemStack.class);
                recipe = recipeCtor.newInstance(key, result);
            } catch (Throwable t) {
                // Pre-1.12 Bukkit constructor
                recipe = new ShapedRecipe(result);
            }

            recipe.shape("RIR", "IBI", "RIR");
            recipe.setIngredient('R', Material.REDSTONE);
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('B', Material.BOOK);

            getServer().addRecipe(recipe);
        } catch (Throwable ex) {
            getLogger().warning("Konnte Magnet-Rezept nicht registrieren: " + ex.getMessage());
        }
    }

    private void unregisterRecipe() {
        try {
            // Safe removal if NamespacedKey exists (1.12+)
            Class<?> keyClass = Class.forName("org.bukkit.NamespacedKey");
            Constructor<?> keyCtor = keyClass.getConstructor(org.bukkit.plugin.Plugin.class, String.class);
            Object key = keyCtor.newInstance(this, "magnet_book_recipe");
            getServer().getClass().getMethod("removeRecipe", keyClass).invoke(getServer(), key);
        } catch (Throwable ignored) {}
    }

    private void startTask() {
        if (magnetTask != null) {
            magnetTask.cancel();
        }
        int interval = Math.max(1, configManager.getTickInterval());
        magnetTask = new MagnetTask(this).runTaskTimer(this, interval, interval);
    }

    public boolean toggleMagnet(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
            return true;
        } else {
            disabledPlayers.add(uuid);
            return false;
        }
    }

    public boolean isMagnetEnabled(Player player) {
        return !disabledPlayers.contains(player.getUniqueId()) && player.hasPermission("magnet.use");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MagnetEnchantment getMagnetEnchantment() {
        return magnetEnchantment;
    }
}
