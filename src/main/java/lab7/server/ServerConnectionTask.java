package lab7.server;

import com.google.gson.Gson;
import lab7.client.BCrypt;
import lab7.server.commands.base.CommandManager;
import lab7.utils.ClientRequest;
import lab7.utils.DatagramChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerConnectionTask implements Runnable{

    private final DatagramPacket receivePacket;
    private final DatagramSocket serverSocket;
    private CommandManager manager;
    private ClientRequest receivedData;
    private Logger logger = LoggerFactory.getLogger(ServerConnectionTask.class);
    private DataBaseManager DBManager;
    Gson gson;

    public ServerConnectionTask(DatagramSocket serverSocket, DatagramPacket receivePacket, CommandManager manager, ClientRequest receivedData, DataBaseManager DBManager){
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.manager = manager;
        this.receivedData = receivedData;
        this.DBManager = DBManager;
        gson = MyGsonFactory.get();
    }

    @Override
    public void run() {
        try {
            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            logger.info("Получено от {}:{} -> {}", clientAddress, clientPort, receivedData);

            if (!checkUser(receivedData.getLogin(), receivedData.getPassword(), receivedData.getCommand()))
            {
                sendBack("Для выполнения команд нужно авторизоваться", clientAddress, clientPort);
                return;
            }

            if (receivedData.getCommand().startsWith("u?")){
                String data = manager.executeCommand(receivedData.getLogin(), receivedData.getCommand());
                sendBack(data, clientAddress, clientPort);
                return;
            }

            String command = receivedData.getCommand();
            logger.info("Обработка команды: {}", command);
            //String user_id = DataBaseManager.getInstance().getUserId(receivedData.getLogin());
            String responseData = manager.executeCommand(receivedData.getLogin(), command);
            logger.info("Результат работы: {}", responseData);

            sendBack(responseData, clientAddress, clientPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendBack(String responseData, InetAddress clientAddress, int clientPort) throws IOException {
        DatagramChunk[] responseChunks = DatagramChunk.split(responseData);
        //System.out.println(responseChunks.length);

        for (DatagramChunk chunk : responseChunks) {
            byte[] sendBuffer = gson.toJson(chunk).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
            logger.info("Отправлено: {}", chunk);
            serverSocket.send(sendPacket);
        }
    }

    private boolean checkUser(String login, String password, String command) {
        String[] parse = command.split(" ");
        DataBaseManager db = DataBaseManager.getInstance();
        boolean isRegister = parse[0].equals("register");
        boolean isLogin = parse[0].equals("login");
        boolean unauthorizedCheck = isLogin || isRegister;
        boolean authorizedCheck = db.hasUser(login) && BCrypt.checkpw(password, db.getUserPasswordHash(login));
        //System.out.println("Check user result: " + (unauthorizedCheck || authorizedCheck) + " a:" + authorizedCheck + " ua:" + unauthorizedCheck);
        return unauthorizedCheck || authorizedCheck;
    }
}
