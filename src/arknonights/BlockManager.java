package arknonights;

import processing.core.PApplet;

public class BlockManager {
    private PApplet parent;

    // 💡 阻挡核心数据
    public int maxBlockCount = 3;       
    public int currentlyBlocked = 0;     
    public boolean isPlayerBlocked = false; 
    
    // ==========================================
    // 💡 新增核心：精准追踪雷达！
    // 0 = 自由态，1 = 被 XXL(Boss) 阻挡，2 = 被 Shu(黍) 阻挡
    // ==========================================
    public int blockedTarget = 0; 

    public BlockManager(PApplet parent) {
        this.parent = parent;
    }

    // 💡 状态机升级：同时监控 Boss 和 Shu 的血量
    public void updateBlockStatus(Person player, float bossHp, float shuHp) {
        // 击杀释放判定
        if (bossHp <= 0 && blockedTarget == 1) {
            releaseBlock();
            return;
        }
        if (shuHp <= 0 && blockedTarget == 2) {
            releaseBlock();
            return;
        }

        int playerCol = (int) (player.x / player.tileWidth);
        int playerRow = (int) (player.y / player.tileHeight);

        // 判定 1: XXL 的物理控制区 (列 21~27, 行 15~17)
        boolean touchingXXL = (bossHp > 0) && (playerRow >= 15 && playerRow <= 17) && (playerCol >= 21 && playerCol <= 27);
        
        // 判定 2: Shu 的物理控制区 (根据她的坐标 26列, 14.5行，划定周围一圈防区)
        boolean touchingShu = (shuHp > 0) && (playerRow >= 13 && playerRow <= 15) && (playerCol >= 25 && playerCol <= 27);

        if (touchingXXL) {
            if (blockedTarget != 1) {
                isPlayerBlocked = true;
                blockedTarget = 1; // 锁定为 XXL
                currentlyBlocked++;
                System.out.println("🔒 [阻挡组件] 被 XXL 恐怖的质量拦截了！");
            }
        } 
        else if (touchingShu) {
            if (blockedTarget != 2) {
                isPlayerBlocked = true;
                blockedTarget = 2; // 锁定为 Shu
                currentlyBlocked++;
                System.out.println("🌿 [阻挡组件] 被 Shu (黍) 的阵线拦下！开启单方面木桩训练！");
            }
        } 
        else {
            releaseBlock();
        }
    }

    private void releaseBlock() {
        if (isPlayerBlocked) {
            isPlayerBlocked = false;
            blockedTarget = 0;
            if (currentlyBlocked > 0) currentlyBlocked--;
            System.out.println("🔓 [阻挡组件] 脱离控制区，释放阻挡名额。");
        }
    }

    public void reset() {
        isPlayerBlocked = false;
        currentlyBlocked = 0;
        blockedTarget = 0;
    }
}