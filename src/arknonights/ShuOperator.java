package arknonights;

import processing.core.PApplet;
import java.awt.Image;

public class ShuOperator {
    private PApplet parent;

    // Grid Coordinates
    public float GRID_COL = 26.0f; 
    public float GRID_ROW = 11.0f;  
    
    // Character Texture Visual Offsets (Controls the raw base texture positioning)
    public float visualOffsetX = 0.0f; 
    public float visualOffsetY = 0.0f; 

    // =========================================================================
    // 💡 Status Bar Offset Adjusters: Dedicated to transparent frame canvas corrections
    // =========================================================================
    // Initial configuration presets to shift rightward and upward. 
    // You can fine-tune these values manually if alignment tweaks are needed!
    public float barOffsetX = 5.0f;  // Positive values shift right, negative values shift left
    public float barOffsetY = 45.0f; // Negative values shift upward, positive values shift downward

    // --- Core Attributes ---
    public float hp = 70.0f;      
    public float maxHp = 100.0f;
    public float sp = 0.0f;
    public float maxSp = 100.0f;
    public float spChargeSpeed = 0.4f; 

    public int deployStartTime = 0;
    public int deployAnimDuration = 1200; 

    public ShuOperator(PApplet parent) {
        this.parent = parent;
    }

    public void update(boolean isGamePlaying) {
        if (!isGamePlaying || hp <= 0) return;

        if (sp < maxSp) {
            sp += spChargeSpeed;
        } 
        
        if (sp >= maxSp) {
            float healAmount = maxHp * 0.3f; 
            hp += healAmount;
            if (hp > maxHp) hp = maxHp; 
            
            sp = 0.0f; 
            System.out.println("✨ [Shu-Skill] Activated: Shield of Grain! Regenerated 30% HP pool. Current HP: " + (int)hp);
        }
    }

    public void display(java.awt.Graphics2D g2d, Image shuGif, Image shuDefaultGif, 
                        float tileWidth, float tileHeight, float baseDrawWidth, 
                        float shuRatio, float shuDefaultRatio, int currentMillis, java.awt.image.ImageObserver observer) {
        
        float pixelX = (GRID_COL + 0.5f) * tileWidth + visualOffsetX;
        float pixelY = (GRID_ROW + 0.5f) * tileHeight + visualOffsetY;

        Image currentImg;
        float currentRatio;

        if (currentMillis - deployStartTime < deployAnimDuration) {
            currentImg = shuGif;       
            currentRatio = shuRatio;
        } else {
            currentImg = shuDefaultGif; 
            currentRatio = shuDefaultRatio;
        }

        if (currentImg != null && g2d != null) {
            float drawHeight = baseDrawWidth * currentRatio;
            float drawX = pixelX - baseDrawWidth / 2f;
            float drawY = pixelY - drawHeight / 2f;

            if (currentImg.getWidth(null) > 0) {
                g2d.drawImage(currentImg, (int) drawX, (int) drawY, (int) baseDrawWidth, (int) drawHeight, observer);
                
                // 💡 Status bars coordinate metrics decoupled from variable bounding boxes.
                // Uses the stabilized center anchor (pixelY) with absolute translation modifiers.
                drawStatusBars(pixelX + barOffsetX, pixelY + barOffsetY, tileWidth);
            }
        }
    }

    private void drawStatusBars(float x, float y, float tileWidth) {
        float fixedBarW = tileWidth * 1.2f; 
        float hpBarH = 5;                  
        float spBarH = 3;                  
        float gap = 2;                     
        float startX = x - fixedBarW / 2;

        parent.pushStyle();
        parent.noStroke();

        // Render backing tray containers
        parent.fill(35, 38, 43, 200);
        parent.rect(startX, y, fixedBarW, hpBarH, 1);
        parent.rect(startX, y + hpBarH + gap, fixedBarW, spBarH, 1);

        // HP Logic: Set cyan above threshold limit, flash crimson during dangerous states
        if (hp > maxHp * 0.5f) parent.fill(0, 162, 232); 
        else parent.fill(237, 28, 36);                  
        
        float currentHpW = PApplet.map(hp, 0, maxHp, 0, fixedBarW);
        parent.rect(startX, y, currentHpW, hpBarH, 1);

        // SP Logic Track: Vibrant Green
        parent.fill(34, 177, 76); 
        float currentSpW = PApplet.map(sp, 0, maxSp, 0, fixedBarW);
        parent.rect(startX, y + hpBarH + gap, currentSpW, spBarH, 1);

        parent.popStyle();
    }
    
    public void takeDamage(float dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }
}