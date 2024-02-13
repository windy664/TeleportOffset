package org.windy.teleportoffset;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import java.util.List;
import java.util.Objects;



public class TeleportOffset extends JavaPlugin implements Listener {
    // 默认Debug模式关闭
    String value = this.getConfig().getString("Debug");
    boolean debugMode = Boolean.parseBoolean(value);


    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        String version = this.getDescription().getVersion();
        String serverName = this.getServer().getName();
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
        this.getServer().getConsoleSender().sendMessage("§a" + version + " §e " + serverName + "\n");
        this.getServer().getConsoleSender().sendMessage("§6+--------------------------------------+");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getServer().getConsoleSender().sendMessage("检测到PlaceholderAPI，已兼容！");
        }
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
            } else if(args.length > 0 && args[0].equalsIgnoreCase("debug")) {
                if (!sender.hasPermission("teleportoffset.debug")) {
                    sender.sendMessage("§c你没有权限来执行这个指令!");
                    return true;
                }
                debugMode = !debugMode; // 切换debug模式
                sender.sendMessage("§aDebug模式已临时" + (debugMode ? "开启" : "关闭"));
                return true;
            }else{
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
        List<String> disabledWorlds = this.getConfig().getStringList("Disabled-world"); //白名单世界

        Location oldLocation = Objects.requireNonNull(event.getFrom()).clone();
        Location location = Objects.requireNonNull(event.getTo()).clone();

        // 获取玩家和目标世界的名字
        Player player = event.getPlayer();
        String worldName = Objects.requireNonNull(location.getWorld()).getName();
        String playerName = player.getName();
        if (disabledWorlds.contains(worldName)) {
            location.add(offsetX, offsetY, offsetZ);
        }else{
            location = findHighestNonAirBlockLocation(location);
            if(debugMode) {
                this.getLogger().info("玩家 " + playerName + " 当前世界最高点：" + location);
            }
        }
        event.setTo(location);
        if(debugMode) {
            this.getLogger().info("玩家 " + playerName + " 从 " + oldLocation + " 传送到: " + location);
        }
    }
    private Location findHighestNonAirBlockLocation(Location location) {
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        assert world != null;
        for (int y = world.getMaxHeight(); y >= 0; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isAir()) {
                return block.getLocation();
            }
        }

        return location;  // 如果所有方块都是空气，返回原始位置
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();
        FileConfiguration config = this.getConfig();

        double yThreshold = config.getDouble("void.y");
        boolean enable = config.getBoolean("void.enable");
        String worldName = event.getTo().getWorld().getName();
        List<String> worlds = config.getStringList("void.worlds");
        boolean status = worlds.contains(worldName);

        if (status && playerLocation.getY() < yThreshold && enable) {
            List<String> commands = config.getStringList("void.commands");

            // 使用PlaceholderAPI处理可能存在的占位符
            for (String command : commands) {
                String processedCommand = PlaceholderAPI.setPlaceholders(player, command);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                if (debugMode) {
                    getLogger().info("玩家 " + player + " 因y坐标低于阈值被执行: " + processedCommand);
                }
            }
        }
    }
    @Override
    public void onDisable() {
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
        this.getServer().getConsoleSender().sendMessage("已卸载！\n");
        this.getServer().getConsoleSender().sendMessage("§6+--------------------------------------+");
    }


}
