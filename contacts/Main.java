package contacts;

import java.util.*;
import java.util.regex.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Contact> list = new ArrayList<>(); 
        Menu menu = new Menu(scanner);
        Contact.scanner = scanner;

        String fileName = "";
        if (args.length > 0 && !"".equals(args[0])) {
            fileName = args[0];
            if (new File(fileName).exists()) { 
                try {
                    Contact[] clist = (Contact[]) SerializationUtils.deserialize(fileName);
                    for (Contact contact: clist) {
                        list.add(contact);
                    }
                    System.out.println("open " + fileName);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        Action action = new Action(scanner, list, menu, fileName);

        String command = "";
        while (!"exit".equals(command)) {
            command = menu.getMenuCommand();
            switch (command) {
                case "add":
                    action.add();
                    break;
                case "list":
                    action.list();
                    break;
                case "search":
                    action.search();
                    break;
                case "count":
                    action.count();
                    break;
            }

            if (!"exit".equals(command)) {
                System.out.println();
            }
        }       

        scanner.close();
    }

}

class Menu {
    Scanner scanner;

    Menu(Scanner scanner) {
        this.scanner = scanner;
    }

    String getMenuCommand() {
        String prompt = "[menu] Enter action (add, list, search, count, exit): ";
        String[] validList = new String[]{"add", "list", "search", "count", "exit"}; 
        return Input.getString(scanner, prompt, validList);
    }

    String getSearchCommand(int maxNumber) {
        String prompt = "[search] Enter action ([number], back, again): ";
        String[] validList = new String[]{"[number]", "back", "again"}; 
        return Input.getString(scanner, prompt, validList, maxNumber);
    }

    String getRecordCommand() {
        String prompt = "[record] Enter action (edit, delete, menu): ";
        String[] validList = new String[]{"edit", "delete", "menu"}; 
        return Input.getString(scanner, prompt, validList);
    }    

    String getListCommand(int maxNumber) {
        String prompt = "[list] Enter action ([number], back): ";
        String[] validList = new String[]{"[number]", "back"}; 
        return Input.getString(scanner, prompt, validList, maxNumber);
    }

    String getType() {
        String prompt = "Enter the type (person, organization): ";
        String[] validList = new String[]{"person", "organization"}; 
        return Input.getString(scanner, prompt, validList);
    }
}

class Action {
    int counter = 0;
    Scanner scanner;
    ArrayList<Contact> list;
    Menu menu;
    String fileName;

    Action(Scanner scanner, ArrayList<Contact> list, Menu menu, String fileName) {
        this.scanner = scanner;
        this.list = list;
        this.menu = menu;
        this.fileName = fileName;
    } 
    
    void save() {

        if (!"".equals(fileName)) {
            try { 
                Contact[] clist = new Contact[list.size()];
                int i = 0;
                for (Contact contact: list) {
                    clist[i++] = contact;
                }
                SerializationUtils.serialize(clist, fileName);
                System.out.println("Saved");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    void displayList() {
        for (int i = 0; i < list.size(); i++) {
            System.out.print((i + 1) + ". ");
            System.out.println(list.get(i));
        }
    } 

    void displayList(ArrayList<Contact> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.print((i + 1) + ". ");
            System.out.println(list.get(i));
        }
    } 

    void list() {
        displayList();
        System.out.println();
        String command = "";
        while (!"back".equals(command)) {
            command = menu.getListCommand(list.size());
            if (command.matches("[0-9]+")) {
                int i = Integer.parseInt(command);
                Contact contact = list.get(i - 1);
                contact.info();
                System.out.println();
                String subcommand = "";
                while (!"menu".equals(subcommand)) {
                    subcommand = menu.getRecordCommand();
                    switch (subcommand) {
                        case "edit":
                            contact.edit();
                            save();
                            contact.info();
                            System.out.println();
                            break;
                        case "delete":
                            list.remove(contact);
                            System.out.println();
                            break;
                    }
                }
                command = "back";
            }
        }
    }

    void count() {
        System.out.println(String.format("The Phone Book has %d records.", list.size()));
    }

    void add() {
        String input = menu.getType();
        Contact contact;
        if ("person".equals(input)) {
            contact = new PersonContact();
        } else {
            contact = new OrganizationContact();
        }
        contact.add();    
        list.add(contact);
        save();

        System.out.println("The record added.");
    }

    void search() {
        String command = "";
        while (!"back".equals(command)) {
            System.out.println("Enter search query: ");
            String word = scanner.nextLine();
            ArrayList<Contact> foundList = searchWord(word);
            System.out.println(String.format("Found %d results: ", foundList.size()));
            displayList(foundList);
            System.out.println();
            command = menu.getSearchCommand(list.size());
            if (command.matches("[0-9]+")) {
                int i = Integer.parseInt(command);
                Contact contact = foundList.get(i - 1);
                contact.info();
                System.out.println();
                String subcommand = "";
                while (!"menu".equals(subcommand)) {
                    subcommand = menu.getRecordCommand();
                    switch (subcommand) {
                        case "edit":
                            contact.edit();
                            save();
                            contact.info();
                            System.out.println();
                            break;
                        case "delete":
                            list.remove(contact);
                            System.out.println();
                            break;
                    }
                }
                command = "back";
            }
        }
    }

    ArrayList<Contact> searchWord(String word) {
        ArrayList<Contact> foundList = new ArrayList<>(); 
        for (Contact contact: list) {
            if (contact.isContains(word)) {
                foundList.add(contact);
            }
        }
        return foundList;
    }

} 

abstract class Contact implements Serializable {
    private static final long serialVersionUID = 1L;

    protected static Scanner scanner;
    protected String name = "";
    protected String phoneNumber = "";
    protected LocalDateTime created;
    protected LocalDateTime lastEdit;

    Contact() {
        this.created = LocalDateTime.now();
    }

    void updateTimeStamp() {
        this.lastEdit = LocalDateTime.now();
    }

    String getName() {
        if ("".equals(name)) {
            return "[no name]";
        }
        return name;
    }

    String getPhoneNumber() {
        if ("".equals(phoneNumber)) {
            return "[no number]";
        }
        return phoneNumber;
    }

    String getCreated() {
        return created.toString().substring(0, 16);
    }

    String getLastEdit() {
        return lastEdit.toString().substring(0, 16);
    }

    void setName(String name) {
        this.name = name;
        updateTimeStamp();
    }

    boolean setPhoneNumber(String phoneNumber) {
        if (!checkPhoneNumber(phoneNumber)) {
            this.phoneNumber = "";
            return false;
        }
        this.phoneNumber = phoneNumber;
        return true;
    }

    boolean checkPhoneNumber(String phoneNumber) {

        String[] strs = phoneNumber.split("[ -]");  //1
        int parenthesesCount = 0;
        int strCount = 0;
        for (String str: strs) {
            if ("".equals(str)) {
                continue;
            }
            strCount++;
            if (strCount == 1 && str.startsWith("+")) { //2
                str = str.substring(1); 
            }
            if (str.length() >= 2 && str.startsWith("(") && str.endsWith(")")) {  //3
                parenthesesCount++;
                if (parenthesesCount > 1) {
                    return false;
                }
                str = str.substring(1, str.length() - 1);
            }
            if (!str.matches("[0-9a-zA-Z]+")) { //4
                return false;
            }
            if (strCount > 1 && str.length() < 2) {  //4
                return false;
            }
        }
        return true;
    }

    String getString(String[] validList) {
        String input = "";
        boolean okFlag = false;
        while (!okFlag) {
            input = scanner.nextLine();
            for (String item: validList) {
                if (input.equals(item)) {
                    okFlag = true;
                    break;
                }
            }
        }
        return input;
    }

    @Override
    public String toString() {
        String nameStr = "".equals(name) ? "[no name]" : name;
        return nameStr;
    } 

    abstract void add();
    abstract void edit(); 
    abstract void info();
    abstract boolean isContains(String word);
}

class PersonContact extends Contact {
    private static final long serialVersionUID = 1L;

    private String surname = "";
    private LocalDate birthDate = null;
    private String gender = "";

    PersonContact() {
        super();
    }

    String getSurname() {
        if ("".equals(surname)) {
            return "[no surname]";
        }
        return surname;
    }

    String getGender() {
        if ("".equals(gender)) {
            return "[no data]";
        }
        return gender;
    }

    String getBirthDate() {
        if (birthDate == null) {
            return "[no data]";
        }
        return birthDate.toString();
    }

    void setSurname(String surname) {
        this.surname = surname;
        updateTimeStamp();
    }

    boolean setGender(String gender) {
        if (!"M".equals(gender) && !"F".equals(gender)) {
            this.gender = "";;
            return false;
        }
        this.gender = gender;
        updateTimeStamp();
        return true;
    }

    boolean setBirthDate(String birthDate) {
        try {
            this.birthDate = LocalDate.parse(birthDate);
        } catch (DateTimeParseException e) {
            this.birthDate = null;
            return false;
        }
        updateTimeStamp();
        return true;
    }
    
    @Override
    public String toString() {
        String nameStr = "".equals(name) ? "[no name]" : name;
        String surnameStr = "".equals(surname) ? "[no surname]" : surname;
        return nameStr + " " + surnameStr;
    } 
    
    @Override
    void add() {
        System.out.println("Enter the name:");
        String name = scanner.nextLine();
        setName(name);

        System.out.println("Enter the surname: ");
        String surname = scanner.nextLine();
        setSurname(surname);
 
        System.out.println("Enter the birth date: ");
        String birthDate = scanner.nextLine();
        if (!setBirthDate(birthDate)) {
            System.out.println("Bad birth date!");
        } 

        System.out.println("Enter the gender (M, F): ");
        String gender = scanner.nextLine();
        if (!setGender(gender)) {
            System.out.println("Bad gender!");
        }

        System.out.println("Enter the number: ");
        String phoneNumber = scanner.nextLine();
        if (!setPhoneNumber(phoneNumber)) {
            System.out.println("Wrong number format!");         
        }     
    }    

    @Override
    void edit() {
        System.out.println("Select a field (name, surname, birth, gender, number): ");
        String property = getString(new String[]{"name", "surname", "birth", "gender", "number"});
        switch (property) {
            case "name":
                System.out.println("Enter name: ");
                String name = scanner.nextLine();
                setName(name);
                break;
            case "surname":
                System.out.println("Enter surname: ");
                String surname = scanner.nextLine();
                setSurname(surname);
                break;
            case "number":
                System.out.println("Enter number: ");
                String number = scanner.nextLine();
                if (!setPhoneNumber(number)) {
                    System.out.println("Wrong number format!");
                }
                break;
            case "birth":
                System.out.println("Enter birth date: ");
                String birthDate = scanner.nextLine();
                if (!setBirthDate(birthDate)) {
                    System.out.println("Bad birth date!");
                }
                break;
            case "gender":
                System.out.println("Enter the gender (M, F): ");
                String gender = scanner.nextLine();
                if (!setGender(gender)) {
                    System.out.println("Bad gender!");
                }
                break;
        }
    }

    @Override
    void info() {
        System.out.println("Name: " + getName());
        System.out.println("Surname: " + getSurname());
        System.out.println("Birth date: " + getBirthDate());
        System.out.println("Gender: " + getGender());
        System.out.println("Number: " + getPhoneNumber());
        System.out.println("Time created: "+ getCreated());
        System.out.println("Time last edit: " + getLastEdit());
    }

    @Override
    boolean isContains(String word) {
        String str = "";
        str += name;
        str += " " + surname;
        str += " " + phoneNumber;
        str += " " + birthDate;
        str += " " + gender;
        Pattern pattern = Pattern.compile(".*" + word + ".*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}

class OrganizationContact extends Contact {
    private static final long serialVersionUID = 1L;

    private String address;
    
    OrganizationContact() {
        super();
    }

    String getAddress() {
        if ("".equals(address)) {
            return "[no address]";
        }
        return address;
    }

    void setAddress(String address) {
        this.address = address;
        updateTimeStamp();
    }

    @Override
    public String toString() {
        String nameStr = "".equals(name) ? "[no name]" : name;
        return nameStr;
    } 
    
    @Override
    void add() {
        System.out.println("Enter the organization name: ");
        String name = scanner.nextLine();
        System.out.println("Enter the address: ");
        String address = scanner.nextLine();
        System.out.println("Enter the number:");
        String phoneNumber = scanner.nextLine();

        setName(name);
        setAddress(address);
        if (!setPhoneNumber(phoneNumber)) {
            System.out.println("Wrong number format!");         
        }     
    }    

    @Override
    void edit() {
        System.out.println("Select a field (name, address, number): ");
        String property = getString(new String[]{"name", "address", "number"});
        switch (property) {
            case "name":
                System.out.println("Enter organization name: ");
                String name = scanner.nextLine();
                setName(name);
                break;
            case "address":
                System.out.println("Enter address: ");
                String address = scanner.nextLine();
                setAddress(address);
                break;
            case "number":
                System.out.println("Enter number: ");
                String number = scanner.nextLine();
                if (!setPhoneNumber(number)) {
                    System.out.println("Wrong number format!");
                }
                break;
        }
    }
  
    @Override
    void info() {
        System.out.println("Organization name: " + getName());
        System.out.println("Address: " + getAddress());
        System.out.println("Number: " + getPhoneNumber());
        System.out.println("Time created: "+ getCreated());
        System.out.println("Time last edit: " + getLastEdit());
    }

    @Override
    boolean isContains(String word) {
        String str = "";
        str += name;
        str += " " + address;
        str += " " + phoneNumber;
        Pattern pattern = Pattern.compile(".*" + word + ".*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}

class Input {

    static String getString(Scanner scanner, String prompt, String[] validList, int maxNumber) {
        String input = "";
        boolean okFlag = false;
        while (!okFlag) {
            System.out.println(prompt);
            input = scanner.nextLine();
            for (String item: validList) {
                if ("[number]".equals(item)) {
                    if (input.matches("[0-9]+")) {
                        int number = Integer.parseInt(input);
                        if (number <= maxNumber && number > 0) {
                            okFlag = true;
                            break;
                        }
                    }
                }
                if (input.equals(item)) {
                    okFlag = true;
                    break;
                }
            }
        }
        return input;
    }

    static String getString(Scanner scanner, String prompt, String[] validList) {
        String input = "";
        boolean okFlag = false;
        while (!okFlag) {
            System.out.println(prompt);
            input = scanner.nextLine();
            for (String item: validList) {
                if (input.equals(item)) {
                    okFlag = true;
                    break;
                }
            }
        }
        return input;
    }

    static String getString(Scanner scanner, String[] validList) {
        String input = "";
        boolean okFlag = false;
        while (!okFlag) {
            input = scanner.nextLine();
            for (String item: validList) {
                if (input.equals(item)) {
                    okFlag = true;
                    break;
                }
            }
        }
        return input;
    }

    static int getNumber(Scanner scanner, int maxNumber) {
        String input;
        int number;
        while (true) {
            input = scanner.nextLine();
            if (input.matches("[0-9]+")) {
                number = Integer.parseInt(input);
                if (number > 0 && number <= maxNumber) { 
                    break;
                }
            }
        }
        return number;
    }
}

class SerializationUtils {
    /**
     * Serialize the given object to the file
     */
    public static void serialize(Object obj, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    }

    /**
     * Deserialize to an object from the file
     */
    public static Object deserialize(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}
