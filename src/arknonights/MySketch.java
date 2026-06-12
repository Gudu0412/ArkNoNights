package arknonights;

import processing.core.PApplet;
import processing.core.PImage;
import java.io.File;
import javax.swing.ImageIcon; 
import java.awt.Image;
import java.awt.image.ImageObserver;

public class MySketch extends PApplet {

    volatile int gameState = -1; 
    volatile String loadingStatus = "Initializing Rhodes Island System Engine..."; 

    int victoryPhase = 0; 
    
    // 💡 Complete Tutorial State Machine: 0 means not in tutorial, 1~8 correspond to the 8 phases of battle guidance
    int inGameTutorialStep = 0;

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

    Image xxSkillA;
    Image xxSkillB;
    Image xxSkillC;
    Image xxCurrentSkillGif; 
    
    float targetGifRatioXXSkillA = 1.0f;
    float targetGifRatioXXSkillB = 1.0f;
    float targetGifRatioXXSkillC = 1.0f;

    @Override
    public void keyReleased() {
        if (gameState == 2) {
            if (keyCode == UP    || key == 'w' || key == 'W') player.isUp = false;
            if (keyCode == DOWN  || key == 's' || key == 'S') player.isDown = false;
            if (keyCode == LEFT  || key == 'a' || key == 'A') player.isLeft = false;
            if (keyCode == RIGHT || key == 'd' || key == 'D') player.isRight = false;
        }
    }
    JzOperator jzOperator; 
    Image jzSkillGif;        
    Image jzDefaultGif;
    float jzSkillRatio = 1.0f; 
    float jzDefaultGifRatio = 1.0f;

    ShuOperator shuOperator;
    Image shuGif;
    Image shuDefaultGif;
    float shuRatio = 1.0f;
    float shuDefaultRatio = 1.0f;

    Image m3Gif;
    float m3Ratio = 1.0f;

    ToolItem tacticalTool;
    PlayerBuffManager buffManager;
    PImage toolTexture;

    PImage bg; 
    PImage finishPng; 
    PImage realFinishPng; 

    int enterTime;              
    boolean isSwitched = false; 
    boolean hasDealtDamage = false;  
    boolean hasPlayedDieSFX = false; 

    boolean hasPlayedSkillSFX = false; 

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
                    loadingStatus = "Loading main menu and core UI components...";
                    mainMenu = new Menu(MySketch.this);
                    bossStatus = new SkillHealth(MySketch.this);
                    mapRenderer = new MapManager(MySketch.this);
                    stageSelector = new StageSelect(MySketch.this);
                    audioSys = new AudioManager(MySketch.this); 
                    blockSys = new BlockManager(MySketch.this); 
                    
                    operatorHealth = new PlayerHealth(MySketch.this, 30.0f);
                    
                    jzOperator = new JzOperator(MySketch.this);
                    shuOperator = new ShuOperator(MySketch.this);
                    
                    buffManager = new PlayerBuffManager(MySketch.this);
                    tacticalTool = new ToolItem(MySketch.this);
                    
                    Thread.sleep(100); 
                    
                    loadingStatus = "Mounting 60FPS dynamic selection map environment...";
                    mapRenderer.loadFrames();
                    Thread.sleep(100);
                    
                    loadingStatus = "Loading combat level grid map and item assets...";
                    File bgFile = new File("Image/background.png");
                    if (bgFile.exists()) bg = loadImage(bgFile.getPath());
                    else bg = loadImage(dataPath("../Image/background.png"));
                    
                    File toolFile = new File("Image/Tool.png");
                    if (toolFile.exists()) toolTexture = loadImage(toolFile.getPath());
                    else toolTexture = loadImage(dataPath("../Image/Tool.png"));
                    
                    Thread.sleep(100);
                    
                    loadingStatus = "Deploying Operator initial combat coordinates...";
                    float startGridCol = 14.0f; 
                    float startGridRow = 16.0f; 
                    int startPixelX = Math.round(startGridCol * ((float) width / 48f));
                    int startPixelY = Math.round(startGridRow * ((float) height / 26f));
                    player = new Person(startPixelX, startPixelY);
                    
                    loadingStatus = "Decrypting audio system streams...";
                    audioSys.loadBGM();
                    audioSys.loadLevelMapBGM();
                    audioSys.loadSFX();
                    audioSys.loadBossDieSFX();
                    Thread.sleep(100);
                    
