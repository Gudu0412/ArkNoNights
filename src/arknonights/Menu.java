package arknonights;

import processing.core.PApplet;
import processing.core.PImage;

public class Menu {
    private PApplet parent; // 💡 持有 MySketch 的引用，用来调用 Processing 的画图函数
    private PImage menuBg;  // 主菜单背景图

    // 💡 按钮热区属性
    private float btnX, btnY, btnW, btnH;

    // 构造函数
    public Menu(PApplet parent) {
        this.parent = parent;
        
        // 💡 按钮热区像素对齐（1024x461 分辨率下精准覆盖“开始唤醒”）
        this.btnW = 180;
        this.btnH = 54;
        this.btnX = parent.width / 2f - this.btnW / 2f; 
        this.btnY = 306; 

        // 加载主菜单背景图
        try {
            java.io.File mBgFile = new java.io.File("Image/Mainmenu.png");
            if (mBgFile.exists()) {
                this.menuBg = parent.loadImage(mBgFile.getPath());
                System.out.println("✅ Menu类成功加载主菜单背景: Mainmenu.png");
            } else {
                this.menuBg = parent.loadImage(parent.dataPath("../Image/Mainmenu.png"));
            }
        } catch (Exception e) {
            System.out.println("🚨 警告：Menu类未能在 Image 文件夹下找到 Mainmenu.png");
        }
    }

    // 💡 负责绘制菜单界面的方法
    public void display() {
        if (menuBg != null) {
            parent.imageMode(PApplet.CORNER);
            parent.image(menuBg, 0, 0, parent.width, parent.height);
        } else {
            parent.background(20, 24, 28); 
        }

        // 检测鼠标是否正好悬停在“开始唤醒”方框里
        boolean isHover = parent.mouseX >= btnX && parent.mouseX <= btnX + btnW 
                       && parent.mouseY >= btnY && parent.mouseY <= btnY + btnH;

        // 绘制按钮热区微光反馈
        if (isHover) {
            parent.fill(255, 255, 255, 40); // 叠加淡白色全息微光
            parent.stroke(255, 255, 255, 100);
            parent.strokeWeight(1);
            parent.cursor(PApplet.HAND);    // 鼠标变小手
        } else {
            parent.noFill(); 
            parent.noStroke();
            parent.cursor(PApplet.ARROW);   // 默认箭头
        }
        
        // 渲染无形按钮
        parent.rect(btnX, btnY, btnW, btnH, 2); 
    }

    // 💡 暴露给外部调用的点击判定接口
    public boolean isStartClicked() {
        return parent.mouseX >= btnX && parent.mouseX <= btnX + btnW 
            && parent.mouseY >= btnY && parent.mouseY <= btnY + btnH;
    }
}