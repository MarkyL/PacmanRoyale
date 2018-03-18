package com.example.mark.pacmanroyale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.User.Ghost;
import com.example.mark.pacmanroyale.User.Pacman;
import com.example.mark.pacmanroyale.User.UserInformation;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.example.mark.pacmanroyale.Utilities.VirtualRoomUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Mark on 14/02/2018.
 */

public class DrawingView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private static final String TAG = "DrawingView";
    public static final int BLOCK_SIZE_DIVIDER = 17;
    private static final int XYDelta = 45;

    private static final int GHOST1_SPEED = 20;
    private static final int GHOST2_SPEED = 20;
    private static final int GHOST3_SPEED = 20;
    private static final int DEFAULT_VISIBILITY = 255 ;
    private static final int PACMAN_SPEED = 10;

    private Thread mThread;
    private SurfaceHolder mSurfaceHolder;
    private boolean canDraw;

    private Context mContext;
    private Canvas mCanvas;

    private DatabaseReference mVirtualRoomReference;
    private DatabaseReference virtualRoomGhostReference;
    private DatabaseReference virtualRoomPacmanReference;

    private Paint mPaint;
    private Bitmap[] mPacmanRight, mPacmanDown, mPacmanLeft, mPacmanUp;

    private int currentControlledGhost = 1; // 1 is the default starting ghost.
    private int pendingControlledGhost = 1; // 1 is the default starting ghost.
    private boolean mGhostControlChanged = false;

    private Bitmap mGhost1Bitmap;
    private Bitmap mGhostBitmapSelected;
    private Bitmap mGhost2BitmapSelected;
    private Bitmap mGhost3BitmapSelected;

    private Bitmap mCurrentGhost1Bitmap;
    private Bitmap mCurrentGhost2Bitmap;
    private Bitmap currentGhost3Bitmap;

    private Bitmap ghost2Bitmap;
    private Bitmap ghost3Bitmap;

    private int visibility;
    public boolean mGoThroughTunnelEnabled = false;

    final MediaPlayer mEatFruitSound;

    private int currentPelletsAmount = 0;
    private int totalPellets;
    private String pelletsPercentage;
    private boolean isFirstTimePellet = true;

    private int totalFrame = 4; // amount of frames for each direction
    private int currentPacmanFrame = 0;     // Current Pacman frame to draw
    private int currentArrowFrame = 0;      // Current arrow frame to draw
    private long frameTicker;               // Current time since last frame has been drawn

    private Pacman mPacman;
    private int xPosPacman;                 // x-axis position of pacman
    private int yPosPacman;                 // y-axis position of pacman

    private GameMode gameMode;

    private int xPosGhost;                  // x-axis position of ghost
    private int yPosGhost;                  // y-axis position of ghost

    private int xPosGhost2;
    private int yPosGhost2;

    private int xPosGhost3;
    private int yPosGhost3;

    int xDistance;
    int yDistance;

    private float x1, x2, y1, y2;           // Initial/Final positions of swipe

    //private int direction = 4;              // Direction of the swipe, initial direction is right
    private int nextDirection = 4;          // Buffer for the next direction you choose
    private int viewDirection = 2;          // Direction that pacman is facing

    private int viewDirectionEnemyPacman = 4;// Direction that enemy pacman is facing
    private int ghostDirection;
    private int ghost2Direction;
    private int ghost3Direction;
    private int pacmanDirection = 4;

    private boolean isGhost2Turn;

    private int screenWidth;                // Width of the phone screen
    private int blockSize;                  // Size of a block on the map
    private int enemyBlockSize;
    public static int LONG_PRESS_TIME = 750;  // Time in milliseconds
    private int spriteSize;
    public static int GHOST2_DELAY = 750;  // Time in milliseconds
    public static int GHOST3_DELAY = 1250;  // Time in milliseconds
    private int currentScore = 0;           //Current game score

    final Handler handler = new Handler();
