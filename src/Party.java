/*
 * STDISCM P2 - Looking for Group Synchronization
 * By: Jaeme Rebano S14
 */
package src;
import java.util.Random;

public class Party implements Runnable {
    private final int partyId;
    private final int t1, t2;

    public Party(int partyId, int t1, int t2) {
        this.partyId = partyId;
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public void run() {
        DungeonManager dungeonManager = DungeonManager.getInstance();
        
        System.out.println("[PARTY_CALL] Party" + this.partyId + " is calling getAvailableDungeon");
        int dungeonId = dungeonManager.getAvailableDungeon(this.partyId);
        
        Dungeon dungeon = dungeonManager.getDungeon(dungeonId);
        int time = new Random().nextInt(t2 - t1 + 1) + t1;
        
        System.out.println("[PARTY_ENTER] Party " + this.partyId + " entered Dungeon " + dungeonId + " for " + time + " seconds.");
        dungeon.startInstance(time);

        System.out.println("[PARTY_EXIT] Party " + this.partyId + " finished and releasedDungeon " + dungeonId);
        dungeonManager.releaseDungeon(dungeonId); // Release dungeon
        dungeonManager.printAvailableDungeons();
    }
}
