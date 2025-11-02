package kr.rtustudio.fieldzone.bukkit;

import kr.rtustudio.fieldzone.bukkit.command.MainCommand;
import kr.rtustudio.fieldzone.bukkit.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.bukkit.configuration.MapFrontiersConfig;
import kr.rtustudio.fieldzone.bukkit.dependency.PlaceholderAPI;
import kr.rtustudio.fieldzone.bukkit.listener.PlayerJoinQuit;
import kr.rtustudio.fieldzone.bukkit.listener.RegionWarning;
import kr.rtustudio.fieldzone.bukkit.listener.WandInteract;
import kr.rtustudio.fieldzone.bukkit.manager.RegionManager;
import kr.rtustudio.fieldzone.bukkit.manager.WandManager;
import kr.rtustudio.fieldzone.bukkit.mapfrontiers.MapFrontiersBridge;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.permissions.PermissionDefault;

public class FieldZone extends RSPlugin {

    @Getter
    private static FieldZone instance;

    @Getter
    private NamespacedKey wandKey;
    @Getter
    private RegionManager regionManager;
    @Getter
    private WandManager wandManager;

    public FieldZone() {
        super("ko_kr");
    }

    @Override
    public void enable() {
        instance = this;

        initStorage("Region");

        wandKey = new NamespacedKey(this, "wand_owner");

        registerConfiguration(GlobalConfig.class, "Global");
        registerConfiguration(MapFrontiersConfig.class, "MapFrontiers");

        regionManager = new RegionManager(this);
        regionManager.reload();

        wandManager = new WandManager(this);

        registerPermission("wand", PermissionDefault.OP);

        registerEvent(new PlayerJoinQuit(this));
        registerEvent(new WandInteract(this));
        registerEvent(new RegionWarning(this));

        registerCommand(new MainCommand(this), true);

        registerIntegration(new PlaceholderAPI(this));

        // MapFrontiers Plugin Messaging 채널 등록 (Bukkit -> Fabric 클라이언트 송신)
        MapFrontiersBridge.register(this);
    }

    @Override
    public void disable() {
        // 등록한 채널 해제
        MapFrontiersBridge.unregister(this);
    }
}
