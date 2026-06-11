package arknonights;

import processing.core.PApplet;
import processing.core.PImage;
import java.io.File;
import javax.swing.ImageIcon; 
import java.awt.Image;
import java.awt.image.ImageObserver;

public class MySketch extends PApplet {

    volatile int gameState = -1; 
    volatile String loadingStatus = "正在初始化罗德岛 system 引擎..."; 

    int victoryPhase = 0; 
    
    // 💡 教学状态机完全体：0代表不在教学中，1~8对应八个阶段的战斗指引
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
                    loadingStatus = "正在装载主界面与核心 UI 组件...";
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
                    
                    loadingStatus = "正在挂载 60 帧【动态选关大地图】环境...";
                    mapRenderer.loadFrames();
                    Thread.sleep(100);
                    
                    loadingStatus = "正在加载【战斗关卡静态网格地图与道具资源】...";
                    File bgFile = new File("Image/background.png");
                    if (bgFile.exists()) bg = loadImage(bgFile.getPath());
                    else bg = loadImage(dataPath("../Image/background.png"));
                    
                    File toolFile = new File("Image/Tool.png");
                    if (toolFile.exists()) toolTexture = loadImage(toolFile.getPath());
                    else toolTexture = loadImage(dataPath("../Image/Tool.png"));
                    
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
                    
                    loadingStatus = "正在渲染最终行动结算面板...";
                    File fPngFile = new File("Image/Finish.png");
                    if (fPngFile.exists()) finishPng = loadImage(fPngFile.getPath());
                    else finishPng = loadImage(dataPath("../Image/Finish.png"));

                    File rfPngFile = new File("Image/RealFinish.png");
                    if (rfPngFile.exists()) realFinishPng = loadImage(rfPngFile.getPath());
                    else realFinishPng = loadImage(dataPath("../Image/RealFinish.png"));
                    
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
                System.out.println("🚨 [预警] 图片装载成了空气，检查路径 -> " + finalPath);
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
        text("欢迎回来，博士，请点击这里开始游戏", boxX + 20, boxY + boxHeight / 2f - 2);
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
        text("博士，请点击这里开始游戏", boxX + 20, boxY + boxHeight / 2f - 2);
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

        // 💡 教学拦截：如果处于教学状态 (inGameTutorialStep > 0)，冻结玩家移动
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

        // 💡 教学拦截：冻结 Boss 的 SP 积攒与大招
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
            
            // 💡 教学拦截：非教学期间才处理碰撞和时间衰减
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
                // 冻结期间，强力锁死各项核心时间锚点，防止解除教学后暴毙
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
        
        // 💡 顶层激活：八阶段保姆级新手指引图层
        if (inGameTutorialStep > 0) {
            drawInGameTutorial();
        }

