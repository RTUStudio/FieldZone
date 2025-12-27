package kr.rtustudio.fieldzone.bukkit.listener;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.configuration.GlobalConfig;
import kr.rtustudio.fieldzone.bukkit.data.WandMode;
import kr.rtustudio.fieldzone.bukkit.data.WandPos;
import kr.rtustudio.fieldzone.bukkit.manager.WandManager;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import kr.rtustudio.framework.bukkit.api.registry.CustomItems;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class WandInteract extends RSListener<FieldZone> {

    private final GlobalConfig config;
    private final WandManager manager;

    private final List<Action> actions = List.of(
            Action.LEFT_CLICK_BLOCK,
            Action.LEFT_CLICK_AIR,
            Action.RIGHT_CLICK_BLOCK,
            Action.RIGHT_CLICK_AIR
    );

    public WandInteract(FieldZone plugin) {
        super(plugin);
        this.config = plugin.getConfiguration(GlobalConfig.class);
        this.manager = plugin.getWandManager();
    }

    @EventHandler
    private void onSwapHand(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        if (!getPlugin().hasPermission(player, "wand")) return;

        ItemStack main = e.getOffHandItem();
        if (!isOwnedWand(player, main)) return;

        WandMode mode = manager.toggleMode(player.getUniqueId());
        String modeKey = "wand.mode." + mode.name().toLowerCase();
        String msg = message().get(player, "wand.mode.toggle")
                .replace("{mode}", message().get(player, modeKey));
        chat().announce(player, msg);
        e.setCancelled(true);
    }

    private boolean isOwnedWand(Player player, ItemStack stack) {
        if (stack == null) return false;
        String id = CustomItems.to(stack);
        if (id == null) return false;
        if (!id.equalsIgnoreCase(config.getWand().getItem())) return false;
        return check(player, stack);
    }

    @EventHandler
    private void onItemInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (!actions.contains(action)) return;
        Player player = e.getPlayer();
        if (!getPlugin().hasPermission(player, "wand")) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String id = CustomItems.to(mainHand);
        if (id == null) return;
        if (!id.equalsIgnoreCase(config.getWand().getItem())) return;
        if (!check(player, mainHand)) return;

        WandMode mode = manager.getMode(player.getUniqueId());
        switch (mode) {
            case SQUARE -> handleSquareMode(player, e, action);
            case RAYCAST -> handleRaycastMode(player, e, action);
            case FREE -> handleFreeMode(player, e, action);
        }
        e.setCancelled(true);
    }

    private void handleSquareMode(Player player, PlayerInteractEvent e, Action action) {
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (action == Action.LEFT_CLICK_BLOCK) {
            manager.addSquareFirst(player, block.getLocation());
            WandPos pos = manager.get(player.getUniqueId());
            int count = pos != null ? pos.positions().size() : 0;
            String msg = replace(message().get(player, "wand.square.first"), block.getLocation(), count);
            chat().announce(player, msg);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            manager.addSquareSecond(player, block.getLocation());
            WandPos pos = manager.get(player.getUniqueId());
            int count = pos != null ? pos.positions().size() : 0;
            String msg = replace(message().get(player, "wand.square.second"), block.getLocation(), count);
            chat().announce(player, msg);
        }
    }

    private void handleRaycastMode(Player player, PlayerInteractEvent e, Action action) {
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            Block block = e.getClickedBlock();
            if (block == null) {
                block = player.getTargetBlockExact(config.getWand().getRaycastMaxRange()); // 최대 200블록까지
            }
            if (block != null) {
                addPosition(player, block.getLocation());
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            removePosition(player);
        }
    }

    private void handleFreeMode(Player player, PlayerInteractEvent e, Action action) {
        if (action == Action.LEFT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if (block == null) return;
            addPosition(player, block.getLocation());
        } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            removePosition(player);
        }
    }

    private boolean check(Player player, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        String uuid = container.get(getPlugin().getWandKey(), PersistentDataType.STRING);
        if (uuid == null) return false;
        try {
            return player.getUniqueId().equals(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void addPosition(Player player, Location location) {
        manager.addPosition(player, location);
        WandPos wandPos = manager.get(player.getUniqueId());
        if (wandPos == null) return;

        int count = wandPos.positions().size();
        String message = replace(message().get(player, "wand.add"), location, count);
        chat().announce(player, message);
    }

    private void removePosition(Player player) {
        UUID uuid = player.getUniqueId();
        WandPos before = manager.get(uuid);
        int beforeCount = before != null ? before.positions().size() : 0;
        if (beforeCount == 0) return; // 제거할 점이 없으면 메시지 출력하지 않음

        manager.removeLastPosition(player);
        WandPos after = manager.get(uuid);
        int count = after != null ? after.positions().size() : 0;
        String message = message().get(player, "wand.remove").replace("{count}", String.valueOf(count));
        chat().announce(player, message);
    }

    private String replace(String message, Location location, int count) {
        return message.replace("{world}", location.getWorld().getName())
                .replace("{x}", String.valueOf(location.getBlockX()))
                .replace("{z}", String.valueOf(location.getBlockZ()))
                .replace("{count}", String.valueOf(count));
    }

}
