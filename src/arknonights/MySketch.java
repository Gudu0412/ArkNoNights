package arknonights;

import processing.core.PApplet;
import processing.core.PImage;
import java.io.File;
import javax.swing.ImageIcon; 
import java.awt.Image;
import java.awt.image.ImageObserver;

public class MySketch extends PApplet {

    volatile int gameState = -1; 
    volatile String loadingStatus = "正在初始化罗德岛系统引擎..."; 

    int victoryPhase = 0; 

    Menu mainMenu;
    Person player;
    
    SkillHealth bossStatus;
    MapManager mapRenderer;    
    StageSelect stageSelector; 
    AudioManager audioSys;     

    Image gifRight; 
    Image gifLeft;  
    Image currentGif; 
    
    Image targetGifXX;  
    Image targetGifXXL; 
    Image currentTargetGif; 

    PImage bg; 
    PImage finishPng; 
    PImage realFinishPng; 

    int enterTime;              
    boolean isSwitched = false; 
    boolean hasDealtDamage = false;  
    
    // 💡 新增状态开关：防止死亡音效在 draw 循环里每帧鬼畜重复触发
    boolean hasPlayedDieSFX = false; 

    float baseTargetWidth = 140f; 
    float gifRatio = 1.0f;       
    float targetGifRatioXX = 1.0f;  
    float targetGifRatioXXL = 1.0f; 

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
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadingStatus = "正在装载主界面与核心 UI 组件...";
                    mainMenu = new Menu(MySketch.this);
                    bossStatus = new SkillHealth(MySketch.this);
                    mapRenderer = new MapManager(MySketch.this);
                    stageSelector = new StageSelect(MySketch.this);
                    audioSys = new AudioManager(MySketch.this); 
                    Thread.sleep(100); 
                    
                    loadingStatus = "正在挂载 60 帧【动态选关大地图】环境...";
                    mapRenderer.loadFrames();
                    Thread.sleep(100);
                    
                    loadingStatus = "正在加载【战斗关卡静态网格地图】...";
                    File bgFile = new File("Image/background.png");
                    if (bgFile.exists()) bg = loadImage(bgFile.getPath());
                    else bg = loadImage(dataPath("../Image/background.png"));
                    Thread.sleep(100);
                    
                    loadingStatus = "正在部署干员初始作战坐标系...";
                    float startGridCol = 14.0f; 
                    float startGridRow = 6.0f;  
                    int startPixelX = Math.round(startGridCol * ((float) width / 48f));
                    int startPixelY = Math.round(startGridRow * ((float) height / 26f));
                    player = new Person(startPixelX, startPixelY);
                    
                    loadingStatus = "正在解密音频系统流...";
                    audioSys.loadBGM();
                    audioSys.loadLevelMapBGM();
                    audioSys.loadSFX();
                    // 💡 加载全新的 Boss 击杀死亡音效到内存
                    audioSys.loadBossDieSFX();
                    Thread.sleep(100);
                    
                    loadingStatus = "正在构建干员及敌方单位动画序列...";
                    gifRight = loadAwtGif("Image/cj.gif");
                    gifLeft = loadAwtGif("Image/jc.gif");
                    currentGif = gifRight; 
                    targetGifXX = loadAwtGif("Image/xx.gif");
                    targetGifXXL = loadAwtGif("Image/xxl.gif"); 
                    currentTargetGif = targetGifXX;
                    Thread.sleep(100);
                    
                    loadingStatus = "正在渲染最终行动结算面板...";
                    File fPngFile = new File("Image/Finish.png");
                    if (fPngFile.exists()) finishPng = loadImage(fPngFile.getPath());
                    else finishPng = loadImage(dataPath("../Image/Finish.png"));

                    File rfPngFile = new File("Image/RealFinish.png");
                    if (rfPngFile.exists()) realFinishPng = loadImage(rfPngFile.getPath());
                    else realFinishPng = loadImage(dataPath("../Image/RealFinish.png"));
                    
                    loadingStatus = "系统自检完成，Neuro-connection established.";
                    Thread.sleep(300);
                    
