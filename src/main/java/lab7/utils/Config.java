package lab7.utils;

import lab7.server.MyGsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Config {

    private static Config instance;
    private final String filePath;
    private Logger logger;
    private Properties properties;

    private Config(String filePath) {
        this.filePath = filePath;
        logger = LoggerFactory.getLogger(Config.class);
        properties = new Properties();
        reload();
    }

    public static Config getInstance(String filePath) {
        if (instance == null)
            instance = new Config(filePath);
        return instance;
    }

    public static Config getInstance() {
        return getInstance("config.cfg");
    }

    public void reload() {
        File configFile = new File(filePath);
        if (!configFile.exists()){
            String s =  MyGsonFactory.get().toJson(properties);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(configFile))) {
                bw.write(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.info("Не обнаружен файл конфигурации, создан базовый файл.");
            System.exit(0);
        }
        try (FileReader input = new FileReader(filePath)) {
            Properties newProps = MyGsonFactory.get().fromJson(input, Properties.class);

            properties = newProps;

            logger.info("Загружена конфигурация: \n{}", properties.toString());
        } catch (IOException ex) {
            logger.error("Ошибка при перезагрузке конфигурации: " + ex.getMessage());
        }
    }

    public String getServerAddress(){
        return properties.serverAddress;
    }

    public int getServerPort(){
        return properties.port;
    }

    public String getCollectionFileName(){
        return properties.collectionFileName;
    }

    public boolean canExecuteOnServer(String command){
        return properties.serverLockedCommands.contains(command);
    }

    public boolean canExecuteOnClient(String command){
        return canExecuteOnServer(command) && properties.clientLockedCommands.contains(command);
    }

    public int getDataByteSize(){
        return properties.dataByteSize;
    }

    public String getDBURL(){
        return properties.DBURL;
    }

    public String getDBUser(){
        return properties.DBUser;
    }

    public String getDBPassword(){
        return properties.DBPassword;
    }


    private class Properties {
        String serverAddress = "localHost";
        int port = 9988;
        String collectionFileName = "collection.json",
        DBURL = "url",
        DBUser = "user",
        DBPassword = "password";
        HashSet<String> serverLockedCommands = new HashSet<>(),
                        clientLockedCommands = new HashSet<>();
        int dataByteSize = 2048;

        @Override
        public String toString() {
            ExecutorService s = Executors.newSingleThreadExecutor();
            return "Properties{" +
                    "serverAddress='" + serverAddress + '\'' +
                    ", port=" + port +
                    ", collectionFileName='" + collectionFileName + '\'' +
                    ", DBURL='" + DBURL + '\'' +
                    ", DBUser='" + DBUser + '\'' +
                    ", DBPassword='" + DBPassword + '\'' +
                    ", serverLockedCommands=" + serverLockedCommands +
                    ", clientLockedCommands=" + clientLockedCommands +
                    ", dataByteSize=" + dataByteSize +
                    '}';
        }
    }

}