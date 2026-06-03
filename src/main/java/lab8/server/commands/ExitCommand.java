package lab8.server.commands;

import lab8.server.commands.base.Command;

public class ExitCommand implements Command {
    @Override
    public String execute(String userName, String... args) {
        System.exit(-1);
        return "";
    }

    @Override
    public String getDescription() {
        return "exit : завершить программу (без сохранения в файл)";
    }
}
