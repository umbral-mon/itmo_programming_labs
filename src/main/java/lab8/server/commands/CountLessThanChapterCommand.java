package lab8.server.commands;

import com.google.gson.Gson;
import lab8.collectionItems.Chapter;
import lab8.server.CollectionController;
import lab8.server.commands.base.CollectionCommand;

public class CountLessThanChapterCommand extends CollectionCommand {

    public CountLessThanChapterCommand(CollectionController controller) {
        super(controller);
    }

    @Override
    public String execute(String userName, String... args) {
        try {
            //Chapter base = new Chapter(args[0], args[1], Integer.parseInt(args[2]));
            Chapter base = new Gson().fromJson(args[0], Chapter.class);
            Long ans = controller.getCollectionElements().stream().filter(x -> x.getChapter().compareTo(base) < 0).count();
            //IOHelper.consoleOut.println(ans);
            return ans.toString();
        } catch (ArrayIndexOutOfBoundsException ex){
            throw new IllegalArgumentException("Chapter должна иметь 3 параметра name, parentLegion, marinesCount");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("marinesCount - целое число");
        }
    }

    @Override
    public String getDescription() {
        return "count_less_than_chapter chapter : вывести количество элементов, значение поля chapter которых меньше заданного";
    }
}
