/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;

import java.util.Random;

public class Dungeon {
    private final int id;
    private boolean isActive = false;
    private int partiesServed = 0;
    private int totalTimeServed = 0;

    public Dungeon(int id) {
        this.id = id;
        
    }

    public void startInstance(int t1, int t2) {
        DungeonManager manager = DungeonManager.getInstance();
        boolean enter = false;
        while (manager.getStatus()) {
            // if there is a party, execute
            synchronized (this) {
                if (this.isActive) {
                    enter = true;
                }
            }
            if (enter) {
                // manager.printStatus();
                // System.out.println("[ENTERED] Dungeon " + this.id);
                int time = new Random().nextInt(t2 - t1 + 1) + t1;

                // run time
                try {
                    // System.out.println("[SLEEP] Dungeon " + this.id + "for " + time + " seconds");
                    Thread.sleep(time * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Dungeon " + id + " interrupted.");
                    return;
                }
                synchronized (this) {
                    // remove party from dungeon
                    this.isActive = false;
                    enter = false;
                    this.partiesServed++;
                    this.totalTimeServed += time;
                    manager.releaseDungeon(this.id);
                    // manager.printStatus();
                }
                // System.out.println("[EXIT] Dungeon " + this.id);
            } else {
                // System.out.println("[WAIT] Dungeon " + this.id);
            }
            // no party, keep waiting
        }
    }

    public int getId() {
        return this.id;
    }

    public synchronized void startParty() {
        this.isActive = true;
    }

    public synchronized boolean isActive() {
        return this.isActive;
    }
    
    public synchronized int getPartiesServed() {
        return this.partiesServed;
    }
    
    public synchronized int getTotalTimeServed() {
        return this.totalTimeServed;
    }
    
}
