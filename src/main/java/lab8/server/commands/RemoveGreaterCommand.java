package lab8.server.commands;

import lab8.collectionItems.SpaceMarine;
import lab8.server.CollectionController;
import lab8.server.MyGsonFactory;
import lab8.server.commands.base.CollectionCommand;

public class RemoveGreaterCommand extends CollectionCommand {

    public RemoveGreaterCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        //SpaceMarine origin = IOHelper.readMarine();
        SpaceMarine origin = new SpaceMarine(MyGsonFactory.get().fromJson(args[1], SpaceMarine.class));
        controller.removeGreater(origin, userName);
        //IOHelper.printlnIfUsingConsole("Элементы удалены");
        return "Элементы удалены";
    }

    @Override
    public String getDescription() {
        return "remove_greater {element} : удалить из коллекции все элементы, превышающие заданный";
    }
}
