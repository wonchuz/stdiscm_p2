/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DungeonManager {
    private static DungeonManager instance;
    private final ConcurrentLinkedQueue<Integer> availableDungeons;
    private final ConcurrentLinkedQueue<Integer> waitingParties = new ConcurrentLinkedQueue<>();;
    private final Map<Integer, Dungeon> dungeons;
    private static int numOfDungeons;

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
        System.out.println("[DG_ENTER] Party" + partyId + " entered getAvailableDungeon");
        this.waitingParties.offer(partyId); // add to waitingThreads List
        System.out.println("CURRENT WAITING PARTIES: " + this.waitingParties);

        while (!(partyId == waitingParties.peek() && !availableDungeons.isEmpty())) {
            try {
                //System.out.println("[DG_WAIT] Party" + partyId + " waiting getAvailableDungeon");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.waitingParties.poll();
        int dungeonId = this.availableDungeons.poll();
        System.out.println("[DG_GOT] Party" + partyId + " exits getAvailableDungeon and got dungeon " + dungeonId);
        System.out.println("CURRENT WAITING PARTIES: " + this.waitingParties);
        return dungeonId;
    }

    public Dungeon getDungeon(int id) {
        return dungeons.get(id);
    }

    public synchronized void releaseDungeon(int dungeonId) {
        availableDungeons.offer(dungeonId);
        notifyAll(); // Wake up waiting parties
    }    

    public void printAvailableDungeons() {
        System.out.println("AVAILABLE DUNGEONS: " + this.availableDungeons);
    }
}
