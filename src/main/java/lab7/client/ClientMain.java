package lab7.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lab7.collectionItems.Chapter;
import lab7.collectionItems.SpaceMarine;
import lab7.collectionItems.utils.IdGenerator;
import lab7.collectionItems.utils.SpaceMarineAdapter;
import lab7.utils.ClientRequest;
import lab7.utils.DatagramChunk;
import lab7.utils.FileReadingException;
import lab7.utils.IOHelper;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

public class ClientMain implements Runnable{

    interface Command{
        void execute(String... args);
    }

    private DatagramSocket socket;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9988;
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 2000;
    private DatagramChannel channel;
    private final HashSet<String> openedFiles = new HashSet<>();
    private HashMap<String, Command> clientCommands = new HashMap<>() {{
            put("exit", (args) -> System.exit(0));
            put("execute_script", (args) -> {
                if (openedFiles.contains(args[0])) {
                    System.err.println("Попытка открыть уже открытый файл");
                    return;
                }
                //InputStreamReader old = IOHelper.defaultIn;
                try (FileInputStream fileReader = new FileInputStream(args[0]);
                     InputStreamReader reader = new InputStreamReader(fileReader)) {
                    IOHelper.switchTo(fileReader);
                    openedFiles.add(args[0]);
                    //IOHelper.defaultIn = reader;
                    //String command = IOHelper.readFileLine(reader);
                    String command = IOHelper.readLine();
                    while (!command.isEmpty()) {
                        String message = handleCommand(command);
                        if (message == null) continue;
                        ClientRequest request = new ClientRequest(login, password, message);
                        sendToServer(request);
                        //command = IOHelper.readFileLine(reader);
                        command = IOHelper.readLine();
                    }
                    IOHelper.consoleOut.println("Скрипт завершен");
                } catch (IOException e) {
                } catch (FileReadingException e) {
                    IOHelper.errOut.println("Ошибка при чтении файла. Изменения не будут применены");
                } finally {
                    //IOHelper.defaultIn = old;
                    IOHelper.switchBack();
                    openedFiles.remove(args[0]);
                }
            });
    }};
    private Gson gson = new Gson();
    private Scanner sc;
    private Selector selector;

    private String login = "unidentified_user", password = "unidentified_user";
    //private Logger logger = LoggerFactory.getLogger(ClientMain.class);

    {
        gson = new GsonBuilder()
                .registerTypeAdapter(
                        LocalDate.class,
                        new TypeAdapter<LocalDate>(){

                            @Override
                            public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
                                jsonWriter.value(localDate.toString());
                            }

                            @Override
                            public LocalDate read(JsonReader jsonReader) throws IOException {
                                return LocalDate.parse(jsonReader.nextString());
                            }
                        }
                )
                .registerTypeAdapter(SpaceMarine.class, new SpaceMarineAdapter(IdGenerator.getInstance())).create();
    }

    @Override
    public void run() {
//        SecureRandom rnd = new SecureRandom();
//        sc = new Scanner(System.in);
//        String hashed = BCrypt.hashpw("123456", BCrypt.gensalt());
//        System.out.println(hashed);
//        System.out.println("=====================================");
//        while (sc.hasNextLine()){
//            String s = sc.nextLine();
//            String h = BCrypt.hashpw(s, BCrypt.gensalt());
//            System.out.println(h);
//            System.out.println(BCrypt.checkpw(h, hashed));
//            System.out.println(BCrypt.checkpw(s, hashed));
//            System.out.println("=====================================");
//        }


        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_MS);


            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);

            IOHelper.consoleOut.print(">>");
            while (IOHelper.hasNext()) {
                //IOHelper.consoleOut.print(">>");
                //String message = handleCommand(sc.nextLine(), IOHelper.consoleIn);
                String message = handleCommand(IOHelper.readLine());
                if (message == null) { IOHelper.consoleOut.print(">>"); continue; }
                ClientRequest request = new ClientRequest(login, password, message);
                String response = sendToServer(request);
                handleResponse(response);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String sendToServer(ClientRequest request) throws IOException {
        String message = gson.toJson(request);
        System.out.println(message);
        DatagramChunk[] chunks = DatagramChunk.split(message);
        InetSocketAddress serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);
        for (DatagramChunk chunk : chunks) {
            String json = gson.toJson(chunk);
            //ByteBuffer sendBuffer = ByteBuffer.wrap(json.getBytes());
            //System.out.println("Отправка данных: " + chunk);
            //channel.send(sendBuffer, serverAddress);
            DatagramPacket sendPacket = new DatagramPacket(json.getBytes(), json.getBytes().length, serverAddress);
            socket.send(sendPacket);
        }

        boolean responseReceived = false;
        int retryCount = 0;

        List<DatagramChunk> responseChunks = new LinkedList<>();
        while (retryCount < MAX_RETRIES) {
            boolean gotData = getResponseChunks(responseChunks);

            if (!gotData) {
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    if (responseChunks.isEmpty()) {
                        System.out.println("Сервер временно недоступен. Повторная попытка ("
                                + retryCount + "/" + MAX_RETRIES + ")...");
                        for (DatagramChunk chunk : chunks) {
                            String json = gson.toJson(chunk);
                            ByteBuffer sendBuffer = ByteBuffer.wrap(json.getBytes());
                            //System.out.println("Отправка данных: " + chunk);
                            channel.send(sendBuffer, serverAddress);
                        }
                    }
                }
            } else {
                responseReceived = !responseChunks.isEmpty() && responseChunks.size() == responseChunks.get(0).getPacketCount();
                if (responseReceived) break;
            }
        }

        if (!responseReceived) {
            System.out.println("Ошибка: Сервер так и не ответил после " + MAX_RETRIES + " попыток.");
        }

        responseChunks.sort(Comparator.comparingInt(DatagramChunk::getPacketNumber));
        StringBuilder response = new StringBuilder();
        for (DatagramChunk chunk : responseChunks)
            response.append(new String(chunk.getData()));

        //IOHelper.consoleOut.println(response);
        return response.toString();


        //return null;
    }

    private boolean getResponseChunks(List<DatagramChunk> responseChunks) throws IOException {

        byte[] buf = new byte[65000];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        socket.receive(receivePacket);
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
        //System.out.println(response);
        DatagramChunk chunk = gson.fromJson(response, DatagramChunk.class);
        responseChunks.add(chunk);

        return !response.isEmpty();
    }

    private int canUseUpdateCommand(int id) throws IOException{
        String response = sendToServer(new ClientRequest(login, password, "u? " + id));
        return Integer.parseInt(response.trim());
    }

