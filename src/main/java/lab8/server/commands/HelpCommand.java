package lab8.server.commands;

import lab8.server.commands.base.Command;

import java.util.HashMap;

public class HelpCommand implements Command {

    private HashMap<String, Command> commands;

    public HelpCommand(HashMap<String, Command> commands){
        this.commands = commands;
    }

    @Override
    public String execute(String userName, String... args) {
        StringBuilder builder = new StringBuilder();
        for (var x: commands.entrySet()) {
            //builder.append(x.getKey());
            //builder.append(" ");
            builder.append(x.getValue().getDescription());
            builder.append("\n");
        }
        //System.out.println(builder);
        return builder.toString();
    }

    @Override
    public String getDescription() {
        return "help: вывести справку по доступным командам";
    }
}
