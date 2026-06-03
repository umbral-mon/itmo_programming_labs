package lab8.server.commands;//package lab7.server.commands;
//
//import lab7.server.CollectionController;
//import lab7.server.commands.base.CollectionCommand;
//import lab7.server.commands.base.Command;
//import lab7.server.commands.base.CommandManager;
//import lab7.utils.FileReadingException;
//import lab7.utils.IOHelper;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.HashSet;
//
//public class ExecuteScriptCommand extends CollectionCommand implements Command {
//
//    private static HashSet<String> openedFiles = new HashSet<>();
//
//    public ExecuteScriptCommand(CollectionController controller) {
//        super(controller);
//    }
//
//    @Override
//    public String execute(String... args) {
//        if (args.length == 0) return "";
//        if (openedFiles.contains(args[0])) {
//            //System.err.println("Попытка открыть уже открытый файл");
//            return "";
//        }
//        //InputStreamReader old = IOHelper.defaultIn;
//        try (FileInputStream fileReader = new FileInputStream(args[0]);
//        InputStreamReader reader = new InputStreamReader(fileReader)) {
//            openedFiles.add(args[0]);
//            //IOHelper.defaultIn = reader;
//            IOHelper.switchTo(fileReader);
//            CollectionController newController = new CollectionController();
//            CommandManager manager = new CommandManager(newController);
//            //String command = IOHelper.readFileLine(reader);
//            String command = IOHelper.readLine();
//            while (!command.isEmpty()) {
//                manager.executeCommand(command);
//                //command = IOHelper.readFileLine(reader);
//                command = IOHelper.readLine();
//            }
//            controller.updateCollection(newController);
//            //IOHelper.consoleOut.println("Скрипт завершен");
//            return "Скрипт завершен";
//        } catch (IOException e) {
//        } catch (FileReadingException e) {
//            //IOHelper.errOut.println("Ошибка при чтении файла. Изменения не будут применены");
//            return "Ошибка при чтении файла. Изменения не будут применены";
//        } finally {
//            //IOHelper.defaultIn = old;
//            IOHelper.switchBack();
//            openedFiles.remove(args[0]);
//        }
//        return "";
//    }
//
//    @Override
//    public String getDescription() {
//        return "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме";
//    }
//}
