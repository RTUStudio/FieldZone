package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.manager.WandManager;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import org.bukkit.entity.Player;

import java.util.List;

public class ParticleCommand extends RSCommand<FieldZone> {

    private final WandManager wandManager;

    public ParticleCommand(FieldZone plugin) {
        super(plugin, "particle");
        this.wandManager = plugin.getWandManager();
    }

    @Override
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        boolean enabled = wandManager.toggleParticle(player.getUniqueId());
        String status = enabled ? message().get(player, "particle.on") : message().get(player, "particle.off");
        chat().announce(player, message().get(player, "particle.toggle").replace("{status}", status));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        return List.of();
    }

}
