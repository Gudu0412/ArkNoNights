package arknonights;

import processing.core.PApplet;
import java.awt.Image;

public class ShuOperator {
    private PApplet parent;

    // 坐标位置
    public float GRID_COL = 26.0f; 
    public float GRID_ROW = 11.0f;  
    
    // 人物贴图偏移（控制本体位置）
    public float visualOffsetX = 0.0f; 
    public float visualOffsetY = 0.0f; 

    // ==========================================
    // 💡 新增：血条专属偏移微调器！专门对付透明画布
    // ==========================================
    // 目前根据你的截图，我预设向右挪 45 像素，向上挪 50 像素。
    // 如果进去发现还差一点，你自己加减这两个数字就行！
    public float barOffsetX = 5.0f;  // 正数往右移，负数往左移
    public float barOffsetY = 45.0f; // 负数往上移，正数往下移

    // --- 核心属性 ---
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
            System.out.println("✨ [黍-技能] 释放：地生五谷！恢复 30% 生命值，当前 HP: " + (int)hp);
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
                
                // 💡 修复：血条坐标已与图片的顶部解绑！改用稳定中心点 (pixelY) + 独立微调偏移量
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

        parent.fill(35, 38, 43, 200);
        parent.rect(startX, y, fixedBarW, hpBarH, 1);
        parent.rect(startX, y + hpBarH + gap, fixedBarW, spBarH, 1);

        if (hp > maxHp * 0.5f) parent.fill(0, 162, 232); 
        else parent.fill(237, 28, 36);                  
        
        float currentHpW = PApplet.map(hp, 0, maxHp, 0, fixedBarW);
        parent.rect(startX, y, currentHpW, hpBarH, 1);

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