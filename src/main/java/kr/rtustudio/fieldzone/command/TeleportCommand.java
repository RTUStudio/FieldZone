package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.data.Point;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.region.Region;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class TeleportCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public TeleportCommand(FieldZone plugin) {
        super(plugin, "teleport", PermissionDefault.OP);
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

            World world = Bukkit.getWorld(region.pos().world());
            if (world == null) {
                notifier.announce(message.get(player, "region.world-not-found"));
                return Result.FAILURE;
            }

            Point center = region.pos().center();
            // 플레이어 현재 높이 또는 월드 스폰 높이 사용
            int y = player.getLocation().getBlockY();
            Location location = new Location(world, center.x() + 0.5, y, center.z() + 0.5);

            // 안전한 위치 찾기 (최대 10블록 위까지)
            int maxAttempts = 10;
            while (maxAttempts-- > 0 && !location.getBlock().isPassable()) {
                location.add(0, 1, 0);
            }

            player.teleport(location);
            notifier.announce(message.get(player, "region.teleport").replace("{region}", name));
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
