package org.windy.teleportoffset;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class TeleportOffset extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        String version = this.getDescription().getVersion();
        String serverName = this.getServer().getName();
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
        this.getServer().getConsoleSender().sendMessage("§a" + version + " §e " + serverName + "\n");
        this.getServer().getConsoleSender().sendMessage("§6+--------------------------------------+");
        // Plugin startup logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("teleportoffset")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("teleportoffset.reload")) {
                    sender.sendMessage("§c你没有权限来执行这个指令!");
                    return true;
                }

                this.reloadConfig();
                sender.sendMessage("§aTeleportOffset 配置已重新加载!");
                return true;
            } else {
                sender.sendMessage("§c用法: /teleportoffset reload");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        double offsetX = this.getConfig().getDouble("teleport-offset.x");
        double offsetY = this.getConfig().getDouble("teleport-offset.y");
        double offsetZ = this.getConfig().getDouble("teleport-offset.z");

        Location oldLocation = Objects.requireNonNull(event.getFrom()).clone();
        Location location = Objects.requireNonNull(event.getTo()).clone();

        // 获取玩家和目标世界的名字
        Player player = event.getPlayer();
        String worldName = location.getWorld().getName();
        String playerName = player.getName();

        // 如果玩家被传送到他们自己名字的世界，将他们传送到方块的最高点
        if (worldName.equalsIgnoreCase(playerName)) {
            int highestY = location.getWorld().getHighestBlockYAt(location);
            location.setY(highestY);
        } else {
            // 否则，添加偏移量
            location.add(offsetX, offsetY, offsetZ);
        }

        this.getLogger().info("玩家 " + playerName + " 从 " + oldLocation + " 传送到: " + location);
        event.setTo(location);
    }

    @Override
    public void onDisable() {
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
        this.getServer().getConsoleSender().sendMessage("已卸载！\n");
        this.getServer().getConsoleSender().sendMessage("§6+--------------------------------------+");
    }
}
