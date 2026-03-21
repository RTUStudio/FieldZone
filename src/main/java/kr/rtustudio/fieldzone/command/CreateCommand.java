package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.data.PolygonPos;
import kr.rtustudio.fieldzone.data.WandPos;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.manager.WandManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
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
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;
        if (data.length() < 2) return Result.WRONG_USAGE;

        String name = data.get(1);
        if (manager.get(name) != null) {
            notifier.announce(message.get(player, "region.exists"));
            return Result.FAILURE;
        }

        WandPos wandPos = plugin.getWandManager().get(player.getUniqueId());
        if (wandPos == null || wandPos.positions().size() < 3) {
            notifier.announce(message.get(player, "region.not-enough-points"));
            return Result.FAILURE;
        }

        Region region = new Region(name, new kr.rtustudio.fieldzone.data.PolygonPos(wandPos.world(), wandPos.toPoints()));
        manager.add(region).thenAccept(success -> {
            if (success) {
                notifier.announce(message.get(player, "region.create").replace("{region}", name));
                plugin.getWandManager().clear(player.getUniqueId());
                // MapFrontiers 연동은 RegionManager 내부에서 add 시 브로드캐스트됨
            } else {
                notifier.announce(message.get(player, "region.exists"));
            }
        });
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        return List.of("<이름>");
    }

}
