package com.example.mark.pacmanroyale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.User.Pacman;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Mark on 14/02/2018.
 */

public class DrawingView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private static final String TAG = "DrawingView";
    private static final int BLOCK_SIZE_DIVIDER = 17;
    private static final int XYDelta = 5;

    private static final int GHOST1_SPEED = 20;
    private static final int GHOST2_SPEED = 30;
    private static final int GHOST3_SPEED = 40;

    private Thread mThread;
    private SurfaceHolder surfaceHolder;
    private boolean canDraw;// = true;

    private Canvas canvas;

    private DatabaseReference virtualRoomReference;
    private DatabaseReference virtualRoomGhostReference;
    private DatabaseReference virtualRoomPacmanReference;

    private Paint paint;
    private Bitmap[] pacmanRight, pacmanDown, pacmanLeft, pacmanUp;
    private Bitmap[] arrowRight, arrowDown, arrowLeft, arrowUp;
    private Bitmap ghostBitmap;
    private Bitmap ghost2Bitmap;
    private Bitmap ghost3Bitmap;

    private int countPellets = 0;
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
    private int direction = 4;              // Direction of the swipe, initial direction is right
    private int nextDirection = 4;          // Buffer for the next direction you choose
    private int viewDirection = 2;          // Direction that pacman is facing

    private int viewDirectionEnemyPacman =4;// Direction that enemy pacman is facing
    private int ghostDirection;
    private int ghost2Direction;
    private int ghost3Direction;

    private boolean isGhost2Turn;

    private int screenWidth;                // Width of the phone screen
    private int blockSize;                  // Size of a block on the map
    private int enemyBlockSize;
    public static int LONG_PRESS_TIME = 750;  // Time in milliseconds

    public static int GHOST2_DELAY = 750;  // Time in milliseconds
    public static int GHOST3_DELAY = 1250;  // Time in milliseconds
    private int currentScore = 0;           //Current game score

    final Handler handler = new Handler();
    Runnable longPressed = new Runnable() {
        public void run() {
//            Log.i("info", "LongPress");
//            Intent pauseIntent = new Intent(getContext(), MainActivity.class);
//            getContext().startActivity(pauseIntent);
        }
    };
    private int mEnemyGhostDirection;
    private int mEnemyPacmanDirection;
    private boolean isGhostDirectionChanged;
    private boolean isPacmanDirectionChanged;
    private int yPosPacmanForEnemy;
    private int xPosPacmanForEnemy;
    private int xPosGhostForEnemy;
    private int yPosGhostForEnemy;

    private boolean firstTime=true;
    private Iinterface myInterface;

    // Initializing the member variables
    public DrawingView(Context context, GameMode gameMode) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        frameTicker = 1000 / totalFrame;
        paint = new Paint();
        paint.setColor(Color.WHITE);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        blockSize = screenWidth / BLOCK_SIZE_DIVIDER;
        blockSize = (blockSize / 5) * 5;

        xPosGhost = 8 * blockSize;
        yPosGhost = 4 * blockSize;

        xPosGhost2 = 1 * blockSize;
        yPosGhost2 = 2 * blockSize;

        xPosGhost3 = 16 * blockSize;
        yPosGhost3 = 2 * blockSize;

        ghostDirection = 4;
        ghost2Direction = 4;
        ghost3Direction = 4;

        xPosPacman = 8 * blockSize;
        yPosPacman = 13 * blockSize;

        if (gameMode == GameMode.GHOST || gameMode == GameMode.PACMAN) {
            virtualRoomReference = Utils.getVirtualRoomReference();
            virtualRoomGhostReference = virtualRoomReference.child(getResources().getString(R.string.ghost_node));
            virtualRoomPacmanReference = virtualRoomReference.child(getResources().getString(R.string.pacman_node));
        }

        this.gameMode = gameMode;
        loadBitmapImages();
        //loadGhostAndPacman();
        if (gameMode == GameMode.PACMAN) {
            listenToGhostPosOnFireBase();
        } else if (gameMode == GameMode.GHOST) {
            listenToPacmanPosOnFireBase();
        }

