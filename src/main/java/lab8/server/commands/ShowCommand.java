package lab8.server.commands;

import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

public class ShowCommand extends CollectionCommand {

    public ShowCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        StringBuilder sb = new StringBuilder();
        //controller.getCollectionElements().forEach(IOHelper.consoleOut::println);
        controller.getCollectionElements().forEach(x -> {
            sb.append(x).append('\n');
        });
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    }
}
