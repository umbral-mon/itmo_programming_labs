package lab8.server.commands;

import lab8.server.DataBaseManager;
import lab8.server.commands.base.Command;

public class Register implements Command {

    @Override
    public String execute(String userName, String... args) {
        if (args.length != 2)
            return "Команда должна содержать 2 параметра - логин и пароль";
        boolean result = false;
        if (!DataBaseManager.getInstance().hasUser(args[0]))
            result = DataBaseManager.getInstance().createUser(args[0], args[1]);
        return  result ?
                "ok" :
                "error";
    }

    @Override
    public String getDescription() {
        return "";
    }
}
