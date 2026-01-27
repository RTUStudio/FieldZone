package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.configuration.GlobalConfig;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
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
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;

        String wandItemId = config.getWand().getItem();
        ItemStack itemStack = CustomItems.from(wandItemId);
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            chat().announce(message().get(player, "wand.invalid-item"));
            return Result.FAILURE;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(getPlugin().getWandKey(), PersistentDataType.STRING, player.getUniqueId().toString());
            meta.setDisplayName("§6FieldZone Wand");
            meta.setLore(List.of(
                    "§7좌클릭: 점 추가",
                    "§7우클릭: 마지막 점 제거",
                    "§7소유자: §f" + player.getName()
            ));
            itemStack.setItemMeta(meta);
        }

        player.getInventory().addItem(itemStack);
        chat().announce(message().get(player, "wand.give"));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        return List.of();
    }

}
