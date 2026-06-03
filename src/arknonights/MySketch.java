package arknonights;

import processing.core.PApplet;
import processing.core.PImage;
import java.io.File;
import javax.swing.ImageIcon; 
import java.awt.Image;
import java.awt.image.ImageObserver;

// 💡 换回标准原生音频包，这次加入自动格式转换，彻底解决挑食和没声音的问题！
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class MySketch extends PApplet {

    Person player;
    
    // 原生音频播放器载体
    Clip bgmClip; 
    
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
        
        // 💡 2. 【核心优化：智能格式转换音频流加载机制】
        try {
            File musicFile = new File("Music/map1.wav");
            String musicPath = musicFile.exists() ? musicFile.getPath() : dataPath("../Music/map1.wav");
            File audioFile = new File(musicPath);

            if (audioFile.exists()) {
                // 1. 读取原始音频输入流
                AudioInputStream baseInputStream = AudioSystem.getAudioInputStream(audioFile);
                AudioFormat baseFormat = baseInputStream.getFormat();
                
                // 2. 强制定义 Java 能够完美识别的标准 PCM 签名格式
                AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16, // 16位
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false // 小端字节序
                );
                
                // 3. 在内存中将不标准的音频动态转换解码为标准音频流
                AudioInputStream decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, baseInputStream);
                
                // 4. 将转换后的干净音频流塞进播放器
                DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
                bgmClip = (Clip) AudioSystem.getLine(info);
                bgmClip.open(decodedInputStream);
                
                // 5. 开启无限循环并开始播放
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
                
                System.out.println("🎵 [智能解码音频流] map1.wav 加载成功，高燃战歌已正常循环播放！");
            } else {
                System.out.println("🚨 找不到音频文件，请确认 Music 文件夹下是否存在 map1.wav");
            }
        } catch (Exception e) {
            System.out.println("🚨 音频流系统提示：如果还是没声音，请确保 map1.wav 不是强行改后缀名得到的，需要用转换工具转成真正的 .wav 格式。");
            e.printStackTrace();
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