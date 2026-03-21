package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class RemoveCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public RemoveCommand(FieldZone plugin) {
        super(plugin, "remove", PermissionDefault.OP);
        this.manager = plugin.getRegionManager();
    }

    @Override
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;
        if (data.length() < 2) return Result.WRONG_USAGE;

        String name = data.get(1);
        if (manager.remove(name)) {
            notifier.announce(message.get(player, "region.remove").replace("{region}", name));
        } else {
            notifier.announce(message.get(player, "region.not-found"));
        }
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        if (data.length() == 2) {
            return manager.getRegions().stream()
                    .map(Region::name)
                    .toList();
        }
        return List.of();
    }

}
