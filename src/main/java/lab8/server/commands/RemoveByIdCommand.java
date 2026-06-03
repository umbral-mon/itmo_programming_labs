package lab8.server.commands;

import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

public class RemoveByIdCommand extends CollectionCommand {

    public RemoveByIdCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        try {

            int id = Integer.parseInt(args[0]);
            controller.removeById(id, userName);
            //IOHelper.printlnIfUsingConsole("Элемент удален");
            return "Элемент удален";
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex){
            //IOHelper.printlnIfUsingConsole("Для работы команды нужно предоставить корректный id элемента");
            return "Для работы команды нужно предоставить корректный id элемента";
        }
    }

    @Override
    public String getDescription() {
        return "remove_by_id id : удалить элемент из коллекции по его id";
    }
}
