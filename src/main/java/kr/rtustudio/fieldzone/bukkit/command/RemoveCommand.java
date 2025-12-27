package kr.rtustudio.fieldzone.bukkit.command;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.manager.RegionManager;
import kr.rtustudio.fieldzone.bukkit.region.Region;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
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
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;
        if (data.length() < 2) return Result.WRONG_USAGE;

        String name = data.args(1);
        if (manager.remove(name)) {
            chat().announce(message().get(player, "region.remove").replace("{region}", name));
        } else {
            chat().announce(message().get(player, "region.not-found"));
        }
        return Result.SUCCESS;
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
