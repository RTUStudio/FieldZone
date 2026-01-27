package kr.rtustudio.fieldzone.dependency;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.framework.bukkit.api.integration.wrapper.PlaceholderWrapper;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderWrapper<FieldZone> {

    private final RegionManager manager;
    private final GlobalConfig config;

    public PlaceholderAPI(FieldZone plugin) {
        super(plugin);
        this.manager = plugin.getRegionManager();
        this.config = plugin.getConfiguration(GlobalConfig.class);
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String[] params) {
        if (params.length == 0) return "error: invalid_id";

        String key = params[0].toLowerCase();

        return switch (key) {
            case "region" -> {
                // %fieldzone_region% - 현재 위치한 지역 이름
                if (offlinePlayer.isOnline()) {
                    Player player = offlinePlayer.getPlayer();
                    if (player == null) yield "error: player_not_found";
                    Region region = manager.get(player.getLocation());
                    yield region != null ? region.name() : config.getNoRegionText();
                }
                yield "error: player_offline";
            }
            case "count" -> // %fieldzone_count% - 총 지역 개수
                    String.valueOf(manager.getRegions().size());
            case "area" -> {
                // %fieldzone_area_<name>% - 특정 지역의 면적
                if (params.length < 2) yield "error: missing_argument";
                String name = params[1];
                Region region = manager.get(name);
                yield region != null ? String.format("%.1f", region.pos().area()) : "0";
            }
            case "points" -> {
                // %fieldzone_points_<name>% - 특정 지역의 점 개수
                if (params.length < 2) yield "error: missing_argument";
                String name = params[1];
                Region region = manager.get(name);
                yield region != null ? String.valueOf(region.pos().points().size()) : "0";
            }
            default -> "error: invalid_id";
        };
    }

}
