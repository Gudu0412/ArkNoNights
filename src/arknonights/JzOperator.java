package arknonights;

import processing.core.PApplet;
import java.awt.Image;

public class JzOperator {
    private PApplet parent;

    public final int GRID_COL = 23; 
    public final int GRID_ROW = 8;  

    public float sp = 0.0f;
    public final float maxSp = 100.0f;
    public float spChargeSpeed = 0.25f; 

    public int status = 0;
    public int deployStartTime = 0; 
    
    // 💡 Signal Pulse for Ignition SFX Triggering
    public boolean justBombed = false; 

    private int bombStartTime = 0;
    private final int bombDuration = 600; 

    public JzOperator(PApplet parent) {
        this.parent = parent;
    }

    public void update(int playerCol, int playerRow, PlayerHealth operatorHealth, int currentMillis) {
        if (status == 2) {
            if (currentMillis - bombStartTime >= bombDuration) {
                status = 0;
                sp = 0.0f; 
                System.out.println("🛫 [JZ Operator] Action loop completed! Returning to default aerial patrol cruise.");
            }
            return;
        }

        if (status == 0) {
            if (sp < maxSp) {
                sp += spChargeSpeed;
                if (sp >= maxSp) {
                    sp = maxSp;
                    status = 1; 
                    System.out.println("⚡ [JZ Operator] Charge maximum reached! Interception radar zone deployment ready!");
                }
            }
        }

        if (status == 1) {
            boolean isPlayerSteppedIn = 
                (playerCol >= 22 && playerCol <= 24) && 
                (playerRow >= 7 && playerRow <= 10);

            if (isPlayerSteppedIn && !operatorHealth.isDead()) {
                status = 2; 
                bombStartTime = currentMillis;
                
                justBombed = true; // Ignite! Pulses the trigger signal to let MySketch play sound
                operatorHealth.takeDamage(10.0f); 
                System.out.println("💥 [Radar Triggered] Precision lock! Target entered JZ tactical interception perimeter! Dealt 10 burst damage!");
            }
        }
    }

    public void display(java.awt.Graphics2D g2d, Image jzSkillGif, Image jzDefaultGif, 
                        float tileWidth, float tileHeight, float jzDrawWidth, 
                        float jzSkillRatio, float jzDefaultGifRatio, java.awt.image.ImageObserver observer) {
        
        float jzPixelX = 24.2f * tileWidth;
        float jzPixelY = 9.5f * tileHeight;

        Image currentImg = jzDefaultGif;
        float currentRatio = jzDefaultGifRatio;

        if (status == 2) {
            currentImg = jzSkillGif; 
            currentRatio = jzSkillRatio;
        }

        if (currentImg != null && g2d != null) {
            float jzDrawHeight = jzDrawWidth * currentRatio;
            
            float jzDrawX = jzPixelX - jzDrawWidth / 2f;
            float jzDrawY = jzPixelY - jzDrawHeight / 2f;

            g2d.drawImage(currentImg, (int) jzDrawX, (int) jzDrawY, (int) jzDrawWidth, (int) jzDrawHeight, observer);
            
            if (status != 2) {
                parent.rectMode(PApplet.CORNER);
                parent.fill(0, 0, 0, 180);
                parent.rect(jzPixelX - 20, jzDrawY - 8, 40, 3);
                
                float currentSpW = PApplet.map(sp, 0, maxSp, 0, 40);
                if (status == 1) parent.fill(255, 215, 0); 
                else parent.fill(0, 191, 255);            
                parent.rect(jzPixelX - 20, jzDrawY - 8, currentSpW, 3);
            }
        }
    }
}