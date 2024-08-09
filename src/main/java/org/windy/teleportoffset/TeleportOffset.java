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
import java.util.concurrent.atomic.AtomicReference;


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

    private void log(String message) {
        if (debugMode) {
            this.getLogger().info(message);
        }
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

    // 使用 @EventHandler 注解，表示这是一个事件处理器
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        // 通过 event.getFrom() 获取玩家传送前的位置，并通过 clone() 方法创建一个副本，确保不会对原位置对象进行修改
        Location oldLocation = Objects.requireNonNull(event.getFrom()).clone();
        // 通过 event.getTo() 获取玩家传送后的位置，并通过 clone() 方法创建一个副本，确保不会对原位置对象进行修改
        Location location = Objects.requireNonNull(event.getTo()).clone();

        // 获取进行传送的玩家对象以及目标位置所在的世界对象
        Player player = event.getPlayer();
        // 获取目标世界的名称
        String worldName = Objects.requireNonNull(location.getWorld()).getName();
        // 获取玩家的名称
        String playerName = player.getName();

        // 根据配置判断是否要对玩家的传送目标位置进行偏移
        if (disabledWorlds.contains(worldName)) {
            // 如果目标世界在配置的列表中，将按照配置中的偏移量对目标位置进行修改
            location.add(offsetX, offsetY, offsetZ);
            // 使用自定义的日志记录器记录一条信息，表示玩家已被执行偏移
            log("玩家" + playerName + "已被执行偏移" + offsetX + "," + offsetY + "," + offsetZ);
        } else {
            // 如果目标世界不在禁用列表中，调用自定义方法 findHighestNonAirBlockLocation 找到目标位置的最高非空气方块位置
            location = findHighestNonAirBlockLocation(location);
            // 使用自定义的日志记录器记录一条信息，表示已为玩家找到最高位置
            log("玩家 " + playerName + " 当前世界最高点：" + location);
        }

        // 将调整后的位置对象进行 clone()，以确保在设置传送目的地时不会影响到原始 position 对象，并将这个 clone 后的对象作为最终传送目的地
        final Location finalLocation = location.clone();
        // 通过 event.setTo(finalLocation) 将最终的传送目的地设置到 event 中，使得玩家会被传送到这个新的位置
        event.setTo(finalLocation);

        // 使用自定义的日志记录器记录一条信息，表示玩家应该从旧位置传送到新位置
        log("玩家 " + playerName + " 应该从 " + oldLocation + " 传送到: " + location);

        // 获取目标位置的 Y 坐标值并记录到日志中，以方便后续检查
        double TargetY = location.getY();
        log("获得玩家"+playerName+"的§c目标Y值是："+TargetY);



        // 在传送前设置玩家为无敌状态，确保玩家在传送过程中不会受到伤害
        player.setInvulnerable(true);

                    // 获取玩家当前的实际 Y 坐标值并记录到日志中，以方便后续检查和比较
        Bukkit.getScheduler().runTaskLaterAsynchronously(TeleportOffset.this, () -> {
            double currentY = player.getLocation().getY();
            log("但是玩家" + playerName + "§c实际Y值是：" + currentY);

            if (player.isOnline()) {   // 检查玩家是否在线
                if (Math.abs(currentY - TargetY) <= 1) {
                    // 如果玩家当前的 Y 坐标值与目标 Y 坐标值的差的绝对值小于或等于 1，则记录一条日志，表示玩家的 Y 坐标在误差范围内
                    log("所以玩家 " + playerName + " 的 Y 坐标§c在误差§f的上下 1 个单位范围内: " + currentY);
                } else {
                    // 如果玩家当前的 Y 坐标值与目标 Y 坐标值的差的绝对值大于 1，则记录一条日志，表示玩家的 Y 坐标不在误差范围内，并启动一个新的定时任务来检查 Y 坐标
                    log("因此" + playerName + " 的 Y 坐标§c不在误差§f的上下 1 个单位范围内，开始执行重新传送");
                    event.setTo(finalLocation);
                    // 重新传送一遍，限制重试次数
                    new BukkitRunnable() {
                        int retries = 0;

                        @Override
                        public void run() {
                            if (retries >= times || !player.isOnline()) {
                                this.cancel();
                                return;
                            }

                            AtomicReference<Double> currentY = new AtomicReference<>(player.getLocation().getY());

                            if (Math.abs(currentY.get() - TargetY) <= 1) {
                                // 如果玩家的 Y 坐标在误差范围内，记录一条日志并取消当前定时任务
                                log("现在，" + playerName + " 的 Y 坐标在误差的上下 1 个单位范围内: " + currentY);
                                this.cancel();
                            } else {
                                // 如果玩家的 Y 坐标不在误差范围内，重新传送玩家到最终位置，并增加重试次数
                                Bukkit.getScheduler().runTask(TeleportOffset.this, () -> {
                                    player.teleport(finalLocation);
                                    currentY.set(player.getLocation().getY());
                                    // 记录一条日志，表示重新传送
                                    log("由于未达到预期，继续" + playerName + "再次传送到: " + finalLocation);
                                });
                                retries++;
                            }
                        }
                    }.runTaskTimer(TeleportOffset.this, 0L, 5L); // 每0.5秒检查一次
                }
            } else {
                // 如果玩家不在线，记录一条日志表示已退出传送修正过程
                log("由于玩家 " + playerName + "不在线，已退出传送修正。");
            }
        }, 30L); // 延迟1.5秒（30 ticks）后执行


        // 启动一个新的 BukkitRunnable 任务，用于在 4 秒后移除玩家的无敌效果
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // 如果玩家在线，移除其无敌效果，并记录一条日志表示移除成功
                    player.setInvulnerable(false);
                    log("移除玩家 " + player.getName() + " 的无敌效果。");
                } else {
                    // 如果玩家不在线，记录一条日志表示玩家不存在或已离线
                    log("玩家 " + player.getName() + " 不在线或不存在！");
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
                getLogger().info("玩家 " + player + " 因y坐标低于阈值被执行: " + processedCommand);
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
