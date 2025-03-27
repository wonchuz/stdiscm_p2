/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;

import java.io.*;
import java.util.*;

public class App {
    /*
     * Prints the program's header
     */
    public static void printHeader() {
        System.out.println("------------------------------------------------\n");
        System.out.println("STDISCM P2 - Looking for Group Synchronization\nBy: Jaeme Rebano S14\n");
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
        String[] varNames = { "n", "t", "h", "d", "t1", "t2" };
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

        // t1 should not be greater than 15
        if (inputs[4] > 15) {
            System.err.println("Invalid value for t1: " + inputs[4] + " (must be <= 15)");
            isValid = false;
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
                    System.err.println("Invalid format in config file (not an integer or out of data type bounds): " + line);
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

    public static int getNumOfParties(int t, int h, int d) {
        int dps = d / 3;
        return Math.min(t, Math.min(h, dps));
    }

    public static int getPriority(int t1, int t2, int time) {
        int diffToT1 = Math.abs(time - t1);
        int diffToT2 = Math.abs(time - t2);
    
        // If the time is closer to t1, assign priority 1; otherwise, assign priority 2
        if (diffToT1 < diffToT2) {
            return 1; // Closer to t1
        } else {
            return 2; // Closer to t2
        }
    }
    

    public static void main(String[] args) {
        printHeader();
        int[] inputs = getInputs();

        // Check the inputs are valid
        if (inputs != null) {
            int n = inputs[0]; // Max concurrent instances
            int tankPlayers = inputs[1]; // Tanks
            int healerPlayers = inputs[2]; // Healers
            int dpsPlayers = inputs[3]; // DPS
            int t1 = inputs[4]; // min time before instance finished
            int t2 = inputs[5]; // max time before instance finished

            printConfiguration(n, tankPlayers, healerPlayers, dpsPlayers, t1, t2);

            int numParties = getNumOfParties(tankPlayers, healerPlayers, dpsPlayers);
            int numPartiesLeft = numParties;
            int[] partyTimes = new int[numParties];

            Random random = new Random();
            for (int i = 0; i < numParties; i++) {
                partyTimes[i] = random.nextInt(t2 - t1 + 1) + t1;
            }

            List<Integer> p1Parties = new ArrayList<>();
            List<Integer> p2Parties = new ArrayList<>();

            // Assign parties to priority lists
            for (int time : partyTimes) {
                int priority = getPriority(t1, t2, time);
                if (priority == 1) {
                    p1Parties.add(time);
                } else {
                    p2Parties.add(time);
                }
            }

            DungeonManager manager = DungeonManager.getInstance(n, numParties);
            // Start dungeon instances
            for (int i = 1; i <= n; i++) {
                manager.startDungeonInstance(i, t1, t2);
            }

            int currentPriority = 1; // Start with priority 1
            // manager.printAvailableDungeons();
            while (numPartiesLeft > 0) {
                manager.printStatus();
                // get party priority
                int partyTime;

                if (currentPriority == 1 && !p1Parties.isEmpty()) {
                    partyTime = p1Parties.remove(0); // Get first priority 1 party and remove it
                } else if (!p2Parties.isEmpty()) {
                    partyTime = p2Parties.remove(0); // Get first priority 2 party and remove it
                } else if (!p1Parties.isEmpty()) {
                    partyTime = p1Parties.remove(0);
                } else {
                    break; // No more parties
                }
                int dungeonId = manager.getAvailableDungeon();
                Dungeon dungeon = manager.getDungeon(dungeonId);

                manager.printEnter(dungeonId);
                dungeon.startParty(partyTime);
                manager.printStatus();
                numPartiesLeft--;
                
                // Alternate priorities
                if (currentPriority == 1 && !p2Parties.isEmpty()) {
                    currentPriority = 2;
                } else if (currentPriority == 2 && !p1Parties.isEmpty()) {
                    currentPriority = 1;
                }
            }
            manager.shutdown();
            manager.printSummary();

            System.out.println("\nLeftover Players:");
            System.out.println(tankPlayers - numParties + " Tanks");
            System.out.println(healerPlayers - numParties + " Healers");
            System.out.println(dpsPlayers - (numParties * 3) + " DPS");
            
            System.out.println("\nThank you! :3");
        }
    }
}