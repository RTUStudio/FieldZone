package kr.rtustudio.fieldzone.command;

import kr.rtustudio.fieldzone.FieldZone;
import kr.rtustudio.fieldzone.command.flag.ClearCommand;
import kr.rtustudio.fieldzone.command.flag.ListCommand;
import kr.rtustudio.fieldzone.command.flag.SetCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.permissions.PermissionDefault;

public class FlagCommand extends RSCommand<FieldZone> {

    public FlagCommand(FieldZone plugin) {
        super(plugin, "flag", PermissionDefault.OP);
        registerCommand(new SetCommand(plugin));
        registerCommand(new ClearCommand(plugin));
        registerCommand(new ListCommand(plugin));
    }
}
