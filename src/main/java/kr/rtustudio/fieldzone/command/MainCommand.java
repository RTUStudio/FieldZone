package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;

public class MainCommand extends RSCommand<FieldZone> {

    private final RegionManager manager;

    public MainCommand(FieldZone plugin) {
        super(plugin, "fieldzone");
        this.manager = plugin.getRegionManager();
        registerCommand(new WandCommand(plugin));
        registerCommand(new CreateCommand(plugin));
        registerCommand(new RemoveCommand(plugin));
        registerCommand(new TeleportCommand(plugin));
        registerCommand(new ListCommand(plugin));
        registerCommand(new InfoCommand(plugin));
        registerCommand(new ClearCommand(plugin));
        registerCommand(new ParticleCommand(plugin));
        registerCommand(new FlagCommand(plugin));
    }

    @Override
    public void reload(CommandArgs data) {
        plugin.reloadConfiguration(GlobalConfig.class);
        plugin.reloadConfiguration(MapFrontiersConfig.class);
        manager.reload();
    }

}
