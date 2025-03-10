/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DungeonManager {
    private static DungeonManager instance;
    private final ConcurrentLinkedQueue<Integer> availableDungeons;
    private final Map<Integer, Dungeon> dungeons;
    private static int numOfDungeons;

    private final Lock lock = new ReentrantLock(); // Explicit lock

    private DungeonManager(int n) {
        this.availableDungeons = new ConcurrentLinkedQueue<>();
        numOfDungeons = n;

        for (int i = 1; i <= numOfDungeons; i++) {
            availableDungeons.offer(i);
        }
        System.out.println("[AVAIL DUNGEONS] " + availableDungeons);
        this.dungeons = new HashMap<>();
        for (int i = 0; i < numOfDungeons; i++) {
            dungeons.put(i + 1, new Dungeon(i + 1));
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
    
    public synchronized int getAvailableDungeon(int partyId) {
        System.out.println("[DG_TRY] Party" + partyId + " enters getAvailableDungeon and tries to acquire semaphore");
        
        // Wait until a dungeon is available
        if (this.availableDungeons.isEmpty()) { 
            System.out.println("[DG_WAIT] Party" + partyId + " is waiting");
            System.out.println("[AVAIL DUNGEONS] " + availableDungeons);
            try {
                wait();  // Wait until notified by another thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        }
        int dungeonId = this.availableDungeons.poll();
        System.out.println("[DG_GOT] Party" + partyId + " exits getAvailableDungeon and got dungeon " + dungeonId);
        return dungeonId;
    }

    public Dungeon getDungeon(int id) {
        return dungeons.get(id);
    }

    public synchronized void releaseDungeon(int dungeonId) {
        System.out.println("[PARTY_EXIT] Party will release dungeon " + dungeonId);
        this.availableDungeons.offer(dungeonId);
    
        // Notify one of the waiting threads (if any) that a dungeon is available
        notify();  // Or use notifyAll() if you want to wake up all waiting threads
    
        System.out.println("[PARTY_EXIT] Party releasedDungeon " + dungeonId);
    }

    public void printAvailableDungeons() {
        System.out.println("AVAILABLE DUNGEONS: " + this.availableDungeons);
    }
}
