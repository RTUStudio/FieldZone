package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
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

        List<Region> regions = manager.getRegions();
        if (regions.isEmpty()) {
            notifier.announce(message.get(player, "region.list.empty"));
            return Result.SUCCESS;
        }

        notifier.announce(message.get(player, "region.list.header").replace("{count}", String.valueOf(regions.size())));
        for (Region region : regions) {
            String line = message.get(player, "region.list.format")
                    .replace("{region}", region.name())
                    .replace("{world}", region.pos().world())
                    .replace("{points}", String.valueOf(region.pos().points().size()))
                    .replace("{area}", String.format("%.1f", region.pos().area()));
            notifier.announce(player, line);
        }

        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        return List.of();
    }

}
