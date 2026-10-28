package de.magnet;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class ModernDiscoveryListener implements Listener {

    private final MagnetPlugin plugin;

    public ModernDiscoveryListener(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isLootChestsEnabled() || event.getLootTable() == null) {
            return;
        }

        try {
            String key = event.getLootTable().getKey().toString().toLowerCase();
            if (key.contains("chest") || key.contains("dungeon") || key.contains("mineshaft") ||
                key.contains("ancient_city") || key.contains("stronghold") || key.contains("pyramid") ||
                key.contains("temple") || key.contains("bastion") || key.contains("end_city") ||
                key.contains("underwater_ruin") || key.contains("shipwreck")) {

                if (Math.random() < config.getLootChestChance()) {
                    double roll = Math.random();
                    int level = roll < 0.70 ? 1 : (roll < 0.93 ? 2 : 3);
                    ItemStack book = plugin.getMagnetEnchantment().createBook(level);
                    event.getLoot().add(book);
                }
            }
        } catch (Throwable ignored) {}
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerTrade(VillagerAcquireTradeEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isVillagerTradesEnabled()) {
            return;
        }

        MerchantRecipe recipe = event.getRecipe();
        if (recipe.getResult().getType() == Material.ENCHANTED_BOOK) {
            if (Math.random() < config.getVillagerTradeChance()) {
                double roll = Math.random();
                int level = roll < 0.60 ? 1 : (roll < 0.90 ? 2 : 3);
                ItemStack book = plugin.getMagnetEnchantment().createBook(level);

                int price = 12 + (level * 8);
                ItemStack emeraldCost = new ItemStack(Material.EMERALD, price);
                ItemStack bookCost = new ItemStack(Material.BOOK, 1);

                List<ItemStack> ingredients = new ArrayList<>();
                ingredients.add(emeraldCost);
                ingredients.add(bookCost);

                MerchantRecipe magnetTrade = new MerchantRecipe(book, recipe.getMaxUses());
                magnetTrade.setIngredients(ingredients);
                magnetTrade.setExperienceReward(recipe.hasExperienceReward());
                magnetTrade.setVillagerExperience(recipe.getVillagerExperience());

                event.setRecipe(magnetTrade);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isPiglinBarterEnabled()) {
            return;
        }

        if (Math.random() < config.getPiglinBarterChance()) {
            ItemStack book = plugin.getMagnetEnchantment().createBook(1);
            event.getOutcome().add(book);
        }
    }
}
