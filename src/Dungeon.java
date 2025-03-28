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
    private int partyTime = 0;

    public Dungeon(int id) {
        this.id = id;
        
    }

    public void startInstance(int t1, int t2) {
        DungeonManager manager = DungeonManager.getInstance();
        boolean status = manager.getStatus();
        while (status) {
            // if there is a party, execute
            if (this.isActive() && this.partyTime != 0) {
                // System.out.println("[ENTERED] Dungeon " + this.id);
                int time = this.partyTime;

                // run time
                try {
                    // System.out.println("[SLEEP] Dungeon " + this.id + "for " + time + " seconds");
                    Thread.sleep(time * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Dungeon " + id + " interrupted.");
                    return;
                }
                // remove party from dungeon
                this.setIsActive(false);
                this.partiesServed++;
                this.totalTimeServed += time;
                this.partyTime = 0;
                manager.releaseDungeon(this.id);
                // System.out.println("[EXIT] Dungeon " + this.id);
            } else {
                // System.out.println("[WAIT] Dungeon " + this.id);
            }
            // no party, keep waiting
            status = manager.getStatus();
        }
    }

    public int getId() {
        return this.id;
    }

    public void startParty(int partyTime) {
        this.isActive = true;
        this.partyTime = partyTime;
    }

    public synchronized boolean isActive() {
        return this.isActive;
    }

    public synchronized void setIsActive(boolean val) {
        this.isActive = val;
    }
    
    public int getPartiesServed() {
        return this.partiesServed;
    }
    
    public int getTotalTimeServed() {
        return this.totalTimeServed;
    }
    
}
