package arknonights;

import processing.core.PApplet;

public class PlayerBuffManager {
    private PApplet parent;

    public int shieldCount = 0;       // 剩余格挡次数
    public float damageMultiplier = 1.0f; // 攻击力倍率（1.0为常态）

    public PlayerBuffManager(PApplet parent) {
        this.parent = parent;
    }

    // 激活道具BUFF
    public void activateBuff(int count, float multiplier) {
        this.shieldCount = count;
        this.damageMultiplier = multiplier;
    }

    // 核心拦截：当受到伤害时，判断是否用护盾抵挡
    public boolean tryBlockDamage() {
        if (shieldCount > 0) {
            shieldCount--;
            System.out.println("🛡️ [圣盾格挡] 成功拦截伤害！剩余格挡次数: " + shieldCount + " 次");
            
            // 如果次数耗尽，攻击力加成立刻失效，打回原形
            if (shieldCount <= 0) {
                damageMultiplier = 1.0f;
                System.out.println("🥀 [BUFF失效] 10 次圣盾格挡已全部耗尽！攻击力恢复常态。");
            }
            return true; // 代表成功免疫了这次伤害
        }
        return false; // 没有护盾，正常掉血
    }

    // ==========================================
    // 💡 修复后的大功臣：传参和返回值名字对齐！
    // ==========================================
    public float getBuffedDamage(float baseDamage) {
        return baseDamage * damageMultiplier; 
    }
}