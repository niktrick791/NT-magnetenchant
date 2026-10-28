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
import java.util.Locale;

public class MagnetCommand implements CommandExecutor, TabCompleter {

    private final MagnetPlugin plugin;

    public MagnetCommand(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
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
            case "lang":
            case "language":
                handleLanguage(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(plugin.getConfigManager().getMessage("unknown-sub", "§cUnbekannter Unterbefehl. Nutze §e/magnet help§c."));
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        ConfigManager cfg = plugin.getConfigManager();
        sender.sendMessage("§8=== §bMagnetEnchant §8v" + plugin.getDescription().getVersion() + " ===");
        sender.sendMessage(cfg.getMessage("help-toggle", "§e/magnet toggle [player] §7- Toggle magnet for yourself or a player"));
        if (sender.hasPermission("magnet.admin") || sender.isOp()) {
            sender.sendMessage(cfg.getMessage("help-give", "§e/magnet give <player> [level] §7- Gives an enchanted magnet book"));
            sender.sendMessage(cfg.getMessage("help-enchant", "§e/magnet enchant [level] §7- Enchants the item in your hand"));
            sender.sendMessage(cfg.getMessage("help-lang", "§e/magnet lang <de|en> §7- Changes the plugin language"));
            sender.sendMessage(cfg.getMessage("help-reload", "§e/magnet reload §7- Reloads configuration"));
        }
    }

    private void handleLanguage(CommandSender sender, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("magnet.admin") && !sender.isOp()) {
            sender.sendMessage(cfg.getMessage("no-permission", "§cDazu hast du keine Berechtigung."));
            return;
        }

        if (args.length < 2) {
            boolean isDe = "de".equalsIgnoreCase(cfg.getLanguage());
            sender.sendMessage(isDe
                    ? "§eAktuelle Sprache: §bDeutsch§e. Nutze §b/magnet lang <de|en>§e."
                    : "§eCurrent language: §bEnglish§e. Use §b/magnet lang <de|en>§e.");
            return;
        }

        String target = args[1].toLowerCase(Locale.ROOT);
        if (!target.equals("de") && !target.equals("en")) {
            sender.sendMessage("§cInvalid language / Ungültige Sprache: §e" + target + " §7(de / en)");
            return;
        }

        cfg.setLanguage(target);
        sender.sendMessage(cfg.getMessage("lang-changed", "§aLanguage successfully updated!"));
    }

    private void handleToggle(CommandSender sender, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("magnet.toggle") && !sender.isOp()) {
            sender.sendMessage(cfg.getMessage("no-permission", "§cDazu hast du keine Berechtigung."));
            return;
        }

        Player target;
        if (args.length > 1) {
            if (!sender.hasPermission("magnet.admin") && !sender.isOp()) {
                sender.sendMessage(cfg.getMessage("no-permission", "§cDazu hast du keine Berechtigung."));
                return;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cSpieler nicht gefunden / Player not found: " + args[1]);
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§cUsage: /magnet toggle <player>");
            return;
        }

        boolean newState = plugin.toggleMagnet(target);
        if (newState) {
            target.sendMessage(cfg.getMessage("toggle-on", "§aYour magnet has been enabled!"));
            if (!sender.equals(target)) {
                sender.sendMessage(cfg.getMessage("toggle-other-on", "§aMagnet for §e{target} §aenabled.").replace("{target}", target.getName()));
            }
        } else {
            target.sendMessage(cfg.getMessage("toggle-off", "§cYour magnet has been disabled."));
            if (!sender.equals(target)) {
                sender.sendMessage(cfg.getMessage("toggle-other-off", "§cMagnet for §e{target} §cdisabled.").replace("{target}", target.getName()));
            }
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("magnet.admin") && !sender.isOp()) {
            sender.sendMessage(cfg.getMessage("no-permission", "§cDazu hast du keine Berechtigung."));
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
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUngültiges Level: " + args[2]);
                return;
            }
        }

        ItemStack book = plugin.getMagnetEnchantment().createBook(level);
        target.getInventory().addItem(book);
        sender.sendMessage(cfg.getMessage("give-success", "§aMagnet-Buch Level {level} an §e{target} §agegeben.")
                .replace("{level}", String.valueOf(level))
                .replace("{target}", target.getName()));
    }

    private void handleEnchant(CommandSender sender, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("magnet.admin") && !sender.isOp()) {
            sender.sendMessage(cfg.getMessage("no-permission", "§cDazu hast du keine Berechtigung."));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl ausführen.");
            return;
        }

        Player player = (Player) sender;
        ItemStack item = XMagnetVersion.getItemInHand(player);

        if (item == null || !plugin.getMagnetEnchantment().isApplicable(item)) {
            sender.sendMessage(cfg.getMessage("enchant-no-item", "§cDu musst ein Werkzeug oder Rüstungsteil in der Hand halten."));
            return;
        }

        int level = 1;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUngültiges Level: " + args[1]);
                return;
            }
        }

        ItemStack enchanted = plugin.getMagnetEnchantment().applyMagnet(item, level);
        XMagnetVersion.setItemInHand(player, enchanted);
        sender.sendMessage(cfg.getMessage("enchant-success", "§aItem erfolgreich mit §bMagnet {level} §averzaubert!")
                .replace("{level}", String.valueOf(level)));
    }

    private void handleReload(CommandSender sender) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!sender.hasPermission("magnet.admin") && !sender.isOp()) {
            sender.sendMessage(cfg.getMessage("no-permission", "§cDazu hast du keine Berechtigung."));
            return;
        }

        cfg.load();
        sender.sendMessage(cfg.getMessage("reload-success", "§aKonfiguration erfolgreich neu geladen!"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> sub = new ArrayList<String>();
            sub.add("toggle");
            if (sender.hasPermission("magnet.admin") || sender.isOp()) {
                sub.add("give");
                sub.add("enchant");
                sub.add("lang");
                sub.add("language");
                sub.add("reload");
            }
            return filterStartingWith(args[0], sub);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if ((sub.equals("lang") || sub.equals("language")) && (sender.hasPermission("magnet.admin") || sender.isOp())) {
                return filterStartingWith(args[1], Arrays.asList("de", "en"));
            }
            if (sub.equals("toggle") && (sender.hasPermission("magnet.admin") || sender.isOp())) {
                List<String> players = new ArrayList<String>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    players.add(p.getName());
                }
                return filterStartingWith(args[1], players);
            }
            if (sub.equals("give") && (sender.hasPermission("magnet.admin") || sender.isOp())) {
                List<String> players = new ArrayList<String>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    players.add(p.getName());
                }
                return filterStartingWith(args[1], players);
            }
            if (sub.equals("enchant") && (sender.hasPermission("magnet.admin") || sender.isOp())) {
                return filterStartingWith(args[1], Arrays.asList("1", "2", "3"));
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && (sender.hasPermission("magnet.admin") || sender.isOp())) {
            return filterStartingWith(args[2], Arrays.asList("1", "2", "3"));
        }

        return Collections.emptyList();
    }

    private List<String> filterStartingWith(String prefix, List<String> list) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<String>();
        for (String item : list) {
            if (item.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(item);
            }
        }
        return result;
    }
}
