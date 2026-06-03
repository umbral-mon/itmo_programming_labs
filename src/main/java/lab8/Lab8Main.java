package lab8;

import lab8.client.ClientMain;
import lab8.server.ServerMain;
import main.Solution;

public class Lab8Main implements Solution {
    private boolean isServer;

    public Lab8Main(boolean isServer){
        this.isServer = isServer;
    }

    @Override
    public void solve() {
        Runnable side = isServer ? new ServerMain() : new ClientMain();
        side.run();
    }
}
