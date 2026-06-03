package lab8.server.commands;

import lab8.collectionItems.SpaceMarine;
import lab8.server.CollectionController;
import lab8.server.MyGsonFactory;
import lab8.server.commands.base.CollectionCommand;

public class UpdateIdCommand extends CollectionCommand {

    public UpdateIdCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        if (args.length == 0) return "";
        try {
            int id = Integer.parseInt(args[0]);
            //if (id < 0 || id > controller.getCollectionSize()) throw new NumberFormatException("");
            if (id < 0 || !controller.containsElementWithId(id)) throw new NumberFormatException("");
            //SpaceMarine marine = IOHelper.readMarine();
            SpaceMarine marine = new SpaceMarine(MyGsonFactory.get().fromJson(args[1], SpaceMarine.class));
            controller.updateElement(id, marine, userName);
//            IOHelper.printlnIfUsingConsole(String.format("Элемент %s обновлен на позиции %d",
//                    controller.getCollectionElements().stream().filter(x -> x.getID() == id).findFirst(),
//                    id));
            return String.format("Элемент %s обновлен на позиции %d",
                    controller.getCollectionElements().stream().filter(x -> x.getID() == id).findFirst(),
                    id);
        } catch (NumberFormatException ex) {
            //IOHelper.printlnIfUsingConsole(String.format("id должен быть целым числом > 0 и присутствовать в коллекции", controller.getCollectionSize()-1));
            return String.format("id должен быть целым числом > 0 и присутствовать в коллекции", controller.getCollectionSize()-1);
        }
    }

    @Override
    public String getDescription() {
        return "update id {element} : обновить значение элемента коллекции, id которого равен заданному";
    }
}
