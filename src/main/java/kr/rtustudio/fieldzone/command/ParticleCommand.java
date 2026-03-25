package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.WandManager;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;

import java.util.List;

public class ParticleCommand extends RSCommand<FieldZone> {

    private final WandManager wandManager;

    public ParticleCommand(FieldZone plugin) {
        super(plugin, "particle");
        this.wandManager = plugin.getWandManager();
    }

    @Override
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        boolean enabled = wandManager.toggleParticle(player.getUniqueId());
        notifier.announce(player, message.get(player, enabled ? "wand.particle.enable" : "wand.particle.disable"));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        return List.of();
    }

}
