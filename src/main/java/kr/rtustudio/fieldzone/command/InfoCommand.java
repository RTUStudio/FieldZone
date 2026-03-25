package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.fieldzone.region.RegionFlag;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class InfoCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public InfoCommand(FieldZone plugin) {
        super(plugin, "info", PermissionDefault.OP);
        this.manager = plugin.getRegionManager();
    }

    @Override
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        if (data.length() >= 2) {
            String name = data.get(1);
            Region region = manager.get(name);
            if (region == null) {
                notifier.announce(message.get(player, "region.not-found"));
                return Result.FAILURE;
            }

            // 플래그 정보 표시
            if (!region.flags().isEmpty()) {
                String flagsStr = region.flags().stream()
                        .map(RegionFlag::getKey)
                        .map(key -> message.get(player, "region.flag." + key))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                notifier.announce(message.get(player, "region.info.flags").replace("{flags}", flagsStr));
            }

            return Result.SUCCESS;
        }
        return Result.WRONG_USAGE;
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
