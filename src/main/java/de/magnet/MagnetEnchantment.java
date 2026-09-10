package de.magnet;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MagnetEnchantment {

    public static final String KEY_NAME = "magnet";
    private final NamespacedKey key;
    private final MagnetPlugin plugin;

    public MagnetEnchantment(MagnetPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, KEY_NAME);
    }

    public NamespacedKey getKey() {
        return key;
    }

    public boolean hasMagnet(ItemStack item) {
        return getLevel(item) > 0;
    }

    public int getLevel(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer level = pdc.get(key, PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }

    public ItemStack applyMagnet(ItemStack item, int level) {
        if (item == null || item.getType().isAir()) {
            return item;
        }
        if (level <= 0) {
            return removeMagnet(item);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.INTEGER, level);

        // Visual enchantment glint
        meta.setEnchantmentGlintOverride(true);

        // Update Lore
        Component magnetLine = Component.text("Magnet " + toRoman(level))
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);

        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore = new ArrayList<>(lore);
            lore.removeIf(line -> {
                String plain = PlainTextComponentSerializer.plainText().serialize(line);
                return plain.toLowerCase().startsWith("magnet ");
            });
        }

        lore.add(0, magnetLine);
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack removeMagnet(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(key, PersistentDataType.INTEGER)) {
            pdc.remove(key);

            List<Component> lore = meta.lore();
            if (lore != null) {
                lore = new ArrayList<>(lore);
                lore.removeIf(line -> {
                    String plain = PlainTextComponentSerializer.plainText().serialize(line);
                    return plain.toLowerCase().startsWith("magnet ");
                });
                meta.lore(lore.isEmpty() ? null : lore);
            }

            // Remove glint override if no other enchants exist
            if (meta.getEnchants().isEmpty()) {
                meta.setEnchantmentGlintOverride(null);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Verzaubertes Buch")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            book.setItemMeta(meta);
        }
        return applyMagnet(book, level);
    }

    public boolean isApplicable(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        Material mat = item.getType();
        String name = mat.name();

        if (mat == Material.BOOK || mat == Material.ENCHANTED_BOOK) return true;

        // Tools
        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") ||
            name.endsWith("_HOE") || name.endsWith("_SWORD") || mat == Material.FISHING_ROD ||
            mat == Material.BOW || mat == Material.CROSSBOW || mat == Material.TRIDENT ||
            mat == Material.MACE || mat == Material.SHEARS) {
            return true;
        }

        // Armor
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
            name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || mat == Material.ELYTRA) {
            return true;
        }

        return false;
    }

    public int getMaxMagnetLevel(Player player, ConfigManager config) {
        int max = 0;

        if (config.isCheckMainHand()) {
            ItemStack main = player.getInventory().getItemInMainHand();
            max = Math.max(max, getLevel(main));
        }

        if (config.isCheckOffHand()) {
            ItemStack off = player.getInventory().getItemInOffHand();
            max = Math.max(max, getLevel(off));
        }

        if (config.isCheckArmor()) {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null) {
                    max = Math.max(max, getLevel(armor));
                }
            }
        }

        return max;
    }

    public static String toRoman(int number) {
        if (number <= 0) return String.valueOf(number);
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }
}
