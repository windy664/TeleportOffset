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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;



public class TeleportOffset extends JavaPlugin implements Listener {
    // 默认Debug模式关闭
    String value = this.getConfig().getString("Debug");
    boolean debugMode = Boolean.parseBoolean(value);

    double offsetX;
    double offsetY;
    double offsetZ;
    List<String> disabledWorlds;
    int times;
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
        //获取配置文件
        offsetX = this.getConfig().getDouble("teleport-offset.x");
        offsetY = this.getConfig().getDouble("teleport-offset.y");
        offsetZ = this.getConfig().getDouble("teleport-offset.z");
        disabledWorlds = this.getConfig().getStringList("Disabled-world");
        times = this.getConfig().getInt("times");
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
        //旧位置
        Location oldLocation = Objects.requireNonNull(event.getFrom()).clone();
        //目的位置
        Location location = Objects.requireNonNull(event.getTo()).clone();

        // 获取玩家和目标世界的名字
        Player player = event.getPlayer();
        String worldName = Objects.requireNonNull(location.getWorld()).getName();
        String playerName = player.getName();

        if (disabledWorlds.contains(worldName)) {
            location.add(offsetX, offsetY, offsetZ);
            this.getLogger().info("玩家" + playerName + "已被执行偏移" + offsetX + "," + offsetY + "," + offsetZ);
        }else{
            location = findHighestNonAirBlockLocation(location);
            if(debugMode) {
                this.getLogger().info("玩家 " + playerName + " 当前世界最高点：" + location);
            }
        }
        final Location finalLocation = location.clone();
        event.setTo(finalLocation);


        if(debugMode) {
            this.getLogger().info("玩家 " + playerName + " 应该从 " + oldLocation + " 传送到: " + location);
        }


        //传送检查

        double initialY = player.getLocation().getY();
        if(debugMode) {
            getLogger().info("玩家" + playerName + "Y值应该是：" + initialY);
        }
        // 传送前设置无敌
        player.setInvulnerable(true);

      //  new BukkitRunnable() {
        //    @Override
          //  public void run() {
        if (player.isOnline()) {   // 检查玩家是否在线
            double currentY = player.getLocation().getY(); //检查后的Y值
            if (Math.abs(currentY - initialY) <= 1) {
                if(debugMode) {
                    getLogger().info("但是玩家 " + playerName + " 的 Y 坐标在误差的上下 1 个单位范围内: " + currentY);
                }
            } else {
                if (debugMode) {
                    getLogger().info("但是玩家 " + playerName + " 的 Y 坐标不在误差的上下 1 个单位范围内: " + currentY);
                    // 重新传送一遍，限制重试次数
                }
                    new BukkitRunnable() {
                        int retries = 0;

                        @Override
                        public void run() {
                            if (retries >= times || !player.isOnline()) {
                                this.cancel();
                                return;
                            }

                            double currentY = player.getLocation().getY();
                            if (Math.abs(currentY - initialY) <= 1) {
                                if(debugMode) {
                                    getLogger().info("尝试重新传送，但是 " + playerName + " 的 Y 坐标在误差的上下 1 个单位范围内: " + currentY);
                                }
                                this.cancel();
                            } else {
                                // 重新传送玩家
                                Bukkit.getScheduler().runTask(TeleportOffset.this, () -> {
                                    player.teleport(finalLocation);
                                    if(debugMode){
                                        getLogger().info("因此玩家" + playerName + "再次传送到: " + finalLocation);
                                    }
                                });
                                retries++;
                            }
                        }
                    }.runTaskTimer(TeleportOffset.this, 0L, 5L); // 每0.5秒检查一次
                }
        } else if(debugMode){
            getLogger().info("由于玩家 " + playerName + "不在线，已退出传送修正。");
        }
         //   }
      //  }.runTaskLater(this, 60L); // 3秒后检查

        // 4秒后移除无敌效果
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.setInvulnerable(false);
                    getLogger().info("移除玩家 " + player.getName() + " 的无敌效果。");
                } else {
                    getLogger().info("玩家 " + player.getName() + " 不在线或不存在！");
                }
            }
        }.runTaskLater(this, 80L);
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
