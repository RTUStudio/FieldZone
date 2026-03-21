package kr.rtustudio.fieldzone.handler;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.manager.WandManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@SuppressWarnings("unused")
public class PlayerJoinQuit extends RSListener<FieldZone> {

    private final MapFrontiersConfig mfConfig;
    private final WandManager wandManager;

    public PlayerJoinQuit(FieldZone plugin) {
        super(plugin);
        this.mfConfig = plugin.getConfiguration(MapFrontiersConfig.class);
        this.wandManager = plugin.getWandManager();
    }

    private void sendRegion(Player player) {
        String worldName = player.getWorld().getName();
        int delay = mfConfig.getJoinSyncDelayTicks();

        Runnable task = () -> sendAllRegionsForWorld(player.getUniqueId(), worldName);
        if (delay <= 0) task.run();
        else CraftScheduler.delay(getPlugin(), task, delay);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        sendRegion(e.getPlayer());
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent e) {
        sendRegion(e.getPlayer());
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        sendRegion(e.getPlayer());
    }

    private void sendAllRegionsForWorld(java.util.UUID playerId, String worldName) {
        var player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) return;
        for (Region region : getPlugin().getRegionManager().getRegions()) {
            if (region.pos().world().equalsIgnoreCase(worldName)) {
                getPlugin().getMapFrontiersBridge().sendRegionCreatedToPlayer(player, region);
            }
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        wandManager.remove(e.getPlayer().getUniqueId());
    }

}
