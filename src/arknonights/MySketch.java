package arknonights;

import processing.core.PApplet;
import processing.core.PImage;
import java.io.File;
import javax.swing.ImageIcon; 
import java.awt.Image;
import java.awt.image.ImageObserver;

// 导入原生音频流
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class MySketch extends PApplet {

    // 游戏状态控制变量：0 - 菜单主界面，1 - 战斗游戏关卡，2 - 关卡胜利通关页面
    int gameState = 0; 

    // 胜利页面的两阶段子状态变量（0 - 展示 Finish.png，1 - 展示 RealFinish.png）
    int victoryPhase = 0; 

    Menu mainMenu;
    Person player;
    
    // 原生音频播放器载体
    Clip bgmClip; 
    
    Image gifRight; 
    Image gifLeft;  
    Image currentGif; 
    
    Image targetGifXX;  
    Image targetGifXXL; 
    Image currentTargetGif; 
    
    // 结算静态图片载体
    PImage finishPng; 
    // 最终结算战报图载体
    PImage realFinishPng; 

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
        
        mainMenu = new Menu(this);
        
        float startGridCol = 14.0f; 
        float startGridRow = 6.0f;  
        int startPixelX = Math.round(startGridCol * ((float) width / 48f));
        int startPixelY = Math.round(startGridRow * ((float) height / 26f));
        
        player = new Person(startPixelX, startPixelY);
        
        // 1. 加载地图背景
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
        
        // 2. 智能格式转换音频流预加载
        try {
            File musicFile = new File("Music/map1.wav");
            String musicPath = musicFile.exists() ? musicFile.getPath() : dataPath("../Music/map1.wav");
            File audioFile = new File(musicPath);

            if (audioFile.exists()) {
                AudioInputStream baseInputStream = AudioSystem.getAudioInputStream(audioFile);
                AudioFormat baseFormat = baseInputStream.getFormat();
                
                AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16, 
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false 
                );
                
                AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, baseInputStream);
                DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
                bgmClip = (Clip) AudioSystem.getLine(info);
                bgmClip.open(decodedInputStream);
            }
        } catch (Exception e) {
            System.out.println("🚨 音频流加载异常。");
        }
        
        // 3. 预加载所有干员/怪物动图
        gifRight = loadAwtGif("Image/cj.gif");
        gifLeft = loadAwtGif("Image/jc.gif");
        currentGif = gifRight; 
        
        targetGifXX = loadAwtGif("Image/xx.gif");
        targetGifXXL = loadAwtGif("Image/xxl.gif"); 
        currentTargetGif = targetGifXX;
        
        // 4. 预加载第一阶段结算图 Finish.png
        try {
            File fPngFile = new File("Image/Finish.png");
            if (fPngFile.exists()) {
                finishPng = loadImage(fPngFile.getPath());
                System.out.println("✅ 第一阶段结算图 Finish.png 加载成功！");
            } else {
                finishPng = loadImage(dataPath("../Image/Finish.png"));
            }
        } catch (Exception e) {
            System.out.println("🚨 警告：未找到 Finish.png");
        }

        // 5. 预加载第二阶段最终战报图 RealFinish.png
        try {
            File rfPngFile = new File("Image/RealFinish.png");
            if (rfPngFile.exists()) {
                realFinishPng = loadImage(rfPngFile.getPath());
                System.out.println("✅ 第二阶段战报图 RealFinish.png 加载成功！");
            } else {
                realFinishPng = loadImage(dataPath("../Image/RealFinish.png"));
            }
        } catch (Exception e) {
            System.out.println("🚨 警告：在 Image 文件夹里没找到 RealFinish.png 文件！");
        }
        
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
        if (gameState == 0) {
            mainMenu.display();
        } else if (gameState == 1) {
            drawGamePlay();
        } else if (gameState == 2) {
            // 保持定格关卡画面，并在最上层覆盖结算大图
            drawGamePlay(); 
            drawVictoryOverlay(); 
        }
    }

    // ================= 绘制核心战斗游戏关卡 =================
    private void drawGamePlay() {
        if (width != lastWidth || height != lastHeight) {
            float scaleX = (float) width / lastWidth;
            float scaleY = (float) height / lastHeight;
            player.x = (int) (player.x * scaleX);
            player.y = (int) (player.y * scaleY);
            lastWidth = width;
            lastHeight = height;
        }

        player.targetHP = this.targetHP;
        
        if (gameState == 1) {
            player.update(width, height);
            player.updateTileSize(width, height);

            int playerCol = (int) (player.x / player.tileWidth);  
            int playerRow = (int) (player.y / player.tileHeight); 
            
            if ((playerRow >= 16 && playerRow <= 20) && (playerCol == 35 || playerCol == 36)) {
                gameState = 2;       
                victoryPhase = 0;   
                
                if (bgmClip != null && bgmClip.isRunning()) {
                    bgmClip.stop();
                }
                System.out.println("🎉 OPERATION COMPLETE - 触达蓝门！");
                return; 
            }
        }

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
                    
                    player.resetSpeedAndTimer();
                    hasDealtDamage = true; 
                }
            } else if (!isTouching) {
                if (hasDealtDamage) {
                    hasDealtDamage = false;
                    System.out.println("🔄 已脱离碰撞，伤害和冲刺充能刷新！");
                }
            }
        }
        
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

        if (debugMode) {
            drawDebugInfo();
        }
    }

    // 💡 ================= 核心重构：将两张结算图完全拉伸铺满视窗 =================
    private void drawVictoryOverlay() {
        // 铺上一层略微暗淡的半透明底层（也可以去掉这行，取决于你图片自带不自带黑底）
        fill(0, 0, 0, 100);
        noStroke();
        rect(0, 0, width, height);
        
        // 💡 采用左上角对齐模式，方便计算完全拉伸
        imageMode(PApplet.CORNER); 
        
        if (victoryPhase == 0) {
            // 💡 第一阶段：让 Finish.png 完美铺满整个窗口 (0, 0, width, height)
            if (finishPng != null) {
                image(finishPng, 0, 0, width, height);
            } else {
                textAlign(CENTER, CENTER); textSize(40); fill(255, 200, 0);
                text("OPERATION COMPLETE\n(点击屏幕任意处继续)", width / 2f, height / 2f);
            }
        } 
        else if (victoryPhase == 1) {
            // 💡 第二阶段：让 RealFinish.png 完美铺满整个窗口 (0, 0, width, height)
            if (realFinishPng != null) {
                image(realFinishPng, 0, 0, width, height);
            } else {
                textAlign(CENTER, CENTER); textSize(40); fill(60, 220, 100);
                text("STATS SETTLEMENT PANEL", width / 2f, height / 2f);
            }
        }
    }

    private void drawDebugInfo() {
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

    @Override
    public void mousePressed() {
        if (gameState == 0) {
            if (mainMenu.isStartClicked()) {
                gameState = 1; 
                enterTime = millis(); 
                if (bgmClip != null && !bgmClip.isRunning()) {
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                }
                System.out.println("🚀 行动开始！");
            }
        } 
        else if (gameState == 2) {
            if (victoryPhase == 0) {
                victoryPhase = 1; 
                System.out.println("🔄 展现最终满屏战报 (RealFinish)...");
            }
        }
    }

    @Override
    public void keyPressed() {
        if (keyCode == TAB) debugMode = !debugMode; 
        
        if (gameState == 1) {
            if (keyCode == UP    || key == 'w' || key == 'W') player.isUp = true;
            if (keyCode == DOWN  || key == 's' || key == 'S') player.isDown = true;
            if (keyCode == LEFT  || key == 'a' || key == 'A') player.isLeft = true;
            if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = true;
        }
    }

    @Override
    public void keyReleased() {
        if (gameState == 1) {
            if (keyCode == UP    || key == 'w' || key == 'W') player.isUp = false;
            if (keyCode == DOWN  || key == 's' || key == 'S') player.isDown = false;
            if (keyCode == LEFT  || key == 'a' || key == 'A') player.isLeft = false;
            if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = false;
        }
    }
}