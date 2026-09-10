package de.magnet;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Objects;
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

        // Register commands via Paper's modern LifecycleEventManager
        getLifecycleManager().registerEventHandler(io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("magnet", "MagnetEnchant Hauptbefehl", java.util.List.of("mag", "mgn"), new MagnetCommand(this));
        });

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new AnvilGrindstoneListener(this), this);
        getServer().getPluginManager().registerEvents(new BookDiscoveryListener(this), this);

        // Register crafting recipe
        registerRecipe();

        // Start scheduler task
        startTask();

        getLogger().info("MagnetEnchant erfolgreich aktiviert für Purpur 26.2!");
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
            org.bukkit.NamespacedKey recipeKey = new org.bukkit.NamespacedKey(this, "magnet_book_recipe");
            org.bukkit.inventory.ShapedRecipe recipe = new org.bukkit.inventory.ShapedRecipe(recipeKey, magnetEnchantment.createBook(1));
            recipe.shape("RIR", "IBI", "RIR");
            recipe.setIngredient('R', org.bukkit.Material.REDSTONE);
            recipe.setIngredient('I', org.bukkit.Material.IRON_INGOT);
            recipe.setIngredient('B', org.bukkit.Material.BOOK);
            getServer().addRecipe(recipe);
        } catch (Exception ignored) {}
    }

    private void unregisterRecipe() {
        try {
            org.bukkit.NamespacedKey recipeKey = new org.bukkit.NamespacedKey(this, "magnet_book_recipe");
            getServer().removeRecipe(recipeKey);
        } catch (Exception ignored) {}
    }

    private void startTask() {
        if (magnetTask != null && !magnetTask.isCancelled()) {
            magnetTask.cancel();
        }
        int interval = Math.max(1, configManager.getTickInterval());
        magnetTask = new MagnetTask(this).runTaskTimer(this, 10L, interval);
    }

    public boolean isMagnetEnabled(Player player) {
        return !disabledPlayers.contains(player.getUniqueId());
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

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MagnetEnchantment getMagnetEnchantment() {
        return magnetEnchantment;
    }
}
