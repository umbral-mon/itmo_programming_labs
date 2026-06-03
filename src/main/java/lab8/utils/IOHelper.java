package lab8.utils;

import lab8.collectionItems.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Stack;

public class IOHelper {

    private static Stack<InputStream> inputStack = new Stack<>();
    private static InputStream input;
    private static InputStream consoleIn;

    public static final PrintStream consoleOut, errOut;

    static {
        consoleIn = System.in;
        input = System.in;
        consoleOut = System.out;
        errOut = System.err;
    }

    public static void switchTo(InputStream other){
        inputStack.push(input);
        input = other;
    }

    public static void switchBack(){
        if (inputStack.isEmpty())
            throw new RuntimeException("Empty storage");
        input = inputStack.pop();
    }

    public static boolean hasNext(){
        return true;
    }

    /**
     * Reads line
     * @param reader InputStreamReader to read from
     * @return String until \n on EOF
     */
    private static String getLine(InputStream reader) throws InterruptedException{
        try {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int c = reader.read();
                if (c == -1) {
                    //reader.read();
                    throw new InterruptedException("ввод прерван");
                }
                if (c == -1 || c == '\n')
                    break;
                sb.append((char)c);
            }
            return sb.toString().strip();
        } catch (IOException ex){
            consoleOut.println("Unknown error. Program terminated");
            System.exit(-1);
        }
        return null;
    }

    public static String readLine() throws InterruptedException{
        return getLine(input);
    }

//    public static void printlnIfUsingConsole(String s){
//        if (defaultIn == consoleIn) consoleOut.println(s);
//    }

    /**
     * reads SpaceMarine from stream
     * @return SpaceMarine object
     */
    public static SpaceMarine readMarine() throws InterruptedException {
        boolean needGreet = input == consoleIn;
        String buff;

        String name; //Поле не может быть null, Строка не может быть пустой
        Coordinates coordinates; //Поле не может быть null
        Double health; //Поле не может быть null, Значение поля должно быть больше 0
        AstartesCategory category; //Поле может быть null
        Weapon weaponType; //Поле не может быть null
        MeleeWeapon meleeWeapon; //Поле может быть null
        Chapter chapter; //Поле может быть null

        while (true) {
            if (needGreet) consoleOut.print(">>> Введите имя: ");
            buff = getLine(input);
            if (needGreet && (buff == null || buff.isEmpty()))
                continue;
            name = buff;
            break;
        }

        coordinates = readCoordinates();

        while (true) {
            try {
                if (needGreet) consoleOut.print(">>> Введите здоровье(> 0): ");
                buff = getLine(input);
                Double tmp = Double.parseDouble(buff);
                if (needGreet && tmp <= 0)
                    continue;
                health = tmp;
                break;
            } catch (NumberFormatException ex){
                if (!needGreet) throw new FileReadingException("Ошибка чтения файла");
            }
        }

        while (true) {
            try {
                if (needGreet) consoleOut.print(">>> Введите категорию(SCOUT, AGGRESSOR, SUPPRESSOR, TACTICAL, TERMINATOR): ");
                buff = getLine(input).toUpperCase();
                if (buff.isEmpty())
                    buff = "SCOUT";
                category = AstartesCategory.valueOf(buff);
                break;
            } catch (IllegalArgumentException ex){
                if (!needGreet) throw new FileReadingException("Ошибка чтения файла");
            }
        }
        while (true) {
            try {
                if (needGreet) consoleOut.print(">>> Введите тип оружия(MELTAGUN, GRAV_GUN, GRENADE_LAUNCHER): ");
                buff = getLine(input).toUpperCase();
                if (buff.isEmpty())
                    buff = "MELTAGUN";
                weaponType = Weapon.valueOf(buff);
                break;
            } catch (IllegalArgumentException ex){
                if (!needGreet) throw new FileReadingException("Ошибка чтения файла");
            }
        }
        while (true) {
            try {
                if (needGreet) consoleOut.print(">>> Введите оружие ближнего боя(CHAIN_SWORD, POWER_SWORD, CHAIN_AXE, MANREAPER, POWER_BLADE): ");
                buff = getLine(input).toUpperCase();
                if (buff.isEmpty())
                    buff = "CHAIN_SWORD";
                meleeWeapon = MeleeWeapon.valueOf(buff);
                break;
            } catch (IllegalArgumentException ex){
                if (!needGreet) throw new FileReadingException("Ошибка чтения файла");
            }
        }

        chapter = readChapter();

        return new SpaceMarine(name, coordinates, health, category, weaponType, meleeWeapon, chapter, "TEMP");
    }

    /**
     * reads Coordinates from stream
     * @return Coordinates object
     */
    public static Coordinates readCoordinates() throws InterruptedException{
        boolean needGreet = input == consoleIn;
        String buff;
        Long x;
        long y;
        while (true){
            try {
                if (needGreet) consoleOut.print(">>> Введите x(> -201): ");
                buff = getLine(input);
                Long tmp = Long.parseLong(buff);
                if (tmp <= -201)
                    continue;

                x = tmp;
                break;
            } catch (NumberFormatException ex){
                if (!needGreet) throw new FileReadingException("Ошибка чтения файла");
            }
        }

        while (true){
            try {
                if (needGreet) consoleOut.print(">>> Введите y: ");
                buff = getLine(input);
                y = Long.parseLong(buff);
                break;
            } catch (NumberFormatException ex){
                if (!needGreet) throw new FileReadingException("Ошибка чтения файла");
            }
        }
        return new Coordinates(x, y);
    }

    /**
     * reads Chapter from stream
     * @return Chapter object
     */
    public static Chapter readChapter() throws InterruptedException{
        boolean needGreet = input == consoleIn;
        String name, parentLegion;
        Integer marinesCount;

        String buff;

        while (true) {
            if (needGreet) consoleOut.print(">>> Введите имя главы: ");
            buff = getLine(input);
            if (needGreet && (buff == null || buff.isEmpty()))
                continue;
            name = buff;
            break;
        }

        if (needGreet) consoleOut.print(">>> Введите легион: ");
        parentLegion = getLine(input);

        while (true){
            try {
                if (needGreet) consoleOut.print(">>> Введите количество морпехов: ");
                buff = getLine(input);
                Integer tmp = Integer.parseInt(buff);
                if (needGreet && (tmp < 0 || tmp > 1000))
                    continue;

                marinesCount = tmp;
                break;
            } catch (NumberFormatException ex){
                if (!needGreet) throw new FileReadingException("Ошибка чтения файла");
            }
        }

        return new Chapter(name, parentLegion, marinesCount);
    }

}
