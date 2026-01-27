package kr.rtustudio.fieldzone.command.flag;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class ListCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public ListCommand(FieldZone plugin) {
        super(plugin, "list", PermissionDefault.OP);
        this.manager = plugin.getRegionManager();
    }

    @Override
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        if (data.length() < 3) return Result.WRONG_USAGE;

        String regionName = data.args(2);
        Region region = manager.get(regionName);
        if (region == null) {
            chat().announce(message().get(player, "region.not-found"));
            return Result.FAILURE;
        }

        if (region.flags().isEmpty()) {
            chat().announce(message().get(player, "region.flag.list.empty").replace("{region}", regionName));
        } else {
            chat().announce(message().get(player, "region.flag.list.header").replace("{region}", regionName));
            for (RegionFlag flag : region.flags()) {
                chat().announce(message().get(player, "region.flag.list.item")
                        .replace("{name}", message().get(player, "region.flag." + flag.getKey()))
                        .replace("{key}", flag.getKey()));
            }
        }
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length() == 3) {
            return manager.getRegions().stream().map(Region::name).toList();
        }
        return List.of();
    }

}
