package arknonights;

import processing.core.PApplet;
import processing.core.PImage;

public class ToolItem {
    private PApplet parent;

    // 💡 准确定位到你红圈圈出的格点坐标：画面第 25 列，第 12 行 (代码索引 24, 11)
    public final int GRID_COL = 24;
    public final int GRID_ROW = 5;

    public boolean isPicked = false; // 是否已被捡起

    public ToolItem(PApplet parent) {
        this.parent = parent;
    }

    // 实时检测阿消是否踩中道具
    public void update(int playerCol, int playerRow, PlayerBuffManager buffSys) {
        if (isPicked) return;

        // 如果阿消踩中了这格
        if (playerCol == GRID_COL && playerRow == GRID_ROW) {
            isPicked = true;
            if (buffSys != null) {
                buffSys.activateBuff(10, 1.3f); // 注入 10 次圣盾，攻击力变为 1.3 倍
                System.out.println("🎁 [战术补给] 阿消捡起了战术道具！获得 10 次免疫护盾，攻击力提升 30%！");
            }
        }
    }

    // 渲染道具图标
    public void display(PImage toolImg, float tileWidth, float tileHeight) {
        if (isPicked || toolImg == null) return;

        // 计算格子的像素位置
        float drawX = GRID_COL * tileWidth;
        float drawY = GRID_ROW * tileHeight;

        parent.imageMode(PApplet.CORNER);
        // 让道具完美契合一个格子的大小
        parent.image(toolImg, drawX, drawY, tileWidth, tileHeight);
    }
}