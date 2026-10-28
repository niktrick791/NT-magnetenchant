package de.magnet;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class BookDiscoveryListener implements Listener {

    private final MagnetPlugin plugin;

    public BookDiscoveryListener(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Verzauberungstisch (Enchanting Table) - 1.8 bis 1.21.4+
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isEnchantingTableEnabled()) {
            return;
        }

        ItemStack item = event.getItem();
        MagnetEnchantment enchantment = plugin.getMagnetEnchantment();

        if (!enchantment.isApplicable(item)) {
            return;
        }

        if (Math.random() < config.getEnchantingTableChance()) {
            int cost = event.getExpLevelCost();
            int level = cost >= 30 ? 3 : (cost >= 15 ? 2 : 1);
            level = Math.min(level, config.getMaxLevel());

            enchantment.applyMagnet(item, level);
        }
    }

    /**
     * Angeln (Fishing) - 1.8 bis 1.21.4+
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isFishingEnabled() || event.getCaught() == null) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            Item caught = (Item) event.getCaught();
            if (caught.getItemStack().getType() == Material.BOOK || caught.getItemStack().getType() == XMagnetVersion.getEnchantedBookMaterial()) {
                if (Math.random() < config.getFishingChance()) {
                    int level = Math.random() < 0.8 ? 1 : 2;
                    ItemStack book = plugin.getMagnetEnchantment().createBook(level);
                    caught.setItemStack(book);
                }
            }
        }
    }
}
