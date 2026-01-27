package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.data.Point;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class InfoCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public InfoCommand(FieldZone plugin) {
        super(plugin, "info", PermissionDefault.OP);
        this.manager = plugin.getRegionManager();
    }

    @Override
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        if (data.length() >= 2) {
            String name = data.args(1);
            Region region = manager.get(name);
            if (region == null) {
                chat().announce(message().get(player, "region.not-found"));
                return Result.FAILURE;
            }

            chat().announce(message().get(player, "region.info.header").replace("{region}", name));
            chat().announce(message().get(player, "region.info.world").replace("{world}", region.pos().world()));
            chat().announce(message().get(player, "region.info.points").replace("{points}", String.valueOf(region.pos().points().size())));
            chat().announce(message().get(player, "region.info.area").replace("{area}", String.format("%.1f", region.pos().area())));
            chat().announce(message().get(player, "region.info.perimeter").replace("{perimeter}", String.format("%.1f", region.pos().perimeter())));

            Point center = region.pos().center();
            chat().announce(message().get(player, "region.info.center")
                    .replace("{x}", String.valueOf(center.x()))
                    .replace("{z}", String.valueOf(center.z())));

            // 플래그 정보 표시
            if (!region.flags().isEmpty()) {
                String flagsStr = region.flags().stream()
                        .map(f -> message().get(player, "region.flag." + f.getKey()))
                        .reduce((a, b) -> a + ", " + b).orElse("");
                chat().announce(message().get(player, "region.info.flags").replace("{flags}", flagsStr));
            }

            return Result.SUCCESS;
        }
        return Result.WRONG_USAGE;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length() == 2) {
            return manager.getRegions().stream()
                    .map(Region::name)
                    .toList();
        }
        return List.of();
    }

}
