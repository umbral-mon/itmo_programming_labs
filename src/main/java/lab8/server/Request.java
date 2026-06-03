package lab8.server;

public class Request {

    private String rawCommand;

    public Request(String rawCommand){
        this.rawCommand = rawCommand;
    }

    public String getRawCommand() { return rawCommand; }

    public static Request of(String rawCommand){
        return new Request(rawCommand);
    }

}
