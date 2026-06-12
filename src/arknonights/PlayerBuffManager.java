package arknonights;

import processing.core.PApplet;

public class PlayerBuffManager {
    private PApplet parent;

    public int shieldCount = 0;       // Remaining block instances
    public float damageMultiplier = 1.0f; // Attack damage multiplier (1.0 is baseline)

    public PlayerBuffManager(PApplet parent) {
        this.parent = parent;
    }

    // Activate item BUFF bonuses
    public void activateBuff(int count, float multiplier) {
        this.shieldCount = count;
        this.damageMultiplier = multiplier;
    }

    // Core Interception: Determines if incoming damage can be absorbed by the Aegis shield
    public boolean tryBlockDamage() {
        if (shieldCount > 0) {
            shieldCount--;
            System.out.println("🛡️ [Aegis Block] Damage successfully deflected! Shield charges left: " + shieldCount);
            
            // If charges are depleted, the attack multiplier resets immediately
            if (shieldCount <= 0) {
                damageMultiplier = 1.0f;
                System.out.println("🥀 [BUFF Expired] All 10 Aegis charges depleted! Attack modifier reverted to baseline.");
            }
            return true; // Successfully mitigated incoming damage instance
        }
        return false; // No shield charges remaining, take raw damage
    }

    // =========================================================================
    // 💡 Aligned signature tracking parameters and modified returns
    // =========================================================================
    public float getBuffedDamage(float baseDamage) {
        return baseDamage * damageMultiplier; 
    }
}