package arknonights;

import processing.core.PApplet;
import processing.core.PImage;
import java.io.File;
import javax.swing.ImageIcon; 
import java.awt.Image;
import java.awt.image.ImageObserver;

public class MySketch extends PApplet {

    Person player;
    
    Image gifRight; 
    Image gifLeft;  
    Image currentGif; 
    
    Image targetGifXX;  
    Image targetGifXXL; 
    Image currentTargetGif; 
    
    int enterTime;              
    boolean isSwitched = false; 
    
    // 【血量机制与离断触发锁】
    float targetHP = 100.0f;         
    boolean hasDealtDamage = false;  

    float baseTargetWidth = 140f; 
    float gifRatio = 1.0f;       
    
    float targetGifRatioXX = 1.0f;  
    float targetGifRatioXXL = 1.0f; 

    PImage bg; 

    int lastWidth;
    int lastHeight;

    boolean debugMode = false;

    private final ImageObserver stableObserver = new ImageObserver() {
        @Override
        public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
            return true;
        }
    };

    @Override
    public void settings() {
        size(1024, 461); 
    }

    @Override
    public void setup() {
        getSurface().setResizable(false);
        lastWidth = width;
        lastHeight = height;
        
        float startGridCol = 14.0f; 
        float startGridRow = 6.0f;  
        int startPixelX = Math.round(startGridCol * ((float) width / 48f));
        int startPixelY = Math.round(startGridRow * ((float) height / 26f));
        
        player = new Person(startPixelX, startPixelY);
        
        try {
            File bgFile = new File("Image/background.png");
            if (bgFile.exists()) {
                bg = loadImage(bgFile.getPath());
            } else {
                bg = loadImage(dataPath("../Image/background.png"));
            }
        } catch (Exception e) {
            System.out.println("未找到 background.png");
        }
        
        gifRight = loadAwtGif("Image/cj.gif");
        gifLeft = loadAwtGif("Image/jc.gif");
        currentGif = gifRight; 
        
        targetGifXX = loadAwtGif("Image/xx.gif");
        targetGifXXL = loadAwtGif("Image/xxl.gif"); 
        currentTargetGif = targetGifXX;
        
        enterTime = millis();
    }

    private Image loadAwtGif(String relativePath) {
        try {
            File file = new File(relativePath);
            String finalPath = file.exists() ? file.getPath() : dataPath("../" + relativePath);
            ImageIcon icon = new ImageIcon(finalPath);
            Image img = icon.getImage();
            int rawWidth = icon.getIconWidth();
            int rawHeight = icon.getIconHeight();
            if (rawWidth > 0) {
                if (relativePath.contains("cj")) {
                    gifRatio = (float) rawHeight / rawWidth;
                } 
                else if (relativePath.equals("Image/xx.gif")) {
                    targetGifRatioXX = (float) rawHeight / rawWidth;
                }
                else if (relativePath.equals("Image/xxl.gif")) {
                    targetGifRatioXXL = (float) rawHeight / rawWidth;
                }
            }
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void draw() {
        if (width != lastWidth || height != lastHeight) {
            float scaleX = (float) width / lastWidth;
            float scaleY = (float) height / lastHeight;
            player.x = (int) (player.x * scaleX);
            player.y = (int) (player.y * scaleY);
            lastWidth = width;
            lastHeight = height;
        }

        // 1. 同步最新血量给底层
        player.targetHP = this.targetHP;

        player.update(width, height);
        player.updateTileSize(width, height);

        if (bg != null) {
            imageMode(CORNER);
            image(bg, 0, 0, width, height);
        } else {
            background(240); 
        }
        
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();
        
        if (!isSwitched && (millis() - enterTime >= 1000)) {
            if (targetGifXXL != null) {
                currentTargetGif = targetGifXXL; 
                System.out.println("⏰ 变身：xxl 进场！");
            }
            isSwitched = true; 
        }
        
        // 2. 【伤害精度结算机制 + 命中后泄劲能量清除机制】
        if (isSwitched && targetHP > 0) {
            int playerCol = (int) (player.x / player.tileWidth);
            int playerRow = (int) (player.y / player.tileHeight);
            
            boolean isTouching = (playerRow >= 15 && playerRow <= 17) && (playerCol >= 21 && playerCol <= 27);
            boolean isPressingAnyKey = player.isUp || player.isDown || player.isLeft || player.isRight;
            
            if (isTouching && isPressingAnyKey) {
                if (!hasDealtDamage && player.isJustCollided) {
                    float realDamage = player.getDamage(); 
                    targetHP -= realDamage; 
                    if (targetHP < 0) targetHP = 0;
                    
                    System.out.println("💥 [爆发成功] 输出核心伤害: " + realDamage + " | xxl 剩余生命: " + targetHP);
                    
                    // 💡 【全新机制判定点】：一旦将这次高额的冲击伤害结算完毕
                    // 立刻调用底层重置方法，瞬间让主角将积攒的冲刺动能全部归零清空！
                    player.resetSpeedAndTimer();
                    
                    hasDealtDamage = true; // 打开离断锁，防止黏墙产生多帧连击
                }
            } else if (!isTouching) {
                // 完全拉开距离后，重置离断锁
                if (hasDealtDamage) {
                    hasDealtDamage = false;
                    System.out.println("🔄 已脱离碰撞，伤害和冲刺充能刷新！");
                }
            }
        }
        
        // 3. 渲染目标单位
        if (currentTargetGif != null && g2d != null && targetHP > 0) {
            float targetGridCol = (currentTargetGif == targetGifXX) ? 24.7f : 25.2f; 
            float targetGridRow = 15.0f; 

            float centerPixelX = targetGridCol * player.tileWidth;
            float centerPixelY = targetGridRow * player.tileHeight;
            float xxDrawWidth = baseTargetWidth * ((float) width / 1024f);
            float currentRatio = (currentTargetGif == targetGifXX) ? targetGifRatioXX : targetGifRatioXXL;
            float xxDrawHeight = xxDrawWidth * currentRatio;
            float xxX = centerPixelX - xxDrawWidth / 2f;
            float xxY = centerPixelY - xxDrawHeight / 2f;
            
            g2d.drawImage(currentTargetGif, (int)xxX, (int)xxY, (int)xxDrawWidth, (int)xxDrawHeight, stableObserver);
        }
        
        if (player.isLeft) {
            currentGif = gifLeft;
        } else if (player.isRight) {
            currentGif = gifRight;
        }
        
        if (currentGif != null && g2d != null) {
            int currentDrawWidth = (int) (baseTargetWidth * ((float) width / 1024f));
            int currentDrawHeight = (int) (currentDrawWidth * gifRatio);
            int drawX = player.x - currentDrawWidth / 2;
            int drawY = player.y - currentDrawHeight / 2;
            g2d.drawImage(currentGif, drawX, drawY, currentDrawWidth, currentDrawHeight, stableObserver);
            g.setModified(); 
        }

        if (isSwitched && targetHP <= 0) {
            fill(0, 255, 0);
            textSize(24);
            textAlign(CENTER, CENTER);
        }

        if (debugMode) {
            textSize(10); 
            for (int row = 0; row < player.MAP_MATRIX.length; row++) {
                for (int col = 0; col < player.MAP_MATRIX[row].length; col++) {
                    float gridX = col * player.tileWidth;
                    float gridY = row * player.tileHeight;
                    if (player.MAP_MATRIX[row][col] == 1) {
                        fill(255, 0, 0, 60);  
                    } else {
                        fill(0, 255, 0, 10);  
                    }
                    stroke(255, 255, 255, 40); 
                    rect(gridX, gridY, player.tileWidth, player.tileHeight);
                    fill(255, 255, 255, 120); 
                    textAlign(LEFT, TOP);     
                    text(String.valueOf(col + 1), gridX + 3, gridY + 2);
                }
            }
            fill(255, 255, 0);
            textSize(16);
            textAlign(LEFT, BASELINE); 
            text("FPS: " + (int)frameRate, 20, 30); 
            text("Player Pixel: (" + player.x + ", " + player.y + ")", 20, 50);
            text("Speed Multiplier: " + player.getCurrentSpeed(), 20, 70);
            text("Current ATK (Damage): " + (int)player.getDamage(), 20, 90);
            text("XXL Target HP: " + (int)targetHP + " / 100", 20, 110);
            text("Damage Locked Status: " + hasDealtDamage, 20, 130);
        }
    }

    @Override
    public void keyPressed() {
        if (keyCode == TAB) debugMode = !debugMode; 
        if (keyCode == UP    || key == 'w' || key == 'W') player.isUp = true;
        if (keyCode == DOWN  || key == 's' || key == 'S') player.isDown = true;
        if (keyCode == LEFT  || key == 'a' || key == 'A') player.isLeft = true;
        if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = true;
    }

    @Override
    public void keyReleased() {
        if (keyCode == UP    || key == 'w' || key == 'W') player.isUp = false;
        if (keyCode == DOWN  || key == 's' || key == 'S') player.isDown = false;
        if (keyCode == LEFT  || key == 'a' || key == 'A') player.isLeft = false;
        if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = false;
    }
}