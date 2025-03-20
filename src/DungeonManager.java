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
import java.util.concurrent.TimeUnit;

public class DungeonManager {
    private static DungeonManager instance;
    private final ConcurrentLinkedQueue<Integer> availableDungeons;
    private final Map<Integer, Dungeon> dungeons = new HashMap<>();
    private final int numOfDungeons;
    private final ExecutorService executor;
    private boolean partiesLeft = true;
    private final CountDownLatch latch;
    private final Object statusLock = new Object();   // Separate lock for dungeons
    private final Object latchLock = new Object();     // Separate lock for latch operations


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
    }

    public void shutdown() {
        this.partiesLeft = false;

        try {
            System.out.println("Waiting for all dungeon parties to finish...");
            latch.await();  // Wait until all parties finish
            System.out.println("All parties cleared dungeons. Shutting down.");
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
        return availableDungeons.poll();
    }

    public Dungeon getDungeon(int id) {
        return dungeons.get(id);
    }

    public void releaseDungeon(int dungeonId) {
        availableDungeons.offer(dungeonId);
            System.out.println("Dungeon released: " + dungeonId);

        synchronized (latchLock) {    // Lock only on latch operations
            latch.countDown();
            System.out.println("Latch counted down");
            System.out.println("Parties remaining: " + latch.getCount());
        }
    }

    public void printAvailableDungeons() {
        System.out.println("AVAILABLE DUNGEONS: " + this.availableDungeons);
    }

    public void printStatus() {
        System.out.println("Current status of all available instances:");
        for (int i = 1; i <= this.numOfDungeons; i++) {
            Dungeon dungeon = dungeons.get(i);
            synchronized (dungeon) {
                boolean isActive = dungeon.isActive();
                String status = "empty";
                if (isActive) {
                    status = "active";
                }
                System.out.println("Dungeon " + i + " = " + status);
            }
        }
    }

    public void printSummary() {
        int partiesServed = 0;
        System.out.println("Dungeon Instance Summary:");
        for (int i = 1; i <= this.numOfDungeons; i++) {
            Dungeon dungeon = dungeons.get(i);
            System.out.println("Dungeon " + i + " served " + dungeon.getPartiesServed() + " parties and total time is " + dungeon.getTotalTimeServed() + " seconds");
            partiesServed += dungeon.getPartiesServed();
        }
        System.out.println("Number of parties: " + partiesServed);
    }

    public boolean getStatus() {
        synchronized (statusLock) {
            return this.partiesLeft;
        }
    }
}
