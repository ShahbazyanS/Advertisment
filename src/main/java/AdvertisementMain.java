import model.Category;
import model.Gender;
import model.Item;
import model.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.DataStorage;
import util.FileUtil;
import util.XLSXUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class AdvertisementMain implements Commands {


    private static Scanner scanner = new Scanner(System.in);
    private static DataStorage dataStorage = new DataStorage();
    private static User currentUser = null;

    public static void main(String[] args) {
        dataStorage.initData();
        boolean isRun = true;
        while (isRun) {
            Commands.printMainCommands();
            int command;
            try {
                command = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                command = -1;
            }
            switch (command) {
                case EXIT:
                    isRun = false;
                    break;
                case LOGIN:
                    loginUser();
                    break;
                case REGISTER:
                    registerUser();
                    break;
                case IMPORT_USERS:
                    importFromXlsX();
                    break;
                default:
                    System.out.println("Wrong command!");
            }
        }
    }


    private static void importFromXlsX() {

        System.out.println("please select xlsx path");
        String xlsxPath = scanner.nextLine();
        List<User> users = XLSXUtil.readUsers(xlsxPath);
        for (User user : users) {
            dataStorage.add(user);
        }

    }


    private static void registerUser() {
        System.out.println("Please input user data " +
                "name,surname,gender(MALE,FEMALE),age,phoneNumber,password");
        try {
            String userDataStr = scanner.nextLine();
            String[] userDataArr = userDataStr.split(",");
            User userFromStorage = dataStorage.getUser(userDataArr[4]);
            if (userFromStorage == null) {
                User user = new User();
                user.setName(userDataArr[0]);
                user.setSurname(userDataArr[1]);
                user.setGender(Gender.valueOf(userDataArr[2].toUpperCase()));
                user.setAge(Integer.parseInt(userDataArr[3]));
                user.setPhoneNumber(userDataArr[4]);
                user.setPassword(userDataArr[5]);
                dataStorage.add(user);
                System.out.println("User was successfully added");
            } else {
                System.out.println("User already exists!");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Wrong Data!");
        }
    }

    private static void loginUser() {
        System.out.println("Please input phoneNumber,password");
        try {
            String loginStr = scanner.nextLine();
            String[] loginArr = loginStr.split(",");
            User user = dataStorage.getUser(loginArr[0]);
            if (user != null && user.getPassword().equals(loginArr[1])) {
                currentUser = user;
                loginSuccess();
            } else {
                System.out.println("Wrong phoneNumber or password");
            }
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            System.out.println("Wrong Data!");
        }
    }

    private static void loginSuccess() throws IOException {
        System.out.println("Welcome " + currentUser.getName() + "!");
        boolean isRun = true;
        while (isRun) {
            Commands.printUserCommands();
            int command;
            try {
                command = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                command = -1;
            }
            switch (command) {
                case LOGOUT:
                    isRun = false;
                    break;
                case ADD_NEW_AD:
                    addNewItem();
                    break;
                case PRINT_MY_ADS:
                    dataStorage.printItemsByUser(currentUser);
                    break;
                case PRINT_ALL_ADS:
                    dataStorage.printItems();
                    break;
                case PRINT_ADS_BY_CATEGORY:
                    printByCategory();
                    break;
                case PRINT_ALL_ADS_SORT_BY_TITLE:
                    dataStorage.printItemsOrderByTitle();
                    break;
                case PRINT_ALL_ADS_SORT_BY_DATE:
                    dataStorage.printItemsOrderByDate();
                    break;
                case DELETE_MY_ALL_ADS:
                    dataStorage.deleteItemsByUser(currentUser);
                    break;
                case DELETE_AD_BY_ID:
                    deleteById();
                    break;
                case IMPORT_ITEMS:
                    importItems();
                    break;
                case EXPORT_ITEMS:
                    exportItems();
                    break;
                default:
                    System.out.println("Wrong command!");
            }
        }
    }

    private static void exportItems() {
        System.out.println("please select folder path");
        String path = scanner.nextLine();
        new Thread(() -> {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                XLSXUtil.writeItems(path, dataStorage.getItems());
            } else {
                System.out.println("please input valid folder path!");
            }
        }).start();

    }


    private static void importItems() {
        System.out.println("please select excel file path");
        String path = scanner.nextLine();
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            new Thread(() -> {
                List<Item> items = XLSXUtil.readItems(path);
                for (Item item : items) {
                    item.setUser(dataStorage.getUser(item.getUser().getPhoneNumber()));
                    dataStorage.add(item);
                }
            }).start();

        } else {
            System.out.println("please input valid folder path!");
        }
    }

    private static void deleteById() {
        System.out.println("please choose id from list");
        dataStorage.printItemsByUser(currentUser);
        long id;
        try {
            id = Long.parseLong(scanner.nextLine());
            Item itemById = dataStorage.getItemById(id);
            if (itemById != null && itemById.getUser().equals(currentUser)) {
                dataStorage.deleteItemsById(id);
            } else {
                System.out.println("Wrong id!");
            }
        } catch (NumberFormatException e) {
            id = -1;
            System.out.println("wrong id");
        }
    }

    private static void printByCategory() {
        System.out.println("Please choose category name from list: " + Arrays.toString(Category.values()));
        try {
            String categoryStr = scanner.nextLine();
            Category category = Category.valueOf(categoryStr);
            dataStorage.printItemsByCategory(category);
        } catch (Exception e) {
            System.out.println("Wrong Category!");
        }
    }

    private static void addNewItem() {
        System.out.println("Please input item data title,text,price,category");
        System.out.println("Please choose category name from list: " + Arrays.toString(Category.values()));
        try {
            String itemDataStr = scanner.nextLine();
            String[] itemDataArr = itemDataStr.split(",");
            Item item = new Item(itemDataArr[0], itemDataArr[1], Double.parseDouble(itemDataArr[2])
                    , currentUser, Category.valueOf(itemDataArr[3].toUpperCase()), new Date());
            dataStorage.add(item);
            System.out.println("Item was successfully added");
        } catch (Exception e) {
            System.out.println("Wrong Data!");

        }
    }
}