//        enemyBlockSize = Utils.getEnemyBlockSize();
//        Log.d(TAG, "DrawingView: enemyBlockSize = " +enemyBlockSize);
//        enemyBlockSize=84;
//        initEnemyPosVariables();

        //WaitForOpponentAsyncTask waitForOpponentAsyncTask = new WaitForOpponentAsyncTask();
        Log.d(TAG, "test1 end of drawingView constructor ");

        myInterface = (Iinterface) context;

    }

    //virtualRoomGhostReference
    private void listenToGhostPosOnFireBase() {
        virtualRoomGhostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos))
                        && dataSnapshot.hasChild(getResources().getString(R.string.yPos))) {
                    xPosGhost = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos)).getValue().toString());
                    yPosGhost = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos)).getValue().toString());
                    Log.d(TAG, "onDataChange() x/y ghost = " + xPosGhost + "/" + yPosGhost);
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
                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos))
                        && dataSnapshot.hasChild(getResources().getString(R.string.yPos))) {
                    xPosPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos)).getValue().toString());
                    yPosPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos)).getValue().toString());
                    Log.d(TAG, "onDataChange() x/y pacman = " + xPosPacman + "/" + yPosPacman);
                }

                if(dataSnapshot.hasChild(getResources().getString(R.string.pacmanDirection))){
                    viewDirectionEnemyPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.pacmanDirection)).getValue().toString());
                    Log.d(TAG, "onDataChange: direction is: "+viewDirectionEnemyPacman);
                }
//                if (mPacman != null) {
//                    xPosPacman = mPacman.getxPos();
//                    yPosPacman = mPacman.getyPos();
//                    Log.d(TAG, "onDataChange() just listened to the enemy pacman!");
//                }
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
                handler.postDelayed(longPressed, LONG_PRESS_TIME);
            }
            break;
            case (MotionEvent.ACTION_UP): {
                x2 = event.getX();
                y2 = event.getY();
                calculateSwipeDirection();
                handler.removeCallbacks(longPressed);
            }
            break;
        }
        return true;
    }

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

    @Override
    public void run() {
        Log.d(TAG, "run method starts");
        while (canDraw) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue; // need to check what it does - and why/do we need it.
            }

//            if (gameMode == GameMode.VS_PC && xPosGhost == xPosPacman && yPosGhost == yPosPacman) {
//                Log.d(TAG, "run: Game Over!");
//                canDraw = false;
//            }
            // Log.d(TAG, "run: Pacman X = " + xPosPacman +" Pacman Y = "+yPosPacman +" Ghost X = "+xPosGhost +" Ghost Y = "+yPosGhost);

            boolean xCatched = xPosPacman <= xPosGhost+XYDelta && xPosPacman >= xPosGhost-XYDelta;
            boolean yCatched = yPosPacman <= yPosGhost+XYDelta && yPosPacman >= yPosGhost-XYDelta;
            if(gameMode == GameMode.VS_PC && xCatched && yCatched){
                Log.d(TAG, "run: Game Over!");
                canDraw = false;
            }

            if (enemyBlockSize == 0) {
                enemyBlockSize = Utils.getEnemyBlockSize();
                initEnemyPosVariables();
            }
            canvas = surfaceHolder.lockCanvas();
            // Set background color to Transparent
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);

                drawMap();

                updateFrame(System.currentTimeMillis());

                if (gameMode == GameMode.PACMAN || gameMode == GameMode.GHOST) {
                    if (gameMode == GameMode.PACMAN) {
                        moveCharacter(xPosPacman, yPosPacman, gameMode);
                    } else {
                        moveCharacter(xPosGhost, yPosGhost, gameMode);
                    }
                } else {
                    moveGhostWithAI(xPosGhost, yPosGhost, ghostDirection, GHOST1_SPEED, 1, ghostBitmap);

                    moveGhostWithAI(xPosGhost2, yPosGhost2, ghost2Direction, GHOST2_SPEED, 2, ghost2Bitmap);

                    moveGhostWithAI(xPosGhost3, yPosGhost3, ghost3Direction, GHOST3_SPEED, 3, ghost3Bitmap);
                    //if (isGhost2Turn) {
                    //canvas.drawBitmap(ghost3Bitmap, xPosGhost3, yPosGhost3, paint);
                    // isGhost2Turn = false;
                    // } else {
                      //  canvas.drawBitmap(ghost2Bitmap, xPosGhost2, yPosGhost2, paint);
                      //  isGhost2Turn = true;
                   // }


//                moveGhostWithAD(ghost2Bitmap, xPosGhost2, yPosGhost2, 2);
//                moveGhostWithAD(ghost3Bitmap, xPosGhost3, yPosGhost3, 3);
                    // Moves the pacman based on his direction
                    movePacman();
                }


                // Draw the pellets
                if (gameMode != GameMode.GHOST) {
                    drawPellets();
                }

                updatePositionsToDB();

                surfaceHolder.unlockCanvasAndPost(canvas);

                if (firstTime) {
                    firstTime=false;
                    myInterface.setVisibilities();
                }
                //Log.d(TAG, "run: user utils ghostX="+Utils.getUserInformation().getGhost().getxPos());
            }
        }
    }

    public interface Iinterface{
        void setVisibilities();
    }

