/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DungeonManager {
    private static DungeonManager instance;
    private final ConcurrentLinkedQueue<Integer> availableDungeons;
    private final Map<Integer, Dungeon> dungeons = new HashMap<>();
    private final int numOfDungeons;
    private final ExecutorService executor;
    private boolean partiesLeft = true;
    private final CountDownLatch latch;
    private final Semaphore dungeonSemaphore;
    private final Semaphore printSemaphore = new Semaphore(1);


    private DungeonManager(int n, int numParties) {
        this.availableDungeons = new ConcurrentLinkedQueue<>();
        this.numOfDungeons = n;

        for (int i = 1; i <= numOfDungeons; i++) {
            availableDungeons.offer(i);
        }
        for (int i = 1; i <= numOfDungeons; i++) {
            dungeons.put(i, new Dungeon(i));
        }
    
        this.executor = Executors.newFixedThreadPool(n);
        this.latch = new CountDownLatch(numParties);
        this.dungeonSemaphore = new Semaphore(n);
    }

    public void shutdown() {
        try {
            // System.out.println("Waiting for all dungeon parties to finish...");
            latch.await();  // Wait until all parties finish
            this.partiesLeft = false;
            System.out.println("All parties cleared dungeons.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Shutdown interrupted.");
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("Forcing shutdown...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
    
    public void startDungeonInstance(int dungeonId, int t1, int t2) {
        Dungeon dungeon = dungeons.get(dungeonId);
        if (dungeon != null && !dungeon.isActive()) {
            executor.submit(() -> dungeon.startInstance(t1, t2));
        }
    }


    public static DungeonManager getInstance(int n, int numParties) {
        if (instance == null) { // First check (no locking)
            synchronized (DungeonManager.class) {
                if (instance == null) { // Second check (inside lock)
                    instance = new DungeonManager(n, numParties);
                }
            }
        }
        return instance;
    }

    public static DungeonManager getInstance() {
        if (instance == null) { // First check (no locking)
            return null;
        }
        return instance;
    }
    
    public Integer getAvailableDungeon() {
        try {
            // System.out.println("Try to acquire sem.");
            dungeonSemaphore.acquire(); // Blocks until a dungeon is available
            // System.out.println("Got sem.");
            return availableDungeons.poll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted.");
            return null;
        }
    }

    public Dungeon getDungeon(int id) {
        return dungeons.get(id);
    }

    public void releaseDungeon(int dungeonId) {
        try {
            availableDungeons.offer(dungeonId);
            System.out.println("Dungeon released: " + dungeonId);
            this.printStatus();
        } finally {
            dungeonSemaphore.release();
            latch.countDown();
        }
    }

    public void printAvailableDungeons() {
        System.out.println("[AVAIL DUNGEONS] " + this.availableDungeons);
        System.out.println("Available dungeons count: " + this.availableDungeons.size());
    }

    public void printStatus() {
        try {
            printSemaphore.acquire();
            System.out.println("\n============================");
            System.out.println("Current status of all available instances:");
            for (int i = 1; i <= this.numOfDungeons; i++) {
                Dungeon dungeon = dungeons.get(i);
                boolean isActive = dungeon.isActive();
                String status = isActive ? "active" : "empty";
                System.out.println("Dungeon " + i + " = " + status);
            }
            System.out.println("============================\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Status printing was interrupted.");
        } finally {
            printSemaphore.release();
        }
    }
    
    public void printEnter(int dungeonId) {
        try {
            printSemaphore.acquire();
            System.out.println("Party will enter Dungeon " + dungeonId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Status printing was interrupted.");
        } finally {
            printSemaphore.release();
        }
    }

    public void printSummary() {
        int partiesServed = 0;
        System.out.println("\n============================");
        System.out.println("Dungeon Instances Summary:");
        for (int i = 1; i <= this.numOfDungeons; i++) {
            Dungeon dungeon = dungeons.get(i);
            System.out.println("Dungeon " + i + " served " + dungeon.getPartiesServed() + " parties and total time is " + dungeon.getTotalTimeServed() + " seconds");
            partiesServed += dungeon.getPartiesServed();
        }
        System.out.println("\nTotal # of parties: " + partiesServed);
        System.out.println("============================");
    }

    public synchronized boolean getStatus() {
        return this.partiesLeft;
    }
}
