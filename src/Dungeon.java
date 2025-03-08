/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;

public class Dungeon {
    private final int id;
    private boolean isActive = false;
    private int partiesServed = 0;
    private int totalTimeServed = 0;

    public Dungeon(int id) {
        this.id = id;
    }

    public synchronized void startInstance(int time) {
        this.isActive = true;
        this.partiesServed++;
    
        try {
            Thread.sleep(time * 1000); // Convert seconds to milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    
        this.totalTimeServed += time;
        this.isActive = false;
    }
    

    public int getId() {
        return this.id;
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
