package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.command.flag.AddCommand;
import kr.rtustudio.fieldzone.command.flag.ListCommand;
import kr.rtustudio.fieldzone.command.flag.RemoveCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.permissions.PermissionDefault;

public class FlagCommand extends RSCommand<FieldZone> {

    public FlagCommand(FieldZone plugin) {
        super(plugin, "flag", PermissionDefault.OP);
        registerCommand(new AddCommand(plugin));
        registerCommand(new RemoveCommand(plugin));
        registerCommand(new ListCommand(plugin));
    }
}
