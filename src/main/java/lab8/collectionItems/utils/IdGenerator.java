package lab8.collectionItems.utils;

import java.util.HashSet;
import java.util.Set;

public class IdGenerator {
    private final Set<Integer> usedIds = new HashSet<>();
    private int currentCounter = 0;

    private static IdGenerator instance = null;

    private IdGenerator (){

    }

    public static IdGenerator getInstance(){
        if (instance == null)
            instance = new IdGenerator();
        return instance;
    }

    public int generateId() {
        int id;
        do {
            id = currentCounter++;
        } while (usedIds.contains(id));
        usedIds.add(id);
        return id;
    }

    public void registerExistingId(Integer id) {
        if (id == null) return;
        if (usedIds.contains(id)) {
            throw new IllegalStateException("Дублирующийся ID: " + id);
        }
        usedIds.add(id);
        if (id >= currentCounter) {
            currentCounter = id + 1;
        }
    }

    public void setCurrentId(int id){
        this.currentCounter = id;
    }
}