//    public void initGhostPositions() {
//        virtualRoomGhostReference.child("blocksize").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                enemyBlockSize = Integer.parseInt(dataSnapshot.getValue().toString());
//                initEnemyPosVariables();
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {}
//        });
//    }
//
//    public void setEnemyBlockSizeAndInitPacmanPositions() {
//        virtualRoomPacmanReference.child("blocksize").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                enemyBlockSize = Integer.parseInt(dataSnapshot.getValue().toString());
//                initEnemyPosVariables();
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {}
//        });
//    }

    public void initEnemyPosVariables() {
        Log.d(TAG, "initEnemyPosVariables() starting...");
        //
        if (gameMode == GameMode.GHOST) {
            if (xPosGhostForEnemy == 0 || yPosGhostForEnemy == 0) {
                xPosGhostForEnemy = 8 * enemyBlockSize;
                yPosGhostForEnemy = 4 * enemyBlockSize;
                //Utils.getUserInformation().getGhost().setxPos(xPosGhostForEnemy);
                //Utils.getUserInformation().getGhost().setyPos(yPosGhostForEnemy);
                Log.d(TAG, "initEnemyPosVariables: starting ghost values x= " + xPosGhostForEnemy + " y= " + yPosGhostForEnemy);
            }
        } else if (gameMode == GameMode.PACMAN) { // enemy = pacman
            if (xPosPacmanForEnemy == 0 || yPosPacmanForEnemy == 0) {
                xPosPacmanForEnemy = 8 * enemyBlockSize;
                yPosPacmanForEnemy = 13 * enemyBlockSize;
                //Utils.getUserInformation().getPacman().setxPos(xPosPacmanForEnemy);
                //Utils.getUserInformation().getPacman().setyPos(yPosPacmanForEnemy);
                Log.d(TAG, "initEnemyPosVariables: starting pacman values x= " + xPosPacmanForEnemy + " y= " + yPosPacmanForEnemy);
            }
        }
        updatePositionsToDB();
    }

    private void updatePositionsToDB() {
        if (gameMode == GameMode.PACMAN) {
            //virtualRoomPacmanReference.child("direction").setValue(direction);
            Log.d(TAG, "updatePositionsToDB: update enemy pacman x=" + xPosPacmanForEnemy + " y=" + yPosPacmanForEnemy);
            if (xPosPacmanForEnemy != 0 && yPosPacmanForEnemy != 0) {
                virtualRoomPacmanReference.child(getResources().getString(R.string.xPos)).setValue(xPosPacmanForEnemy);
                virtualRoomPacmanReference.child(getResources().getString(R.string.yPos)).setValue(yPosPacmanForEnemy);
            }
            //virtualRoomPacmanReference.child(getResources().getString(R.string.xPos)).setValue(Utils.getUserInformation().getPacman().getxPos());
            //virtualRoomPacmanReference.child(getResources().getString(R.string.yPos)).setValue(Utils.getUserInformation().getPacman().getyPos());

        } else if (gameMode == GameMode.GHOST) {
            //virtualRoomGhostReference.child("direction").setValue(direction);
            Log.d(TAG, "updatePositionsToDB: update enemy ghost x=" + xPosGhostForEnemy + " y=" + yPosGhostForEnemy);
            if (xPosGhostForEnemy != 0 && yPosGhostForEnemy != 0) {
                virtualRoomGhostReference.child(getResources().getString(R.string.xPos)).setValue(xPosGhostForEnemy);
                virtualRoomGhostReference.child(getResources().getString(R.string.yPos)).setValue(yPosGhostForEnemy);
            }
            //virtualRoomGhostReference.child(getResources().getString(R.string.xPos)).setValue(Utils.getUserInformation().getGhost().getxPos());
            //virtualRoomGhostReference.child(getResources().getString(R.string.yPos)).setValue(Utils.getUserInformation().getGhost().getyPos());
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
                    if(isFirstTimePellet) {
                        countPellets++;
                    }
                    canvas.drawCircle(x + blockSize / 2, y + blockSize / 2, blockSize / 10, paint);
                }
            }
        }
        isFirstTimePellet = false;
    }

    // Updates the character sprite and handles collisions
    public void moveCharacter(int charXPos, int charYPos, GameMode isPacman) {
        short ch;

        // Check if xPos and yPos of pacman is both a multiple of block size
        if ((charXPos % blockSize == 0) && (charYPos % blockSize == 0)) {

            // When pacman goes through tunnel on
            // the right reappear at left tunnel
            if (charXPos >= blockSize * 17) {
                charXPos = 0;
                if (gameMode == GameMode.PACMAN) {
                    xPosPacmanForEnemy = 0;
                } else {
                    xPosGhostForEnemy = 0;
                }
            }

            // Is used to find the number in the level array in order to
            // check wall placement, pellet placement, and candy placement
            ch = leveldata1[charYPos / blockSize][charXPos / blockSize];

            // If there is a pellet, eat it
            if ((ch & 16) != 0) {

                // Toggle pellet so it won't be drawn anymore
                leveldata1[charYPos / blockSize][charXPos / blockSize] = (short) (ch ^ 16);
                if(--countPellets <=0){
                    canDraw=false;
                }
                currentScore += 10;
            }

            // Checks for direction buffering
            if (!((nextDirection == 3 && (ch & 1) != 0) ||
                    (nextDirection == 1 && (ch & 4) != 0) ||
                    (nextDirection == 0 && (ch & 2) != 0) ||
                    (nextDirection == 2 && (ch & 8) != 0))) {
                viewDirection = direction = nextDirection;
            }

            if (gameMode == GameMode.PACMAN && direction != 4 ) {

                virtualRoomPacmanReference.child(getContext().getString(R.string.pacmanDirection)).setValue(direction);
            }

            // Checks for wall collisions
            if ((direction == 3 && (ch & 1) != 0) ||
                    (direction == 1 && (ch & 4) != 0) ||
                    (direction == 0 && (ch & 2) != 0) ||
                    (direction == 2 && (ch & 8) != 0)) {
                direction = 4;
            }

        }

        // When pacman goes through tunnel on
        // the left reappear at right tunnel
        if (charXPos < 0) {
            charXPos = blockSize * 17;
            if (gameMode == GameMode.PACMAN) {
                xPosPacmanForEnemy = enemyBlockSize * 17;
            } else {
                xPosGhostForEnemy = enemyBlockSize * 17;
            }
        }


        if (isPacman == GameMode.PACMAN) {
            xPosPacman = charXPos;
            yPosPacman = charYPos;
//            Log.d(TAG, "moveCharacter: xpospacman="+xPosPacman +"xpospacmanForEnemy="+xPosPacmanForEnemy);
//            Log.d(TAG, "moveCharacter: ypospacman="+yPosPacman +"ypospacmanForEnemy="+yPosPacmanForEnemy);
            drawPacman();
//            setEnemyGhostDirection();
//            if (isGhostDirectionChanged) {
//                determineEnemyGhostXY(mEnemyGhostDirection);
//            }
            canvas.drawBitmap(ghostBitmap, xPosGhost, yPosGhost, paint);

        } else {
            // i'll update my direction - so the enemy can draw me on the right position aswell.
            //virtualRoomGhostReference.child("direction").setValue(direction);

            xPosGhost = charXPos;
            yPosGhost = charYPos;

//            setEnemyPacmanDirection();
//            if (isPacmanDirectionChanged) {
//                determineEnemyPacmanXY(mEnemyPacmanDirection);
//            }
            canvas.drawBitmap(ghostBitmap, charXPos, charYPos, paint);
            drawPacman();

        }
        // Depending on the direction move the position of pacman
        if (isPacman == GameMode.PACMAN && enemyBlockSize != 0) {
            Log.d(TAG, "moveCharacter enemyblock size = " + enemyBlockSize);
            if (direction == 0) {
                Log.d(TAG, "moveCharacter: direction = 0");
                yPosPacman += -blockSize / 15;
                yPosPacmanForEnemy += -enemyBlockSize / 15;
            } else if (direction == 1) {
                Log.d(TAG, "moveCharacter: direction = 1");
                xPosPacman += blockSize / 15;
                xPosPacmanForEnemy += enemyBlockSize / 15;
            } else if (direction == 2) {
                Log.d(TAG, "moveCharacter: direction = 2");
                yPosPacman += blockSize / 15;
                yPosPacmanForEnemy += enemyBlockSize / 15;
            } else if (direction == 3) {
                Log.d(TAG, "moveCharacter: direction = 3");
                xPosPacman += -blockSize / 15;
                xPosPacmanForEnemy += -enemyBlockSize / 15;
            }
        } else if (isPacman == GameMode.GHOST && enemyBlockSize != 0) {
            Log.d(TAG, "moveCharacter enemyblock size = " + enemyBlockSize);
            if (direction == 0) {
                yPosGhost += -blockSize / 15;
                yPosGhostForEnemy += -enemyBlockSize / 15;
            } else if (direction == 1) {
                xPosGhost += blockSize / 15;
                xPosGhostForEnemy += enemyBlockSize / 15;
            } else if (direction == 2) {
                yPosGhost += blockSize / 15;
                yPosGhostForEnemy += enemyBlockSize / 15;
            } else if (direction == 3) {
                xPosGhost += -blockSize / 15;
                xPosGhostForEnemy += -enemyBlockSize / 15;
            }
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
                leveldata1[yPosPacman / blockSize][xPosPacman / blockSize] = (short) (ch ^ 16);
                if(--countPellets <=0){
                    canDraw=false;
                }
                currentScore += 10;
            }

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
        }

        // When pacman goes through tunnel on
        // the left reappear at right tunnel
        if (xPosPacman < 0) {
            xPosPacman = blockSize * 17;
        }

        drawPacman();

        // Depending on the direction move the position of pacman
        if (direction == 0) {
            yPosPacman += -blockSize / 15;
        } else if (direction == 1) {
            xPosPacman += blockSize / 15;
        } else if (direction == 2) {
            yPosPacman += blockSize / 15;
        } else if (direction == 3) {
            xPosPacman += -blockSize / 15;
        }

    }

    // Method that draws pacman based on his viewDirection
    public void drawPacman() {
        int test;
        if(gameMode == GameMode.GHOST){
            test= viewDirectionEnemyPacman;
        } else {
            test = viewDirection;
        }
        switch (test) {
            case (0):
                canvas.drawBitmap(pacmanUp[currentPacmanFrame], xPosPacman, yPosPacman, paint);
                break;
            case (1):
                canvas.drawBitmap(pacmanRight[currentPacmanFrame], xPosPacman, yPosPacman, paint);
                break;
            case (3):
                canvas.drawBitmap(pacmanLeft[currentPacmanFrame], xPosPacman, yPosPacman, paint);
                break;
            default:
                canvas.drawBitmap(pacmanDown[currentPacmanFrame], xPosPacman, yPosPacman, paint);
                break;
        }
    }

    private void moveGhostWithAI(int xPos, int yPos, int currentGhostDirection, final int ghostSpeed, int ghostNumber, Bitmap ghostResource) {
        // Artificial intelligence
        short ch;

        xDistance = xPosPacman - xPos;
        yDistance = yPosPacman - yPos;

        if ((xPos % blockSize == 0) && (yPos % blockSize == 0)) {
            ch = leveldata1[yPos / blockSize][xPos / blockSize];

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

        if (currentGhostDirection == 0) {
            yPos += -blockSize / ghostSpeed;
        } else if (currentGhostDirection == 1) {
            xPos += blockSize / ghostSpeed;
        } else if (currentGhostDirection == 2) {
            yPos += blockSize / ghostSpeed;
        } else if (currentGhostDirection == 3) {
            xPos += -blockSize / ghostSpeed;
        }
        switch (ghostNumber) {
            case 1: {
                xPosGhost = xPos;
                yPosGhost = yPos;
                ghostDirection = currentGhostDirection;
            }
            break;
            case 2: {
                xPosGhost2 = xPos;
                yPosGhost2 = yPos;
                ghost2Direction = currentGhostDirection;
            }
            break;
            case 3: {
                xPosGhost3 = xPos;
                yPosGhost3 = yPos;
                ghost3Direction = currentGhostDirection;
            }
            break;
        }
        canvas.drawBitmap(ghostResource, xPos, yPos, paint);

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

        canvas.drawBitmap(ghostBitMap, xPos, yPos, paint);

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
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2.5f);
        int x, y;
        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 17; j++) {
                x = j * blockSize;
                y = i * blockSize;
                if ((leveldata1[i][j] & 1) != 0) // draws left
                    canvas.drawLine(x, y, x, y + blockSize - 1, paint);

                if ((leveldata1[i][j] & 2) != 0) // draws top
                    canvas.drawLine(x, y, x + blockSize - 1, y, paint);

                if ((leveldata1[i][j] & 4) != 0) // draws right
                    canvas.drawLine(
                            x + blockSize, y, x + blockSize, y + blockSize - 1, paint);
                if ((leveldata1[i][j] & 8) != 0) // draws bottom
                    canvas.drawLine(
                            x, y + blockSize, x + blockSize - 1, y + blockSize, paint);
            }
        }
        paint.setColor(Color.WHITE);
    }

    private void loadBitmapImages() {
        // Scales the sprites based on screen
        int spriteSize = screenWidth / 17;        // Size of Pacman & Ghost
        spriteSize = (spriteSize / 5) * 5;      // Keep it a multiple of 5
        int arrowSize = 7 * blockSize;            // Size of arrow indicators

        // Add bitmap images of pacman facing right
        pacmanRight = new Bitmap[totalFrame];
        pacmanRight[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right1), spriteSize, spriteSize, false);
        pacmanRight[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right2), spriteSize, spriteSize, false);
        pacmanRight[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right3), spriteSize, spriteSize, false);
        pacmanRight[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_right), spriteSize, spriteSize, false);

        // Add bitmap images of pacman facing left
        pacmanLeft = new Bitmap[totalFrame];
        pacmanLeft[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left1), spriteSize, spriteSize, false);
        pacmanLeft[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left2), spriteSize, spriteSize, false);
        pacmanLeft[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left3), spriteSize, spriteSize, false);
        pacmanLeft[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_left), spriteSize, spriteSize, false);

        // Add bitmap images of pacman facing up
        pacmanUp = new Bitmap[totalFrame];
        pacmanUp[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up1), spriteSize, spriteSize, false);
        pacmanUp[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up2), spriteSize, spriteSize, false);
        pacmanUp[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up3), spriteSize, spriteSize, false);
        pacmanUp[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_up), spriteSize, spriteSize, false);

        // Add bitmap images of pacman facing down
        pacmanDown = new Bitmap[totalFrame];
        pacmanDown[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down1), spriteSize, spriteSize, false);
        pacmanDown[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down2), spriteSize, spriteSize, false);
        pacmanDown[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down3), spriteSize, spriteSize, false);
        pacmanDown[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.pacman_down), spriteSize, spriteSize, false);

        ghostBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost), spriteSize, spriteSize, false);

        ghost2Bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost2), spriteSize, spriteSize, false);

        ghost3Bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.ghost3), spriteSize, spriteSize, false);

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

    private class WaitForOpponentAsyncTask extends AsyncTask<Void, Integer, String> {
        private ValueEventListener valueEventListener;

        public WaitForOpponentAsyncTask() {

        }

        @Override
        protected String doInBackground(Void... voids) {
            while (enemyBlockSize == 0) {
                if (gameMode == GameMode.PACMAN) { // need to listen to ghost path
                    valueEventListener = virtualRoomGhostReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("blocksize")) {
                                //enemyBlockSize =
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            return "test";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
