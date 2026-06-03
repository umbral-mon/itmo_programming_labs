package main;

import lab1.Lab1Main;
import lab2.Lab2Main;
import lab3.Lab3Main;
import lab5.Lab5Main;
import lab6.Lab6Main;
import lab7.Lab7Main;
import lab8.Lab8Main;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0){
            System.out.println("Please provide work's id");
            System.exit(0);
        }
        if (args.length > 1){
            System.out.println("Too many args. Please provide only work's id");
            System.exit(0);
        }
        try {
            Solution solution = getWork(args[0]);
            solution.solve();
        } catch (NumberFormatException ex){
            System.out.printf("Can't parse \"%s\" to integer", args[0]);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static Solution getWork(String id){
        return switch (id) {
            case "1" -> new Lab1Main();
            case "2" -> new Lab2Main();
            case "3","4" -> new Lab3Main();
            case "5" -> new Lab5Main();
            case "6c" -> new Lab6Main(false);
            case "6s" -> new Lab6Main(true);
            case "7c" -> new Lab7Main(false);
            case "7s" -> new Lab7Main(true);
            case "8c" -> new Lab8Main(false);
            case "8s" -> new Lab8Main(true);
            default -> throw new IllegalArgumentException(String.format("Work with id = %d doesn't exist", id));
        };
    }
}
