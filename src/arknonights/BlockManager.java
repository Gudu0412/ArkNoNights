package arknonights;

import processing.core.PApplet;

public class BlockManager {
    private PApplet parent;

    // --- Core Blocking Data ---
    public int maxBlockCount = 3;       // Max block capacity for operators
    public int currentlyBlocked = 0;     // Current number of blocked targets
    public boolean isPlayerBlocked = false; // Whether the player is currently blocked and locked

    // ==========================================
    // 💡 Target Tracking Radar
    // 0 = Free state, 1 = Blocked by XXL (Boss), 2 = Blocked by Shu
    // ==========================================
    public int blockedTarget = 0; 

    public BlockManager(PApplet parent) {
        this.parent = parent;
    }

    // 💡 State Machine Update: Monitors both Boss and Shu health simultaneously
    public void updateBlockStatus(Person player, float bossHp, float shuHp) {
        // Elimination Release Check
        if (bossHp <= 0 && blockedTarget == 1) {
            releaseBlock();
            return;
        }
        if (shuHp <= 0 && blockedTarget == 2) {
            releaseBlock();
            return;
        }

        int playerCol = (int) (player.x / player.tileWidth);
        int playerRow = (int) (player.y / player.tileHeight);

        // Zone 1: XXL Physical Control Area (Col 21~27, Row 15~17)
        boolean touchingXXL = (bossHp > 0) && (playerRow >= 15 && playerRow <= 17) && (playerCol >= 21 && playerCol <= 27);
        
        // Zone 2: Shu Physical Control Area (Around Col 26, Row 14.5)
        boolean touchingShu = (shuHp > 0) && (playerRow >= 13 && playerRow <= 15) && (playerCol >= 25 && playerCol <= 27);

        if (touchingXXL) {
            if (blockedTarget != 1) {
                isPlayerBlocked = true;
                blockedTarget = 1; // Locked onto XXL
                currentlyBlocked++;
                System.out.println("🔒 [Block Manager] Intercepted by XXL's massive mass!");
            }
        } 
        else if (touchingShu) {
            if (blockedTarget != 2) {
                isPlayerBlocked = true;
                blockedTarget = 2; // Locked onto Shu
                currentlyBlocked++;
                System.out.println("🌿 [Block Manager] Intercepted by Shu's defensive line! Entering target dummy training mode!");
            }
        } 
        else {
            releaseBlock();
        }
    }

    private void releaseBlock() {
        if (isPlayerBlocked) {
            isPlayerBlocked = false;
            blockedTarget = 0;
            if (currentlyBlocked > 0) currentlyBlocked--;
            System.out.println("🔓 [Block Manager] Escaped control zone. Block slot released.");
        }
    }

    // 💡 Stage Reset Method
    public void reset() {
        isPlayerBlocked = false;
        currentlyBlocked = 0;
        blockedTarget = 0;
    }
}