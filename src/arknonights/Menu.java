package arknonights;

import processing.core.PApplet;
import processing.core.PImage;

public class Menu {
    private PApplet parent; // Reference to MySketch to invoke Processing drawing functions
    private PImage menuBg;  // Main menu background image

    // --- Button Hotspot Attributes ---
    private float btnX, btnY, btnW, btnH;

    // Constructor
    public Menu(PApplet parent) {
        this.parent = parent;
        
        // Button hotspot alignment (Precisely covers 'Start Awakening' under 1024x461 resolution)
        this.btnW = 180;
        this.btnH = 54;
        this.btnX = parent.width / 2f - this.btnW / 2f; 
        this.btnY = 306; 

        // Load main menu background image
        try {
            java.io.File mBgFile = new java.io.File("Image/Mainmenu.png");
            if (mBgFile.exists()) {
                this.menuBg = parent.loadImage(mBgFile.getPath());
                System.out.println("✅ [Menu] Main menu background loaded successfully: Mainmenu.png");
            } else {
                this.menuBg = parent.loadImage(parent.dataPath("../Image/Mainmenu.png"));
            }
        } catch (Exception e) {
            System.out.println("🚨 [Warning] Menu class failed to locate Mainmenu.png in Image folder.");
        }
    }

    // 💡 Renders the menu interface elements
    public void display() {
        if (menuBg != null) {
            parent.imageMode(PApplet.CORNER);
            parent.image(menuBg, 0, 0, parent.width, parent.height);
        } else {
            parent.background(20, 24, 28); 
        }

        // Checks if mouse is currently hovering within the 'Start Awakening' bounding box
        boolean isHover = parent.mouseX >= btnX && parent.mouseX <= btnX + btnW 
                       && parent.mouseY >= btnY && parent.mouseY <= btnY + btnH;

        // Draw hover shimmering glow feedback
        if (isHover) {
            parent.fill(255, 255, 255, 40); // Overlays soft white holographic glow
            parent.stroke(255, 255, 255, 100);
            parent.strokeWeight(1);
            parent.cursor(PApplet.HAND);    // Change cursor to interactive hand pointer
        } else {
            parent.noFill(); 
            parent.noStroke();
            parent.cursor(PApplet.ARROW);   // Fallback to default arrow cursor
        }
        
        // Render invisible logical collision rectangle
        parent.rect(btnX, btnY, btnW, btnH, 2); 
    }

    // 💡 Interface exposed for external click event interception
    public boolean isStartClicked() {
        return parent.mouseX >= btnX && parent.mouseX <= btnX + btnW 
            && parent.mouseY >= btnY && parent.mouseY <= btnY + btnH;
    }
}