                    loadingStatus = "Building animation sequences for Operators and enemy units...";
                    gifRight = loadAwtGif("Image/cj.gif");
                    gifLeft = loadAwtGif("Image/jc.gif");
                    cjAttackGif = loadAwtGif("Image/cj-attack.gif"); 
                    currentGif = gifRight; 

                    targetGifXX = loadAwtGif("Image/xx.gif");
                    targetGifXXL = loadAwtGif("Image/xxl.gif"); 
                    xxAttackGif = loadAwtGif("Image/xx-attack.gif"); 
                    currentTargetGif = targetGifXX;

                    xxSkillA = loadAwtGif("Image/xx-skill-A.gif");
                    xxSkillB = loadAwtGif("Image/xx-skill-B.gif");
                    xxSkillC = loadAwtGif("Image/xx-skill-C.gif");
                    xxCurrentSkillGif = xxSkillA; 

                    jzSkillGif = loadAwtGif("Operator/jz-skill.gif");
                    jzDefaultGif = loadAwtGif("Operator/jz-default.gif");
                    
                    shuGif = loadAwtGif("Operator/Shu.gif");
                    shuDefaultGif = loadAwtGif("Operator/Shu-default.gif");
                    
                    m3Gif = loadAwtGif("Operator/M3.gif");
                    
                    Thread.sleep(100);
                    
                    loadingStatus = "Rendering final mission stats settlement panel...";
                    File fPngFile = new File("Image/Finish.png");
                    if (fPngFile.exists()) finishPng = loadImage(fPngFile.getPath());
                    else finishPng = loadImage(dataPath("../Image/Finish.png"));

                    File rfPngFile = new File("Image/RealFinish.png");
                    if (rfPngFile.exists()) realFinishPng = loadImage(rfPngFile.getPath());
                    else realFinishPng = loadImage(dataPath("../Image/RealFinish.png"));
                    
