package arknonights;

public class Person {
    public int x, y;
    
    // External synchronized variable: XXL's current health. Defaults to 100 on setup.
    public float targetHP = 100.0f;
    
    // =========================================================================
    // 💡 Synchronized Variables: Tracking Shu's survival state and map grid coordinates
    // =========================================================================
    public float shuHP = 0.0f;
    public int shuCol = -1;
    public int shuRow = -1;
    
    // Balanced bounding box proportions to prevent movement artifacts
    private final int radiusX = 14;                
    private final int radiusY = 10;                
    
    // Movement Tuning Configurations
    private float currentSpeedMultiplier;         
    private final float BASE_SPEED = 0.8f;         
    private final float MAX_SPEED = 5.5f;          
    private final float ACCELERATION = 0.02f;      
    private final float DECELERATION = 0.15f;      

    public String imageBase = "Image/cj"; 
    public boolean isUp, isDown, isLeft, isRight;

    public float tileWidth;
    public float tileHeight;

    // Direct registration flag for initial impact collisions across all directions
    public boolean isJustCollided = false; 

    // Time-decay anchor tracking intervals without active positional displacement
    private int lastMoveTime = 0;         
    private boolean timerStarted = false; 

    // 26 Rows × 48 Columns Static Grid Collision Matrix
    public final int[][] MAP_MATRIX = new int[26][48];

    {
        // Default Configuration: Hardcode entire layout block structure as un-walkable (1)
        for (int r = 0; r < 26; r++) {
            for (int c = 0; c < 48; c++) {
                MAP_MATRIX[r][c] = 1;
            }
        }

        // Map Grid Excavation: Carve out valid walkable corridors (0) using 1-based indexing offsets
        setWalkable(6, 14, 16); setWalkable(6, 24, 35);
        setWalkable(7, 14, 15); setWalkable(7, 24, 25); setWalkable(7, 31, 35);
        for (int r = 8; r <= 9; r++) { setWalkable(r, 14, 20); setWalkable(r, 24, 25); }
        for (int r = 10; r <= 11; r++) { setWalkable(r, 11, 25); }
        setWalkable(12, 10, 25);
        setWalkable(13, 10, 36);
        setWalkable(14, 10, 37);
        for (int r = 15; r <= 18; r++) { setWalkable(r, 12, 20); setWalkable(r, 23, 26); setWalkable(r, 35, 37); }
        for (int r = 19; r <= 21; r++) { setWalkable(r, 17, 20); setWalkable(r, 23, 38); }
    }

    private void setWalkable(int humanRow, int humanColStart, int humanColEnd) {
        int r = humanRow - 1; 
        int cStart = humanColStart - 1;
        int cEnd = humanColEnd - 1;
        for (int c = cStart; c <= cEnd; c++) {
            if (r >= 0 && r < 26 && c >= 0 && c < 48) {
                MAP_MATRIX[r][c] = 0;
            }
        }
    }

    public Person(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.currentSpeedMultiplier = BASE_SPEED; 
    }

    public void updateTileSize(int currentWidth, int currentHeight) {
        this.tileWidth = (float) currentWidth / MAP_MATRIX[0].length;
        this.tileHeight = (float) currentHeight / MAP_MATRIX.length;
    }

    public void update(int currentWidth, int currentHeight) {
        boolean isMoving = isUp || isDown || isLeft || isRight;

        float baseSpeedX = currentSpeedMultiplier * ((float) currentWidth / 1024f);
        float baseSpeedY = currentSpeedMultiplier * ((float) currentHeight / 461f);

        boolean isDiagonal = (isUp || isDown) && (isLeft || isRight);
        if (isDiagonal) {
            baseSpeedX *= 0.7071f;
            baseSpeedY *= 0.7071f;
        }

        int frameSpeedX = Math.round(baseSpeedX);
        int frameSpeedY = Math.round(baseSpeedY);

        int nextX = x;
        int nextY = y;

        if (isUp)    nextY -= frameSpeedY;
        if (isDown)  nextY += frameSpeedY;
        if (isLeft)  nextX -= frameSpeedX;
        if (isRight) nextX += frameSpeedX;

        nextX = (int) processing.core.PApplet.constrain(nextX, 0, currentWidth);
        nextY = (int) processing.core.PApplet.constrain(nextY, 0, currentHeight);

        int rX = (int) (radiusX * ((float) currentWidth / 1024f)); 
        int rY = (int) (radiusY * ((float) currentHeight / 461f)); 

        int oldX = x;
        int oldY = y;

        boolean canMoveX = isWalkable(nextX - rX, y) && isWalkable(nextX + rX, y);
        boolean canMoveY = isWalkable(x, nextY - rY) && isWalkable(x, nextY + rY);

        if (isMoving) {
            if (!canMoveX || !canMoveY) {
                if (!isJustCollided) {
                    isJustCollided = true; 
                }
            }
        } else {
            isJustCollided = false; 
        }

        if (canMoveX) x = nextX;
        if (canMoveY) y = nextY;

        int currentTime = (int) System.currentTimeMillis();

        if (isMoving) {
            if (x != oldX || y != oldY) {
                lastMoveTime = currentTime;
                timerStarted = false; 
                
                if (currentSpeedMultiplier < MAX_SPEED) {
                    currentSpeedMultiplier += ACCELERATION;
                    if (currentSpeedMultiplier > MAX_SPEED) currentSpeedMultiplier = MAX_SPEED;
                }
            } else {
                if (targetHP > 0) { 
                    if (!timerStarted) {
                        lastMoveTime = currentTime;
                        timerStarted = true;
                    }
                    
                    if (currentTime - lastMoveTime > 1000) {
                        currentSpeedMultiplier = BASE_SPEED;
                    }
                } else {
                    if (currentSpeedMultiplier < MAX_SPEED) {
                        currentSpeedMultiplier += ACCELERATION;
                    }
                }
            }
        } else {
            timerStarted = false;
            if (currentSpeedMultiplier > BASE_SPEED) {
                currentSpeedMultiplier -= DECELERATION;
                if (currentSpeedMultiplier < BASE_SPEED) currentSpeedMultiplier = BASE_SPEED;
            }
        }
    }

    public boolean isWalkable(float checkX, float checkY) {
        int col = (int) (checkX / tileWidth);
        int row = (int) (checkY / tileHeight);

        if (row < 0 || row >= MAP_MATRIX.length || col < 0 || col >= MAP_MATRIX[0].length) {
            return false;
        }

        // XXL Custom Bounding Solid Wall Logic
        if (targetHP > 0 && row == 16 && (col >= 22 && col <= 26)) {
            return false;
        }

        // ==========================================
        // 💡 Shu solid obstruction logic block
        // ==========================================
        if (shuHP > 0 && row == shuRow && col == shuCol) {
            return false;
        }

        return MAP_MATRIX[row][col] == 0;
    }

    public float getCurrentSpeed() {
        return this.currentSpeedMultiplier;
    }

    public void resetSpeedAndTimer() {
        this.currentSpeedMultiplier = BASE_SPEED; 
        this.timerStarted = false;               
        this.isJustCollided = false;             
    }

    public float getDamage() {
        if (currentSpeedMultiplier <= BASE_SPEED) return 5.0f;
        if (currentSpeedMultiplier >= MAX_SPEED) return 50.0f;
        return 5.0f + ((currentSpeedMultiplier - BASE_SPEED) / (MAX_SPEED - BASE_SPEED)) * (50.0f - 5.0f);
    }
}