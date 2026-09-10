package de.magnet;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MagnetCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final MagnetPlugin plugin;

    public MagnetCommand(MagnetPlugin plugin) {
        this.plugin = plugin;
    }

    // --- Paper Brigadier BasicCommand ---

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        executeCommand(stack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return suggestCompletions(stack.getSender(), args);
    }

    // --- Bukkit Fallback CommandExecutor & TabCompleter ---

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        executeCommand(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return suggestCompletions(sender, args);
    }

    // --- Core Command Logic ---

    public void executeCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "toggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern ausgeführt werden!").color(NamedTextColor.RED));
                    return;
                }
                if (!player.hasPermission("magnet.toggle") && !player.hasPermission("magnet.use")) {
                    player.sendMessage(Component.text("Keine Berechtigung!").color(NamedTextColor.RED));
                    return;
                }
                boolean newState = plugin.toggleMagnet(player);
                if (newState) {
                    player.sendMessage(Component.text("Magnet-Modus: ").color(NamedTextColor.GRAY)
                            .append(Component.text("AKTIVIERT").color(NamedTextColor.GREEN)));
                } else {
                    player.sendMessage(Component.text("Magnet-Modus: ").color(NamedTextColor.GRAY)
                            .append(Component.text("DEAKTIVIERT").color(NamedTextColor.RED)));
                }
            }

            case "enchant" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern ausgeführt werden!").color(NamedTextColor.RED));
                    return;
                }
                if (!player.hasPermission("magnet.admin")) {
                    player.sendMessage(Component.text("Keine Berechtigung!").color(NamedTextColor.RED));
                    return;
                }

                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().isAir()) {
                    player.sendMessage(Component.text("Du musst ein Item in der Haupthand halten!").color(NamedTextColor.RED));
                    return;
                }

                int level = 1;
                if (args.length > 1) {
                    try {
                        level = Math.max(0, Integer.parseInt(args[1]));
                    } catch (NumberFormatException e) {
                        player.sendMessage(Component.text("Ungültiges Level: " + args[1]).color(NamedTextColor.RED));
                        return;
                    }
                }

                plugin.getMagnetEnchantment().applyMagnet(hand, level);
                if (level == 0) {
                    player.sendMessage(Component.text("Magnet-Verzauberung vom Item entfernt.").color(NamedTextColor.YELLOW));
                } else {
                    player.sendMessage(Component.text("Item erfolgreich mit ")
                            .color(NamedTextColor.GREEN)
                            .append(Component.text("Magnet " + MagnetEnchantment.toRoman(level)).color(NamedTextColor.AQUA))
                            .append(Component.text(" verzaubert!").color(NamedTextColor.GREEN)));
                }
            }

            case "give" -> {
                if (!sender.hasPermission("magnet.admin")) {
                    sender.sendMessage(Component.text("Keine Berechtigung!").color(NamedTextColor.RED));
                    return;
                }

                if (args.length < 2) {
                    sender.sendMessage(Component.text("Verwendung: /magnet give <Spieler> [Level]").color(NamedTextColor.RED));
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Spieler '" + args[1] + "' nicht gefunden!").color(NamedTextColor.RED));
                    return;
                }

                int level = 1;
                if (args.length > 2) {
                    try {
                        level = Math.max(1, Integer.parseInt(args[2]));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Ungültiges Level: " + args[2]).color(NamedTextColor.RED));
                        return;
                    }
                }

                ItemStack book = plugin.getMagnetEnchantment().createBook(level);
                target.getInventory().addItem(book);
                sender.sendMessage(Component.text("Magnet " + MagnetEnchantment.toRoman(level) + " Buch an " + target.getName() + " gegeben.")
                        .color(NamedTextColor.GREEN));
                target.sendMessage(Component.text("Du hast ein Buch mit ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text("Magnet " + MagnetEnchantment.toRoman(level)).color(NamedTextColor.AQUA))
                        .append(Component.text(" erhalten!").color(NamedTextColor.GREEN)));
            }

            case "book" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Dieser Befehl kann nur von Spielern ausgeführt werden! Verwende /magnet give <Spieler> [Level]").color(NamedTextColor.RED));
                    return;
                }
                if (!player.hasPermission("magnet.admin")) {
                    player.sendMessage(Component.text("Keine Berechtigung!").color(NamedTextColor.RED));
                    return;
                }

                int level = 1;
                if (args.length > 1) {
                    try {
                        level = Math.max(1, Integer.parseInt(args[1]));
                    } catch (NumberFormatException e) {
                        player.sendMessage(Component.text("Ungültiges Level: " + args[1]).color(NamedTextColor.RED));
                        return;
                    }
                }

                ItemStack book = plugin.getMagnetEnchantment().createBook(level);
                player.getInventory().addItem(book);
                player.sendMessage(Component.text("Du hast ein Buch mit ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text("Magnet " + MagnetEnchantment.toRoman(level)).color(NamedTextColor.AQUA))
                        .append(Component.text(" erhalten!").color(NamedTextColor.GREEN)));
            }

            case "reload" -> {
                if (!sender.hasPermission("magnet.admin")) {
                    sender.sendMessage(Component.text("Keine Berechtigung!").color(NamedTextColor.RED));
                    return;
                }
                plugin.reload();
                sender.sendMessage(Component.text("[MagnetEnchant] Konfiguration erfolgreich neu geladen!").color(NamedTextColor.GREEN));
            }

            default -> sendHelp(sender);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("--- [ MagnetEnchant Befehle ] ---").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/magnet toggle").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Schaltet das Einsammeln für dich an/aus").color(NamedTextColor.GRAY)));
        if (sender.hasPermission("magnet.admin")) {
            sender.sendMessage(Component.text("/magnet book [Level]").color(NamedTextColor.YELLOW)
                    .append(Component.text(" - Gibt dir direkt ein Magnet-Verzauberungsbuch").color(NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/magnet enchant [Level]").color(NamedTextColor.YELLOW)
                    .append(Component.text(" - Verzaubert das Item in der Hand").color(NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/magnet give <Spieler> [Level]").color(NamedTextColor.YELLOW)
                    .append(Component.text(" - Gibt ein Magnet-Buch an einen Spieler").color(NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/magnet reload").color(NamedTextColor.YELLOW)
                    .append(Component.text(" - Lädt die Konfiguration neu").color(NamedTextColor.GRAY)));
        }
    }

    public List<String> suggestCompletions(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            List<String> completions = new ArrayList<>();
            completions.add("toggle");
            if (sender.hasPermission("magnet.admin")) {
                completions.add("book");
                completions.add("enchant");
                completions.add("give");
                completions.add("reload");
            }
            String current = args.length == 1 ? args[0].toLowerCase() : "";
            return completions.stream().filter(s -> s.startsWith(current)).toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give") && sender.hasPermission("magnet.admin")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
            }
            if ((args[0].equalsIgnoreCase("enchant") || args[0].equalsIgnoreCase("book")) && sender.hasPermission("magnet.admin")) {
                return Arrays.asList("1", "2", "3");
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("magnet.admin")) {
            return Arrays.asList("1", "2", "3");
        }

        return Collections.emptyList();
    }
}
