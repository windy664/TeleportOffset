package org.windy.teleportoffset.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.windy.teleportoffset.TeleportOffset;

import java.util.List;

public class PlayerLower implements Listener {
    public PlayerLower() {

    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();
        String worldName = event.getTo().getWorld().getName();
        FileConfiguration config = TeleportOffset.getInstance().getConfig();
        double yThreshold = config.getDouble("void.y");
        boolean enable = config.getBoolean("void.enable");
        List<String> worlds = config.getStringList("void.worlds");
        boolean status = worlds.contains(worldName);


        if (status && playerLocation.getY() < yThreshold && enable) {
            List<String> commands = config.getStringList("void.commands");

            // 使用PlaceholderAPI处理可能存在的占位符
            for (String command : commands) {
                String processedCommand = PlaceholderAPI.setPlaceholders(player, command);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                log("玩家 " + player + " 因y坐标低于阈值被执行: " + processedCommand);
            }
        }
    }
    private void log(String message) {
        if (TeleportOffset.getInstance().getConfig().getBoolean("debug",false)) {
            TeleportOffset.getInstance().getLogger().info(message);
        }
    }
}
