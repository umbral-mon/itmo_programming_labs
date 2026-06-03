package lab8.server;

import com.google.gson.Gson;
import lab5.IOHelper;
import lab8.server.commands.base.CommandManager;
import lab8.utils.ClientRequest;
import lab8.utils.Config;
import lab8.utils.DatagramChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerMain implements Runnable {

    private ExecutorService processExecutor;
    private ExecutorService readExecutor;
    private ExecutorService serverConsoleExecutor;
    private final int PORT;
    private DataBaseManager DBManager;

    private CommandManager manager;

    private Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public ServerMain(){
        Config cfg = Config.getInstance();
        PORT = cfg.getServerPort();

        processExecutor = Executors.newCachedThreadPool();
        readExecutor = Executors.newCachedThreadPool();

        DBManager = DataBaseManager.getInstance();
        CollectionController cc = new CollectionController(DBManager);
        manager = new CommandManager(cc);
        serverConsoleExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        Gson gson = MyGsonFactory.get();
        serverConsoleExecutor.submit(() -> {
            while (true) {
                try {
                    IOHelper.consoleOut.print(">>");
                    String consoleLine = IOHelper.readConsoleLine();
//                    if (consoleLine.equals("save")) {
//                        logger.info(manager.executeSaveForServer());
//                        continue;
//                    }
                    logger.info(manager.executeCommand("", consoleLine));
                } catch (IllegalArgumentException ex){
                    IOHelper.consoleOut.println(ex.getMessage());
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }
            }
        });
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            logger.info("Сервер запущен на порту {}", PORT);

            byte[] receiveBuffer = new byte[65000];

            Runnable readingTask = getTask(receiveBuffer, serverSocket, gson);

            readExecutor.submit(readingTask);
            readExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            readExecutor.shutdown();
            processExecutor.shutdown();
        }
    }

    private Runnable getTask(byte[] receiveBuffer, DatagramSocket serverSocket, Gson gson) {
        Map<Integer, List<DatagramChunk>> receivedChunks = Collections.synchronizedMap(new HashMap<>());

        return () -> {
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    serverSocket.receive(receivePacket);
                    String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    DatagramChunk chunk = gson.fromJson(receivedData, DatagramChunk.class);

                    synchronized (receivedChunks) {
                        if (!receivedChunks.containsKey(chunk.getTransactionId()))
                            receivedChunks.put(chunk.getTransactionId(), new LinkedList<>());
                        receivedChunks.get(chunk.getTransactionId()).add(chunk);
                        if (receivedChunks.get(chunk.getTransactionId()).size() == chunk.getPacketCount()) {
                            StringBuilder builder = new StringBuilder();
                            for (DatagramChunk c : receivedChunks.get(chunk.getTransactionId()))
                                builder.append(new String(c.getData()));
                            ClientRequest request = gson.fromJson(builder.toString().trim(), ClientRequest.class);
                            //System.out.println("ПИДАРАСЫ");
                            processExecutor.submit(new ServerConnectionTask(serverSocket, receivePacket, manager, request, DBManager));
                            receivedChunks.get(chunk.getTransactionId()).clear();
                        }
                    }
                } catch (Exception e) {
                    if (serverSocket.isClosed()) {
                        break;
                    }
                    logger.error("Ошибка в потоке чтения: {}", e.getMessage());
                }
            }
        };
    }
}
