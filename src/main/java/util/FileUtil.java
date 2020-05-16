package util;

import model.Item;
import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {
    private static final String PATH_ITEM = "src\\main\\resources\\itemList.obj";
    private static final String PATH_USER = "src\\main\\resources\\userMap.obj";

    public static void serializeUserMap(Map<String, User> userMap) {
        File userMapFile = new File(PATH_USER);
        try {
            if (!userMapFile.exists()) {
                userMapFile.createNewFile();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PATH_USER))) {
                oos.writeObject(userMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, User> deserializeUserMap() {
        Map<String, User> result = new HashMap<>();
        File userMapFile = new File(PATH_USER);
        if (userMapFile.exists()) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(PATH_USER))) {
                Object o = objectInputStream.readObject();
                return (Map<String, User>) o;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void serializeIterList(List<Item> item) {
        File itemListFile = new File(PATH_ITEM);
        try {
            if (!itemListFile.exists()) {
                itemListFile.createNewFile();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PATH_ITEM))) {
                oos.writeObject(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static List<Item> desrializeItemList(){
        List<Item> result = new ArrayList<>();
        File itemListFile = new File(PATH_ITEM);
        if (itemListFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PATH_ITEM))) {
                Object desrialize = ois.readObject();
                return (List<Item>) desrialize;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
