package arknonights;

import processing.core.PApplet;
import java.awt.Image;

public class JzOperator {
    private PApplet parent;

    public final int GRID_COL = 23; 
    public final int GRID_ROW = 8;  

    public float sp = 0.0f;
    public float maxSp = 100.0f;
    public float spChargeSpeed = 0.25f; 

    public int status = 0;
    public int deployStartTime = 0; 
    
    // 💡 新增：引爆瞬间点火信号脉冲
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
                System.out.println("🛫 [JZ干员] 动作闭环！瞬间切回常态空战巡逻姿态。");
            }
            return;
        }

        if (status == 0) {
            if (sp < maxSp) {
                sp += spChargeSpeed;
                if (sp >= maxSp) {
                    sp = maxSp;
                    status = 1; 
                    System.out.println("⚡ [JZ干员] 充能满额 100！拦截雷达区域扩散就绪！");
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
                
                justBombed = true; // ⚡ 点火！瞬间拉高触发信号，告诉主类播声音！
                operatorHealth.takeDamage(10.0f); 
                System.out.println("💥 [雷达触发] 精准捕获！目标踏入 JZ 战术防区拦截范围！造成 10 点爆发伤害！");
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