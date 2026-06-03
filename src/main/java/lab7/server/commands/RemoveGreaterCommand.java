package lab7.server.commands;

import lab7.server.CollectionController;
import lab7.collectionItems.SpaceMarine;
import lab7.server.MyGsonFactory;
import lab7.server.commands.base.CollectionCommand;
import lab7.utils.IOHelper;

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
