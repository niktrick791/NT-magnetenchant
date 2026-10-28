package de.magnet;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilGrindstoneListener implements Listener {

    private final MagnetPlugin plugin;

    public AnvilGrindstoneListener(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (first == null || second == null || first.getType().name().endsWith("AIR") || second.getType().name().endsWith("AIR")) {
            return;
        }

        MagnetEnchantment enchantment = plugin.getMagnetEnchantment();
        int firstLevel = enchantment.getLevel(first);
        int secondLevel = enchantment.getLevel(second);

        if (firstLevel == 0 && secondLevel == 0) {
            return;
        }

        if (secondLevel > 0) {
            if (!enchantment.isApplicable(first)) {
                return;
            }

            int maxLevel = plugin.getConfigManager().getMaxLevel();
            int newLevel;
            if (firstLevel == 0) {
                newLevel = secondLevel;
            } else if (firstLevel == secondLevel) {
                newLevel = Math.min(maxLevel, firstLevel + 1);
            } else {
                newLevel = Math.max(firstLevel, secondLevel);
            }

            ItemStack result = event.getResult();
            if (result == null || result.getType().name().endsWith("AIR")) {
                result = first.clone();
            } else {
                result = result.clone();
            }

            result = enchantment.applyMagnet(result, newLevel);
            event.setResult(result);

            try {
                inv.getClass().getMethod("setRepairCost", int.class).invoke(inv, Math.max(1, newLevel * 2));
            } catch (Throwable ignored) {}
        } else if (firstLevel > 0) {
            ItemStack result = event.getResult();
            if (result != null && !result.getType().name().endsWith("AIR")) {
                result = enchantment.applyMagnet(result.clone(), firstLevel);
                event.setResult(result);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == null) return;
        String typeName = event.getInventory().getType().name();

        if (typeName.equals("GRINDSTONE")) {
            if (event.getRawSlot() == 2) {
                ItemStack current = event.getCurrentItem();
                if (current != null && plugin.getMagnetEnchantment().hasMagnet(current)) {
                    ItemStack cleaned = plugin.getMagnetEnchantment().removeMagnet(current.clone());
                    event.setCurrentItem(cleaned);
                }
            }
        }
    }
}
