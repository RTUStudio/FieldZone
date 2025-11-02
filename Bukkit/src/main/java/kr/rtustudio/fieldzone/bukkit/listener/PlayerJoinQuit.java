package kr.rtustudio.fieldzone.bukkit.listener;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.bukkit.manager.WandManager;
import kr.rtustudio.fieldzone.bukkit.mapfrontiers.MapFrontiersBridge;
import kr.rtustudio.fieldzone.common.region.Region;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("unused")
public class PlayerJoinQuit extends RSListener<FieldZone> {

    private final WandManager wandManager;

    public PlayerJoinQuit(FieldZone plugin) {
        super(plugin);
        this.wandManager = plugin.getWandManager();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        String worldName = e.getPlayer().getWorld().getName();
        MapFrontiersConfig cfg = getPlugin().getConfiguration(MapFrontiersConfig.class);
        int delay = cfg.getJoinSyncDelayTicks();
        int retry = Math.max(0, cfg.getJoinSyncRetryCount());
        int interval = Math.max(0, cfg.getJoinSyncRetryIntervalTicks());

        Runnable task = () -> sendAllRegionsForWorld(e.getPlayer().getUniqueId(), worldName);
        if (delay <= 0) task.run(); else Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay);

        // 추가 재시도 스케줄링
        for (int i = 1; i <= retry; i++) {
            long when = (long) delay + (long) interval * i;
            if (when <= 0) when = (long) interval * i;
            Bukkit.getScheduler().runTaskLater(getPlugin(), task, when);
        }
    }

    // 월드 이동 시에도 재동기화 (클라 초기화 타이밍 문제 대비)
    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent e) {
        String worldName = e.getPlayer().getWorld().getName();
        MapFrontiersConfig cfg = getPlugin().getConfiguration(MapFrontiersConfig.class);
        int delay = cfg.getJoinSyncDelayTicks();
        int retry = Math.max(0, cfg.getJoinSyncRetryCount());
        int interval = Math.max(0, cfg.getJoinSyncRetryIntervalTicks());

        Runnable task = () -> sendAllRegionsForWorld(e.getPlayer().getUniqueId(), worldName);
        if (delay <= 0) task.run(); else Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay);
        for (int i = 1; i <= retry; i++) {
            long when = (long) delay + (long) interval * i;
            if (when <= 0) when = (long) interval * i;
            Bukkit.getScheduler().runTaskLater(getPlugin(), task, when);
        }
    }

    // 리스폰 시에도 재동기화(선택적) - 일부 클라이언트에서 맵 초기화 타이밍 이슈 방지
    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        String worldName = e.getPlayer().getWorld().getName();
        MapFrontiersConfig cfg = getPlugin().getConfiguration(MapFrontiersConfig.class);
        int delay = cfg.getJoinSyncDelayTicks();
        int retry = Math.max(0, cfg.getJoinSyncRetryCount());
        int interval = Math.max(0, cfg.getJoinSyncRetryIntervalTicks());

        Runnable task = () -> sendAllRegionsForWorld(e.getPlayer().getUniqueId(), worldName);
        if (delay <= 0) task.run(); else Bukkit.getScheduler().runTaskLater(getPlugin(), task, delay);
        for (int i = 1; i <= retry; i++) {
            long when = (long) delay + (long) interval * i;
            if (when <= 0) when = (long) interval * i;
            Bukkit.getScheduler().runTaskLater(getPlugin(), task, when);
        }
    }

    private void sendAllRegionsForWorld(java.util.UUID playerId, String worldName) {
        var player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) return;
        for (Region region : getPlugin().getRegionManager().getRegions()) {
            if (region.pos().world().equalsIgnoreCase(worldName)) {
                MapFrontiersBridge.sendRegionCreatedToPlayer(player, region);
            }
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        wandManager.remove(e.getPlayer().getUniqueId());
    }

}
