package de.magnet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MagnetEnchantment {

    public static final String KEY_NAME = "magnet";
    private NamespacedKey key;
    private final MagnetPlugin plugin;

    public MagnetEnchantment(MagnetPlugin plugin) {
        this.plugin = plugin;
        try {
            this.key = new NamespacedKey(plugin, KEY_NAME);
        } catch (Throwable ignored) {
            this.key = null;
        }
    }

    public NamespacedKey getKey() {
        return key;
    }

    public boolean hasMagnet(ItemStack item) {
        return getLevel(item) > 0;
    }

    public int getLevel(ItemStack item) {
        if (item == null || item.getType().name().endsWith("AIR") || !item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        // Try PDC first
        int pdcLvl = XMagnetVersion.getPdcLevel(meta, key);
        if (pdcLvl > 0) return pdcLvl;

        // Fallback: Check Lore
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    String clean = ChatColor.stripColor(line).trim();
                    if (clean.equalsIgnoreCase("Magnet I")) return 1;
                    if (clean.equalsIgnoreCase("Magnet II")) return 2;
                    if (clean.equalsIgnoreCase("Magnet III")) return 3;
                    if (clean.toLowerCase().startsWith("magnet ")) {
                        String roman = clean.substring(7).trim();
                        int parsed = fromRoman(roman);
                        if (parsed > 0) return parsed;
                    }
                }
            }
        }
        return 0;
    }

    public ItemStack applyMagnet(ItemStack item, int level) {
        if (item == null || item.getType().name().endsWith("AIR")) {
            return item;
        }
        if (level <= 0) {
            return removeMagnet(item);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        XMagnetVersion.setPdcLevel(meta, key, level);
        XMagnetVersion.setGlint(meta);

        // Update Lore
        String magnetLine = "§7Magnet " + toRoman(level);

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        Iterator<String> it = lore.iterator();
        while (it.hasNext()) {
            String clean = ChatColor.stripColor(it.next()).trim();
            if (clean.toLowerCase().startsWith("magnet ")) {
                it.remove();
            }
        }
        lore.add(0, magnetLine);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack removeMagnet(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        XMagnetVersion.removePdcLevel(meta, key);

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            Iterator<String> it = lore.iterator();
            while (it.hasNext()) {
                String clean = ChatColor.stripColor(it.next()).trim();
                if (clean.toLowerCase().startsWith("magnet ")) {
                    it.remove();
                }
            }
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createBook(int level) {
        ItemStack book = new ItemStack(XMagnetVersion.getEnchantedBookMaterial(), 1);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(XMagnetVersion.colorize("&bVerzaubertes Buch"));
            book.setItemMeta(meta);
        }
        return applyMagnet(book, level);
    }

    public int getMaxMagnetLevel(Player player, ConfigManager config) {
        int max = 0;

        // Check Armor
        if (config.isArmorAllowed() && player.getInventory().getArmorContents() != null) {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null) {
                    max = Math.max(max, getLevel(armor));
                }
            }
        }

        // Check Main Hand
        if (config.isHeldItemAllowed()) {
            ItemStack main = XMagnetVersion.getItemInHand(player);
            if (main != null) {
                max = Math.max(max, getLevel(main));
            }
        }

        // Check Off Hand
        if (config.isOffHandAllowed()) {
            try {
                ItemStack off = player.getInventory().getItemInOffHand();
                if (off != null) {
                    max = Math.max(max, getLevel(off));
                }
            } catch (Throwable ignored) {}
        }

        // Check entire inventory if inventory charm is enabled
        if (config.isInventoryCharmAllowed()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    max = Math.max(max, getLevel(item));
                }
            }
        }

        return max;
    }

    public boolean isApplicable(ItemStack item) {
        if (item == null || item.getType().name().endsWith("AIR")) {
            return false;
        }
        String name = item.getType().name();
        ConfigManager config = plugin.getConfigManager();

        if (config.isToolsAllowed() && (name.endsWith("_PICKAXE") || name.endsWith("_AXE") ||
                name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("_SWORD") ||
                name.equals("BOW") || name.equals("CROSSBOW") || name.equals("TRIDENT") || name.equals("FISHING_ROD") || name.equals("MACE"))) {
            return true;
        }

        if (config.isArmorAllowed() && (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.equals("ELYTRA"))) {
            return true;
        }

        if (name.equals("BOOK") || name.equals("ENCHANTED_BOOK")) {
            return true;
        }

        return false;
    }

    public static String toRoman(int n) {
        return switchRoman(n);
    }

    private static String switchRoman(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(n);
        }
    }

    public static int fromRoman(String roman) {
        if (roman == null) return 0;
        switch (roman.toUpperCase().trim()) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
            default:
                try {
                    return Integer.parseInt(roman);
                } catch (NumberFormatException e) {
                    return 0;
                }
        }
    }
}
