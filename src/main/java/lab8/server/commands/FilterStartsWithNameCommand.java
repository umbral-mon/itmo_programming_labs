package lab8.server.commands;

import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

public class FilterStartsWithNameCommand extends CollectionCommand {

    public FilterStartsWithNameCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        try {
            StringBuilder sb = new StringBuilder();
            controller.getCollectionElements().stream()
                    .filter(x -> x.getName().startsWith(args[0]))
                    .forEach(sb::append);
            return sb.toString();
        } catch (ArrayIndexOutOfBoundsException ex){
            return "name - строка и обязательный параметр";
            //IOHelper.printlnIfUsingConsole("name - строка и обязательный параметр");
        }
    }

    @Override
    public String getDescription() {
        return "filter_starts_with_name name : вывести элементы, значение поля name которых начинается с заданной подстроки";
    }
}
