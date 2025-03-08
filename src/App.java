/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    /*
     * Prints the program's header
     */
    public static void printHeader() {
        System.out.println("------------------------------------------------\n");
        System.out.println("STDISCM P1 - Threaded Prime Number Search\nBy: Jaeme Rebano S14\n");
        System.out.println("------------------------------------------------");
    }

    /*
     * Returns the current time in milliseconds
     */
    public static long getTime() {
        return System.currentTimeMillis();
    }

    /*
     * Prints the chosen values based on config.txt
     */
    public static void printConfiguration(int n, int t, int h, int d, int t1, int t2) {
        System.out.println("[n] Max concurrent instances = " + n);
        System.out.println("[t] Tank players in queue = " + t);
        System.out.println("[h] Healer players in queue = " + h);
        System.out.println("[d] DPS players in queue = " + d);
        System.out.println("[t1] Min time before an instance is finished = " + t1);
        System.out.println("[t2] Max time before an instance is finished = " + t2);
    }

    /*
     * Returns whether the input is negative
     */
    public static boolean checkIfNegative(int value, String name) {
        if (value < 0) {
            System.err.println("Invalid value for " + name + ": " + value + " (must be not be negative)");
            return true;
        }
        return false;
    }

    /*
     * Returns whether the input is 0
     */
    public static boolean checkIfZero(int value, String name) {
        if (value == 0) {
            System.err.println("Invalid value for " + name + ": " + value + " (must be not be zero)");
            return true;
        }
        return false;
    }

    /*
     * Returns whether the inputs in config.txt are valid
     */
    public static boolean validateInputs(int[] inputs) {
        boolean isValid = true;
        String[] varNames = {"n", "t", "h", "d", "t1", "t2"};
        // Must not be negative
        for (int i = 0; i < inputs.length; i++) {
            if (checkIfNegative(inputs[i], varNames[i])) {
                isValid = false;
            }
            
            if ("t".equals(varNames[i]) || "h".equals(varNames[i]) || "d".equals(varNames[i])) {
                continue;
            }
            // n, t1, t2 must not be zero    
            if (checkIfZero(inputs[i], varNames[i])) {
                isValid = false;
            }    
        }
       
        // t2 should not be less than t1 or greater than 15
        if (inputs[5] < inputs[4] || inputs[5] > 15) {
            System.err.println("Invalid value for t2: " + inputs[5] + " (must be >= t1 and <= 15)");
            isValid = false;
        }
        return isValid;
    }

    /*
     * Reads inputs in config.txt and handles validation
     */
    public static int[] getInputs() {
        int[] inputs = new int[6];
    
        try (Scanner sc = new Scanner(new File("config.txt"))) {
            for (int i = 0; i < inputs.length; i++) {
                if (!sc.hasNextLine()) {
                    System.err.println("Missing config values. Expected 6 lines.");
                    return null;
                }
    
                String line = sc.nextLine();
                String[] parts = line.split("=", 2);
    
                if (parts.length != 2) {
                    System.err.println("Invalid format in config file: " + line);
                    return null;
                }
    
                try {
                    inputs[i] = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid format in config file: " + line);
                    return null;
                }
            }
            if (sc.hasNextLine()) {
                System.err.println("Too many inputs. Any input after the first six will be ignored.");
            }
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
            return null;
        }
    
        // Validate inputs
        if (!validateInputs(inputs)) {
            return null;
        }
    
        return inputs;
    }

    public static Map<Integer, Dungeon> createDungeonInstances(int n) {
        Map<Integer, Dungeon> dungeonMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            dungeonMap.put(i + 1, new Dungeon(i + 1));
        }
        return dungeonMap;
    }

    public static Queue<Integer> createDungeonQueue(int n) {
        Queue<Integer> availableDungeons = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < n; i++) {
            availableDungeons.offer(i + 1);
        }
        return availableDungeons;
    }

    public static int getNumOfParties(int t, int h, int d) {
        int dps = d / 3;
        return Math.min(t, Math.min(h, dps));
    }

    public static void main(String[] args) {
        printHeader();
        int[] inputs = getInputs();

        // Check the inputs are valid
        if (inputs != null) {
            int n = inputs[0]; // Max concurrent instances
            int t = inputs[1]; // Tanks
            int h = inputs[2]; // Healers
            int d = inputs[3]; // DPS
            int t1 = inputs[4]; // min time before instance finished
            int t2 = inputs[5]; // max time before instance finished
            
            printConfiguration(n, t, h, d, t1, t2);

            // Different Parties finding a dungeon
            int numParties = getNumOfParties(t, h, d);
            int leftOverTanks = t - numParties;
            int leftOverHealers = h - numParties;
            int leftOverDPS = d - (numParties * 3);
            
            DungeonManager manager = DungeonManager.getInstance(n);
            ExecutorService executor = Executors.newFixedThreadPool(numParties);

            
            for (int i = 1; i <= numParties; i++) {
                executor.execute(new Party(i, t1, t2)); // Shared manager
            }
            
            executor.shutdown(); // No more tasks can be submitted
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) { // Wait up to 10 seconds
                    executor.shutdownNow(); // Force shutdown if tasks are still running
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("Left Over Players:");
            System.out.println(leftOverTanks);
            System.out.println(leftOverHealers);
            System.out.println(leftOverDPS);

            // One Manager
            /*
            
            // Map<Integer, Dungeon> dungeons = createDungeonInstances(n);
            //Queue<Integer> availableDungeons = createDungeonQueue(n);

            
            while (true) { // set end statement
                int nextId = availableDungeons.poll();
                Dungeon nextDungeon = dungeons.get(nextId);
                int time = (int) (Math.random() * (t2 - t1 + 1)) + t1;

                nextDungeon

            }
            */
            
        }
    }
}