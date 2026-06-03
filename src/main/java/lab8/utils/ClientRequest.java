package lab8.utils;

public class ClientRequest {

    private String login, password, command;

    public ClientRequest(String login, String password, String rawCommand){
        this.login = login;
        this.password = password;
        this.command = rawCommand;
    }

    public String getCommand() { return command; }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
