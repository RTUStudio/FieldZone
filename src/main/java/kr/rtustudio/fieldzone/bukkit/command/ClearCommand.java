package kr.rtustudio.fieldzone.bukkit.command;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.manager.WandManager;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
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
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        wandManager.clear(player.getUniqueId());
        chat().announce(message().get(player, "wand.clear"));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        return List.of();
    }

}
