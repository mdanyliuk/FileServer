package server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class IdMap implements Serializable {
    Map<String, Integer> nameToId = new HashMap<>();
    Map<Integer, String> idToName = new HashMap<>();
    int maxId = 0;
    private static final long serialVersionUID = 1L;

    synchronized int addName(String name) {
        maxId++;
        nameToId.put(name, maxId);
        idToName.put(maxId, name);
        return maxId;
    }

    synchronized String getName(int id) {
        return idToName.get(id);
    }

    synchronized void deleteName(String name) {
        int id = nameToId.get(name);
        nameToId.remove(name);
        idToName.remove(id);
    }

    synchronized void deleteId(int id) {
        String name = idToName.get(id);
        nameToId.remove(name);
        idToName.remove(id);
    }
}