        if (debugMode) {
            drawDebugInfo();
        }
    }

    // ==========================================
    // 💡 核心恢复与升级：完整的 8 阶段局内时停指引系统
    // ==========================================
    private void drawInGameTutorial() {
        fill(0, 0, 0, 160);
        noStroke();
        rect(0, 0, width, height);

        float pWidth = player.tileWidth;
        float pHeight = player.tileHeight;

        if (inGameTutorialStep == 1) {
            // 第 1 步：围绕阿消
            float m3X = player.x - 100f * (width / 1024f);
            float m3Y = player.y - 50f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "这个角色是你操控的，并且速度越快伤害越高，\n当一段时间没有移动伤害会回归初始伤害。", 2);
        } 
        else if (inGameTutorialStep == 2) {
            // 第 2 步：围绕 XXL Boss
            float m3X = (23.5f * pWidth) - 80f * (width / 1024f);
            float m3Y = (15.0f * pHeight) - 5f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "蓝色的条是血条，下面的条是技力条，技力条会随着时间积攒，\n满后就可以放技能，xxl的机制是有双倍的血条，\n技能是进行范围锁定攻击。", 3);
        } 
        else if (inGameTutorialStep == 3) {
            // 第 3 步：围绕 Shu
            float m3X = (26.0f * pWidth) - 60f * (width / 1024f);
            float m3Y = (11.0f * pHeight) - 15f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "他是一个不能攻击的干员，\n但是他的技能可以治疗自己30%的生命值。", 2);
        } 
        else if (inGameTutorialStep == 4) {
            // 第 4 步：围绕 JZ
            float m3X = (23.0f * pWidth) - 50f * (width / 1024f);
            float m3Y = (8.0f * pHeight) - 5f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "他是地面干员，但是常态起飞，无法攻击，\n只有技力条充满且玩家在他身下时，\n他才会放技能进行攻击。", 3);
        }
        else if (inGameTutorialStep == 5) {
            // 第 5 步：围绕战术道具补给 (Tool.png 在网格坐标 24, 11)
            float m3X = (24.0f * pWidth) - 70f * (width / 1024f);
            float m3Y = (11.0f * pHeight) - 120f * (height / 461f);
            drawInGameM3Dialog(m3X, m3Y, "这个道具捡起后可以为玩家添加抵挡十次伤害的护盾并增加30%攻击力，\n但是护盾消失后攻击力加成就会失效。", 2);
        }
        else if (inGameTutorialStep == 6) {
            // 第 6 步：讲解阻挡锁定核心规则 (屏幕中心)
            float m3X = width * 0.32f;
            float m3Y = height * 0.45f;
            drawInGameM3Dialog(m3X, m3Y, "当玩家触碰到干员后，就会被“格挡”，被格挡的玩家不可以自由移动，\n只能在击败格挡住玩家的干员后才能正常行动。", 2);
        }
        else if (inGameTutorialStep == 7) {
            // 第 7 步：抵达终点区域蓝门 (右下角 列35, 行17.5 附近)
            float m3X = (35.0f * pWidth) - 120f * (width / 1024f);
            float m3Y = (17.5f * pHeight) - 30f * (height / 461f);

            // 💡 第 7 步单独使用左侧对话框，避免右边超出屏幕
            drawInGameM3DialogLeft(m3X, m3Y, "博士，您的终极任务是操控角色安全抵达右下角的蓝门区域。", 1);
        }
        else if (inGameTutorialStep == 8) {
            // 第 8 步：出征动员令
            float m3X = width * 0.32f;
            float m3Y = height * 0.52f;
            drawInGameM3Dialog(m3X, m3Y, "加油，博士，请努力到达蓝门，并取得胜利吧！", 1);
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
        text("▶ 点击屏幕继续", boxX + boxWidth - 10, boxY + boxHeight - 5);
        popStyle();
    }


    // 💡 只给第 7 步使用：对话框在 M3 左侧，尾巴指向右侧
    private void drawInGameM3DialogLeft(float m3X, float m3Y, String text, int lines) {
        if (m3Gif == null) return;
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.getNative();

        float m3DrawWidth = baseTargetWidth * 1.0f * ((float) width / 1024f);
        float m3DrawHeight = m3DrawWidth * m3Ratio;

        g2d.drawImage(m3Gif, (int)m3X, (int)m3Y, (int)m3DrawWidth, (int)m3DrawHeight, stableObserver);

        // 根据文案行数自适应高度
        float boxWidth = 470f * ((float) width / 1024f);
        float boxHeight = (40f + 25f * lines) * ((float) height / 461f);

        // 💡 对话框放到 M3 左侧
        float boxX = m3X - boxWidth + m3DrawWidth * 0.15f;
        float boxY = m3Y + m3DrawHeight * 0.1f;

        pushStyle();
        fill(25, 30, 35, 230); stroke(0, 162, 232); strokeWeight(2);
        rectMode(CORNER);
        rect(boxX, boxY, boxWidth, boxHeight, 8);

        // 💡 右侧三角形尾巴，指向 M3
        fill(25, 30, 35, 230); noStroke();
        triangle(boxX + boxWidth - 2, boxY + 15, boxX + boxWidth + 14, boxY + 25, boxX + boxWidth - 2, boxY + 35);
        stroke(0, 162, 232); strokeWeight(2);
        line(boxX + boxWidth, boxY + 15, boxX + boxWidth + 14, boxY + 25);
        line(boxX + boxWidth + 14, boxY + 25, boxX + boxWidth, boxY + 35);

        // 文字
        fill(255); textAlign(LEFT, TOP);
        try { textFont(createFont("Microsoft YaHei", 16)); } catch(Exception e) { textSize(16); }
        text(text, boxX + 15, boxY + 15);
        
        // 继续提示
        fill(255, 255, 0); textSize(12); textAlign(RIGHT, BOTTOM);
        text("▶ 点击屏幕继续", boxX + boxWidth - 10, boxY + boxHeight - 5);
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

    private void drawGameOverOverlay() {
        background(0); 
        textAlign(CENTER, CENTER);
        try { textFont(createFont("Microsoft YaHei", 60)); } catch(Exception e) { textSize(60); }
        fill(220, 20, 30); 
        text("失  败", width / 2f, height / 2f - 40);
        textSize(24); fill(150); 
        text("点此重试", width / 2f, height / 2f + 50);
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
        text("Player Map Grid Index: (第 " + cCol + " 列, 第 " + cRow + " 行)", 20, 50);
        
        if (blockSys != null) {
            String blockStr = "🏃 NO (可自由活动)";
            if (blockSys.isPlayerBlocked) {
                blockStr = blockSys.blockedTarget == 1 ? "🔒 YES (被 XXL 阻挡)" : "🔒 YES (被 Shu 阻挡)";
            }
            text("阻挡状态: " + blockStr, 20, 70);
        }
        text("XXL 大招决战释放状态: " + (bossStatus.isSkillActive ? "🔥 ACTIVE (橙闪条倒扣中)" : "⏳ CHARGING (充能中)"), 20, 90);
        text("XXL Target HP: " + (int)bossStatus.hp + " / 100", 20, 110);
        text("阿消生命值 (HP): " + (int)operatorHealth.hp + " / " + (int)operatorHealth.maxHp, 20, 130);

        if (buffManager != null) {
            text("【阿消特种增益】: 圣盾剩余格挡: " + buffManager.shieldCount + " 次 | 当前攻击力倍率: " + buffManager.damageMultiplier + "x", 20, 150);
        }
    }

    private void hardResetGameAndStart() {
        gameState = 2;              
        enterTime = millis();       
        hasPlayedDieSFX = false; 
        
        // 💡 重新开局直接激活第 1 步战斗教学！
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
            // 💡 教学点击切换：从第 1 步顺推到第 8 步后，才真正解除时停！
            if (inGameTutorialStep > 0) {
                inGameTutorialStep++;
                if (inGameTutorialStep > 8) {
                    inGameTutorialStep = 0; // 教学闭环，千军万马恢复运转！
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
                exit(); // 💡 结算最后一页，再次点击退出关闭窗口
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