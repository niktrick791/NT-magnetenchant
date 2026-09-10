package de.magnet;

import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;

public class BlockBreakListener implements Listener {

    private final MagnetPlugin plugin;

    public BlockBreakListener(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (!plugin.isMagnetEnabled(player)) {
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        if (config.isDisableWhileSneaking() && player.isSneaking()) {
            return;
        }

        int level = plugin.getMagnetEnchantment().getMaxMagnetLevel(player, config);
        if (level <= 0) {
            return;
        }

        if (config.isInstantBlockBreakPickup()) {
            Iterator<Item> iterator = event.getItems().iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                ItemStack stack = item.getItemStack();
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
                if (leftover.isEmpty()) {
                    item.remove();
                    iterator.remove();
                } else {
                    item.setItemStack(leftover.values().iterator().next());
                }
            }
            if (config.isPlaySound() && event.getItems().isEmpty()) {
                player.playSound(player.getLocation(), config.getSound(), config.getSoundVolume(), config.getSoundPitch());
            }
        } else if (config.isPullDropsOnBlockBreak()) {
            for (Item item : event.getItems()) {
                item.setPickupDelay(0);
                Vector dir = player.getLocation().add(0, 0.75, 0).toVector().subtract(item.getLocation().toVector());
                if (dir.lengthSquared() > 0.01) {
                    item.setVelocity(dir.normalize().multiply(0.65));
                }
            }
        }
    }
}
