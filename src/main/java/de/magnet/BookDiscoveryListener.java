package de.magnet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class BookDiscoveryListener implements Listener {

    private final MagnetPlugin plugin;

    public BookDiscoveryListener(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loot-Kisten (Dungeons, Minenschächte, Antike Städte, etc.)
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isLootChestsEnabled() || event.getLootTable() == null) {
            return;
        }

        String key = event.getLootTable().key().value().toLowerCase();
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
    }

    /**
     * Verzauberungstisch (Enchanting Table)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isEnchantingTableEnabled()) {
            return;
        }

        ItemStack item = event.getItem();
        MagnetEnchantment enchantment = plugin.getMagnetEnchantment();

        if (!enchantment.isApplicable(item) && item.getType() != Material.BOOK) {
            return;
        }

        double chance = config.getEnchantingTableChance();
        int cost = event.getExpLevelCost();
        if (cost >= 25) {
            chance *= 1.5;
        }

        if (Math.random() < chance) {
            int level = 1;
            if (cost >= 30 && Math.random() < 0.35) {
                level = 2;
            }

            final int chosenLevel = level;
            final Player player = event.getEnchanter();

            // Run 1 tick later so vanilla finishes converting BOOK -> ENCHANTED_BOOK and writing enchants
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                // Check slot 0 in the enchanting table view
                ItemStack current = event.getInventory().getItem(0);
                if (current != null && !current.getType().isAir()) {
                    enchantment.applyMagnet(current, chosenLevel);
                    player.sendMessage(Component.text("✨ Du hast zusätzlich ")
                            .color(NamedTextColor.GOLD)
                            .append(Component.text("Magnet " + MagnetEnchantment.toRoman(chosenLevel)).color(NamedTextColor.AQUA))
                            .append(Component.text(" erhalten!").color(NamedTextColor.GOLD)));
                }
            });
        }
    }

    /**
     * Dorfbewohner / Bibliothekare (Villager Trades)
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isVillagerTradesEnabled()) {
            return;
        }

        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();

        if (result.getType() == Material.ENCHANTED_BOOK) {
            if (Math.random() < config.getVillagerTradeChance()) {
                double roll = Math.random();
                int level = roll < 0.70 ? 1 : (roll < 0.95 ? 2 : 3);
                ItemStack magnetBook = plugin.getMagnetEnchantment().createBook(level);

                MerchantRecipe newRecipe = new MerchantRecipe(
                        magnetBook,
                        recipe.getUses(),
                        recipe.getMaxUses(),
                        recipe.hasExperienceReward(),
                        recipe.getVillagerExperience(),
                        recipe.getPriceMultiplier()
                );
                newRecipe.setIngredients(new ArrayList<>(recipe.getIngredients()));
                event.setRecipe(newRecipe);
            }
        }
    }

    /**
     * Angeln (Fishing)
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isFishingEnabled()) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item item) {
            ItemStack caughtItem = item.getItemStack();
            if (caughtItem.getType() == Material.ENCHANTED_BOOK) {
                if (Math.random() < config.getFishingChance()) {
                    int level = Math.random() < 0.8 ? 1 : 2;
                    item.setItemStack(plugin.getMagnetEnchantment().createBook(level));
                }
            }
        }
    }

    /**
     * Piglin Bartering (Nether-Tauschhandel)
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPiglinBarter(PiglinBarterEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isPiglinBarterEnabled()) {
            return;
        }

        List<ItemStack> outcome = event.getOutcome();
        for (int i = 0; i < outcome.size(); i++) {
            ItemStack stack = outcome.get(i);
            if (stack.getType() == Material.ENCHANTED_BOOK) {
                if (Math.random() < config.getPiglinBarterChance()) {
                    outcome.set(i, plugin.getMagnetEnchantment().createBook(1));
                }
            }
        }
    }
}
