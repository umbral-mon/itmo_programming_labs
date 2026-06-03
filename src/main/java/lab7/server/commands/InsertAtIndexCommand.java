package lab7.server.commands;

import lab7.server.CollectionController;
import lab7.collectionItems.SpaceMarine;
import lab7.server.MyGsonFactory;
import lab7.server.commands.base.CollectionCommand;
import lab7.utils.IOHelper;

public class InsertAtIndexCommand extends CollectionCommand {

    public InsertAtIndexCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        if (args.length == 0) return "";
        try {
            int id = Integer.parseInt(args[0]);
            if (id < 0 || id > controller.getCollectionSize()) return "";
            //SpaceMarine marine = IOHelper.readMarine();
            SpaceMarine marine = new SpaceMarine(MyGsonFactory.get().fromJson(args[1], SpaceMarine.class));
            controller.insertAt(id, marine);
//            IOHelper.printlnIfUsingConsole(String.format("Элемент %s добавлен на позицию %d",
//                    marine,
//                    id));
            return String.format("Элемент %s добавлен на позицию %d",
                    marine,
                    id);
        } catch (NumberFormatException ex){
            //IOHelper.printlnIfUsingConsole(String.format("id должен быть целым числом в пределах от 0 до %d включительно", controller.getCollectionSize()-1));
            return String.format("id должен быть целым числом в пределах от 0 до %d включительно", controller.getCollectionSize()-1);
        }
    }

    @Override
    public String getDescription() {
        return "insert_at index {element} : добавить новый элемент в заданную позицию";
    }
}