                    gameState = 0; 
                    
                } catch (Exception e) {
                    loadingStatus = "🚨 Exception occurred during loading. Please check console.";
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Image loadAwtGif(String relativePath) {
        try {
            File file = new File(relativePath);
            String finalPath = file.exists() ? file.getPath() : dataPath("../" + relativePath);
            
            if (!new File(finalPath).exists() && (relativePath.toLowerCase().contains("shu") || relativePath.toLowerCase().contains("m3"))) {
                String altPath = relativePath.replace("Operator/", "Image/");
                if (new File(altPath).exists()) finalPath = altPath;
                else if (new File(dataPath("../" + altPath)).exists()) finalPath = dataPath("../" + altPath);
            }

            ImageIcon icon = new ImageIcon(finalPath);
            Image img = icon.getImage();
            int rawWidth = icon.getIconWidth();
            int rawHeight = icon.getIconHeight(); 
            
            if (rawWidth > 0) {
                String lowerPath = relativePath.toLowerCase();
                if (lowerPath.contains("cj.gif") || lowerPath.contains("jc.gif")) {
                    moveGifRatio = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("cj-attack.gif")) {
                    attackGifRatio = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("xx.gif")) {
                    targetGifRatioXX = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("xxl.gif")) {
                    targetGifRatioXXL = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("xx-attack.gif")) {
                    targetGifRatioXXAttack = (float) rawHeight / rawWidth; 
                } else if (lowerPath.contains("xx-skill-a.gif")) {
                    targetGifRatioXXSkillA = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("xx-skill-b.gif")) {
                    targetGifRatioXXSkillB = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("xx-skill-c.gif")) {
                    targetGifRatioXXSkillC = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("jz-skill.gif")) {
                    jzSkillRatio = (float) rawHeight / rawWidth; 
                } else if (lowerPath.contains("jz-default.gif")) {
                    jzDefaultGifRatio = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("shu.gif")) {
                    shuRatio = (float) rawHeight / rawWidth; 
                } else if (lowerPath.contains("shu-default.gif")) {
                    shuDefaultRatio = (float) rawHeight / rawWidth;
                } else if (lowerPath.contains("m3.gif")) {
                    m3Ratio = (float) rawHeight / rawWidth; 
                }
            } else {
                System.out.println("🚨 [Warning] Asset loaded as empty space, check path -> " + finalPath);
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
                drawM3MainMenuTutorial(); 
                break;
            case 1:
                if (mapRenderer != null) mapRenderer.display(gameState, width, height);
                if (stageSelector != null) stageSelector.display();
                drawM3MapTutorial();
                break;
            case 2:
                drawGamePlay();
                break;
            case 3:
                drawGamePlay(); 
                drawVictoryOverlay(); 
                break;
            case 4:
                drawGameOverOverlay();
                break;
        }
    }

    private void drawM3MainMenuTutorial() {
        if (m3Gif == null) return;
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();
        float m3DrawWidth = baseTargetWidth * 1.2f * ((float) width / 1024f); 
        float m3DrawHeight = m3DrawWidth * m3Ratio;
        float m3X = width * 0.40f; 
        float m3Y = height * 0.85f - m3DrawHeight / 2f;
        g2d.drawImage(m3Gif, (int)m3X, (int)m3Y, (int)m3DrawWidth, (int)m3DrawHeight, stableObserver);
        
        float boxWidth = 350f * ((float) width / 1024f);
        float boxHeight = 55f * ((float) height / 461f);
        float boxX = m3X - boxWidth + m3DrawWidth * 0.15f; 
        float boxY = m3Y + m3DrawHeight * 0.1f;
        
        pushStyle();
        fill(25, 30, 35, 230); stroke(0, 162, 232); strokeWeight(2);
        rectMode(CORNER);
        rect(boxX, boxY, boxWidth, boxHeight, 8);
        fill(25, 30, 35, 230); noStroke(); 
        triangle(boxX + boxWidth - 2, boxY + 15, boxX + boxWidth + 14, boxY + 25, boxX + boxWidth - 2, boxY + 35);
        stroke(0, 162, 232); strokeWeight(2);
        line(boxX + boxWidth, boxY + 15, boxX + boxWidth + 14, boxY + 25);
        line(boxX + boxWidth + 14, boxY + 25, boxX + boxWidth, boxY + 35);
        fill(255); textAlign(LEFT, CENTER);
        try { textFont(createFont("Microsoft YaHei", 18)); } catch(Exception e) { textSize(18); }
        text("Welcome back, Doctor. Click here to start.", boxX + 20, boxY + boxHeight / 2f - 2);
        popStyle();
    }

    private void drawM3MapTutorial() {
        if (m3Gif == null) return;
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();
        float m3DrawWidth = baseTargetWidth * 1.1f * ((float) width / 1024f); 
        float m3DrawHeight = m3DrawWidth * m3Ratio;
        float m3X = width * 0.18f; 
        float m3Y = height * 0.52f - m3DrawHeight / 2f;
        g2d.drawImage(m3Gif, (int)m3X, (int)m3Y, (int)m3DrawWidth, (int)m3DrawHeight, stableObserver);
        
        float boxWidth = 260f * ((float) width / 1024f);
        float boxHeight = 50f * ((float) height / 461f);
        float boxX = m3X + m3DrawWidth * 0.85f; 
        float boxY = m3Y + m3DrawHeight * 0.15f;
        
        pushStyle();
        fill(25, 30, 35, 230); stroke(0, 162, 232); strokeWeight(2);
        rectMode(CORNER);
        rect(boxX, boxY, boxWidth, boxHeight, 8);
        fill(25, 30, 35, 230); noStroke();
        triangle(boxX + 2, boxY + 15, boxX - 12, boxY + 25, boxX + 2, boxY + 35);
        stroke(0, 162, 232); strokeWeight(2);
        line(boxX, boxY + 15, boxX - 12, boxY + 25);
        line(boxX - 12, boxY + 25, boxX, boxY + 35);
        fill(255); textAlign(LEFT, CENTER);
        try { textFont(createFont("Microsoft YaHei", 18)); } catch(Exception e) { textSize(18); }
        text("Doctor, please click here to start.", boxX + 20, boxY + boxHeight / 2f - 2);
        popStyle();
    }

    private void drawGamePlay() {
        if (gameState == 2 && operatorHealth.isDead()) {
            gameState = 4;
            audioSys.stopBGM(); 
            return; 
        }

        if (width != lastWidth || height != lastHeight) {
            float scaleX = (float) width / lastWidth;
            float scaleY = (float) height / lastHeight;
            player.x = (int) (player.x * scaleX);
            player.y = (int) (player.y * scaleY);
            lastWidth = width;
            lastHeight = height;
        }

        player.targetHP = bossStatus.hp;
        if (shuOperator != null) {
            player.shuHP = shuOperator.hp;
            player.shuCol = (int) shuOperator.GRID_COL;
            player.shuRow = (int) shuOperator.GRID_ROW;
        }

        int playerCol = (int) (player.x / player.tileWidth);  
        int playerRow = (int) (player.y / player.tileHeight); 

        // 💡 Tutorial Interception: If inside tutorial mode (inGameTutorialStep > 0), freeze player movement
        if (gameState == 2 && inGameTutorialStep == 0) {
            if (!operatorHealth.isDead() && blockSys != null && !blockSys.isPlayerBlocked) {
                player.update(width, height);
            }
        }
        player.updateTileSize(width, height);
            
        if (!operatorHealth.isDead() && (playerRow >= 16 && playerRow <= 20) && (playerCol == 35 || playerCol == 36)) {
            gameState = 3;       
            victoryPhase = 0;   
            audioSys.stopBGM();
            audioSys.playVictorySFX();
            return; 
        }

        if (bg != null) {
            imageMode(CORNER);
            image(bg, 0, 0, width, height);
        } else {
            background(50);
        }

        // 💡 Tutorial Interception: Freeze Boss SP accumulation and Ultimate activation
        if (inGameTutorialStep == 0) {
            bossStatus.update(gameState == 2);
        }
        
        if (blockSys != null && inGameTutorialStep == 0) {
            float currentShuHp = (shuOperator != null) ? shuOperator.hp : 0;
            blockSys.updateBlockStatus(player, bossStatus.hp, currentShuHp);
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
        
        if (!isSwitched && (millis() - enterTime >= 1000) && inGameTutorialStep == 0) {
            if (targetGifXXL != null) {
                currentTargetGif = targetGifXXL; 
            }
            isSwitched = true; 
        }
        
        if (isSwitched && bossStatus.hp > 0) {
            int currentMillis = millis();
            
            // 💡 Tutorial Interception: Only process collision and time decay when not in tutorial
            if (inGameTutorialStep == 0) {
                if (tacticalTool != null) {
                    tacticalTool.update(playerCol, playerRow, buffManager);
                }

                if (jzOperator != null) {
                    jzOperator.update(playerCol, playerRow, operatorHealth, currentMillis);
                    if (jzOperator.justBombed && audioSys != null) {
                        audioSys.playJzSkillSFX(); 
                        jzOperator.justBombed = false; 
                    }
                }

                if (shuOperator != null) {
                    shuOperator.update(gameState == 2);
                }

                if (bossStatus.isSkillActive) {
                    if (bossStatus.justActivated && !hasPlayedSkillSFX && audioSys != null) {
                        audioSys.playRandomSkillSFX(); 
                        hasPlayedSkillSFX = true;      
                        bossStatus.justActivated = false; 
                    }
                } else {
                    if (hasPlayedSkillSFX) {
                        hasPlayedSkillSFX = false; 
                    }
                }

                if (currentMillis - lastAttackTime >= attackCooldown) {
                    boolean triggeredTick = false;

                    if (blockSys != null && blockSys.isPlayerBlocked) {
                        if (!isAttackingAnimation && !operatorHealth.isDead()) {
                            currentGif = cjAttackGif; 
                            isAttackingAnimation = true; 
                        }
                        if (!operatorHealth.isDead()) {
                            float finalDamage = buffManager.getBuffedDamage(player.getDamage());
                            
                            if (blockSys.blockedTarget == 1) {
                                bossStatus.hp -= finalDamage; 
                                if (bossStatus.hp < 0) bossStatus.hp = 0;
                                if (!buffManager.tryBlockDamage()) {
                                    operatorHealth.takeDamage(5.0f); 
                                }
                            } 
                            else if (blockSys.blockedTarget == 2) {
                                if (shuOperator != null) {
                                    shuOperator.takeDamage(finalDamage);
                                }
                            }
                            triggeredTick = true;
                        }
                    }

                    if (bossStatus.isSkillActive) {
                        int randomChoice = (int) random(3); 
                        if (randomChoice == 0 && xxSkillA != null) xxCurrentSkillGif = xxSkillA;
                        else if (randomChoice == 1 && xxSkillB != null) xxCurrentSkillGif = xxSkillB;
                        else if (randomChoice == 2 && xxSkillC != null) xxCurrentSkillGif = xxSkillC;
                        
                        boolean isPlayerInSkillRange = 
                            ((playerCol >= 18 && playerCol <= 31) && (playerRow >= 15 && playerRow <= 18)) || 
                            ((playerCol >= 20 && playerCol <= 26) && (playerRow >= 8 && playerRow <= 22));   
                        
                        if (isPlayerInSkillRange && !operatorHealth.isDead()) {
                            if (!buffManager.tryBlockDamage()) {
                                operatorHealth.takeDamage(2.0f); 
                            }
                        }

                        if (shuOperator != null) {
                            int shuCol = 26;
                            int shuRow = 14; 
                            boolean isShuInSkillRange = 
                                ((shuCol >= 18 && shuCol <= 31) && (shuRow >= 15 && shuRow <= 18)) || 
                                ((shuCol >= 20 && shuCol <= 26) && (shuRow >= 8 && shuRow <= 22));
                            if (isShuInSkillRange) {
                                shuOperator.takeDamage(2.0f); 
                            }
                        }
                        triggeredTick = true;
                    }

                    if (triggeredTick) {
                        player.resetSpeedAndTimer(); 
                        lastAttackTime = currentMillis; 
                    }
                }

                if (blockSys != null && !blockSys.isPlayerBlocked && isAttackingAnimation) {
                    isAttackingAnimation = false; 
                    currentGif = gifRight; 
                }
            } else {
                // Hard-lock core time anchors during freeze state to prevent sudden death upon exiting tutorial
                lastAttackTime = currentMillis;
                enterTime = currentMillis; 
            }
        }
        
        if (currentTargetGif != null && g2d != null) {
            if (bossStatus.hp > 0) {
                float bodyGridCol = (currentTargetGif == targetGifXX) ? 23.2f : 23.7f; 
                float targetGridRow = 15.0f; 

                float bodyPixelX = bodyGridCol * player.tileWidth;
                float bodyPixelY = targetGridRow * player.tileHeight;
                float xxDrawWidth = baseTargetWidth * ((float) width / 1024f);
                
                Image finalBossGif = currentTargetGif;
                float currentRatio = (currentTargetGif == targetGifXX) ? targetGifRatioXX : targetGifRatioXXL;
                
                if (!operatorHealth.isDead()) {
                    if (bossStatus.isSkillActive) {
                        finalBossGif = xxCurrentSkillGif;
                        if (xxCurrentSkillGif == xxSkillA) currentRatio = targetGifRatioXXSkillA;
                        else if (xxCurrentSkillGif == xxSkillB) currentRatio = targetGifRatioXXSkillB;
                        else if (xxCurrentSkillGif == xxSkillC) currentRatio = targetGifRatioXXSkillC;
                    } else if (blockSys != null && blockSys.isPlayerBlocked && blockSys.blockedTarget == 1 && xxAttackGif != null) {
                        finalBossGif = xxAttackGif;
                        currentRatio = targetGifRatioXXAttack;
                    }
                }
                
                float xxDrawHeight = xxDrawWidth * currentRatio;
                float bodyX = bodyPixelX - xxDrawWidth / 2f;
                float bodyY = bodyPixelY - xxDrawHeight / 2f;
                
                g2d.drawImage(finalBossGif, (int)bodyX, (int)bodyY, (int)xxDrawWidth, (int)xxDrawHeight, stableObserver);
                bossStatus.display(player.tileWidth, player.tileHeight, 23.3f, 17.5f);
            }

            if (jzOperator != null) {
                float jzDrawWidth = baseTargetWidth * ((float) width / 1024f);
                jzOperator.display(g2d, jzSkillGif, jzDefaultGif, player.tileWidth, player.tileHeight, 
                                   jzDrawWidth, jzSkillRatio, jzDefaultGifRatio, stableObserver);
            }

            if (shuOperator != null && shuOperator.hp > 0) {
                float shuDrawWidth = baseTargetWidth * ((float) width / 1024f);
                shuOperator.display(g2d, shuGif, shuDefaultGif, player.tileWidth, player.tileHeight, 
                                   shuDrawWidth, shuRatio, shuDefaultRatio, millis(), stableObserver);
            }
        }
        
        if (tacticalTool != null && gameState == 2) {
            tacticalTool.display(toolTexture, player.tileWidth, player.tileHeight);
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

        if (bossStatus.hp > 0 && bossStatus.isSkillActive) {
            rectMode(CORNER);
            fill(255, 0, 0, 30); stroke(255, 0, 0, 80);
            rect(18 * player.tileWidth, 15 * player.tileHeight, (32-18)*player.tileWidth, (19-15)*player.tileHeight);
            rect(20 * player.tileWidth, 8 * player.tileHeight, (27-20)*player.tileWidth, (23-8)*player.tileHeight);
        }
        
        // 💡 Top-level Activation: 8-Phase In-Game Tutorial Dialog Layer
        if (inGameTutorialStep > 0) {
            drawInGameTutorial();
        }

        if (debugMode) {
            drawDebugInfo();
        }
    }

    // =========================================================================
    // 💡 Core Recovery & Upgrade: Complete 8-Phase In-Game Time-Stop Tutorial System
    // =========================================================================
    private void drawInGameTutorial() {
        fill(0, 0, 0, 160);
        noStroke();
        rect(0, 0, width, height);

        float pWidth = player.tileWidth;
        float pHeight = player.tileHeight;

        if (inGameTutorialStep == 1) {
            // Step 1: Focusing Shaw (阿消)
            float m3X = player.x - 100f * (width / 1024f);
            float m3Y = player.y - 50f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "This is the character you control. The faster you move, the higher\nyour damage output becomes. If you stay stationary for a while,\nyour attack damage will revert to its baseline values.", 3);
        } 
        else if (inGameTutorialStep == 2) {
            // Step 2: Focusing XXL Boss
            float m3X = (23.5f * pWidth) - 80f * (width / 1024f);
            float m3Y = (15.0f * pHeight) - 5f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "The blue gauge represents HP, and the bar beneath it tracks SP.\nSP builds continuously over time. Once fully charged, the Boss triggers\nan Ultimate. XXL features a double health bar mechanism, and\nher skill executes high-impact AoE locked-range strikes.", 4);
        } 
        else if (inGameTutorialStep == 3) {
            // Step 3: Focusing Shu (黍)
            float m3X = (26.0f * pWidth) - 60f * (width / 1024f);
            float m3Y = (11.0f * pHeight) - 15f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "Shu is a support Operator who cannot engage in direct combat.\nHowever, his skill activates a healing field that restores 30% of\nhis maximum HP pool.", 3);
        } 
        else if (inGameTutorialStep == 4) {
            // Step 4: Focusing JZ (且末 / Jesselton)
            float m3X = (23.0f * pWidth) - 50f * (width / 1024f);
            float m3Y = (8.0f * pHeight) - 5f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "He is a melee ground unit but shifts into airborne status normally,\nmaking him immune to standard attacks. He will only release his lethal\nassault skill when his SP bar is full AND you pass directly beneath him.", 3);
        }
        else if (inGameTutorialStep == 5) {
            // Step 5: Focusing Tactical Item Supply (Tool.png at Grid 24, 11)
            float m3X = (24.0f * pWidth) - 70f * (width / 1024f);
            float m3Y = (11.0f * pHeight) - 120f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "Collecting this tactical case grants a heavy Aegis shield capable of\nblocking 10 instances of incoming damage, while increasing your attack by 30%.\nBe careful: once the shield shatters, the attack buff expires immediately.", 3);
        }
        else if (inGameTutorialStep == 6) {
            // Step 6: Explaining Block and Lock Core Rule (Center Screen)
            float m3X = width * 0.32f;
            float m3Y = height * 0.45f;
            drawInGameM3Dialog(m3X, m3Y, "Colliding with enemy Operators triggers the 'Block' status. While blocked,\nyou lose free movement and become locked in place. You must defeat\nthe blocking unit before you can resume navigating the grid map.", 3);
        }
        else if (inGameTutorialStep == 7) {
            // Step 7: Arriving at the Goal Zone Blue Box (Bottom Right Grid Col 35, Row 17.5)
            float m3X = (35.0f * pWidth) - 120f * (width / 1024f);
            float m3Y = (17.5f * pHeight) - 30f * (height / 461f);

            // 💡 Step 7 uses a Left-flipped dialog frame to avoid overflowing off the right edge of the screen
            drawInGameM3DialogLeft(m3X, m3Y, "Doctor, your ultimate objective is to maneuver the character safely\ninto the Blue Box escape zone located at the bottom-right sector.", 2);
        }
        else if (inGameTutorialStep == 8) {
            // Step 8: Deployment Rally Call
            float m3X = width * 0.32f;
            float m3Y = height * 0.52f;
            drawInGameM3Dialog(m3X, m3Y, "Good luck, Doctor. Fight your way through to the Blue Box\nand secure total tactical victory!", 2);
        }
    }

    private void drawInGameM3Dialog(float m3X, float m3Y, String text, int lines) {
        if (m3Gif == null) return;
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();

        float m3DrawWidth = baseTargetWidth * 1.0f * ((float) width / 1024f);
        float m3DrawHeight = m3DrawWidth * m3Ratio;

        g2d.drawImage(m3Gif, (int)m3X, (int)m3Y, (int)m3DrawWidth, (int)m3DrawHeight, stableObserver);

        float boxWidth = 470f * ((float) width / 1024f);
        float boxHeight = (40f + 25f * lines) * ((float) height / 461f);

        float boxX = m3X + m3DrawWidth * 0.85f;
        float boxY = m3Y + m3DrawHeight * 0.1f;

        pushStyle();
        fill(25, 30, 35, 230); stroke(0, 162, 232); strokeWeight(2);
        rectMode(CORNER);
        rect(boxX, boxY, boxWidth, boxHeight, 8);

        fill(25, 30, 35, 230); noStroke();
        triangle(boxX + 2, boxY + 15, boxX - 12, boxY + 25, boxX + 2, boxY + 35);
        stroke(0, 162, 232); strokeWeight(2);
        line(boxX, boxY + 15, boxX - 12, boxY + 25);
        line(boxX - 12, boxY + 25, boxX, boxY + 35);

        fill(255); textAlign(LEFT, TOP);
        try { textFont(createFont("Microsoft YaHei", 16)); } catch(Exception e) { textSize(16); }
        text(text, boxX + 15, boxY + 15);
        
        fill(255, 255, 0); textSize(12); textAlign(RIGHT, BOTTOM);
        text("▶ CLICK SCREEN TO CONTINUE", boxX + boxWidth - 10, boxY + boxHeight - 5);
        popStyle();
    }


    // 💡 Exclusively tailored for Step 7: Displays dialog window to the left of M3, with the tail pinning rightward
    private void drawInGameM3DialogLeft(float m3X, float m3Y, String text, int lines) {
        if (m3Gif == null) return;
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();

        float m3DrawWidth = baseTargetWidth * 1.0f * ((float) width / 1024f);
        float m3DrawHeight = m3DrawWidth * m3Ratio;

        g2d.drawImage(m3Gif, (int)m3X, (int)m3Y, (int)m3DrawWidth, (int)m3DrawHeight, stableObserver);

        float boxWidth = 470f * ((float) width / 1024f);
        float boxHeight = (40f + 25f * lines) * ((float) height / 461f);

        // 💡 Move box to the left side of M3
        float boxX = m3X - boxWidth + m3DrawWidth * 0.15f;
        float boxY = m3Y + m3DrawHeight * 0.1f;

        pushStyle();
        fill(25, 30, 35, 230); stroke(0, 162, 232); strokeWeight(2);
        rectMode(CORNER);
        rect(boxX, boxY, boxWidth, boxHeight, 8);

        // 💡 Right-anchored triangular tail pointing at M3
        fill(25, 30, 35, 230); noStroke();
        triangle(boxX + boxWidth - 2, boxY + 15, boxX + boxWidth + 14, boxY + 25, boxX + boxWidth - 2, boxY + 35);
        stroke(0, 162, 232); strokeWeight(2);
        line(boxX + boxWidth, boxY + 15, boxX + boxWidth + 14, boxY + 25);
        line(boxX + boxWidth + 14, boxY + 25, boxX + boxWidth, boxY + 35);

        fill(255); textAlign(LEFT, TOP);
        try { textFont(createFont("Microsoft YaHei", 16)); } catch(Exception e) { textSize(16); }
        text(text, boxX + 15, boxY + 15);
        
        fill(255, 255, 0); textSize(12); textAlign(RIGHT, BOTTOM);
        text("▶ CLICK SCREEN TO CONTINUE", boxX + boxWidth - 10, boxY + boxHeight - 5);
        popStyle();
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
                text("OPERATION COMPLETE\n(Click anywhere to continue)", width / 2f, height / 2f);
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

    private void drawGameOverOverlay() {
        background(0); 
        textAlign(CENTER, CENTER);
        try { textFont(createFont("Microsoft YaHei", 60)); } catch(Exception e) { textSize(60); }
        fill(220, 20, 30); 
        text("MISSION FAILED", width / 2f, height / 2f - 40);
        textSize(24); fill(150); 
        text("Click here to retry", width / 2f, height / 2f + 50);
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
        int cCol = (int) (player.x / player.tileWidth) + 1;
        int cRow = (int) (player.y / player.tileHeight) + 1;
        text("Player Map Grid Index: (Col " + cCol + ", Row " + cRow + ")", 20, 50);
        
        if (blockSys != null) {
            String blockStr = "🏃 NO (Free Navigation Available)";
            if (blockSys.isPlayerBlocked) {
                blockStr = blockSys.blockedTarget == 1 ? "🔒 YES (Blocked by XXL)" : "🔒 YES (Blocked by Shu)";
            }
            text("Block Status: " + blockStr, 20, 70);
        }
        text("XXL Ultimate Charge Status: " + (bossStatus.isSkillActive ? "🔥 ACTIVE (Skill duration draining)" : "⏳ CHARGING (Building SP)"), 20, 90);
        text("XXL Target HP: " + (int)bossStatus.hp + " / 100", 20, 110);
        text("Shaw HP: " + (int)operatorHealth.hp + " / " + (int)operatorHealth.maxHp, 20, 130);

        if (buffManager != null) {
            text("【Shaw Special Buffs】: Aegis Charges Left: " + buffManager.shieldCount + " | Current Attack Modifier: " + buffManager.damageMultiplier + "x", 20, 150);
        }
    }

    private void hardResetGameAndStart() {
        gameState = 2;              
        enterTime = millis();       
        hasPlayedDieSFX = false; 
        
        // 💡 Directly trigger Step 1 of the battle tutorial when resetting map!
        inGameTutorialStep = 1;
        
        float startGridCol = 14.0f; 
        float startGridRow = 16.0f; 
        player.x = Math.round(startGridCol * ((float) width / 48f));
        player.y = Math.round(startGridRow * ((float) height / 26f));
        player.resetSpeedAndTimer();
        player.isUp = false; player.isDown = false; player.isLeft = false; player.isRight = false;
        
        if (operatorHealth != null) operatorHealth.reset();
        if (blockSys != null) blockSys.reset();
        if (buffManager != null) buffManager.activateBuff(0, 1.0f);
        if (tacticalTool != null) tacticalTool.isPicked = false;

        if (bossStatus != null) {
            bossStatus.hp = 100.0f;     
            bossStatus.sp = 0;              
            bossStatus.isSkillActive = false; 
            bossStatus.justActivated = false; 
        }
        if (jzOperator != null) {
            jzOperator.sp = 0.0f;
            jzOperator.status = 0;
            jzOperator.deployStartTime = millis(); 
            jzOperator.justBombed = false; 
        }
        if (shuOperator != null) {
            shuOperator.hp = shuOperator.maxHp; 
            shuOperator.sp = 0.0f;
            shuOperator.deployStartTime = millis();
            if (shuGif != null) shuGif.flush(); 
        }

        lastAttackTime = millis(); 
        isAttackingAnimation = false; 
        hasPlayedSkillSFX = false; 
        
        audioSys.stopLevelMapBGM();
        audioSys.playBGM();
        audioSys.playCombinedStartSFX();
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
                hardResetGameAndStart(); 
            }
        }
        else if (gameState == 2) {
            // 💡 Tutorial Progress Handler: Iterate step by step from 1 to 8 before freeing processing clocks!
            if (inGameTutorialStep > 0) {
                inGameTutorialStep++;
                if (inGameTutorialStep > 8) {
                    inGameTutorialStep = 0; // Close the tutorial loop, engine engines resume operational flow!
                    enterTime = millis();
                    lastAttackTime = millis();
                    if (jzOperator != null) jzOperator.deployStartTime = millis();
                    if (shuOperator != null) shuOperator.deployStartTime = millis();
                }
            }
        }
        else if (gameState == 3) {
            if (victoryPhase == 0) {
                victoryPhase = 1; 
            } else if (victoryPhase == 1) {
                exit(); // 💡 Final stats panel page: click once more to shut down program window
            }
        }
        else if (gameState == 4) {
            hardResetGameAndStart(); 
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
}