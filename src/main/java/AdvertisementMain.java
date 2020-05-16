import model.Category;
import model.Gender;
import model.Item;
import model.User;
import org.apache.commons.math3.optim.nonlinear.scalar.LineSearch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import storage.DataStorage;
import util.FileUtil;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.*;

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
                case EXPORT_ITEMS:
                    exportItems();
                    break;
                default:
                    System.out.println("Wrong command!");
            }
        }
    }


    private static void importFromXlsX() {

        System.out.println("please select xlsx path");
        String xlsxPath = scanner.nextLine();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(xlsxPath);
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                String name = row.getCell(0).getStringCellValue();
                String surname = row.getCell(1).getStringCellValue();
                Double age = row.getCell(2).getNumericCellValue();
                Gender gender = Gender.valueOf(row.getCell(3).getStringCellValue());
                Cell phoneNumber = row.getCell(4);
                String phoneNumberStr = phoneNumber.getCellType() == CellType.NUMERIC ?
                        String.valueOf(Double.valueOf(phoneNumber.getNumericCellValue()).intValue()) : phoneNumber.getStringCellValue();
                Cell password = row.getCell(5);
                String passwordStr = password.getCellType() == CellType.NUMERIC ?
                        String.valueOf(Double.valueOf(password.getNumericCellValue()).intValue()) : password.getStringCellValue();
                User user = new User();
                user.setName(name);
                user.setSurname(surname);
                user.setAge(age.intValue());
                user.setGender(gender);
                user.setPhoneNumber(phoneNumberStr);
                user.setPassword(passwordStr);
                System.out.println(user);
                dataStorage.add(user);
                System.out.println("Import was success!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while importing users");
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
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
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

                default:
                    System.out.println("Wrong command!");
            }
        }
    }

    private static void exportItems() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<Item> items = FileUtil.desrializeItemList();
                XSSFWorkbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet();
//        CellStyle cellStyle = workbook.createCellStyle();
//        CreationHelper help = workbook.getCreationHelper();
//        cellStyle.setDataFormat(help.createDataFormat().getFormat("dd/mm/yyyy"));
                int t = 0;
                for (Item item : items) {
                    Row row = sheet.createRow(++t);
                    row.createCell(0).setCellValue(item.getId());
                    row.createCell(1).setCellValue(item.getTitle());
                    row.createCell(2).setCellValue(item.getText());
                    row.createCell(3).setCellValue(item.getPrice());
                    row.createCell(4).setCellValue(String.valueOf(item.getCategory()));
                    row.createCell(5).setCellValue(item.getCreatedDate().toString());
//            cellStyle.setDataFormat(help.createDataFormat().getFormat("dd-mm-yyyy"));
//            Cell cell = row.createCell(5);
//            cell.setCellValue(item.getCreatedDate());
//            cell.setCellStyle(cellStyle);


                    System.out.println(item);
                    final String ITEM_PATH = "src/main/resources/exportItems.xlsx";
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(new File(ITEM_PATH));
                        workbook.write(fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread t = new Thread(runnable);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private static void importItems() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                System.out.println("please select xlsx path items");
                String xlsxPath = scanner.nextLine();
                try {
                    XSSFWorkbook workbook = new XSSFWorkbook(xlsxPath);
                    Sheet sheet = workbook.getSheetAt(0);
                    int lastRowNum = sheet.getLastRowNum();
                    for (int i = 1; i <= lastRowNum; i++) {
                        Row row = sheet.getRow(i);
                        long id = (long) row.getCell(0).getNumericCellValue();
                        String title = row.getCell(1).getStringCellValue();
                        String text = row.getCell(2).getStringCellValue();
                        Double price = row.getCell(3).getNumericCellValue();
                        Category category = Category.valueOf(row.getCell(4).getStringCellValue());
                        Date date = row.getCell(5).getDateCellValue();
                        Item item = new Item();
                        item.setId(id);
                        item.setTitle(title);
                        item.setText(text);
                        item.setPrice(price);
                        item.setUser(currentUser);
                        item.setCategory(category);
                        item.setCreatedDate(date);
                        System.out.println(item);
                        dataStorage.add(item);
                        System.out.println("Import was success!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(runnable);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void deleteById() throws IOException {
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
