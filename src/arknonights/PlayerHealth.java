package arknonights;

import processing.core.PApplet;

public class PlayerHealth {
    private PApplet parent;
    
    // 💡 核心属性
    public float hp;
    public float maxHp;

    // 💡 构造函数：创建时只需传入最大血量即可
    public PlayerHealth(PApplet parent, float maxHp) {
        this.parent = parent;
        this.maxHp = maxHp;
        this.hp = maxHp; // 默认满血出生
    }

    // 💡 封装的受击方法
    public void takeDamage(float damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }

    // 💡 封装的死亡判定
    public boolean isDead() {
        return hp <= 0;
    }

    // 💡 封装的重置回血方法（用于重新开局或医疗干员奶人）
    public void reset() {
        hp = maxHp;
    }

    // 💡 视觉渲染：在干员脚底画一个专属的动态小血条！
    public void display(float pixelX, float pixelY, float tileWidth) {
        if (isDead()) return; // 撤退了就不画了

        // 设定血条的宽和高
        float barWidth = tileWidth * 0.8f; 
        float barHeight = 4.0f;
        // 把血条居中并放到干员脚底的位置
        float drawX = pixelX - barWidth / 2.0f;
        float drawY = pixelY + tileWidth / 1.5f; 

        // 1. 画黑色半透明底槽
        parent.fill(0, 0, 0, 150);
        parent.noStroke();
        parent.rect(drawX, drawY, barWidth, barHeight);

        // 2. 根据剩余血量算出当前色块的长度
        float currentHpWidth = PApplet.map(hp, 0, maxHp, 0, barWidth);
        
        // 3. 动态变色机制：健康绿 -> 警告黄 -> 濒死红
        if (hp > maxHp * 0.5f) {
            parent.fill(0, 255, 0);   // 大于一半是绿色
        } else if (hp > maxHp * 0.2f) {
            parent.fill(255, 255, 0); // 丝血前是黄色
        } else {
            parent.fill(255, 0, 0);   // 濒死是红色
        }
        
        parent.rect(drawX, drawY, currentHpWidth, barHeight);
    }
}