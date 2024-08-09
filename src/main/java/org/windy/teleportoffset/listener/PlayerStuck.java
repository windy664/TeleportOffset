package org.windy.teleportoffset.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.windy.teleportoffset.TeleportOffset;

public class PlayerStuck implements Listener {

    private boolean stuck;

    public PlayerStuck() {
        // 可以在这里做一些初始化工作
        boolean stuck = TeleportOffset.getInstance().getConfig().getBoolean("stuck",true);

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(stuck){
            return;
        }
        // 异步处理玩家卡方块的检测
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isStuckInBlock(player)) {
                    // 由于Bukkit不允许在异步线程中直接操作玩家，这里需要回到主线程来处理
                    Bukkit.getScheduler().runTask((Plugin) PlayerStuck.this, () -> {
                        player.sendMessage("你被卡在方块里了！,已自动传送。");

                        Location location = player.getLocation();
                        TeleportOffset.getInstance().findHighestNonAirBlockLocation(location);
                        player.teleport(location);
                    });
                }
            }
        }.runTaskAsynchronously((Plugin) PlayerStuck.this);
    }
    private boolean isStuckInBlock(Player player) {
        // 检查玩家头部和脚部周围的方块
        Block[] blocksToCheck = {
                player.getLocation().getBlock(),  // 头部位置
                player.getLocation().add(1, 0, 0).getBlock(),
                player.getLocation().add(-1, 0, 0).getBlock(),
                player.getLocation().add(0, 0, 1).getBlock(),
                player.getLocation().add(0, 0, -1).getBlock(),
                player.getLocation().subtract(0, 1, 0).getBlock(),  // 脚下位置
                player.getLocation().subtract(1, 1, 0).getBlock(),
                player.getLocation().subtract(-1, 1, 0).getBlock(),
                player.getLocation().subtract(0, 1, 1).getBlock(),
                player.getLocation().subtract(0, 1, -1).getBlock()
        };

        // 如果任何一个方块是实心的，则认为玩家被卡住了
        for (Block block : blocksToCheck) {
            if (isSolid(block)) {
                return true;
            }
        }
        return false;
    }
    private boolean isSolid(Block block) {
        Material material = block.getType();
        return material.isSolid();  // 判断方块是否为实心方块
    }

}
