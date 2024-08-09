package org.windy.teleportoffset;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final TeleportOffset plugin;

    public CommandHandler() {
        this.plugin = TeleportOffset.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!command.getName().equalsIgnoreCase("teleportoffset")) {
            return false;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig(sender);
                break;
            case "debug":
                toggleDebugMode(player);
                break;
            case "top":
                teleportToHighestPoint(player);
                break;
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void reloadConfig(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage("§aTeleportOffset configuration reloaded!");
    }

    private void toggleDebugMode(Player player) {
        plugin.toggleQueryMode();
        player.sendMessage(plugin.prefix() + "Debug mode is now " + (plugin.isdebugMode() ? "enabled" : "disabled") + ".");
    }

    private void teleportToHighestPoint(Player player) {
        try {
            player.sendMessage(plugin.prefix() + "Teleporting you to the highest point...");
            Location location = player.getLocation();
            plugin.findHighestNonAirBlockLocation(location);
            player.teleport(location);
            player.sendMessage(plugin.prefix() + "Successfully teleported to the highest point!");
        } catch (Exception e) {
            player.sendMessage(plugin.prefix() + "An error occurred while teleporting you.");
            e.printStackTrace();
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Texts.help);
    }
}
