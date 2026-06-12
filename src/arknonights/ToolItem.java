package arknonights;

import processing.core.PApplet;
import processing.core.PImage;

public class ToolItem {
    private PApplet parent;

    // 💡 Precise grid coordinate positioning (Mapping code indices 24, 5)
    public final int GRID_COL = 24;
    public final int GRID_ROW = 5;

    public boolean isPicked = false; // Tracking pick-up status state

    public ToolItem(PApplet parent) {
        this.parent = parent;
    }

    // Real-time intersection tracking to check if Shaw steps onto the item grid
    public void update(int playerCol, int playerRow, PlayerBuffManager buffSys) {
        if (isPicked) return;

        // If Shaw steps directly onto this specific grid block
        if (playerCol == GRID_COL && playerRow == GRID_ROW) {
            isPicked = true;
            if (buffSys != null) {
                buffSys.activateBuff(10, 1.3f); // Grants 10 Aegis blocks, scales attack damage by 1.3x
                System.out.println("🎁 [Tactical Supply] Shaw secured the tactical drop case! Acquired 10 Aegis shield charges, attack damage scaled up by 30%!");
            }
        }
    }

    // Render tactical drop asset texture on-screen
    public void display(PImage toolImg, float tileWidth, float tileHeight) {
        if (isPicked || toolImg == null) return;

        // Map target layout grid index directly into absolute pixel scales
        float drawX = GRID_COL * tileWidth;
        float drawY = GRID_ROW * tileHeight;

        parent.imageMode(PApplet.CORNER);
        // Force dimensions to fit perfectly within a single localized map tile matrix segment
        parent.image(toolImg, drawX, drawY, tileWidth, tileHeight);
    }
}