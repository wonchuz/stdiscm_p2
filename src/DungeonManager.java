/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
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

    private DungeonManager(int n) {
        this.availableDungeons = new ConcurrentLinkedQueue<>();
        this.numOfDungeons = n;

        for (int i = 1; i <= numOfDungeons; i++) {
            availableDungeons.offer(i);
        }
        for (int i = 1; i <= numOfDungeons; i++) {
            dungeons.put(i, new Dungeon(i));
        }
    
        this.executor = Executors.newFixedThreadPool(n);
    }

    public void shutdown() {
        this.partiesLeft = false;
        this.executor.shutdown();

        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Shutdown interrupted.");
        }
    }
    
    public void startDungeonInstance(int dungeonId, int t1, int t2) {
        Dungeon dungeon = dungeons.get(dungeonId);
        if (dungeon != null && !dungeon.isActive()) {
            executor.submit(() -> dungeon.startInstance(t1, t2));
        }
    }


    public static DungeonManager getInstance(int n) {
        if (instance == null) { // First check (no locking)
            synchronized (DungeonManager.class) {
                if (instance == null) { // Second check (inside lock)
                    instance = new DungeonManager(n);
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
    
    public synchronized Integer getAvailableDungeon() {
        return this.availableDungeons.poll();
    }

    public Dungeon getDungeon(int id) {
        return dungeons.get(id);
    }

    public synchronized void releaseDungeon(int dungeonId) {
        this.availableDungeons.offer(dungeonId);
    }

    public void printAvailableDungeons() {
        System.out.println("AVAILABLE DUNGEONS: " + this.availableDungeons);
    }

    public synchronized void printStatus() {
        System.out.println("Current status of all available instances:");
        for (int i = 1; i <= this.numOfDungeons; i++) {
            Dungeon dungeon = dungeons.get(i);
            boolean isActive = dungeon.isActive();
            String status = "empty";
            if (isActive) {
                status = "active";
            }
            System.out.println("Dungeon " + i + " = " + status);
        }
    }

    public void printSummary() {
        System.out.println("Dungeon summary:");
        for (int i = 1; i <= this.numOfDungeons; i++) {
            Dungeon dungeon = dungeons.get(i);
            System.out.println("Dungeon " + i + " = " + dungeon.getPartiesServed() + " parties and " + dungeon.getTotalTimeServed() + " seconds");
        }
    }

    public synchronized boolean getStatus() {
        return this.partiesLeft;
    }
}
