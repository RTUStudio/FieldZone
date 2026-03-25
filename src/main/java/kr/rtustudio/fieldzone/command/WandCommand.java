package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.WandManager;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class WandCommand extends RSCommand<FieldZone> {

    private final WandManager wandManager;

    public WandCommand(FieldZone plugin) {
        super(plugin, "wand", PermissionDefault.OP);
        this.wandManager = plugin.getWandManager();
    }

    @Override
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        if (wandManager.giveWand(player)) {
            return Result.SUCCESS;
        } else {
            return Result.FAILURE;
        }
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        return List.of();
    }

}
