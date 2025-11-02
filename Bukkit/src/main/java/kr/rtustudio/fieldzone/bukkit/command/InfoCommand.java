package kr.rtustudio.fieldzone.bukkit.command;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.manager.RegionManager;
import kr.rtustudio.fieldzone.common.data.Point;
import kr.rtustudio.fieldzone.common.region.Region;
import kr.rtustudio.fieldzone.common.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
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
                StringBuilder flagsStr = new StringBuilder();
                for (RegionFlag flag : region.flags()) {
                    if (!flagsStr.isEmpty()) flagsStr.append(", ");
                    String display = message().get(player, "region.flag." + flag.getKey());
                    flagsStr.append(display);
                }
                chat().announce(message().get(player, "region.info.flags").replace("{flags}", flagsStr.toString()));
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
