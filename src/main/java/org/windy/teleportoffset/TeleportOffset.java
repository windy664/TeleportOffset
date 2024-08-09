package org.windy.teleportoffset;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class TeleportOffset extends JavaPlugin implements Listener {
    // 默认Debug模式关闭
    String value = this.getConfig().getString("Debug");
    boolean debugMode = Boolean.parseBoolean(value);
    private static TeleportOffset instance;
    private String Prefix;
    @Override
    public void onEnable() {
        instance = this;
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
        Prefix = this.getConfig().getString("Prefix");
    }
    public static TeleportOffset getInstance() {
        return instance;
    }
    public void log(String message) {
        if (debugMode) {
            this.getLogger().info(message);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
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
            } else if (args.length > 0 && args[0].equalsIgnoreCase("top")) {
                Location location = player.getLocation();
                findHighestNonAirBlockLocation(location);
                player.teleport(location);
                return true;
            }else{
                sender.sendMessage(Texts.help);
                return true;
            }
        }
        return false;
    }


    public Location findHighestNonAirBlockLocation(Location location) {
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

    @Override
    public void onDisable() {
        this.getServer().getConsoleSender().sendMessage(Texts.logo);
        this.getServer().getConsoleSender().sendMessage("已卸载！\n");
        this.getServer().getConsoleSender().sendMessage("§6+--------------------------------------+");
    }


    public boolean isdebugMode() {
        return debugMode;
    }

    public void setdebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void toggleQueryMode() {
        setdebugMode(!isdebugMode());
    }
    public String prefix() {
        return Prefix;
    }


}
