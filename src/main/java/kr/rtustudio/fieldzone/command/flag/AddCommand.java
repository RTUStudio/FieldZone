package kr.rtustudio.fieldzone.command.flag;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Arrays;
import java.util.List;

public class AddCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public AddCommand(FieldZone plugin) {
        super(plugin, "add", PermissionDefault.OP);
        this.manager = plugin.getRegionManager();
    }

    @Override
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        if (data.length() < 4) return Result.WRONG_USAGE;

        String regionName = data.args(2);
        String flagKey = data.args(3);

        Region region = manager.get(regionName);
        if (region == null) {
            chat().announce(message().get(player, "region.not-found"));
            return Result.FAILURE;
        }

        RegionFlag flag = parseFlag(flagKey);
        if (flag == null) {
            chat().announce(message().get(player, "region.flag.unknown").replace("{flag}", flagKey));
            return Result.FAILURE;
        }

        Region updatedRegion = region.withFlag(flag);
        manager.updateFlags(regionName, updatedRegion.flags());
        chat().announce(message().get(player, "region.flag.add")
                .replace("{region}", regionName)
                .replace("{flag}", message().get(player, "region.flag." + flag.getKey())));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length() == 3) {
            return manager.getRegions().stream().map(Region::name).toList();
        } else if (data.length() == 4) {
            return Arrays.stream(RegionFlag.values()).map(RegionFlag::getKey).toList();
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
