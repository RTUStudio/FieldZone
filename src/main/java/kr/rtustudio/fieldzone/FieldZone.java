package kr.rtustudio.fieldzone;

import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.fieldzone.bridge.MapFrontiersBridge;
import kr.rtustudio.fieldzone.command.MainCommand;
import kr.rtustudio.fieldzone.configuration.FlagConfig;
import kr.rtustudio.fieldzone.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.integration.PlaceholderAPI;
import kr.rtustudio.fieldzone.handler.PlayerJoinQuit;
import kr.rtustudio.fieldzone.handler.RegionWarning;
import kr.rtustudio.fieldzone.handler.WandInteract;
import kr.rtustudio.fieldzone.manager.RegionManager;
import kr.rtustudio.fieldzone.manager.WandManager;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.permissions.PermissionDefault;

public class FieldZone extends RSPlugin {

    @Getter
    private static FieldZone instance;

    @Getter
    private final NamespacedKey wandKey = new NamespacedKey(this, "wand_owner");
    @Getter
    private RegionManager regionManager;
    @Getter
    private WandManager wandManager;

    @Getter
    private MapFrontiersBridge mapFrontiersBridge;

    public FieldZone() {
        super("ko_kr");
        loadLibrary("it.unimi.dsi:fastutil:8.5.15");
    }

    public void load() {
        instance = this;
    }

    @Override
    public void enable() {
        registerStorage("Region");

        registerConfiguration(GlobalConfig.class, ConfigPath.of("Global"));
        registerConfiguration(FlagConfig.class, ConfigPath.of("Flag"));
        registerConfiguration(MapFrontiersConfig.class, ConfigPath.of("Bridges", "MapFrontiers"));

        regionManager = new RegionManager(this);
        wandManager = new WandManager(this);

        registerPermission("wand", PermissionDefault.OP);

        registerEvent(new PlayerJoinQuit(this));
        registerEvent(new WandInteract(this));
        registerEvent(new RegionWarning(this));

        registerCommand(new MainCommand(this), true);

        registerIntegration(new PlaceholderAPI(this));

        mapFrontiersBridge = new MapFrontiersBridge(this);
    }

    @Override
    public void disable() {
        if (mapFrontiersBridge != null) {
            mapFrontiersBridge.close();
            mapFrontiersBridge = null;
        }
    }
}
