package kr.rtustudio.fieldzone.bukkit.command;

import kr.rtustudio.fieldzone.bukkit.FieldZone;
import kr.rtustudio.fieldzone.bukkit.command.flag.AddCommand;
import kr.rtustudio.fieldzone.bukkit.command.flag.ListCommand;
import kr.rtustudio.fieldzone.bukkit.command.flag.RemoveCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class FlagCommand extends RSCommand<FieldZone> {

    public FlagCommand(FieldZone plugin) {
        super(plugin, "flag", PermissionDefault.OP);
        registerCommand(new AddCommand(plugin));
        registerCommand(new RemoveCommand(plugin));
        registerCommand(new ListCommand(plugin));
    }
}
