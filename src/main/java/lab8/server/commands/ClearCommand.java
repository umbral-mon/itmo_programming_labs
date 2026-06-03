package lab8.server.commands;

import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

public class ClearCommand extends CollectionCommand {

    public ClearCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        controller.clear(userName);
        //IOHelper.printlnIfUsingConsole("Коллекция очищена");
        return "Коллекция очищена";
    }

    @Override
    public String getDescription() {
        return "clear : очистить коллекцию";
    }
}
