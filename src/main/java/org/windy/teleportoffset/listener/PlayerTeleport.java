package org.windy.teleportoffset.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.windy.teleportoffset.TeleportOffset;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerTeleport implements Listener {
    private boolean isTeleporting = false;
    List<String> disabledWorlds;
    double offsetX;
    double offsetY;
    double offsetZ;
    int times;
    public PlayerTeleport() {
        offsetX = TeleportOffset.getInstance().getConfig().getDouble("teleport-offset.x");
        offsetY = TeleportOffset.getInstance().getConfig().getDouble("teleport-offset.y");
        offsetZ = TeleportOffset.getInstance().getConfig().getDouble("teleport-offset.z");
        disabledWorlds = TeleportOffset.getInstance().getConfig().getStringList("Disabled-world");
        times = TeleportOffset.getInstance().getConfig().getInt("times");
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        if (isTeleporting) {
            return; // 如果当前正在传送，则跳过事件处理
        }
        isTeleporting = true;
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
            location = TeleportOffset.getInstance().findHighestNonAirBlockLocation(location);
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
        Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin) PlayerTeleport.this, () -> {
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
                                Bukkit.getScheduler().runTask((Plugin) PlayerTeleport.this, () -> {
                                    player.teleport(finalLocation);
                                    currentY.set(player.getLocation().getY());
                                    // 记录一条日志，表示重新传送
                                    log("由于未达到预期，继续" + playerName + "再次传送到: " + finalLocation);
                                });
                                retries++;
                            }
                        }
                    }.runTaskTimer((Plugin) PlayerTeleport.this, 0L, 5L); // 每0.5秒检查一次
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
        }.runTaskLater((Plugin) this, 80L);
        isTeleporting = false;
    }
    private void log(String message) {
        if (TeleportOffset.getInstance().getConfig().getBoolean("debug",false)) {
            TeleportOffset.getInstance().getLogger().info(message);
        }
    }
}
