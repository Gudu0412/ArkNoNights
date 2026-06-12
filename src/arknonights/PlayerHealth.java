package arknonights;

import processing.core.PApplet;

public class PlayerHealth {
    private PApplet parent;
    
    // 💡 Core Attributes
    public float hp;
    public float maxHp;

    // 💡 Constructor: Set the baseline pool using maximum HP configuration
    public PlayerHealth(PApplet parent, float maxHp) {
        this.parent = parent;
        this.maxHp = maxHp;
        this.hp = maxHp; // Spawns with full HP by default
    }

    // 💡 Encapsulated Damage Handler
    public void takeDamage(float damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }

    // 💡 Encapsulated Fatal State Checker
    public boolean isDead() {
        return hp <= 0;
    }

    // 💡 Encapsulated Reset Handler (Used for hard resets or Medic healing bursts)
    public void reset() {
        hp = maxHp;
    }

    // 💡 Visual Rendering: Draw a localized dynamic status bar beneath the Operator's feet!
    public void display(float pixelX, float pixelY, float tileWidth) {
        if (isDead()) return; // Prevent rendering if the unit has retreated/defeated

        // Configure bounding frame width and height metrics
        float barWidth = tileWidth * 0.8f; 
        float barHeight = 4.0f;
        
        // Center the coordinate layout anchor directly beneath the unit model
        float drawX = pixelX - barWidth / 2.0f;
        float drawY = pixelY + tileWidth / 1.5f; 

        // 1. Render black translucent background track container
        parent.fill(0, 0, 0, 150);
        parent.noStroke();
        parent.rect(drawX, drawY, barWidth, barHeight);

        // 2. Map current remaining pool ratio against frame canvas scale
        float currentHpWidth = PApplet.map(hp, 0, maxHp, 0, barWidth);
        
        // 3. Dynamic Threshold Coloring: Healthy Green -> Warning Yellow -> Critical Red
        if (hp > maxHp * 0.5f) {
            parent.fill(0, 255, 0);   // Above 50% pool threshold: Green
        } else if (hp > maxHp * 0.2f) {
            parent.fill(255, 255, 0); // Above 20% pool threshold: Yellow
        } else {
            parent.fill(255, 0, 0);   // Danger status threshold: Red
        }
        
        parent.rect(drawX, drawY, currentHpWidth, barHeight);
    }
}