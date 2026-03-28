package kr.rtustudio.fieldzone.command.flag;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.FlagState;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.fieldzone.region.RegionFlagRegistry;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.Map;

public class ClearCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public ClearCommand(FieldZone plugin) {
        super(plugin, "clear", PermissionDefault.OP);
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

        RegionFlag flag = RegionFlagRegistry.get(flagKey);
        if (flag == null) {
            notifier.announce(message.get(player, "region.flag.invalid"));
            return Result.FAILURE;
        }

        if (region.hasFlag(flag) == FlagState.NONE) {
            notifier.announce(message.get(player, "region.flag.not-set"));
        } else {
            Map<RegionFlag, Boolean> flags = new Object2BooleanOpenHashMap<>(region.flags());
            flags.remove(flag);
            manager.updateFlags(regionName, flags);
            notifier.announce(message.get(player, "region.flag.clear")
                    .replace("{region}", regionName)
                    .replace("{flag}", flag.getKey()));
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
                return region.flags().keySet().stream().map(RegionFlag::getKey).toList();
            }
        }
        return List.of();
    }
}
