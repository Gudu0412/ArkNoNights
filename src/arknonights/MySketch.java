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
    BlockManager blockSys;     

    Image gifRight; 
    Image gifLeft;
    Image cjAttackGif;      
    Image currentGif; 
    
    Image targetGifXX;  
    Image targetGifXXL; 
    Image xxAttackGif;       
    Image currentTargetGif; 

    Image jzGif;
    Image jzDefaultGif;
    float jzGifRatio = 1.0f;
    float jzDefaultGifRatio = 1.0f;
    int jzDeployStartTime = 0;
    int jzDeployAnimDuration = 1000;

    PImage bg; 
    PImage finishPng; 
    PImage realFinishPng; 

    int enterTime;              
    boolean isSwitched = false; 
    boolean hasDealtDamage = false;  
    boolean hasPlayedDieSFX = false; 

    float baseTargetWidth = 140f; 
    
    float moveGifRatio = 1.0f;       
    float attackGifRatio = 1.0f;     
    float targetGifRatioXX = 1.0f;  
    float targetGifRatioXXL = 1.0f; 
    float targetGifRatioXXAttack = 1.0f; 

    PlayerHealth operatorHealth; 

    int lastWidth;
    int lastHeight;

    boolean debugMode = false;

    int lastAttackTime = 0;       
    int attackCooldown = 500;    

    boolean isAttackingAnimation = false; 

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
                    blockSys = new BlockManager(MySketch.this); 
                    
                    operatorHealth = new PlayerHealth(MySketch.this, 30.0f);
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
                    float startGridRow = 16.0f; 
                    int startPixelX = Math.round(startGridCol * ((float) width / 48f));
                    int startPixelY = Math.round(startGridRow * ((float) height / 26f));
                    player = new Person(startPixelX, startPixelY);
                    
                    loadingStatus = "正在解密音频系统流...";
                    audioSys.loadBGM();
                    audioSys.loadLevelMapBGM();
                    audioSys.loadSFX();
                    audioSys.loadBossDieSFX();
                    Thread.sleep(100);
                    
                    loadingStatus = "正在构建干员及敌方单位动画序列...";
                    gifRight = loadAwtGif("Image/cj.gif");
                    gifLeft = loadAwtGif("Image/jc.gif");
                    cjAttackGif = loadAwtGif("Image/cj-attack.gif");
                    currentGif = gifRight; 

                    targetGifXX = loadAwtGif("Image/xx.gif");
                    targetGifXXL = loadAwtGif("Image/xxl.gif"); 
                    xxAttackGif = loadAwtGif("Image/xx-attack.gif"); 
                    currentTargetGif = targetGifXX;

                    jzGif = loadAwtGif("Operator/jz.gif");
                    jzDefaultGif = loadAwtGif("Operator/jz-default.gif");

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
                if (relativePath.equals("Image/cj.gif") || relativePath.equals("Image/jc.gif")) {
                    moveGifRatio = (float) rawHeight / rawWidth;
                } else if (relativePath.equals("Image/cj-attack.gif")) {
                    attackGifRatio = (float) rawHeight / rawWidth;
                } else if (relativePath.equals("Image/xx.gif")) {
                    targetGifRatioXX = (float) rawHeight / rawWidth;
                } else if (relativePath.equals("Image/xxl.gif")) {
                    targetGifRatioXXL = (float) rawHeight / rawWidth;
                } else if (relativePath.equals("Image/xx-attack.gif")) {
                    targetGifRatioXXAttack = (float) rawHeight / rawWidth; 
                } else if (relativePath.equals("Operator/jz.gif")) {
                    jzGifRatio = (float) rawHeight / rawWidth;
                } else if (relativePath.equals("Operator/jz-default.gif")) {
                    jzDefaultGifRatio = (float) rawHeight / rawWidth;
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
                noFill(); 
                stroke(0, 162, 232); 
                strokeWeight(3);
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
            if (!operatorHealth.isDead() && blockSys != null && !blockSys.isPlayerBlocked) {
                player.update(width, height);
            }

            player.updateTileSize(width, height);

            int playerCol = (int) (player.x / player.tileWidth);  
            int playerRow = (int) (player.y / player.tileHeight); 
            
            if (!operatorHealth.isDead() && (playerRow >= 16 && playerRow <= 20) && (playerCol == 35 || playerCol == 36)) {
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
        
        if (blockSys != null) {
            blockSys.updateBlockStatus(player, bossStatus.hp);
        }
        
        if (bossStatus.hp <= 0) {
            if (!hasPlayedDieSFX) {
                audioSys.playBossDieSFX(); 
                hasPlayedDieSFX = true;    
            }

            if (blockSys != null) blockSys.isPlayerBlocked = false;   
            if (blockSys != null) blockSys.currentlyBlocked = 0;  

            isAttackingAnimation = false; 
        }
        
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();
        
        if (!isSwitched && (millis() - enterTime >= 1000)) {
            if (targetGifXXL != null) {
                currentTargetGif = targetGifXXL; 
            }
            isSwitched = true; 
        }
        
        if (isSwitched && bossStatus.hp > 0) {
            int currentMillis = millis();
            
            if (blockSys != null && blockSys.isPlayerBlocked) {
                if (!isAttackingAnimation && !operatorHealth.isDead()) {
                    currentGif = cjAttackGif; 
                    isAttackingAnimation = true; 
                }
                
                if (currentMillis - lastAttackTime >= attackCooldown && !operatorHealth.isDead()) {
                    float realDamage = player.getDamage(); 
                    bossStatus.hp -= realDamage; 

                    if (bossStatus.hp < 0) bossStatus.hp = 0;
                    
                    operatorHealth.takeDamage(5.0f); 
                    
                    player.resetSpeedAndTimer(); 
                    lastAttackTime = currentMillis; 
                }
            } else {
                if (isAttackingAnimation) {
                    isAttackingAnimation = false; 
                    currentGif = gifRight; 
                }
                
                int playerCol = (int) (player.x / player.tileWidth);
                int playerRow = (int) (player.y / player.tileHeight);

                boolean isTouching = (playerRow >= 15 && playerRow <= 17) && (playerCol >= 21 && playerCol <= 27);
                boolean isPressingAnyKey = player.isUp || player.isDown || player.isLeft || player.isRight;
                
                if (isTouching && isPressingAnyKey && !operatorHealth.isDead()) {
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

                lastAttackTime = currentMillis - attackCooldown + 200; 
            }
        }
        
        if (currentTargetGif != null && g2d != null && bossStatus.hp > 0) {
            float bodyGridCol = (currentTargetGif == targetGifXX) ? 23.2f : 23.7f; 
            float targetGridRow = 15.0f; 

            float bodyPixelX = bodyGridCol * player.tileWidth;
            float bodyPixelY = targetGridRow * player.tileHeight;
            
            float xxDrawWidth = baseTargetWidth * ((float) width / 1024f);
            
            Image finalBossGif = currentTargetGif;
            float currentRatio = (currentTargetGif == targetGifXX) ? targetGifRatioXX : targetGifRatioXXL;
            
            if (blockSys != null && blockSys.isPlayerBlocked && xxAttackGif != null && !operatorHealth.isDead()) {
                finalBossGif = xxAttackGif;
                currentRatio = targetGifRatioXXAttack;
            }
            
            float xxDrawHeight = xxDrawWidth * currentRatio;
            float bodyX = bodyPixelX - xxDrawWidth / 2f;
            float bodyY = bodyPixelY - xxDrawHeight / 2f;
            
            g2d.drawImage(
                finalBossGif, 
                (int) bodyX, 
                (int) bodyY, 
                (int) xxDrawWidth, 
                (int) xxDrawHeight, 
                stableObserver
            );

            bossStatus.display(player.tileWidth, player.tileHeight, 23.3f, 17.5f);

            if (jzGif != null || jzDefaultGif != null) {
                float jzGridCol = 24.2f;
                float jzGridRow = 9.5f;

                boolean useDefault = millis() - jzDeployStartTime >= jzDeployAnimDuration;

                Image currentJzGif = useDefault && jzDefaultGif != null ? jzDefaultGif : jzGif;
                float currentJzRatio = useDefault && jzDefaultGif != null ? jzDefaultGifRatio : jzGifRatio;

                if (currentJzGif != null) {
                    float jzPixelX = jzGridCol * player.tileWidth;
                    float jzPixelY = jzGridRow * player.tileHeight;

                    float jzDrawWidth = baseTargetWidth * ((float) width / 1024f);
                    float jzDrawHeight = jzDrawWidth * currentJzRatio;

                    float jzDrawX = jzPixelX - jzDrawWidth / 2f;
                    float jzDrawY = jzPixelY - jzDrawHeight / 2f;

                    g2d.drawImage(
                        currentJzGif,
                        (int) jzDrawX,
                        (int) jzDrawY,
                        (int) jzDrawWidth,
                        (int) jzDrawHeight,
                        stableObserver
                    );

                    g.setModified();
                }
            }
        }
        
        if (!isAttackingAnimation) {
            if (player.isLeft) {
                currentGif = gifLeft;
            } else if (player.isRight) {
                currentGif = gifRight;
            }
        }
        
        if (currentGif != null && g2d != null && !operatorHealth.isDead()) {
            float activeRatio = isAttackingAnimation ? attackGifRatio : moveGifRatio;

            int currentDrawWidth = (int) (baseTargetWidth * ((float) width / 1024f));
            int currentDrawHeight = (int) (currentDrawWidth * activeRatio);

            int drawX = player.x - currentDrawWidth / 2;
            int drawY = player.y - currentDrawHeight / 2;

            g2d.drawImage(currentGif, drawX, drawY, currentDrawWidth, currentDrawHeight, stableObserver);
            g.setModified(); 
            
            operatorHealth.display(player.x, player.y, player.tileWidth);
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
                textAlign(CENTER, CENTER); 
                textSize(40); 
                fill(255, 200, 0);
                text("OPERATION COMPLETE\n(点击屏幕任意处继续)", width / 2f, height / 2f);
            }
        } else if (victoryPhase == 1) {
            if (realFinishPng != null) {
                image(realFinishPng, 0, 0, width, height);
            } else {
                textAlign(CENTER, CENTER); 
                textSize(40); 
                fill(60, 220, 100);
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

        text("FPS: " + (int) frameRate, 20, 30); 
        text("Player Pixel: (" + player.x + ", " + player.y + ")", 20, 50);
        
        if (blockSys != null) {
            text("阻挡状态: " + (blockSys.isPlayerBlocked ? "🔒 YES (独立阻挡管理器锁定)" : "🏃 NO (可自由活动)"), 20, 70);
            text("当前已被占有名额: " + blockSys.currentlyBlocked + " / " + blockSys.maxBlockCount, 20, 90);
        }

        text("XXL Target HP: " + (int) bossStatus.hp + " / 100", 20, 110);
        text("阿消生命值 (HP): " + (int) operatorHealth.hp + " / " + (int) operatorHealth.maxHp + (operatorHealth.isDead() ? " ❌ (干员撤退)" : ""), 20, 130);
        
        int remain = attackCooldown - (millis() - lastAttackTime);
        text("自动交战倒计时: " + (remain < 0 ? 0 : remain) + " ms", 20, 150);

        text("JZ固定部署坐标: 第24.2列，第9.5行附近", 20, 170);
    }

    @Override
    public void mousePressed() {
        if (gameState == -1) return;

        if (gameState == 0) {
            if (mainMenu.isStartClicked()) {
                gameState = 1; 
                audioSys.playLevelMapBGM();
            }
        } else if (gameState == 1) {
            if (stageSelector.isTD1Clicked(mouseX, mouseY)) {
                gameState = 2;              
                enterTime = millis();
                jzDeployStartTime = millis();

                hasPlayedDieSFX = false; 
                
                if (operatorHealth != null) operatorHealth.reset();
                
                if (blockSys != null) blockSys.reset();

                lastAttackTime = millis(); 
                isAttackingAnimation = false; 

                audioSys.stopLevelMapBGM();
                audioSys.playBGM();
                audioSys.playCombinedStartSFX();
            }
        } else if (gameState == 3) {
            if (victoryPhase == 0) {
                victoryPhase = 1; 
            }
        }
    }

    @Override
    public void keyPressed() {
        if (keyCode == TAB) debugMode = !debugMode; 
        
        if (gameState == 2) {
            if (keyCode == UP || key == 'w' || key == 'W') player.isUp = true;
            if (keyCode == DOWN || key == 's' || key == 'S') player.isDown = true;
            if (keyCode == LEFT || key == 'a' || key == 'A') player.isLeft = true;
            if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = true;
        }
    }

    @Override
    public void keyReleased() {
        if (gameState == 2) {
            if (keyCode == UP || key == 'w' || key == 'W') player.isUp = false;
            if (keyCode == DOWN || key == 's' || key == 'S') player.isDown = false;
            if (keyCode == LEFT || key == 'a' || key == 'A') player.isLeft = false;
            if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = false;
        }
    }
}