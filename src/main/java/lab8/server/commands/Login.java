package lab8.server.commands;

import lab8.client.BCrypt;
import lab8.server.DataBaseManager;
import lab8.server.commands.base.Command;

public class Login implements Command {

    @Override
    public String execute(String userName, String... args) {
        if (args.length != 2)
            return "Команда должна содержать 2 параметра - логин и пароль";
        String userHash = DataBaseManager.getInstance().getUserPasswordHash(args[0]);
        if (userHash == null)
            return "Неверный логин или пароль";
        return BCrypt.checkpw(args[1], userHash) ?
                "Добро пожаловать!" :
                "Нет такого пользователя";
    }

    @Override
    public String getDescription() {
        return "";
    }
}
