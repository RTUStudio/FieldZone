package kr.rtustudio.fieldzone.bukkit.command;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.data.PolygonPos;
import kr.rtustudio.fieldzone.bukkit.data.WandPos;
import kr.rtustudio.fieldzone.bukkit.manager.RegionManager;
import kr.rtustudio.fieldzone.bukkit.manager.WandManager;
import kr.rtustudio.fieldzone.bukkit.region.Region;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class CreateCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;
    private final WandManager wandManager;

    public CreateCommand(FieldZone plugin) {
        super(plugin, "create", PermissionDefault.OP);
        this.manager = plugin.getRegionManager();
        this.wandManager = plugin.getWandManager();
    }

    @Override
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;
        if (data.length() < 2) return Result.WRONG_USAGE;

        String name = data.args(1);
        if (manager.get(name) != null) {
            chat().announce(message().get(player, "region.exists"));
            return Result.FAILURE;
        }

        WandPos wandPos = wandManager.get(player.getUniqueId());
        if (wandPos == null || !wandPos.isValid()) {
            chat().announce(message().get(player, "region.no-position"));
            return Result.FAILURE;
        }

        PolygonPos polygonPos = new PolygonPos(wandPos.world(), wandPos.toPoints());

        // 면적이 0인 경우(직선 혹은 점 형태)는 생성 불가
        if (polygonPos.area() <= 0.0) {
            chat().announce(message().get(player, "region.zero-area"));
            return Result.FAILURE;
        }

        Region region = new Region(name, polygonPos);
        manager.add(region).thenAccept(result -> {
            if (result) {
                chat().announce(message().get(player, "region.create")
                        .replace("{region}", name)
                        .replace("{points}", String.valueOf(polygonPos.points().size()))
                        .replace("{area}", String.format("%.1f", polygonPos.area())));
            } else {
                chat().announce(message().get(player, "region.exists"));
            }
        });
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        return List.of("<이름>");
    }

}
