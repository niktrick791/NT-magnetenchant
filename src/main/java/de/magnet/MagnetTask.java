package de.magnet;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;

public class MagnetTask extends BukkitRunnable {

    private final MagnetPlugin plugin;

    public MagnetTask(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ConfigManager config = plugin.getConfigManager();
        MagnetEnchantment enchantment = plugin.getMagnetEnchantment();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.isValid() || player.isDead() || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            if (!plugin.isMagnetEnabled(player)) {
                continue;
            }

            if (config.isDisableWhileSneaking() && player.isSneaking()) {
                continue;
            }

            int level = enchantment.getMaxMagnetLevel(player, config);
            if (level <= 0) {
                continue;
            }

            ConfigManager.LevelSettings settings = config.getLevelSettings(level);
            double radius = settings.radius();
            double speed = settings.speed();

            Location playerLoc = player.getLocation();
            Location targetLoc = playerLoc.clone().add(0, 0.75, 0);

            // Universal getNearbyEntities without Java 8 predicate overload failure
            Collection<Entity> nearby = player.getWorld().getNearbyEntities(playerLoc, radius, radius, radius);
            boolean pulledAny = false;

            for (Entity entity : nearby) {
                if (!(entity instanceof Item)) continue;
                Item item = (Item) entity;
                if (!item.isValid() || item.isDead()) continue;

                // Ignore items with infinite/huge pickup delay (e.g. decorative items)
                if (item.getPickupDelay() >= 32767) continue;

                ItemStack itemStack = item.getItemStack();
                if (config.isOnlyPullIfInventorySpace() && !hasInventorySpace(player, itemStack)) {
                    continue;
                }

                double distSq = item.getLocation().distanceSquared(playerLoc);
                if (distSq > radius * radius) {
                    continue;
                }

                // Make item immediately pickable
                item.setPickupDelay(0);

                Vector dir = targetLoc.toVector().subtract(item.getLocation().toVector());
                double dist = dir.length();

                if (dist > 0.1) {
                    Vector velocity = dir.normalize().multiply(Math.min(dist * 0.4, speed));
                    // Keep small upward arc if on ground
                    if (item.isOnGround() && dist > 1.5) {
                        velocity.setY(Math.max(velocity.getY(), 0.18));
                    }
                    item.setVelocity(velocity);
                    pulledAny = true;
                }

                if (config.isSpawnParticles()) {
                    try {
                        item.getWorld().getClass().getMethod("spawnParticle", Class.forName("org.bukkit.Particle"), Location.class, int.class, double.class, double.class, double.class, double.class)
                                .invoke(item.getWorld(), config.getParticle(), item.getLocation().add(0, 0.2, 0), 1, 0.02, 0.02, 0.02, 0.01);
                    } catch (Throwable ignored) {}
                }
            }

            if (pulledAny && config.isPlaySound() && Math.random() < 0.25) {
                XMagnetVersion.playSound(player, "ENTITY_ITEM_PICKUP", "ITEM_PICKUP", config.getSoundVolume(), config.getSoundPitch());
            }
        }
    }

    public static boolean hasInventorySpace(Player player, ItemStack itemStack) {
        if (itemStack == null || itemStack.getAmount() <= 0) return false;
        PlayerInventory inv = player.getInventory();
        if (inv.firstEmpty() != -1) return true;

        for (ItemStack slot : inv.getContents()) {
            if (slot != null && slot.isSimilar(itemStack)) {
                if (slot.getAmount() < slot.getMaxStackSize()) {
                    return true;
                }
            }
        }
        return false;
    }
}
