package arknonights;

import processing.core.PApplet;

public class BlockManager {
    private PApplet parent;

    // 💡 阻挡核心数据，内聚在管理器内部
    public int maxBlockCount = 3;       // xxl 干员最大阻挡数（重装挡3）
    public int currentlyBlocked = 0;     // 当前已被占用的阻挡名额
    public boolean isPlayerBlocked = false; // 玩家当前是否处于被阻挡锁死状态

    public BlockManager(PApplet parent) {
        this.parent = parent;
    }

    // 💡 核心阻挡状态机更新：传入玩家对象、怪物血量，每帧自动判定
    public void updateBlockStatus(Person player, float bossHp) {
        // 1. 击杀联动判定：一旦 xxl 血量砸到 0，强行无条件立刻释放玩家
        if (bossHp <= 0) {
            isPlayerBlocked = false;
            currentlyBlocked = 0;
            return;
        }

        // 2. 算坐标大格子索引位置
        int playerCol = (int) (player.x / player.tileWidth);
        int playerRow = (int) (player.y / player.tileHeight);

        // 判定玩家当前碰撞箱是否触碰到了 XXL 干员的九宫格阻挡物理控制区 (21列到27列)
        boolean isTouching = (playerRow >= 15 && playerRow <= 17) && (playerCol >= 21 && playerCol <= 27);

        if (isTouching) {
            if (!isPlayerBlocked) {
                // 如果尚未被拦截，且挡3名额未满，进行锁定！
                if (currentlyBlocked < maxBlockCount) {
                    isPlayerBlocked = true;
                    currentlyBlocked++;
                    System.out.println("🔒 [阻挡组件] 目标踩入控制区，拦截成功！名额: " + currentlyBlocked + "/3");
                }
            }
        } else {
            // 脱离控制区，释放状态
            if (isPlayerBlocked) {
                isPlayerBlocked = false;
                if (currentlyBlocked > 0) currentlyBlocked--;
                System.out.println("🔓 [阻挡组件] 脱离控制区，释放名额。");
            }
        }
    }

    // 💡 关卡重置方法
    public void reset() {
        isPlayerBlocked = false;
        currentlyBlocked = 0;
    }
}