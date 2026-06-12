package lab8.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lab8.collectionItems.SpaceMarine;
import lab8.collectionItems.utils.IdGenerator;
import lab8.collectionItems.utils.SpaceMarineAdapter;
import lab8.utils.ClientRequest;
import lab8.utils.DatagramChunk;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Менеджер сетевого взаимодействия. Обеспечивает отправку команд на сервер
 * и получение ответов по UDP. Поддерживает.chunked передачу данных.
 */
public class NetworkManager {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9988;
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 5000;

    private DatagramSocket socket;
    private final Gson gson;
    private String login;
    private String password;

    public NetworkManager() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
                        jsonWriter.value(localDate.toString());
                    }

                    @Override
                    public LocalDate read(JsonReader jsonReader) throws IOException {
                        return LocalDate.parse(jsonReader.nextString());
                    }
                })
                .registerTypeAdapter(SpaceMarine.class, new SpaceMarineAdapter(IdGenerator.getInstance()))
                .create();
    }

    /**
     * Подключается к серверу, создавая DatagramSocket.
     */
    public void connect() throws IOException {
        socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT_MS);
    }

    /**
     * Отключается от сервера.
     */
    public void disconnect() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void setCredentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    /**
     * Отправляет команду на сервер и возвращает ответ.
     */
    public String sendCommand(String command) throws IOException {
        ClientRequest request = new ClientRequest(login, password, command);
        return sendToServer(request);
    }

    /**
     * Отправляет запрос на сервер с указанными учётными данными.
     */
    public String sendCommandWithCredentials(String command, String login, String password) throws IOException {
        ClientRequest request = new ClientRequest(login, password, command);
        return sendToServer(request);
    }

    private String sendToServer(ClientRequest request) throws IOException {
        String message = gson.toJson(request);
        DatagramChunk[] chunks = DatagramChunk.split(message);
        InetSocketAddress serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);

        for (DatagramChunk chunk : chunks) {
            String json = gson.toJson(chunk);
            DatagramPacket sendPacket = new DatagramPacket(
                    json.getBytes(StandardCharsets.UTF_8),
                    json.getBytes(StandardCharsets.UTF_8).length,
                    serverAddress
            );
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
                        for (DatagramChunk chunk : chunks) {
                            String json = gson.toJson(chunk);
                            DatagramPacket resendPacket = new DatagramPacket(
                                    json.getBytes(StandardCharsets.UTF_8),
                                    json.getBytes(StandardCharsets.UTF_8).length,
                                    new InetSocketAddress(SERVER_HOST, SERVER_PORT)
                            );
                            socket.send(resendPacket);
                        }
                    }
                }
            } else {
                responseReceived = !responseChunks.isEmpty()
                        && responseChunks.size() == responseChunks.get(0).getPacketCount();
                if (responseReceived) break;
            }
        }

        responseChunks.sort(Comparator.comparingInt(DatagramChunk::getPacketNumber));
        StringBuilder response = new StringBuilder();
        for (DatagramChunk chunk : responseChunks) {
            response.append(new String(chunk.getData()));
        }
        return response.toString();
    }

    private boolean getResponseChunks(List<DatagramChunk> responseChunks) throws IOException {
        byte[] buf = new byte[65000];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
        socket.receive(receivePacket);
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
        DatagramChunk chunk = gson.fromJson(response, DatagramChunk.class);
        responseChunks.add(chunk);
        return !response.isEmpty();
    }

    public List<SpaceMarine> requestCollection() {
        try {
            String response = sendCommand("show");
            if (response == null || response.trim().isEmpty()) {
                return Collections.emptyList();
            }
            String trimmed = response.trim();
            System.out.println("Получено: " + trimmed);
            String[] marines = trimmed.split("\n");
            LinkedList<SpaceMarine> ret = new LinkedList<>();
            for (String marine : marines) {
                ret.add(gson.fromJson(marine, SpaceMarine.class));
            }
            //System.out.println(ret.size());
            return ret;
            // Проверяем, что ответ начинается с '[' (JSON-массив)
            //if (!trimmed.startsWith("[")) {
            //    return Collections.emptyList();
            //}
            //Type listType = new TypeToken<ArrayList<SpaceMarine>>() {}.getType();
            //List<SpaceMarine> marines = gson.fromJson(trimmed, listType);
            //return marines != null ? marines : Collections.emptyList();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Проверяет, может ли текущий пользователь обновить объект с данным ID.
     * Возвращает: 1 - можно, -1 - не существует, -2 - не владелец.
     */
    public int canUpdate(int id) {
        try {
            String response = sendCommand("u? " + id);
            return Integer.parseInt(response.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public Gson getGson() {
        return gson;
    }
}
