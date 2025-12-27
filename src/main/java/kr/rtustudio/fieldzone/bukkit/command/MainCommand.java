package kr.rtustudio.fieldzone.bukkit.command;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.bukkit.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.bukkit.manager.RegionManager;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

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
    public void reload(RSCommandData data) {
        getPlugin().reloadConfiguration(GlobalConfig.class);
        getPlugin().reloadConfiguration(MapFrontiersConfig.class);
        manager.reload();
    }

}
