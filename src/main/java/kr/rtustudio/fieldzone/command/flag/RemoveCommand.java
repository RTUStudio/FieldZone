package kr.rtustudio.fieldzone.command.flag;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        if (data.length() < 4) return Result.WRONG_USAGE;

        String regionName = data.get(2);
        String flagKey = data.get(3);

        Region region = manager.get(regionName);
        if (region == null) {
            notifier.announce(message.get(player, "region.not-found"));
            return Result.FAILURE;
        }

        try {
            RegionFlag flag = RegionFlag.valueOf(flagKey.toUpperCase());
            if (!region.hasFlag(flag)) {
                notifier.announce(message.get(player, "region.flag.not-exists"));
            } else {
                Set<RegionFlag> flags = new HashSet<>(region.flags());
                flags.remove(flag);
                manager.updateFlags(regionName, flags);
                notifier.announce(message.get(player, "region.flag.remove")
                        .replace("{region}", regionName)
                        .replace("{flag}", flag.getKey()));
            }
        } catch (IllegalArgumentException e) {
            notifier.announce(message.get(player, "region.flag.invalid"));
        }
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        if (data.length() == 3) {
            return manager.getRegions().stream().map(Region::name).toList();
        } else if (data.length() == 4) {
            Region region = manager.get(data.get(2));
            if (region != null) {
                return region.flags().stream().map(RegionFlag::getKey).toList();
            }
        }
        return List.of();
    }

    private static RegionFlag parseFlag(String key) {
        try {
            return RegionFlag.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
