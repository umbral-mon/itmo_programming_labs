package lab8.server.commands;

import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

public class SaveCommand extends CollectionCommand {

    public SaveCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        StringBuilder sb = new StringBuilder();
        //Arrays.stream(args).forEach(System.out::println);
        //Arrays.stream(args).forEach(sb::append);
        if (args.length != 0)
            controller.save(args[0]);
        else
            controller.save();
        return "Коллекция сохранена";
    }

    @Override
    public String getDescription() {
        return "save : сохранить коллекцию в файл";
    }
}
