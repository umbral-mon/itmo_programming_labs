package lab8.server.commands;

import com.google.gson.Gson;
import lab8.server.CollectionController;
import lab8.server.MyGsonFactory;
import lab8.server.commands.base.CollectionCommand;

public class ShowCommand extends CollectionCommand {

    public ShowCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        StringBuilder sb = new StringBuilder();
        //controller.getCollectionElements().forEach(IOHelper.consoleOut::println);
        Gson gson = MyGsonFactory.get();
        controller.getCollectionElements().forEach(x -> {
            sb.append(gson.toJson(x)).append('\n');
        });
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении";
    }
}
