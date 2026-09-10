package de.magnet;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilGrindstoneListener implements Listener {

    private final MagnetPlugin plugin;

    public AnvilGrindstoneListener(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getFirstItem();
        ItemStack second = inv.getSecondItem();

        if (first == null || second == null || first.getType().isAir() || second.getType().isAir()) {
            return;
        }

        MagnetEnchantment enchantment = plugin.getMagnetEnchantment();
        int firstLevel = enchantment.getLevel(first);
        int secondLevel = enchantment.getLevel(second);

        // If neither has Magnet, vanilla anvil handles it
        if (firstLevel == 0 && secondLevel == 0) {
            return;
        }

        // If second item has Magnet, check if first item can accept it
        if (secondLevel > 0) {
            if (!enchantment.isApplicable(first)) {
                return;
            }

            // Calculate new level
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
            if (result == null || result.getType().isAir()) {
                result = first.clone();
            } else {
                result = result.clone();
            }

            enchantment.applyMagnet(result, newLevel);
            event.setResult(result);

            // Ensure repair cost is reasonable via modern AnvilView
            int currentCost = event.getView().getRepairCost();
            event.getView().setRepairCost(Math.max(1, currentCost + (newLevel * 2)));
        } else if (firstLevel > 0) {
            // First item has Magnet, second does not: preserve Magnet on anvil result
            ItemStack result = event.getResult();
            if (result != null && !result.getType().isAir()) {
                result = result.clone();
                enchantment.applyMagnet(result, firstLevel);
                event.setResult(result);
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }

        // Slot 2 in anvil is the result slot
        if (event.getRawSlot() != 2) {
            return;
        }

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType().isAir() || !plugin.getMagnetEnchantment().hasMagnet(current)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) {
            return;
        }

        AnvilInventory anvilInv = (AnvilInventory) event.getInventory();
        int cost = event.getView() instanceof org.bukkit.inventory.view.AnvilView anvilView ? anvilView.getRepairCost() : 1;

        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE && player.getLevel() < cost) {
            player.sendMessage(net.kyori.adventure.text.Component.text("Nicht genügend Erfahrung! (Benötigt: " + cost + " Level)")
                    .color(net.kyori.adventure.text.format.NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        ItemStack cursor = event.getCursor();
        if (!cursor.getType().isAir()) {
            return;
        }

        event.setCancelled(true);

        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            player.setLevel(Math.max(0, player.getLevel() - cost));
        }

        event.getView().setCursor(current.clone());

        ItemStack first = anvilInv.getFirstItem();
        ItemStack second = anvilInv.getSecondItem();

        if (first != null) {
            int amt = first.getAmount() - 1;
            first.setAmount(amt);
            anvilInv.setFirstItem(amt <= 0 ? null : first);
        }

        if (second != null) {
            int amt = second.getAmount() - 1;
            second.setAmount(amt);
            anvilInv.setSecondItem(amt <= 0 ? null : second);
        }

        anvilInv.setResult(null);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    @EventHandler
    public void onGrindstoneClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.GRINDSTONE) {
            return;
        }

        // Slot 2 in grindstone is the result slot
        if (event.getRawSlot() == 2) {
            ItemStack current = event.getCurrentItem();
            if (current != null && plugin.getMagnetEnchantment().hasMagnet(current)) {
                ItemStack stripped = current.clone();
                plugin.getMagnetEnchantment().removeMagnet(stripped);
                event.setCurrentItem(stripped);
            }
        }
    }
}