//    Runnable longPressed = new Runnable() {
//        public void run() {
////            Log.i("info", "LongPress");
////            Intent pauseIntent = new Intent(getContext(), MainActivity.class);
////            getContext().startActivity(pauseIntent);
//        }
//    };

    private int mEnemyGhostDirection;
    private int mEnemyPacmanDirection;
    private boolean isGhostDirectionChanged;
    private boolean isPacmanDirectionChanged;
    private int yPosPacmanForEnemy;
    private int xPosPacmanForEnemy;
    private int xPosGhostForEnemy;
    private int yPosGhostForEnemy;

    private int xPosGhost2ForEnemy;
    private int yPosGhost2ForEnemy;

    private int xPosGhost3ForEnemy;
    private int yPosGhost3ForEnemy;

    private boolean firstTime = true;
    private Iinterface myInterface;
    private boolean isSwiped = false;
    private boolean isGameOver =false;
    private boolean isSFX;

    // Initializing the member variables
    public DrawingView(Context context, GameMode gameMode) {
        super(context);
        this.mContext =context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        frameTicker = 1000 / totalFrame;
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        blockSize = screenWidth / BLOCK_SIZE_DIVIDER;
        blockSize = (blockSize / 5) * 5;

        this.gameMode = gameMode;

        initGhostStartingParams();

        isSFX = UserInformationUtils.getUserInformation().isSfx();

        xPosPacman = 8 * blockSize;
        yPosPacman = 13 * blockSize;

        if (gameMode == GameMode.GHOST || gameMode == GameMode.PACMAN) {
            mVirtualRoomReference = VirtualRoomUtils.getVirtualRoomReference();
            virtualRoomGhostReference = mVirtualRoomReference.child(getResources().getString(R.string.ghost_node));
            virtualRoomPacmanReference = mVirtualRoomReference.child(getResources().getString(R.string.pacman_node));
        }
            if (gameMode == GameMode.PACMAN) {
                listenToGhostPosOnFireBase();
                goInvisible(DEFAULT_VISIBILITY);
            } else if (gameMode == GameMode.GHOST) {
                listenToPacmanPosOnFireBase();
            }
            else {
                goInvisible(DEFAULT_VISIBILITY);
            }

        loadBitmapImages();
        myInterface = (Iinterface) context;

        mEatFruitSound = MediaPlayer.create(getContext(), R.raw.pacman_eatfruit);
    }

    private void goThroughTunnels(boolean goThroughTunnelEnabled) {
        mGoThroughTunnelEnabled = goThroughTunnelEnabled;
    }

    public void goInvisible(int visibility){
        this.visibility = visibility;
        if(gameMode == GameMode.PACMAN)
            virtualRoomPacmanReference.child(getResources().getString(R.string.invisible)).setValue(this.visibility);
    }

    private void initGhostStartingParams() {
        xPosGhost = 8 * blockSize;
        yPosGhost = 4 * blockSize;

        xPosGhost2 = blockSize;
        yPosGhost2 = 2 * blockSize;

        xPosGhost3 = 16 * blockSize;
        yPosGhost3 = 2 * blockSize;

        ghostDirection = 4;
        ghost2Direction = 4;
        ghost3Direction = 4;
    }

    //virtualRoomGhostReference
    private void listenToGhostPosOnFireBase() {
        virtualRoomGhostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "listenToGhostPosOnFireBase() onDataChange ");
                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos))
                        && dataSnapshot.hasChild(getResources().getString(R.string.yPos))) {
                    xPosGhost = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos)).getValue().toString());
                    yPosGhost = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos)).getValue().toString());
                }
                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos2))
                        && dataSnapshot.hasChild(getResources().getString(R.string.yPos2))) {
                    xPosGhost2 = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos2)).getValue().toString());
                    yPosGhost2 = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos2)).getValue().toString());
                }
                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos3))
                        && dataSnapshot.hasChild(getResources().getString(R.string.yPos3))) {
                    xPosGhost3 = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos3)).getValue().toString());
                    yPosGhost3 = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos3)).getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setCanDrawState(boolean state) {
        this.canDraw = state;
    }

    private void listenToPacmanPosOnFireBase() {
        virtualRoomPacmanReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //mPacman = dataSnapshot.getValue(Pacman.class);
                Log.d(TAG, "onDataChange() mPacman Key = " + dataSnapshot.getKey());
                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos))) {
                    xPosPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos)).getValue().toString());
                    Log.d(TAG, "onDataChange() x = " + xPosPacman);
                }
                if (dataSnapshot.hasChild(getResources().getString(R.string.yPos))) {
                    yPosPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos)).getValue().toString());
                    Log.d(TAG, "onDataChange() y = " + yPosPacman);
                }

                if (dataSnapshot.hasChild(getResources().getString(R.string.pacmanDirection))) {
                    viewDirectionEnemyPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.pacmanDirection)).getValue().toString());
                    Log.d(TAG, "onDataChange: direction is: " + viewDirectionEnemyPacman);
                }

                if(dataSnapshot.hasChild(getResources().getString(R.string.invisible))){
                    visibility = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.invisible)).getValue().toString());
                }

                if(dataSnapshot.hasChild(getResources().getString(R.string.matchOver))){
                    // isPacman = true , means Pacman ate all the pellets and won
                    gameOver(true);
                }

                if(dataSnapshot.hasChild(getResources().getString(R.string.pelletspercentage))){
                    pelletsPercentage = dataSnapshot.child(getResources().getString(R.string.pelletspercentage)).getValue().toString();
                    myInterface.setPercentage(pelletsPercentage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "Surface Created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "Surface Changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "Surface Destroyed");
    }

    public void resume() {
        Log.i("info", "resume");
        if (mThread != null) {
            mThread.start();
        }
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
            Log.i("info", "resume thread");
        }
        canDraw = true;
        //startUpdatingFireBase();
    }

    private void startUpdatingFireBase() {
        final Thread updateFBThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (canDraw) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updatePositionsToDB();
                }
            }
        });
        updateFBThread.start();
    }

    public void pause() {
        Log.d(TAG, "pause");
        canDraw = false;
        mThread = null;
    }

    // Method to get touch events
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN): {
                x1 = event.getX();
                y1 = event.getY();
                //handler.postDelayed(longPressed, LONG_PRESS_TIME);
            }
            break;
            case (MotionEvent.ACTION_UP): {
                if (gameMode == GameMode.GHOST && !mGoThroughTunnelEnabled && !isGhostCloseToTunnel()) {
                    isNewGhostSelected(event.getX(), event.getY());
                }
                isSwiped = true;
                x2 = event.getX();
                y2 = event.getY();
                calculateSwipeDirection();
                //handler.removeCallbacks(longPressed);
            }
            break;
        }
        return true;
    }

    private boolean isGhostCloseToTunnel() {
        switch (currentControlledGhost) {
            case 1: {
                if ((xPosGhost >= 16 * blockSize || xPosGhost <= blockSize) && yPosGhost == 8 * blockSize) {
                    return true;
                }
            } break;
            case 2: {
                if ((xPosGhost2 >= 16 * blockSize || xPosGhost2 <= blockSize) && yPosGhost2 == 8 * blockSize) {
                    return true;
                }
            } break;
            case 3: {
                if ((xPosGhost3 >= 16 * blockSize || xPosGhost3 <= blockSize) && yPosGhost3 == 8 * blockSize) {
                    return true;
                }
            } break;
        }
        return false;
    }

    private void isNewGhostSelected(float x, float y) {
        final int extraDelta = 10;
        // see if ghost 1 was selected
        if ((x + mGhost1Bitmap.getWidth() + extraDelta >= xPosGhost && x - mGhost1Bitmap.getWidth() - extraDelta <= xPosGhost)
                && (y + mGhost1Bitmap.getHeight() + extraDelta >= yPosGhost && y - mGhost1Bitmap.getHeight() - extraDelta <= yPosGhost)) {
            if (currentControlledGhost == 1) {
                return;
            }
            pendingControlledGhost = 1;
            //moveGhostToCenterOfABlock(1);
            mCurrentGhost1Bitmap = mGhostBitmapSelected;
            mCurrentGhost2Bitmap = ghost2Bitmap;
            currentGhost3Bitmap = ghost3Bitmap;
            mGhostControlChanged = true;
        }
        // see if ghost 2 was selected
        if ((x + ghost2Bitmap.getWidth() + extraDelta >= xPosGhost2 && x - ghost2Bitmap.getWidth() - extraDelta <= xPosGhost2)
                && (y + ghost2Bitmap.getHeight() + extraDelta >= yPosGhost2 && y - ghost2Bitmap.getHeight() - extraDelta <= yPosGhost2)) {
            if (currentControlledGhost == 2) {
                return;
            }
            pendingControlledGhost = 2;
            //moveGhostToCenterOfABlock(2);
            mCurrentGhost1Bitmap = mGhost1Bitmap;
            mCurrentGhost2Bitmap = mGhost2BitmapSelected;
            currentGhost3Bitmap = ghost3Bitmap;
            mGhostControlChanged = true;
        }
        if (currentControlledGhost == 3) {
            return;
        }
        // see if ghost 3 was selected
        if ((x + ghost3Bitmap.getWidth() + extraDelta >= xPosGhost3 && x - ghost3Bitmap.getWidth() - extraDelta <= xPosGhost3)
                && (y + ghost3Bitmap.getHeight() + extraDelta >= yPosGhost3 && y - ghost3Bitmap.getHeight() - extraDelta <= yPosGhost3)) {
            pendingControlledGhost = 3;
            //moveGhostToCenterOfABlock(3);
            mCurrentGhost1Bitmap = mGhost1Bitmap;
            mCurrentGhost2Bitmap = ghost2Bitmap;
            currentGhost3Bitmap = mGhost3BitmapSelected;
            mGhostControlChanged = true;
        }
    }

    private void moveGhostToCenterOfABlock(int ghostNumber) {
        // tests for ghost 2.
        switch (ghostNumber) {
            case 1: {
                if (xPosGhost % blockSize == 0 && yPosGhost % blockSize == 0) {
                    return; // it's ok.
                }

                switch (ghostDirection) {
                    case 0: {
                        // moving Up , y should be rounded up to closest blockSize
                        yPosGhost -= yPosGhost % blockSize;
                    }
                    break;
                    case 1: {
                        // moving right , x should be rounded up to closest blockSize
                        xPosGhost += xPosGhost % blockSize;
                    }
                    break;
                    case 2: {
                        // moving down , y should be floored to closest blockSize
                        yPosGhost += yPosGhost % blockSize;
                    }
                    break;
                    case 3: {
                        // moving left , x should be floored to closest blockSize
                        xPosGhost -= xPosGhost % blockSize;
                    }
                    break;
                }
            }
            break;
            case 2: {
                if (xPosGhost2 % blockSize == 0 && yPosGhost2 % blockSize == 0) {
                    return; // it's ok.
                }
                switch (ghost2Direction) {
                    case 0: {
                        // moving Up , y should be rounded up to closest blockSize
                        yPosGhost2 -= yPosGhost2 % blockSize;
                    }
                    break;
                    case 1: {
                        // moving right , x should be rounded up to closest blockSize
                        xPosGhost2 += xPosGhost2 % blockSize;
                    }
                    break;
                    case 2: {
                        // moving down , y should be floored to closest blockSize
                        yPosGhost2 += yPosGhost2 % blockSize;
                    }
                    break;
                    case 3: {
                        // moving left , x should be floored to closest blockSize
                        xPosGhost2 -= xPosGhost2 % blockSize;
                    }
                    break;
                }
            }
            break;
            case 3: {
                if (xPosGhost3 % blockSize == 0 && yPosGhost3 % blockSize == 0) {
                    return; // it's ok.
                }
                switch (ghost3Direction) {
                    case 0: {
                        // moving Up , y should be rounded up to closest blockSize
                        yPosGhost3 -= yPosGhost3 % blockSize;
                    }
                    break;
                    case 1: {
                        // moving right , x should be rounded up to closest blockSize
                        xPosGhost3 += xPosGhost3 % blockSize;
                    }
                    break;
                    case 2: {
                        // moving down , y should be floored to closest blockSize
                        yPosGhost3 += yPosGhost3 % blockSize;
                    }
                    break;
                    case 3: {
                        // moving left , x should be floored to closest blockSize
                        xPosGhost3 -= xPosGhost3 % blockSize;
                    }
                    break;
                }
            }
            break;
        }
    }

    /*
    if (x >= xOfYourBitmap && x < (xOfYourBitmap + yourBitmap.getWidth())
                && y >= yOfYourBitmap && y < (yOfYourBitmap + yourBitmap.getHeight())) {
            //tada, if this is true, you've started your click inside your bitmap
        }
     */

    // Calculates which direction the user swipes
    // based on calculating the differences in
    // initial position vs final position of the swipe
    private void calculateSwipeDirection() {
        float xDiff = (x2 - x1);
        float yDiff = (y2 - y1);

        // Directions
        // 0 means going up
        // 1 means going right
        // 2 means going down
        // 3 means going left
        // 4 means stop moving, look at move function

        // Checks which axis has the greater distance
        // in order to see which direction the swipe is
        // going to be (buffering of direction)
        if (Math.abs(yDiff) > Math.abs(xDiff)) {
            if (yDiff < 0) {
                nextDirection = 0;
            } else if (yDiff > 0) {
                nextDirection = 2;
            }
        } else {
            if (xDiff < 0) {
                nextDirection = 3;
            } else if (xDiff > 0) {
                nextDirection = 1;
            }
        }
    }

    public void setNextDirection(int nextDirection) {
        this.nextDirection = nextDirection;
    }

    @Override
    public void run() {
        Log.d(TAG, "run method starts");
        while (canDraw) {
            Log.d(TAG, "run: canDraw = true");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!mSurfaceHolder.getSurface().isValid()) {
                continue; // need to check what it does - and why/do we need it.
            }

//            if (gameMode == GameMode.VS_PC && xPosGhost == xPosPacman && yPosGhost == yPosPacman) {
//                Log.d(TAG, "run: Game Over!");
//                canDraw = false;
//            }

            boolean xGhostCought = (xPosPacman <= xPosGhost + XYDelta && xPosPacman >= xPosGhost - XYDelta);
            boolean xGhost2Cought= (xPosPacman <= xPosGhost2 + XYDelta && xPosPacman >= xPosGhost2 - XYDelta);
            boolean xGhost3Cought = (xPosPacman <= xPosGhost3 + XYDelta && xPosPacman >= xPosGhost3 - XYDelta);

            boolean yGhostCought = (yPosPacman <= yPosGhost + XYDelta && yPosPacman >= yPosGhost - XYDelta);
            boolean yGhost2Cought = (yPosPacman <= yPosGhost2 + XYDelta && yPosPacman >= yPosGhost2 - XYDelta);
            boolean yGhost3Cought = (yPosPacman <= yPosGhost3 + XYDelta && yPosPacman >= yPosGhost3 - XYDelta);

            if (((xGhostCought && yGhostCought) || (xGhost2Cought && yGhost2Cought) || (xGhost3Cought && yGhost3Cought)) && (visibility == DEFAULT_VISIBILITY)) {
                String gameOverMessage = gameMode == GameMode.PACMAN ? "Game Over!" : "Well Played Ghost!";
                Log.d(TAG, "" + gameOverMessage);
                // isPacman = false , means Pacman was cought by a ghost
                gameOver(false);
            }

            if (enemyBlockSize == 0) {
                enemyBlockSize = UserInformationUtils.getEnemyBlockSize();
                if (enemyBlockSize != 0) {
                    Log.d(TAG, "run: enemyBlockSize != 0");
                    initEnemyPosVariables();
                }
            }
            mCanvas = mSurfaceHolder.lockCanvas();
            // Set background color to Transparent
            if (mCanvas != null) {
                mCanvas.drawColor(Color.BLACK);

                drawMap();

                updateFrame(System.currentTimeMillis());

                if (gameMode == GameMode.PACMAN || gameMode == GameMode.GHOST) {
                    if (gameMode == GameMode.PACMAN) {
                        moveCharacter(xPosPacman, yPosPacman, pacmanDirection);
                    } else {
                        switch (currentControlledGhost) {
                            case 1: {
                                moveCharacter(xPosGhost, yPosGhost, ghostDirection);
                            }
                            break;
                            case 2: {
                                moveCharacter(xPosGhost2, yPosGhost2, ghost2Direction);
                            }
                            break;
                            case 3: {
                                moveCharacter(xPosGhost3, yPosGhost3, ghost3Direction);
                            }
                            break;
                        }
                    }
                } else {
                    moveGhostWithAI(xPosGhost, yPosGhost, ghostDirection, GHOST1_SPEED, 1, mCurrentGhost1Bitmap);
                    moveGhostWithAI(xPosGhost2, yPosGhost2, ghost2Direction, GHOST2_SPEED, 2, mCurrentGhost2Bitmap);
                    moveGhostWithAI(xPosGhost3, yPosGhost3, ghost3Direction, GHOST3_SPEED, 3, currentGhost3Bitmap);
                    // Moves the pacman based on his direction
                    movePacman();
                }

                // Draw the pellets
                if (gameMode != GameMode.GHOST) {
                    drawPellets();
                }

                //updatePositionsToDB();

                mSurfaceHolder.unlockCanvasAndPost(mCanvas);

                if (firstTime) {
                    firstTime = false;
                    myInterface.setVisibilities();
                }
            }
        }
    }


    public interface Iinterface {
        void setVisibilities();
        void endGame(boolean isPacman);
        void setPercentage(String percentage);
    }

    public void initEnemyPosVariables() {
        Log.d(TAG, "initEnemyPosVariables() starting...");
        if (gameMode == GameMode.GHOST) {
            if (xPosGhostForEnemy == 0 || yPosGhostForEnemy == 0) {
                xPosGhostForEnemy = 8 * enemyBlockSize;
                yPosGhostForEnemy = 4 * enemyBlockSize;
            }
            if (xPosGhost2ForEnemy == 0 || yPosGhost2ForEnemy == 0) {
                xPosGhost2ForEnemy = 1 * enemyBlockSize;
                yPosGhost2ForEnemy = 2 * enemyBlockSize;
            }
            if (xPosGhost3ForEnemy == 0 || yPosGhost3ForEnemy == 0) {
                xPosGhost3ForEnemy = 16 * enemyBlockSize;
                yPosGhost3ForEnemy = 2 * enemyBlockSize;
            }
        } else if (gameMode == GameMode.PACMAN) { // enemy = pacman
            if (xPosPacmanForEnemy == 0 || yPosPacmanForEnemy == 0) {
                xPosPacmanForEnemy = 8 * enemyBlockSize;
                yPosPacmanForEnemy = 13 * enemyBlockSize;
            }
        }
        //updatePositionsToDB();
    }

    private void updatePositionsToDB() {

        if (gameMode == GameMode.PACMAN) {
            Log.d(TAG, "updatePositionsToDB: update enemy pacman x=" + xPosPacmanForEnemy + " y=" + yPosPacmanForEnemy);
            if (xPosPacmanForEnemy != 0 && yPosPacmanForEnemy != 0) {
                virtualRoomPacmanReference.child(getResources().getString(R.string.xPos)).setValue(xPosPacmanForEnemy);
                virtualRoomPacmanReference.child(getResources().getString(R.string.yPos)).setValue(yPosPacmanForEnemy);
                if (pacmanDirection != 4) {
                    virtualRoomPacmanReference.child(getContext().getString(R.string.pacmanDirection)).setValue(pacmanDirection);
                }
            }

        } else if (gameMode == GameMode.GHOST) {
            Log.d(TAG, "updatePositionsToDB: update enemy ghost positions");

            // Ghost #1
            virtualRoomGhostReference.child(getResources().getString(R.string.xPos)).setValue(xPosGhostForEnemy);
            virtualRoomGhostReference.child(getResources().getString(R.string.yPos)).setValue(yPosGhostForEnemy);

            // Ghost #2
            virtualRoomGhostReference.child(getResources().getString(R.string.xPos2)).setValue(xPosGhost2ForEnemy);
            virtualRoomGhostReference.child(getResources().getString(R.string.yPos2)).setValue(yPosGhost2ForEnemy);

            // Ghost #2
            virtualRoomGhostReference.child(getResources().getString(R.string.xPos3)).setValue(xPosGhost3ForEnemy);
            virtualRoomGhostReference.child(getResources().getString(R.string.yPos3)).setValue(yPosGhost3ForEnemy);
        }
    }

    // Method that draws pellets and updates them when eaten
    private void drawPellets() {
        float x;
        float y;
        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 17; j++) {
                x = j * blockSize;
                y = i * blockSize;
                // Draws pellet in the middle of a block
                if ((leveldata1[i][j] & 16) != 0) {
                    if (isFirstTimePellet) {
                        currentPelletsAmount++;
                        totalPellets = currentPelletsAmount;
                    }
                    mCanvas.drawCircle(x + blockSize / 2, y + blockSize / 2, blockSize / 10, mPaint);
                }
            }
        }
        isFirstTimePellet = false;
    }

    // Updates the character sprite and handles collisions
    public void moveCharacter(int charXPos, int charYPos, int direction) {
        short ch;

        if (gameMode == GameMode.GHOST && mGhostControlChanged) {
            mGhostControlChanged = false;
            setCurrentControlledGhost();
            //moveGhostToCenterOfABlock(currentControlledGhost);
            return;
        }

        // Check if xPos and yPos of pacman is both a multiple of block size
        if ((charXPos % blockSize == 0) && (charYPos % blockSize == 0)) {

            // When pacman goes through tunnel on
            // the right reappear at left tunnel
            if (charXPos >= blockSize * 17) {
                if (gameMode == GameMode.PACMAN) {
                    charXPos = 0;
                    xPosPacmanForEnemy = 0;
                } else { // game mode is ghost.
                    if (mGoThroughTunnelEnabled) { // ghost is using SKILL!
                        charXPos = 0;
                        ghostWentThroughTunnel(false);
                    } else { // ghost is not using SKILL!
                        charXPos = blockSize * 16;
                        ghostBumpInWall(false);
                    }
                }
            }
            if (charXPos < 0) {
                // When pacman goes through tunnel on
                // the left reappear at right tunnel
                if (gameMode == GameMode.PACMAN) {
                    charXPos = blockSize * 16;
                    xPosPacmanForEnemy = enemyBlockSize * 16;
                } else { // game mode is ghost.
                    if (mGoThroughTunnelEnabled) {
                        charXPos = blockSize * 16;
                        ghostWentThroughTunnel(true);
                    } else {
                        charXPos = blockSize;
                        ghostBumpInWall(true);
                    }
                }
            }

            // Is used to find the number in the level array in order to
            // check wall placement, pellet placement, and candy placement
            ch = leveldata1[charYPos / blockSize][charXPos / blockSize];

            // Checks for direction buffering
            if (!((nextDirection == 3 && (ch & 1) != 0) ||
                    (nextDirection == 1 && (ch & 4) != 0) ||
                    (nextDirection == 0 && (ch & 2) != 0) ||
                    (nextDirection == 2 && (ch & 8) != 0))) {
                viewDirection = direction = nextDirection;
            }

            // Checks for wall collisions
            if ((direction == 3 && (ch & 1) != 0) ||
                    (direction == 1 && (ch & 4) != 0) ||
                    (direction == 0 && (ch & 2) != 0) ||
                    (direction == 2 && (ch & 8) != 0)) {
                direction = 4;
            }

            if (gameMode == GameMode.PACMAN) {
                // If there is a pellet, eat it
                if ((ch & 16) != 0) {
                    // Toggle pellet so it won't be drawn anymore
                    leveldata1[charYPos / blockSize][charXPos / blockSize] = (short) (ch ^ 16);
                    if (currentPelletsAmount % 5 == 0 && isSFX) {
                        mEatFruitSound.start();
                    }
                    if (--currentPelletsAmount <= 0 && (gameMode != GameMode.GHOST)) {
                        virtualRoomPacmanReference.child(getResources().getString(R.string.matchOver)).setValue(true);
                        // isPacman = true , means Pacman ate all the pellets and won
                        gameOver(true);
                    }
                    float pelletsEatenPercentage = (float)(totalPellets - currentPelletsAmount) / totalPellets;
                    pelletsEatenPercentage *= 100;
                    int pelletsEatenInt = (int)pelletsEatenPercentage;
                    //String pelletsEatenPercentageSTR = String.format("%f", pelletsEatenPercentage);
                    myInterface.setPercentage(String.valueOf(pelletsEatenInt));
                    virtualRoomPacmanReference.child(getResources().getString(R.string.pelletspercentage)).setValue(pelletsEatenInt);
                    currentScore += 10;
                }
            }
        }

        if (gameMode == GameMode.PACMAN) {
            xPosPacman = charXPos;
            yPosPacman = charYPos;
            pacmanDirection = direction;

            if (enemyBlockSize != 0) {
                updatePacmanPositions();
            }
            mCanvas.drawBitmap(mGhost1Bitmap, xPosGhost, yPosGhost, mPaint);
            mCanvas.drawBitmap(ghost2Bitmap, xPosGhost2, yPosGhost2, mPaint);
            mCanvas.drawBitmap(ghost3Bitmap, xPosGhost3, yPosGhost3, mPaint);
            drawPacman();
        } else {
            // i'll update my direction - so the enemy can draw me on the right position as well.
            updateControlledGhostPositions(charXPos, charYPos, direction);
            if (enemyBlockSize != 0) {
                moveControlledGhostPosition();
            }
            if (isSwiped) {
                moveUncontrolledGhostsWithAI();
            } else {
                mCanvas.drawBitmap(ghost2Bitmap, xPosGhost2, yPosGhost2, mPaint);
                mCanvas.drawBitmap(ghost3Bitmap, xPosGhost3, yPosGhost3, mPaint);
            }
            mCanvas.drawBitmap(mGhost1Bitmap, xPosGhost, yPosGhost, mPaint);
            drawPacman();
            drawRelevantGhostBitmap();
        }
    }

    private void ghostBumpInWall(boolean isLeftTunnel) {
        switch (currentControlledGhost) {
            case 1: {
                xPosGhostForEnemy = isLeftTunnel ? enemyBlockSize : enemyBlockSize * 16;
            } break;
            case 2: {
                xPosGhost2ForEnemy = isLeftTunnel ? enemyBlockSize : enemyBlockSize * 16;
            } break;
            case 3: {
                xPosGhost3ForEnemy = isLeftTunnel ? enemyBlockSize : enemyBlockSize * 16;
            } break;
        }
    }

    private void ghostWentThroughTunnel(boolean isLeftTunnel) {
        switch (currentControlledGhost) {
            case 1: {
                xPosGhostForEnemy = isLeftTunnel ? enemyBlockSize * 16 : 0;
            } break;
            case 2: {
                xPosGhost2ForEnemy = isLeftTunnel ? enemyBlockSize * 16 : 0;
            } break;
            case 3: {
                xPosGhost2ForEnemy = isLeftTunnel ? enemyBlockSize * 16 : 0;
            } break;
        }
    }

    private void gameOver(boolean isPacman) {
        // stop drawing , game is over.
        canDraw = false;
        //update pacman/ghost statistics
        if(gameMode != GameMode.VS_PC) {
            updateStatistics(isPacman);
        }
        // show relevant end game dialog to each of the players
        myInterface.endGame(isPacman);
    }

    private void updateStatistics(boolean isPacman) {

        UserInformation theUser = UserInformationUtils.getUserInformation();
        Pacman usersPacman = theUser.getPacman();
        Ghost usersGhost = theUser.getGhost();

        if(isPacman) {// means Pacman won so we add 1 win to Pacman and 1 game to totalGames of Ghost/Pacman and update win Ratio for both
            if (gameMode == GameMode.PACMAN) {
                usersPacman.setWins(usersPacman.getWins()+1);
                usersPacman.setTotalGames(usersPacman.getTotalGames()+1);
                UserInformationUtils.setUserInformation(theUser);
                UserInformationUtils.updateUsersPacmanWins(mContext);
                UserInformationUtils.updateUsersPacmanTotalGames(mContext);
                UserInformationUtils.updateUsersPacmanWinRatio(mContext);

            } else if (gameMode == GameMode.GHOST) {
                usersGhost.setTotalGames(usersGhost.getTotalGames()+1);
                UserInformationUtils.setUserInformation(theUser);
                UserInformationUtils.updateUsersGhostTotalGames(mContext);
                UserInformationUtils.updateUsersGhostWinRatio(mContext);
            }
        }
        else{// means Ghost won so we add 1 win to Ghost and 1 game to totalGames of Ghost/Pacman and update win Ratio for both
            if (gameMode == GameMode.PACMAN) {
                usersPacman.setTotalGames(usersPacman.getTotalGames()+1);
                UserInformationUtils.setUserInformation(theUser);
                UserInformationUtils.updateUsersPacmanTotalGames(mContext);
                UserInformationUtils.updateUsersPacmanWinRatio(mContext);

            } else if (gameMode == GameMode.GHOST) {
                usersGhost.setWins(usersGhost.getWins()+1);
                usersGhost.setTotalGames(usersGhost.getTotalGames()+1);
                UserInformationUtils.setUserInformation(theUser);
                UserInformationUtils.updateUsersGhostWins(mContext);
                UserInformationUtils.updateUsersGhostTotalGames(mContext);
                UserInformationUtils.updateUsersGhostWinRatio(mContext);
            }
        }
    }

    private void setCurrentControlledGhost() {
        switch (pendingControlledGhost) {
            case 1: {
                currentControlledGhost = 1;
            }
            break;
            case 2: {
                currentControlledGhost = 2;
            }
            break;
            case 3: {
                currentControlledGhost = 3;
            }
            break;
        }
    }

    private void moveControlledGhostPosition() {
        switch (currentControlledGhost) {
            case 1: {
                if (ghostDirection == 0) {
                    yPosGhost += -blockSize / GHOST1_SPEED;
                    yPosGhostForEnemy += -enemyBlockSize / GHOST1_SPEED;
                } else if (ghostDirection == 1) {
                    xPosGhost += blockSize / GHOST1_SPEED;
                    xPosGhostForEnemy += enemyBlockSize / GHOST1_SPEED;
                } else if (ghostDirection == 2) {
                    yPosGhost += blockSize / GHOST1_SPEED;
                    yPosGhostForEnemy += enemyBlockSize / GHOST1_SPEED;
                } else if (ghostDirection == 3) {
                    xPosGhost += -blockSize / GHOST1_SPEED;
                    xPosGhostForEnemy += -enemyBlockSize / GHOST1_SPEED;
                }
            }
            break;
            case 2: {
                if (ghost2Direction == 0) {
                    yPosGhost2 += -blockSize / GHOST2_SPEED;
                    yPosGhost2ForEnemy += -enemyBlockSize / GHOST2_SPEED;
                } else if (ghost2Direction == 1) {
                    xPosGhost2 += blockSize / GHOST2_SPEED;
                    xPosGhost2ForEnemy += enemyBlockSize / GHOST2_SPEED;
                } else if (ghost2Direction == 2) {
                    yPosGhost2 += blockSize / GHOST2_SPEED;
                    yPosGhost2ForEnemy += enemyBlockSize / GHOST2_SPEED;
                } else if (ghost2Direction == 3) {
                    xPosGhost2 += -blockSize / GHOST2_SPEED;
                    xPosGhost2ForEnemy += -enemyBlockSize / GHOST2_SPEED;
                }
            }
            break;
            case 3: {
                if (ghost3Direction == 0) {
                    yPosGhost3 += -blockSize / GHOST3_SPEED;
                    yPosGhost3ForEnemy += -enemyBlockSize / GHOST3_SPEED;
                } else if (ghost3Direction == 1) {
                    xPosGhost3 += blockSize / GHOST3_SPEED;
                    xPosGhost3ForEnemy += enemyBlockSize / GHOST3_SPEED;
                } else if (ghost3Direction == 2) {
                    yPosGhost3 += blockSize / GHOST3_SPEED;
                    yPosGhost3ForEnemy += enemyBlockSize / GHOST3_SPEED;
                } else if (ghost3Direction == 3) {
                    xPosGhost3 += -blockSize / GHOST3_SPEED;
                    xPosGhost3ForEnemy += -enemyBlockSize / GHOST3_SPEED;
                }
            }
            break;
        }
        updatePositionsToDB();
    }

    private void updatePacmanPositions() {
        if (pacmanDirection == 0) {
            yPosPacman += -blockSize / PACMAN_SPEED;
            yPosPacmanForEnemy += -enemyBlockSize / PACMAN_SPEED;
        } else if (pacmanDirection == 1) {
            xPosPacman += blockSize / PACMAN_SPEED;
            xPosPacmanForEnemy += enemyBlockSize / PACMAN_SPEED;
        } else if (pacmanDirection == 2) {
            yPosPacman += blockSize / PACMAN_SPEED;
            yPosPacmanForEnemy += enemyBlockSize / PACMAN_SPEED;
        } else if (pacmanDirection == 3) {
            xPosPacman += -blockSize / PACMAN_SPEED;
            xPosPacmanForEnemy += -enemyBlockSize / PACMAN_SPEED;
        }
        updatePositionsToDB();
    }

    private void moveUncontrolledGhostsWithAI() {
        switch (currentControlledGhost) {
            case 1: { // I control ghost #1 , need to move with AI ghosts #2 & #3
                moveGhostWithAI(xPosGhost2, yPosGhost2, ghost2Direction, GHOST2_SPEED, 2, mCurrentGhost2Bitmap);
                moveGhostWithAI(xPosGhost3, yPosGhost3, ghost3Direction, GHOST3_SPEED, 3, currentGhost3Bitmap);
            }
            break;
            case 2: { // I control ghost #2 , need to move with AI ghosts #1 & #3
                moveGhostWithAI(xPosGhost, yPosGhost, ghostDirection, GHOST1_SPEED, 1, mCurrentGhost1Bitmap);
                moveGhostWithAI(xPosGhost3, yPosGhost3, ghost3Direction, GHOST3_SPEED, 3, currentGhost3Bitmap);
            }
            break;
            case 3: { // I control ghost #3 , need to move with AI ghosts #1 & #2
                moveGhostWithAI(xPosGhost, yPosGhost, ghostDirection, GHOST1_SPEED, 1, mCurrentGhost1Bitmap);
                moveGhostWithAI(xPosGhost2, yPosGhost2, ghost2Direction, GHOST2_SPEED, 2, mCurrentGhost2Bitmap);
            }
            break;
        }
    }

    private void drawRelevantGhostBitmap() {
        switch (currentControlledGhost) {
            case 1: {
                mCanvas.drawBitmap(mCurrentGhost1Bitmap, xPosGhost, yPosGhost, mPaint);
            }
            break;
            case 2: {
                mCanvas.drawBitmap(mCurrentGhost2Bitmap, xPosGhost2, yPosGhost2, mPaint);
            }
            break;
            case 3: {
                mCanvas.drawBitmap(currentGhost3Bitmap, xPosGhost3, yPosGhost3, mPaint);
            }
            break;
        }
    }

    private void updateControlledGhostPositions(int charXPos, int charYPos, int ControlledGhostDirection) {
        switch (currentControlledGhost) {
            case 1: {
                xPosGhost = charXPos;
                yPosGhost = charYPos;
                ghostDirection = ControlledGhostDirection;
            }
            break;
            case 2: {
                xPosGhost2 = charXPos;
                yPosGhost2 = charYPos;
                ghost2Direction = ControlledGhostDirection;
            }
            break;
            case 3: {
                xPosGhost3 = charXPos;
                yPosGhost3 = charYPos;
                ghost3Direction = ControlledGhostDirection;
            }
            break;
        }
    }

    // Updates the character sprite and handles collisions
    public void movePacman() {
        short ch;

        // Check if xPos and yPos of pacman is both a multiple of block size
        if ((xPosPacman % blockSize == 0) && (yPosPacman % blockSize == 0)) {

            // When pacman goes through tunnel on
            // the right reappear at left tunnel
            if (xPosPacman >= blockSize * 17) {
                xPosPacman = 0;
            }

            // Is used to find the number in the level array in order to
            // check wall placement, pellet placement, and candy placement
            ch = leveldata1[yPosPacman / blockSize][xPosPacman / blockSize];

            // If there is a pellet, eat it
            if ((ch & 16) != 0) {
                Log.d(TAG, "movePacman: pellet eaten");
                // Toggle pellet so it won't be drawn anymore
                if (currentPelletsAmount % 4 == 0 && isSFX) {
                    mEatFruitSound.start();
                }
                float pelletsEatenPercentage = (float)(totalPellets - currentPelletsAmount) / totalPellets;
                pelletsEatenPercentage *= 100;
                int pelletsEatenInt = (int)pelletsEatenPercentage;
                myInterface.setPercentage(String.valueOf(pelletsEatenInt));
                leveldata1[yPosPacman / blockSize][xPosPacman / blockSize] = (short) (ch ^ 16);
                if (--currentPelletsAmount <= 0) {
                    gameOver(true);
                }
                currentScore += 10;
            }

            // Checks for direction buffering
            if (!((nextDirection == 3 && (ch & 1) != 0) ||
                    (nextDirection == 1 && (ch & 4) != 0) ||
                    (nextDirection == 0 && (ch & 2) != 0) ||
                    (nextDirection == 2 && (ch & 8) != 0))) {
                viewDirection = pacmanDirection = nextDirection;
            }

            // Checks for wall collisions
            if ((pacmanDirection == 3 && (ch & 1) != 0) ||
                    (pacmanDirection == 1 && (ch & 4) != 0) ||
                    (pacmanDirection == 0 && (ch & 2) != 0) ||
                    (pacmanDirection == 2 && (ch & 8) != 0)) {
                pacmanDirection = 4;
            }
        }

        // When pacman goes through tunnel on
        // the left reappear at right tunnel
        if (xPosPacman < 0) {
            xPosPacman = blockSize * 17;
        }

        drawPacman();

        // Depending on the direction move the position of pacman
        if (pacmanDirection == 0) {
            yPosPacman += -blockSize / PACMAN_SPEED;
        } else if (pacmanDirection == 1) {
            xPosPacman += blockSize / PACMAN_SPEED;
        } else if (pacmanDirection == 2) {
            yPosPacman += blockSize / PACMAN_SPEED;
        } else if (pacmanDirection == 3) {
            xPosPacman += -blockSize / PACMAN_SPEED;
        }

    }

    // Method that draws pacman based on his viewDirection
    public void drawPacman() {
        int theView;
        if (gameMode == GameMode.GHOST) {
            theView = viewDirectionEnemyPacman;
        } else {
            theView = viewDirection;
        }
        mPaint.setAlpha(visibility);
        switch (theView) {
            case (0):
                mCanvas.drawBitmap(mPacmanUp[currentPacmanFrame], xPosPacman, yPosPacman, mPaint);
                break;
            case (1):
                mCanvas.drawBitmap(mPacmanRight[currentPacmanFrame], xPosPacman, yPosPacman, mPaint);
                break;
            case (3):
                mCanvas.drawBitmap(mPacmanLeft[currentPacmanFrame], xPosPacman, yPosPacman, mPaint);
                break;
            default:
                mCanvas.drawBitmap(mPacmanDown[currentPacmanFrame], xPosPacman, yPosPacman, mPaint);
                break;
        }
        mPaint.setAlpha(DEFAULT_VISIBILITY);
    }

    private void moveGhostWithAI(int xPos, int yPos, int currentGhostDirection, final int ghostSpeed, int ghostNumber, Bitmap ghostResource) {
        // Artificial intelligence
        short ch;

        xDistance = xPosPacman - xPos;
        yDistance = yPosPacman - yPos;

        if ((xPos % blockSize == 0) && (yPos % blockSize == 0)) {
            int col = yPos / blockSize;
            int row = xPos / blockSize;
            if (leveldata1.length < row - 1 && leveldata1[0].length < col - 1) {
                // might need to set him to other location or else it will be stuck.
                return;
            }
            Log.d(TAG, "moveGhostWithAI: leveldata.length =" +leveldata1.length + " leveldata[0].length="+leveldata1[0].length
            + "row - 1 = " + (row-1) + " col - 1 =" + (col-1));
            ch = leveldata1[col][row];

            if (xPos >= blockSize * 17) {
                xPos = 0;
            }
            if (xPos < 0) {
                xPos = blockSize * 17;
            }

            if (xDistance >= 0 && yDistance >= 0) { // Move right and down
                if ((ch & 4) == 0 && (ch & 8) == 0) {
                    if (Math.abs(xDistance) > Math.abs(yDistance)) {
                        currentGhostDirection = 1;
                    } else {
                        currentGhostDirection = 2;
                    }
                } else if ((ch & 4) == 0) {
                    currentGhostDirection = 1;
                } else if ((ch & 8) == 0) {
                    currentGhostDirection = 2;
                } else
                    currentGhostDirection = 3;
            }
            if (xDistance >= 0 && yDistance <= 0) { // Move right and up
                if ((ch & 4) == 0 && (ch & 2) == 0) {
                    if (Math.abs(xDistance) > Math.abs(yDistance)) {
                        currentGhostDirection = 1;
                    } else {
                        currentGhostDirection = 0;
                    }
                } else if ((ch & 4) == 0) {
                    currentGhostDirection = 1;
                } else if ((ch & 2) == 0) {
                    currentGhostDirection = 0;
                } else currentGhostDirection = 2;
            }
            if (xDistance <= 0 && yDistance >= 0) { // Move left and down
                if ((ch & 1) == 0 && (ch & 8) == 0) {
                    if (Math.abs(xDistance) > Math.abs(yDistance)) {
                        currentGhostDirection = 3;
                    } else {
                        currentGhostDirection = 2;
                    }
                } else if ((ch & 1) == 0) {
                    currentGhostDirection = 3;
                } else if ((ch & 8) == 0) {
                    currentGhostDirection = 2;
                } else currentGhostDirection = 1;
            }
            if (xDistance <= 0 && yDistance <= 0) { // Move left and up
                if ((ch & 1) == 0 && (ch & 2) == 0) {
                    if (Math.abs(xDistance) > Math.abs(yDistance)) {
                        currentGhostDirection = 3;
                    } else {
                        currentGhostDirection = 0;
                    }
                } else if ((ch & 1) == 0) {
                    currentGhostDirection = 3;
                } else if ((ch & 2) == 0) {
                    currentGhostDirection = 0;
                } else currentGhostDirection = 2;
            }
            // Handles wall collisions
            if ((currentGhostDirection == 3 && (ch & 1) != 0) ||
                    (currentGhostDirection == 1 && (ch & 4) != 0) ||
                    (currentGhostDirection == 0 && (ch & 2) != 0) ||
                    (currentGhostDirection == 2 && (ch & 8) != 0)) {
                currentGhostDirection = 4;
            }
        }

        updateAIGhostPositions(xPos, yPos, currentGhostDirection, ghostSpeed, ghostNumber);

//        switch (ghostNumber) {
//            case 1: {
//                xPosGhost = xPos;
//                yPosGhost = yPos;
//                ghostDirection = currentGhostDirection;
//            }
//            break;
//            case 2: {
//                xPosGhost2 = xPos;
//                yPosGhost2 = yPos;
//                ghost2Direction = currentGhostDirection;
//            }
//            break;
//            case 3: {
//                xPosGhost3 = xPos;
//                yPosGhost3 = yPos;
//                ghost3Direction = currentGhostDirection;
//            }
//            break;
//        }
        mCanvas.drawBitmap(ghostResource, xPos, yPos, mPaint);
    }

    private void updateAIGhostPositions(int xPos, int yPos, int currentGhostDirection, int ghostSpeed, int ghostNumber) {
        int distanceTraveled = blockSize / ghostSpeed;
        int distanceTraveledForEnemy = enemyBlockSize / ghostSpeed;
        switch (ghostNumber) {
            case 1: {
                if (currentGhostDirection == 0) {
                    yPos += -distanceTraveled;
                    yPosGhostForEnemy += -distanceTraveledForEnemy;
                } else if (currentGhostDirection == 1) {
                    xPos += distanceTraveled;
                    xPosGhostForEnemy += distanceTraveledForEnemy;
                } else if (currentGhostDirection == 2) {
                    yPos += distanceTraveled;
                    yPosGhostForEnemy += distanceTraveledForEnemy;
                } else if (currentGhostDirection == 3) {
                    xPos += -distanceTraveled;
                    xPosGhostForEnemy += -distanceTraveledForEnemy;
                }
                xPosGhost = xPos;
                yPosGhost = yPos;
                ghostDirection = currentGhostDirection;
            } break;
            case 2: {
                if (currentGhostDirection == 0) {
                    yPos += -distanceTraveled;
                    yPosGhost2ForEnemy += -distanceTraveledForEnemy;
                } else if (currentGhostDirection == 1) {
                    xPos += distanceTraveled;
                    xPosGhost2ForEnemy += distanceTraveledForEnemy;
                } else if (currentGhostDirection == 2) {
                    yPos += distanceTraveled;
                    yPosGhost2ForEnemy += distanceTraveledForEnemy;
                } else if (currentGhostDirection == 3) {
                    xPos += -distanceTraveled;
                    xPosGhost2ForEnemy += -distanceTraveledForEnemy;
                }
                xPosGhost2 = xPos;
                yPosGhost2 = yPos;
                ghost2Direction = currentGhostDirection;
            } break;
            case 3: {
                if (currentGhostDirection == 0) {
                    yPos += -distanceTraveled;
                    yPosGhost3ForEnemy += -distanceTraveledForEnemy;
                } else if (currentGhostDirection == 1) {
                    xPos += distanceTraveled;
                    xPosGhost3ForEnemy += distanceTraveledForEnemy;
                } else if (currentGhostDirection == 2) {
                    yPos += distanceTraveled;
                    yPosGhost3ForEnemy += distanceTraveledForEnemy;
                } else if (currentGhostDirection == 3) {
                    xPos += -distanceTraveled;
                    xPosGhost3ForEnemy += -distanceTraveledForEnemy;
                }
                xPosGhost3 = xPos;
                yPosGhost3 = yPos;
                ghost3Direction = currentGhostDirection;
            }
            break;
        }
        updatePositionsToDB();
    }

    /*
    // Directions
        // 0 means going up
        // 1 means going right
        // 2 means going down
        // 3 means going left
        // 4 means stop moving - probably not relevant for ghost AD/AI
     */
    private void moveGhostWithAD(Bitmap ghostBitMap, int xPos, int yPos, int ghostNumber) {
        // Artificial Dumbnace
        short ch;
        int ghostDirectionAD;
        if ((xPos % blockSize == 0) && (yPos % blockSize == 0)) {

            // get the current block char
            ch = leveldata1[xPos / blockSize][yPos / blockSize];

            // if it goes through tunnel - let it through.
            if (xPos >= blockSize * 17) {
                xPos = 0;
            }
            if (xPos < 0) {
                xPos = blockSize * 17;
            }

            if ((ch & 4) == 0) { //
                ghostDirection = 1;
            }

            // Handles wall collisions
            if ((ghostDirection == 3 && (ch & 1) != 0) ||
                    (ghostDirection == 1 && (ch & 4) != 0) ||
                    (ghostDirection == 0 && (ch & 2) != 0) ||
                    (ghostDirection == 2 && (ch & 8) != 0)) {
                ghostDirection = 4;
            }

        }

        if (ghostDirection == 0) {
            xPos += -blockSize / 20;
        } else if (ghostDirection == 1) {
            yPos += blockSize / 20;
        } else if (ghostDirection == 2) {
            yPos += blockSize / 20;
        } else if (ghostDirection == 3) {
            xPos += -blockSize / 20;
        }

        mCanvas.drawBitmap(ghostBitMap, xPos, yPos, mPaint);

        if (ghostNumber == 2) {
            xPosGhost2 = xPos;
            yPosGhost2 = yPos;
        } else { // it's ghostNumber == 3 then
            xPosGhost3 = xPos;
            yPosGhost3 = yPos;
        }
    }

    private void updateFrame(long gameTime) {
        // If enough time has passed go to next frame
        if (gameTime > frameTicker + (totalFrame * 30)) {
            frameTicker = gameTime;

            // Increment the frame
            currentPacmanFrame++;
            // Loop back the frame when you have gone through all the frames
            if (currentPacmanFrame >= totalFrame) {
                currentPacmanFrame = 0;
            }
        }
        if (gameTime > frameTicker + (50)) {
            currentArrowFrame++;
            if (currentArrowFrame >= 7) {
                currentArrowFrame = 0;
            }
        }
    }

    // Method to draw map layout
    private void drawMap() {
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(2.5f);
        int x, y;
        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 17; j++) {
                x = j * blockSize;
                y = i * blockSize;
                if ((leveldata1[i][j] & 1) != 0) // draws left
                    mCanvas.drawLine(x, y, x, y + blockSize - 1, mPaint);

                if ((leveldata1[i][j] & 2) != 0) // draws top
                    mCanvas.drawLine(x, y, x + blockSize - 1, y, mPaint);

                if ((leveldata1[i][j] & 4) != 0) // draws right
                    mCanvas.drawLine(
                            x + blockSize, y, x + blockSize, y + blockSize - 1, mPaint);
                if ((leveldata1[i][j] & 8) != 0) // draws bottom
                    mCanvas.drawLine(
                            x, y + blockSize, x + blockSize - 1, y + blockSize, mPaint);
            }
        }
        mPaint.setColor(Color.WHITE);
    }

    private void loadBitmapImages() {
        // Scales the sprites based on screen
        spriteSize = screenWidth / 17;        // Size of Pacman & Ghost
        spriteSize = (spriteSize / 5) * 5;      // Keep it a multiple of 5
        int arrowSize = 7 * blockSize;            // Size of arrow indicators

        // Add bitmap images of pacman facing right
        mPacmanRight = new Bitmap[totalFrame];
        mPacmanRight[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right1), spriteSize, spriteSize, false);
        mPacmanRight[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right2), spriteSize, spriteSize, false);
        mPacmanRight[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right3), spriteSize, spriteSize, false);
        mPacmanRight[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right), spriteSize, spriteSize, false);


        // Add bitmap images of pacman facing left
        mPacmanLeft = new Bitmap[totalFrame];
        mPacmanLeft[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left1), spriteSize, spriteSize, false);
        mPacmanLeft[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left2), spriteSize, spriteSize, false);
        mPacmanLeft[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left3), spriteSize, spriteSize, false);
        mPacmanLeft[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left), spriteSize, spriteSize, false);

        // Add bitmap images of pacman facing up
        mPacmanUp = new Bitmap[totalFrame];
        mPacmanUp[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up1), spriteSize, spriteSize, false);
        mPacmanUp[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up2), spriteSize, spriteSize, false);
        mPacmanUp[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up3), spriteSize, spriteSize, false);
        mPacmanUp[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up), spriteSize, spriteSize, false);

        // Add bitmap images of pacman facing down
        mPacmanDown = new Bitmap[totalFrame];
        mPacmanDown[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down1), spriteSize, spriteSize, false);
        mPacmanDown[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down2), spriteSize, spriteSize, false);
        mPacmanDown[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down3), spriteSize, spriteSize, false);
        mPacmanDown[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down), spriteSize, spriteSize, false);

        mGhost1Bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost), spriteSize, spriteSize, false);
        mGhostBitmapSelected = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost1_red), spriteSize, spriteSize, false);
        if (gameMode == GameMode.GHOST) {
            mCurrentGhost1Bitmap = mGhostBitmapSelected;
        } else {
            mCurrentGhost1Bitmap = mGhost1Bitmap;
        }

        ghost2Bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost2), spriteSize, spriteSize, false);
        mCurrentGhost2Bitmap = ghost2Bitmap;
        mGhost2BitmapSelected = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost2_red), spriteSize, spriteSize, false);

        ghost3Bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost3), spriteSize, spriteSize, false);
        currentGhost3Bitmap = ghost3Bitmap;
        mGhost3BitmapSelected = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost3_red), spriteSize, spriteSize, false);

        Log.d(TAG, "loadBitmapImages: finished loading bitmap images");
    }

    final short leveldata1[][] = new short[][]{
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {19, 26, 26, 18, 26, 26, 26, 22, 0, 19, 26, 26, 26, 18, 26, 26, 22},
            {21, 0, 0, 21, 0, 0, 0, 21, 0, 21, 0, 0, 0, 21, 0, 0, 21},
            {17, 26, 26, 16, 26, 18, 26, 24, 26, 24, 26, 18, 26, 16, 26, 26, 20},
            {25, 26, 26, 20, 0, 25, 26, 22, 0, 19, 26, 28, 0, 17, 26, 26, 28},
            {0, 0, 0, 21, 0, 0, 0, 21, 0, 21, 0, 0, 0, 21, 0, 0, 0},
            {0, 0, 0, 21, 0, 19, 26, 24, 26, 24, 26, 22, 0, 21, 0, 0, 0},
            {26, 26, 26, 16, 26, 20, 0, 0, 0, 0, 0, 17, 26, 16, 26, 26, 26},
            {0, 0, 0, 21, 0, 17, 26, 26, 26, 26, 26, 20, 0, 21, 0, 0, 0},
            {0, 0, 0, 21, 0, 21, 0, 0, 0, 0, 0, 21, 0, 21, 0, 0, 0},
            {19, 26, 26, 16, 26, 24, 26, 22, 0, 19, 26, 24, 26, 16, 26, 26, 22},
            {21, 0, 0, 21, 0, 0, 0, 21, 0, 21, 0, 0, 0, 21, 0, 0, 21},
            {25, 22, 0, 21, 0, 0, 0, 17, 2, 20, 0, 0, 0, 21, 0, 19, 28}, // "2" in this line is for
            {0, 21, 0, 17, 26, 26, 18, 24, 24, 24, 18, 26, 26, 20, 0, 21, 0}, // pacman's spawn
            {19, 24, 26, 28, 0, 0, 25, 18, 26, 18, 28, 0, 0, 25, 26, 24, 22},
            {21, 0, 0, 0, 0, 0, 0, 21, 0, 21, 0, 0, 0, 0, 0, 0, 21},
            {25, 26, 26, 26, 26, 26, 26, 24, 26, 24, 26, 26, 26, 26, 26, 26, 28},
    };

}
