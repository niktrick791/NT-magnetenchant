package de.magnet;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MagnetCommand implements CommandExecutor, TabCompleter {

    private final MagnetPlugin plugin;

    public MagnetCommand(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "toggle":
                handleToggle(sender, args);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "enchant":
                handleEnchant(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage("§cUnbekannter Unterbefehl. Nutze §e/magnet help§c.");
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8=== §bMagnetEnchant §8v" + plugin.getDescription().getVersion() + " ===");
        sender.sendMessage("§e/magnet toggle [spieler] §7- Magnet für dich oder einen Spieler umschalten");
        if (sender.hasPermission("magnet.admin")) {
            sender.sendMessage("§e/magnet give <spieler> [level] §7- Gibt ein Magnet-Verzauberungsbuch");
            sender.sendMessage("§e/magnet enchant [level] §7- Verzaubert das Item in deiner Hand");
            sender.sendMessage("§e/magnet reload §7- Lädt die Konfiguration neu");
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("magnet.toggle")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return;
        }

        Player target;
        if (args.length > 1) {
            if (!sender.hasPermission("magnet.admin")) {
                sender.sendMessage("§cDu darfst den Magnet anderer Spieler nicht umschalten.");
                return;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cSpieler nicht gefunden: " + args[1]);
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§cBitte gib einen Spieler an: /magnet toggle <spieler>");
            return;
        }

        boolean newState = plugin.toggleMagnet(target);
        if (newState) {
            target.sendMessage("§aDein Magnet wurde aktiviert!");
            if (!sender.equals(target)) {
                sender.sendMessage("§aMagnet für §e" + target.getName() + " §aaktiviert.");
            }
        } else {
            target.sendMessage("§cDein Magnet wurde deaktiviert.");
            if (!sender.equals(target)) {
                sender.sendMessage("§cMagnet für §e" + target.getName() + " §cdeaktiviert.");
            }
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("magnet.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cVerwendung: /magnet give <spieler> [level]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden: " + args[1]);
            return;
        }

        int level = 1;
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUngültiges Level: " + args[2]);
                return;
            }
        }

        int max = plugin.getConfigManager().getMaxLevel();
        level = Math.max(1, Math.min(max, level));

        ItemStack book = plugin.getMagnetEnchantment().createBook(level);
        target.getInventory().addItem(book);
        sender.sendMessage("§aDu hast §e" + target.getName() + " §aein Magnet " + MagnetEnchantment.toRoman(level) + " Buch gegeben.");
        target.sendMessage("§aDu hast ein Magnet " + MagnetEnchantment.toRoman(level) + " Buch erhalten!");
    }

    private void handleEnchant(CommandSender sender, String[] args) {
        if (!sender.hasPermission("magnet.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden.");
            return;
        }

        Player player = (Player) sender;
        ItemStack hand = XMagnetVersion.getItemInHand(player);

        if (hand == null || hand.getType().name().endsWith("AIR")) {
            sender.sendMessage("§cDu musst ein Item in der Hand halten.");
            return;
        }

        int level = 1;
        if (args.length > 1) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUngültiges Level: " + args[1]);
                return;
            }
        }

        int max = plugin.getConfigManager().getMaxLevel();
        level = Math.max(1, Math.min(max, level));

        ItemStack updated = plugin.getMagnetEnchantment().applyMagnet(hand, level);
        XMagnetVersion.setItemInHand(player, updated);
        sender.sendMessage("§aItem erfolgreich mit Magnet " + MagnetEnchantment.toRoman(level) + " verzaubert!");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("magnet.admin")) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return;
        }
        plugin.reload();
        sender.sendMessage("§aMagnetEnchant Konfiguration neu geladen!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("toggle", "help"));
            if (sender.hasPermission("magnet.admin")) {
                list.add("give");
                list.add("enchant");
                list.add("reload");
            }
            return filter(args[0], list);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("toggle"))) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                names.add(p.getName());
            }
            return filter(args[1], names);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filter(args[2], Arrays.asList("1", "2", "3"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("enchant")) {
            return filter(args[1], Arrays.asList("1", "2", "3"));
        }
        return Collections.emptyList();
    }

    private List<String> filter(String prefix, List<String> list) {
        List<String> result = new ArrayList<>();
        String p = prefix.toLowerCase();
        for (String s : list) {
            if (s.toLowerCase().startsWith(p)) {
                result.add(s);
            }
        }
        return result;
    }
}
