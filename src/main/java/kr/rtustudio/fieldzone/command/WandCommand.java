package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.configuration.GlobalConfig;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.registry.CustomItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class WandCommand extends RSCommand<FieldZone> {

    private final GlobalConfig config;

    public WandCommand(FieldZone plugin) {
        super(plugin, "wand", PermissionDefault.OP);
        this.config = plugin.getConfiguration(GlobalConfig.class);
    }

    @Override
    protected Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        String wandItemId = config.getWand().getItem();
        ItemStack wand = CustomItems.from(wandItemId);
        if (wand == null) {
            notifier.announce(message.get(player, "wand.invalid-item"));
            return Result.FAILURE;
        }

        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(plugin.getWandKey(), PersistentDataType.STRING, player.getUniqueId().toString());
            meta.setDisplayName("§6FieldZone Wand");
            meta.setLore(List.of(
                    "§7좌클릭: 점 추가",
                    "§7우클릭: 마지막 점 제거",
                    "§7소유자: §f" + player.getName()
            ));
            wand.setItemMeta(meta);
        }

        if (player.getInventory().firstEmpty() == -1) {
            notifier.announce(message.get(player, "wand.inventory.full"));
            return Result.FAILURE;
        }

        player.getInventory().addItem(wand);
        notifier.announce(message.get(player, "wand.give"));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        return List.of();
    }

}
