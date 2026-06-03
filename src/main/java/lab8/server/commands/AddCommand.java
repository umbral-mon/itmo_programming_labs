package lab8.server.commands;

import lab8.collectionItems.SpaceMarine;
import lab8.server.CollectionController;
import lab8.server.MyGsonFactory;
import lab8.server.commands.base.CollectionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCommand extends CollectionCommand {

    private Logger logger = LoggerFactory.getLogger(AddCommand.class);

    public AddCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String user_name, String... args) {
        try {
            SpaceMarine marine = new SpaceMarine(MyGsonFactory.get().fromJson(args[0], SpaceMarine.class));
            marine.setOwner(user_name);
            controller.addElement(marine);
            //IOHelper.printlnIfUsingConsole(String.format("Добавлен элемент %s", marine));
            return String.format("Добавлен элемент %s", marine);
        } catch (ArrayIndexOutOfBoundsException ex){
            throw new IllegalArgumentException("add должна иметь 1 параметр SpaceMarine");
        }
    }

    @Override
    public String getDescription() {
        return "add : добавить новый элемент в коллекцию";
    }
}
