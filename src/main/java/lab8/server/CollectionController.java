package lab8.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lab8.collectionItems.SpaceMarine;
import lab8.collectionItems.utils.IdGenerator;
import lab8.collectionItems.utils.SpaceMarineAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class CollectionController implements Cloneable {

    private List<SpaceMarine> stack;
    private LocalDateTime initializationTime;
    private Gson gson;
    private Logger logger = LoggerFactory.getLogger(CollectionController.class);
    private final Object monitor = new Object();
    private final DataBaseManager dbManager;

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

    public CollectionController(DataBaseManager dbManager){
        this.dbManager = dbManager;
        stack = Collections.synchronizedList(new Stack<>());
        readCollection();
        initializationTime = LocalDateTime.now();
    }

    private CollectionController(CollectionController copy){
        List<SpaceMarine> snapshot;
        synchronized (copy.monitor) {
            snapshot = new ArrayList<>(copy.stack);
        }
        this.stack = Collections.synchronizedList(new Stack<>());
        this.stack.addAll(snapshot);
        this.initializationTime = copy.initializationTime;
        this.gson = copy.gson;
        this.logger = copy.logger;
        this.dbManager = copy.dbManager;
    }

    public boolean containsElementWithId(int id){
        synchronized (monitor) {
            return stack.stream().filter(x -> x.getID() == id).count() == 1;
        }
    }

    /**
     * Загружает коллекцию из БД при старте программы.
     * Синхронизирует IdGenerator с максимальным id.
     */
    private void readCollection(){
        List<SpaceMarine> marines = dbManager.loadAllSpaceMarines();
        synchronized (monitor) {
            stack.addAll(marines);
        }
        int maxId = dbManager.getMaxSpaceMarineId();
        IdGenerator.getInstance().setCurrentId(maxId);
        dbManager.syncIdSequence();
        logger.info("Коллекция загружена из БД: {} элементов, max_id={}.", marines.size(), maxId);
    }

    public String getCollectionInfo(){
        return toString();
    }

    /**
     * @return defensive copy (снимок) текущих элементов.
     */
    public List<SpaceMarine> getCollectionElements(){
        synchronized (monitor) {
            return new ArrayList<>(stack);
        }
    }

    /**
     * Добавить элемент в коллекцию. Owner берётся из самого объекта SpaceMarine.
     * Сначала INSERT в БД, при успехе — добавление в память.
     */
    public void addElement(SpaceMarine x){
        int ordernum = dbManager.getMaxOrdernum() + 1;
        if (dbManager.insertSpaceMarine(x, ordernum)) {
            stack.add(x);
        } else {
            throw new RuntimeException("Не удалось добавить SpaceMarine в БД. Изменения в памяти отменены.");
        }
    }

    /**
     * Вставить элемент на позицию index. Owner берётся из объекта.
     * Сначала сдвиг ordernum в БД, затем INSERT, при успехе — в память.
     */
    public void insertAt(int index, SpaceMarine element){
        synchronized (monitor) {
            int shiftResult = dbManager.shiftOrdernumsUpFrom(index);
            if (shiftResult < 0) {
                throw new RuntimeException("Не удалось сдвинуть ordernum в БД для insertAt.");
            }
            if (dbManager.insertSpaceMarine(element, index)) {
                stack.add(index, element);
            } else {
                throw new RuntimeException("Не удалось вставить SpaceMarine в БД на позицию " + index);
            }
        }
    }

    /**
     * Обновить элемент по id.
     * Изменить может только хозяин объекта.
     * Сначала UPDATE в БД (с проверкой owner), при успехе — update в памяти.
     *
     * @param id      id обновляемого элемента
     * @param element новые данные (owner из element должен совпадать с текущим хозяином)
     * @param owner   имя пользователя, выполняющего операцию
     * @throws SecurityException если owner не совпадает с хозяином объекта
     */
    public void updateElement(int id, SpaceMarine element, String owner){
        synchronized (monitor) {
            SpaceMarine existing = findElementById(id);
            if (existing == null) {
                throw new RuntimeException("SpaceMarine id=" + id + " не найден.");
            }
            checkOwnership(existing, owner, "update", id);

            if (dbManager.updateSpaceMarine(existing)) {
                existing.update(element);
            } else {
                throw new RuntimeException("Не удалось обновить SpaceMarine id=" + id + " в БД.");
            }
        }
    }

    /**
     * Удалить элемент по id.
     * Удалить может только хозяин объекта.
     * Сначала DELETE из БД (с проверкой owner), при успехе — удаление из памяти.
     *
     * @param id    id удаляемого элемента
     * @param owner имя пользователя, выполняющего операцию
     * @throws SecurityException если owner не совпадает с хозяином объекта
     */
    public void removeById(int id, String owner){
        synchronized (monitor) {
            SpaceMarine existing = findElementById(id);
            if (existing == null) {
                throw new RuntimeException("SpaceMarine id=" + id + " не найден.");
            }
            checkOwnership(existing, owner, "remove", id);

            if (dbManager.deleteSpaceMarineById(id, owner)) {
                stack.removeIf(x -> x.getID().equals(id));
            } else {
                throw new RuntimeException("Не удалось удалить SpaceMarine id=" + id + " из БД.");
            }
        }
    }

    /**
     * Удалить последний элемент (pop).
     * Удалить может только хозяин последнего элемента.
     * Сначала DELETE из БД (с проверкой owner), при успехе — удаление из памяти.
     *
     * @param owner имя пользователя, выполняющего операцию
     * @return удалённый SpaceMarine
     * @throws SecurityException  если owner не совпадает с хозяином последнего элемента
     * @throws EmptyStackException если стек пуст
     */
    public SpaceMarine remove_last(String owner){
        synchronized (monitor) {
            if (stack.isEmpty()) {
                throw new EmptyStackException();
            }
            SpaceMarine last = stack.get(stack.size() - 1);
            checkOwnership(last, owner, "remove_last", last.getID());

            int deletedId = dbManager.deleteLastSpaceMarine(owner);
            if (deletedId == last.getID()) {
                stack.remove(stack.size() - 1);
                return last;
            } else {
                throw new RuntimeException("Не удалось удалить последний элемент из БД.");
            }
        }
    }

    /**
     * Удалить все элементы, id которых больше id переданного элемента.
     * Удаляются только элементы, принадлежащие данному owner.
     * Сначала массовое удаление в БД (с фильтром owner), при успехе — из памяти.
     *
     * @param element элемент-порог для сравнения
     * @param owner   имя пользователя, выполняющего операцию
     */
    public void removeGreater(SpaceMarine element, String owner){
        synchronized (monitor) {
            int deleted = dbManager.deleteSpaceMarinesWithIdGreaterThan(element.getID(), owner);
            if (deleted >= 0) {
                List<SpaceMarine> toRemove = stack.stream()
                        .filter(x -> x.compareTo(element) > 0 && x.getOwner().equals(owner))
                        .toList();
                stack.removeAll(toRemove);
            } else {
                throw new RuntimeException("Не удалось удалить элементы из БД (removeGreater).");
            }
        }
    }

    /**
     * Очистить коллекцию. Удаляются только элементы, принадлежащие данному owner.
     * Сначала DELETE из БД, при успехе — очистка в памяти.
     *
     * @param owner имя пользователя, выполняющего операцию
     */
    public void clear(String owner){
        dbManager.deleteSpaceMarinesByOwner(owner);
        synchronized (monitor) {
            stack.removeIf(x -> x.getOwner().equals(owner));
        }
    }

    /**
     * Найти элемент по id в памяти (без блокировки — вызывать внутри synchronized).
     */
    private SpaceMarine findElementById(int id) {
        for (SpaceMarine marine : stack) {
            if (marine.getID() == id) {
                return marine;
            }
        }
        return null;
    }

    /**
     * Проверить, что переданный owner является хозяином объекта.
     *
     * @param marine     объект коллекции
     * @param owner      имя пользователя, пытающегося выполнить операцию
     * @param operation  название операции (для логирования)
     * @param id         id объекта (для логирования)
     * @throws SecurityException если owner не совпадает с хозяином
     */
    private void checkOwnership(SpaceMarine marine, String owner, String operation, int id) {
        if (!marine.getOwner().equals(owner)) {
            logger.warn("Попытка {} для id={} от пользователя '{}' (хозяин: '{}').",
                    operation, id, owner, marine.getOwner());
            throw new SecurityException(
                    "Ошибка: пользователь '" + owner + "' не является хозяином SpaceMarine id=" + id
                            + ". Хозяин: '" + marine.getOwner() + "'. Операция отклонена."
            );
        }
    }


    public int getCollectionSize(){
        return stack.size();
    }

    /**
     * Сохранение коллекции.
     * При использовании БД все изменения сохраняются автоматически при каждой
     * модифицирующей операции. Метод оставлен для обратной совместимости.
     */
    public void save(String fileName){
        logger.info("save() вызван. При использовании БД коллекция сохраняется автоматически.");
    }

    public void save(){
        save(null);
    }

    @Override
    public Object clone(){
        return new CollectionController(this);
    }

    public void updateCollection(CollectionController other){
        List<SpaceMarine> snapshot;
        synchronized (other.monitor) {
            snapshot = new ArrayList<>(other.stack);
        }
        synchronized (this.monitor) {
            this.stack.clear();
            this.stack.addAll(snapshot);
        }
    }

    @Override
    public String toString() {
        return String.format("Type: %s%n" +
                        "Created at: %s%n" +
                        "Element count: %d",
                Stack.class,
                initializationTime.toString(),
                stack.size()
        );
    }
}
