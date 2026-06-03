package lab8.server.commands;

import lab8.collectionItems.SpaceMarine;
import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

import java.util.Comparator;

public class PrintFieldDescendingHealthCommand extends CollectionCommand {

    public PrintFieldDescendingHealthCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        StringBuilder sb = new StringBuilder();
        controller.getCollectionElements().stream()
                .sorted(Comparator.comparing(SpaceMarine::getHealth))
                //.forEach(IOHelper.consoleOut::println);
                .forEach(sb::append);
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "print_field_descending_health : вывести значения поля health всех элементов в порядке убывания";
    }
}