                    gameState = 0; 
                    
                } catch (Exception e) {
                    loadingStatus = "🚨 加载发生异常，请检查控制台。";
                    e.printStackTrace();
                }
            }
        }).start();
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
                } else if (relativePath.equals("Image/xx.gif")) {
                    targetGifRatioXX = (float) rawHeight / rawWidth;
                } else if (relativePath.equals("Image/xxl.gif")) {
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
        switch (gameState) {
            case -1:
                background(20); 
                fill(0, 162, 232); 
                textAlign(CENTER, CENTER);
                try {
                    processing.core.PFont f = createFont("Microsoft YaHei", 18);
                    textFont(f);
                } catch(Exception e) {
                    textSize(18);
                }
                text(loadingStatus, width / 2f, height / 2f);
                noFill(); stroke(0, 162, 232); strokeWeight(3);
                arc(width / 2f, height / 2f - 40, 40, 40, radians(frameCount * 5 % 360), radians(frameCount * 5 % 360 + 90));
                break;
            case 0:
                if (mainMenu != null) mainMenu.display();
                break;
            case 1:
                if (mapRenderer != null) mapRenderer.display(gameState, width, height);
                if (stageSelector != null) stageSelector.display();
                break;
            case 2:
                drawGamePlay();
                break;
            case 3:
                drawGamePlay(); 
                drawVictoryOverlay(); 
                break;
        }
    }

    private void drawGamePlay() {
        if (width != lastWidth || height != lastHeight) {
            float scaleX = (float) width / lastWidth;
            float scaleY = (float) height / lastHeight;
            player.x = (int) (player.x * scaleX);
            player.y = (int) (player.y * scaleY);
            lastWidth = width;
            lastHeight = height;
        }

        player.targetHP = bossStatus.hp;
        
        if (gameState == 2) {
            player.update(width, height);
            player.updateTileSize(width, height);

            int playerCol = (int) (player.x / player.tileWidth);  
            int playerRow = (int) (player.y / player.tileHeight); 
            
            if ((playerRow >= 16 && playerRow <= 20) && (playerCol == 35 || playerCol == 36)) {
                gameState = 3;       
                victoryPhase = 0;   
                
                audioSys.stopBGM();
                audioSys.playVictorySFX();
                return; 
            }
        }

        if (bg != null) {
            imageMode(CORNER);
            image(bg, 0, 0, width, height);
        } else {
            background(50);
        }

        bossStatus.update(gameState == 2);
        
        // 💡 核心逻辑：检测 XXL 死亡。当血量清空且音效未播放过时触发
        if (bossStatus.hp <= 0 && !hasPlayedDieSFX) {
            audioSys.playBossDieSFX(); // 瞬发死亡音效
            hasPlayedDieSFX = true;    // 锁死开关，防止下一帧循环再次触发
        }
        
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();
        
        if (!isSwitched && (millis() - enterTime >= 1000)) {
            if (targetGifXXL != null) {
                currentTargetGif = targetGifXXL; 
            }
            isSwitched = true; 
        }
        
        if (isSwitched && bossStatus.hp > 0) {
            int playerCol = (int) (player.x / player.tileWidth);
            int playerRow = (int) (player.y / player.tileHeight);
            boolean isTouching = (playerRow >= 15 && playerRow <= 17) && (playerCol >= 21 && playerCol <= 27);
            boolean isPressingAnyKey = player.isUp || player.isDown || player.isLeft || player.isRight;
            
            if (isTouching && isPressingAnyKey) {
                if (!hasDealtDamage && player.isJustCollided) {
                    float realDamage = player.getDamage(); 
                    bossStatus.hp -= realDamage; 
                    if (bossStatus.hp < 0) bossStatus.hp = 0;
                    player.resetSpeedAndTimer();
                    hasDealtDamage = true; 
                }
            } else if (!isTouching) {
                if (hasDealtDamage) {
                    hasDealtDamage = false;
                }
            }
        }
        
        if (currentTargetGif != null && g2d != null && bossStatus.hp > 0) {
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
            bossStatus.display(player.tileWidth, player.tileHeight);
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

    private void drawVictoryOverlay() {
        fill(0, 0, 0, 100);
        noStroke();
        rect(0, 0, width, height);
        
        imageMode(PApplet.CORNER); 
        
        if (victoryPhase == 0) {
            if (finishPng != null) {
                image(finishPng, 0, 0, width, height);
            } else {
                textAlign(CENTER, CENTER); textSize(40); fill(255, 200, 0);
                text("OPERATION COMPLETE\n(点击屏幕任意处继续)", width / 2f, height / 2f);
            }
        } 
        else if (victoryPhase == 1) {
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
        text("XXL Target HP: " + (int)bossStatus.hp + " / 100", 20, 110);
        text("Damage Locked Status: " + hasDealtDamage, 20, 130);
    }

    @Override
    public void mousePressed() {
        if (gameState == -1) return;

        if (gameState == 0) {
            if (mainMenu.isStartClicked()) {
                gameState = 1; 
                audioSys.playLevelMapBGM();
            }
        } 
        else if (gameState == 1) {
            if (stageSelector.isTD1Clicked(mouseX, mouseY)) {
                gameState = 2;              
                enterTime = millis();       
                
                // 💡 重置击杀音效状态开关，保证下次进游戏还能正常触发
                hasPlayedDieSFX = false; 
                
                audioSys.stopLevelMapBGM();
                audioSys.playBGM();
                audioSys.playCombinedStartSFX();
            }
        }
        else if (gameState == 3) {
            if (victoryPhase == 0) {
                victoryPhase = 1; 
            }
        }
    }

    @Override
    public void keyPressed() {
        if (keyCode == TAB) debugMode = !debugMode; 
        
        if (gameState == 2) {
            if (keyCode == UP    || key == 'w' || key == 'W') player.isUp = true;
            if (keyCode == DOWN  || key == 's' || key == 'S') player.isDown = true;
            if (keyCode == LEFT  || key == 'a' || key == 'A') player.isLeft = true;
            if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = true;
        }
    }

    @Override
    public void keyReleased() {
        if (gameState == 2) {
            if (keyCode == UP    || key == 'w' || key == 'W') player.isUp = false;
            if (keyCode == DOWN  || key == 's' || key == 'S') player.isDown = false;
            if (keyCode == LEFT  || key == 'a' || key == 'A') player.isLeft = false;
            if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = false;
        }
    }
}