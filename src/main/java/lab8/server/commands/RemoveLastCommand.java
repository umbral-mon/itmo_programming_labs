package lab8.server.commands;

import lab8.collectionItems.SpaceMarine;
import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

public class RemoveLastCommand extends CollectionCommand {

    public RemoveLastCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        SpaceMarine marine = controller.remove_last(userName);
        //IOHelper.printlnIfUsingConsole(String.format("Элемент %s удален", marine));
        return String.format("Элемент %s удален", marine);
    }

    @Override
    public String getDescription() {
        return "remove_last : удалить последний элемент из коллекции";
    }
}
