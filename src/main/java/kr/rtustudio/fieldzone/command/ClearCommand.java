package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.WandManager;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class ClearCommand extends RSCommand<FieldZone> {

    private final WandManager wandManager;

    public ClearCommand(FieldZone plugin) {
        super(plugin, "clear", PermissionDefault.OP);
        this.wandManager = plugin.getWandManager();
    }

    @Override
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        wandManager.clear(player.getUniqueId());
        notifier.announce(message.get(player, "wand.clear"));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        return List.of();
    }

}
