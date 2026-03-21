package kr.rtustudio.fieldzone.command.flag;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
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
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        if (data.length() < 3) return Result.WRONG_USAGE;

        String regionName = data.get(2);
        Region region = manager.get(regionName);
        if (region == null) {
            notifier.announce(message.get(player, "region.not-found"));
            return Result.FAILURE;
        }

        if (region.flags().isEmpty()) {
            notifier.announce(message.get(player, "region.flag.empty"));
            return Result.SUCCESS;
        }

        notifier.announce(message.get(player, "region.flag.list.header").replace("{region}", regionName));
        for (RegionFlag flag : region.flags()) {
            notifier.announce(message.get(player, "region.flag.list.item").replace("{flag}", flag.getKey()));
        }
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        if (data.length() == 3) {
            return manager.getRegions().stream().map(Region::name).toList();
        }
        return List.of();
    }

}
