package lab8.server.commands.base;

public interface Command {

    /**
     * Starts execution of a command
     * @param args arguments for command
     */
    String execute(String userName, String... args);

    /**
     * Get Description of a command
     * @return description of a command
     */
    String getDescription();

}
