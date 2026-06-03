package lab8.server.commands.base;

import lab8.collectionItems.SpaceMarine;
import lab8.server.CollectionController;
import lab8.server.DataBaseManager;
import lab8.server.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;

public class CommandManager {

    private HashMap<String, Command> commands;
    private final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    public CommandManager(CollectionController collection){
        commands = new HashMap<>();
        commands.put("help", new HelpCommand(commands));
        commands.put("info", new InfoCommand(collection));
        commands.put("show", new ShowCommand(collection));
        commands.put("add", new AddCommand(collection));
        commands.put("clear", new ClearCommand(collection));
        commands.put("count_less_than_chapter", new CountLessThanChapterCommand(collection));
        //commands.put("execute_script", new ExecuteScriptCommand(collection));
        commands.put("exit", new ExitCommand());
        commands.put("filter_starts_with_name", new FilterStartsWithNameCommand(collection));
        commands.put("insert_at", new InsertAtIndexCommand(collection));
        commands.put("print_field_descending_health", new PrintFieldDescendingHealthCommand(collection));
        commands.put("remove_by_id", new RemoveByIdCommand(collection));
        commands.put("remove_greater", new RemoveGreaterCommand(collection));
        commands.put("remove_last", new RemoveLastCommand(collection));
        commands.put("save", new SaveCommand(collection));
        commands.put("update", new UpdateIdCommand(collection));
        commands.put("login", new Login());
        commands.put("register", new Register());
        commands.put("u?", new Command() {
            @Override
            public String execute(String name, String... args) {
                try {
                    int id = Integer.parseInt(args[0]);
                    if (collection.getCollectionElements().stream().map(SpaceMarine::getID).noneMatch(x -> x == id))
                        return "-1";
                    if (!DataBaseManager.getInstance().getOwnerNameById(id).equals(name))
                        return "-2";
                    return  "0";
                } catch (Exception ex) { return "-2"; }
            }

            @Override
            public String getDescription() {
                return "";
            }
        });
        StringBuilder sb = new StringBuilder();
        commands.keySet().stream().filter(x -> !x.equals("u?")).forEach(x -> sb.append(x).append(", "));
        logger.info("Зарегистрированы команды: " + sb);
    }

    /**
     * Starts execution of a command by its name and args
     * @param commandName name of command
     * @param commandArgs argument to pass for a command
     */
    public String executeCommand(String userName, String commandName, String... commandArgs){
        if (!commands.containsKey(commandName) || commandName.equals("save"))
            //throw new IllegalArgumentException("No command found for name " + commandName + ". Please type help for help");
            return "No command found for name " + commandName + ". Please type help for help";
        logger.info("Provided command: " + commandName);
        try {
            return commands.get(commandName).execute(userName, commandArgs);
        } catch (Exception ex){
            //ex.printStackTrace();
            return ex.getMessage();
        }
    }

//    public String executeSaveForServer(int user_id, String... commandArgs){
//        return commands.get("save").execute(int user_id, commandArgs);
//    }

    /**
     * Parses line and starts execution of a command
     * @param unparsedLine command and args in unparsed format
     */
    public String executeCommand(String userName, String unparsedLine){
        String[] arr = unparsedLine.trim().split(" ");
        if (arr.length == 0)
            return "";
        return executeCommand(userName, arr[0], Arrays.copyOfRange(arr, 1, arr.length));
    }

}