//    private boolean handleResponse(Iterator<SelectionKey> iterator) throws IOException{
//        boolean responseReceived = false;
//        while (iterator.hasNext()) {
//            SelectionKey key = iterator.next();
//            if (key.isReadable()) {
//                ByteBuffer receiveBuffer = ByteBuffer.allocate(65000);
//                InetSocketAddress from = (InetSocketAddress) channel.receive(receiveBuffer);
//
//                if (from != null) {
//                    receiveBuffer.flip();
//                    String response = new String(receiveBuffer.array(), 0, receiveBuffer.limit(), StandardCharsets.UTF_8);
//                    //logger.info("Получен ответ от сервера: " + response);
//                    System.out.println(response);
//                    responseReceived = true;
//                }
//            }
//            iterator.remove();
//        }
//        return responseReceived;
//    }

    private void handleResponse(String response){
        IOHelper.consoleOut.println(response);
    }

    private String handleCommand(String command) throws IOException, NumberFormatException {
        //logger.info("Обработка команды: " + command);
        String[] splittedCommand = command.split(" ");
        if (clientCommands.containsKey(splittedCommand[0])){
            clientCommands.get(splittedCommand[0]).execute(Arrays.copyOfRange(splittedCommand, 1, splittedCommand.length));
            return null;
        }
        if (splittedCommand[0].equals("update")){
            int result = canUseUpdateCommand(Integer.parseInt(splittedCommand[1]));
            if (result == -1) {
                System.out.println("Не существует id " + splittedCommand[1]);
                return null;
            } else if (result == -2) {
                System.out.printf("Ошибка: пользователь '%s' не является хозяином SpaceMarine id=%s", login, splittedCommand[1]);
                return null;
            }
        }
        try {
            Queue<String> args = getCommandArgs(splittedCommand);
            StringBuilder sb = new StringBuilder(splittedCommand[0]);
            while(!args.isEmpty())
                sb.append(" ").append(args.poll());
            return sb.toString();
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private LinkedList<String> getCommandArgs(String[] parsedCommand) throws UnsupportedEncodingException {
        LinkedList<String> ret = new LinkedList<>();
        switch (parsedCommand[0]){
            case "count_less_than_chapter":
                Chapter chapter = IOHelper.readChapter();
                ret.add(gson.toJson(chapter));
                //logger.info(String.format("Добавлен аргумент %s к команде %s", gson.toJson(chapter), parsedCommand[0]));
                break;
            case "filter_starts_with_name":
            case "execute_script":
                ret.add(parsedCommand[1]);
                //logger.info(String.format("Добавлен аргумент %s к команде %s", parsedCommand[1], parsedCommand[0]));
                break;
            case "remove_by_id":
                Integer.parseInt(parsedCommand[1]);
                ret.add(parsedCommand[1]);
                //logger.info(String.format("Добавлен аргумент %s к команде %s", parsedCommand[1], parsedCommand[0]));
                break;
            case "update":
            case "insert_at":
                Integer.parseInt(parsedCommand[1]);
                ret.add(parsedCommand[1]);
                //logger.info(String.format("Добавлен аргумент %s к команде %s", parsedCommand[1], parsedCommand[0]));
            case "add":
            case "remove_greater":
                SpaceMarine marine = IOHelper.readMarine();
                marine.setOwner(login);
                System.out.println(gson.toJson(marine));
                ret.add(gson.toJson(marine));
                //logger.info(String.format("Добавлен аргумент %s к команде %s", gson.toJson(marine), parsedCommand[0]));
                break;
            case "login":
                login = parsedCommand[1];
                password = parsedCommand[2];
                ret.add(login);
                ret.add(password);
                break;
            case "register":
                login = parsedCommand[1];
                password = parsedCommand[2];
                ret.add(login);
                ret.add(BCrypt.hashpw(password, BCrypt.gensalt()));
                break;
        }
        return ret;
    }
}
