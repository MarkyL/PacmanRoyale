package com.example.mark.pacmanroyale;

import android.content.Context;
import android.content.Intent;
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

import com.example.mark.pacmanroyale.Activities.MainActivity;
import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.User.Ghost;
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
    private Thread mThread;
    private SurfaceHolder surfaceHolder;
    private boolean canDraw = true;

    private DatabaseReference virtualRoomReference;
    private DatabaseReference virtualRoomGhostReference;
    private DatabaseReference virtualRoomPacmanReference;

    private Paint paint;
    private Bitmap[] pacmanRight, pacmanDown, pacmanLeft, pacmanUp;
    private Bitmap[] arrowRight, arrowDown, arrowLeft, arrowUp;
    private Bitmap ghostBitmap;

    private int totalFrame = 4; // amount of frames for each direction
    private int currentPacmanFrame = 0;     // Current Pacman frame to draw
    private int currentArrowFrame = 0;      // Current arrow frame to draw
    private long frameTicker;               // Current time since last frame has been drawn

    private Pacman mPacman;
    private int xPosPacman;                 // x-axis position of pacman
    private int yPosPacman;                 // y-axis position of pacman

    private GameMode gameMode;

    private Ghost mGhost;
    private int xPosGhost;                  // x-axis position of ghost
    private int yPosGhost;                  // y-axis position of ghost
    int xDistance;
    int yDistance;

    private float x1, x2, y1, y2;           // Initial/Final positions of swipe
    private int direction = 4;              // Direction of the swipe, initial direction is right
    private int nextDirection = 4;          // Buffer for the next direction you choose
    private int viewDirection = 2;          // Direction that pacman is facing
    private int ghostDirection;
    private int arrowDirection = 4;

    private int screenWidth;                // Width of the phone screen
    private int blockSize;                  // Size of a block on the map
    private int enemyBlockSize;
    public static int LONG_PRESS_TIME = 750;  // Time in milliseconds
    private int currentScore = 0;           //Current game score

    final Handler handler = new Handler();

    Runnable longPressed = new Runnable() {
        public void run() {
            Log.i("info", "LongPress");
            Intent pauseIntent = new Intent(getContext(), MainActivity.class);
            getContext().startActivity(pauseIntent);
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

    // Initializing the member variables
    public DrawingView(Context context, GameMode gameMode) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        frameTicker = 1000 / totalFrame;
        paint = new Paint();
        paint.setColor(Color.WHITE);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
                //(int) getResources().getDimension(R.dimen.drawing_view_fixed_width);//1080; //getResources().getDisplayMetrics().widthPixels;
        blockSize = screenWidth / BLOCK_SIZE_DIVIDER;//(int) (screenWidth * 0.04);// //
        blockSize = (blockSize / 5) * 5;
        xPosGhost = 8 * blockSize;
        yPosGhost = 4 * blockSize;
        ghostDirection = 4;

        xPosPacman = 8 * blockSize;
        yPosPacman = 13 * blockSize;

        if(gameMode == GameMode.GHOST || gameMode == GameMode.PACMAN) {
            virtualRoomReference = Utils.getVirtualRoomReference();
            virtualRoomGhostReference = virtualRoomReference.child(getResources().getString(R.string.ghost_node));
            virtualRoomPacmanReference = virtualRoomReference.child(getResources().getString(R.string.pacman_node));
        }
        //this.isPacmanGameMode = isPacmanGameMode;

        this.gameMode = gameMode;
        loadBitmapImages();
        //loadGhostAndPacman();
        if (gameMode == GameMode.PACMAN) {
            // i'm a pacman , i'll write my block size
            //virtualRoomPacmanReference.child(getResources().getString(R.string.)).setValue(blockSize);
            listenToGhostPosOnFireBase();
        } else if(gameMode == GameMode.GHOST) {
            //virtualRoomGhostReference.child("blocksize").setValue(blockSize);
            listenToPacmanPosOnFireBase();
        }

//        enemyBlockSize = Utils.getEnemyBlockSize();
//        Log.d(TAG, "DrawingView: enemyBlockSize = " +enemyBlockSize);
//        enemyBlockSize=84;
//        initEnemyPosVariables();

        //WaitForOpponentAsyncTask waitForOpponentAsyncTask = new WaitForOpponentAsyncTask();
    }

//    private void retrieveEnemyBlockSize() {
//        if (enemyBlockSize == 0) {
//            Log.d(TAG, "entered the if (enemyblocksize = 0)");
//            Utils.getFireBaseUsersNodeReference(getContext()).child(Utils.getVirtualGameRoom().getUserID2()).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "onDataChange: enemyblocksize="+enemyBlockSize);
//                    if (dataSnapshot.hasChild(getResources().getString(R.string.screenWidth))) {
//                        int enemyScreenWidth = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.screenWidth)).getValue().toString());
//                        enemyBlockSize = enemyScreenWidth / BLOCK_SIZE_DIVIDER;//(int) (enemyScreenWidth * 0.04);////
//                        Log.d(TAG, "retrieved enemyblocksize , now calling initEnemyPosVariables, enemy block size = "+enemyBlockSize);
//                        initEnemyPosVariables();
//                    }
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {}
//            });
//        }
//    }

    //    private void loadGhostAndPacman() {
//        Utils.getFireBaseDataBase().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String userId = Utils.getUserInformation().getUserId();
//                int ghostLevel= -1, ghostEXP= -1, xPosGhost= -1, yPosGhost= -1;
//                for(DataSnapshot ds : dataSnapshot.getChildren()) {
//                    ghostLevel = ds.child(userId).getValue(UserInformation.class).getGhost().getLevel();
//                    ghostEXP = ds.child(userId).getValue(UserInformation.class).getGhost().getExperience();
//                    xPosGhost = ds.child(userId).getValue(UserInformation.class).getGhost().getxPos();
//                    yPosGhost = ds.child(userId).getValue(UserInformation.class).getGhost().getyPos();
//                }
//                ghost = new Ghost(ghostLevel, ghostEXP, xPosGhost, yPosGhost);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//virtualRoomGhostReference
    private void listenToGhostPosOnFireBase() {
        virtualRoomGhostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //xPosGhost = ds.child(Utils.getUserInformation().getUserId()).getValue(UserInformation.class).getGhost().getxPos();
                //yPosGhost = ds.child(Utils.getUserInformation().getUserId()).getValue(UserInformation.class).getGhost().getyPos();
//                    if (dataSnapshot.child("blocksize").getValue() != null) {
//                        enemyBlockSize = Integer.parseInt(dataSnapshot.child("blocksize").getValue().toString());
//                    }
//                    if (xPosGhostForEnemy == 0 || yPosGhostForEnemy == 0) {
//                        xPosGhostForEnemy = 8 * enemyBlockSize;
//                        yPosGhostForEnemy = 4 * enemyBlockSize;
//                    }
//                mGhost = dataSnapshot.getValue(Ghost.class);
//                if (mGhost != null) {
//                    xPosGhost = mGhost.getxPos();
//                    yPosGhost = mGhost.getyPos();
//                    Log.d(TAG, "onDataChange() just listened to the enemy ghost!");
//                }
                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos))
                        && dataSnapshot.hasChild(getResources().getString(R.string.yPos))) {
                    xPosGhost = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos)).getValue().toString());
                    yPosGhost = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos)).getValue().toString());
                    Log.d(TAG, "onDataChange() x/y ghost = "+xPosGhost+"/"+yPosGhost);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void listenToPacmanPosOnFireBase() {
        virtualRoomPacmanReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //xPosGhost = ds.child(Utils.getUserInformation().getUserId()).getValue(UserInformation.class).getGhost().getxPos();
                //yPosGhost = ds.child(Utils.getUserInformation().getUserId()).getValue(UserInformation.class).getGhost().getyPos();

//                if (dataSnapshot.child("blocksize").getValue() != null) {
//                    enemyBlockSize = Integer.parseInt(dataSnapshot.child("blocksize").getValue().toString());
//                }
//                if (xPosPacmanForEnemy == 0 || yPosPacmanForEnemy == 0) {
//                    xPosPacmanForEnemy = 8 * enemyBlockSize;
//                    yPosPacmanForEnemy = 4 * enemyBlockSize;
//                }

                //mPacman = dataSnapshot.getValue(Pacman.class);
                Log.d(TAG, "onDataChange() mPacman Key = " + dataSnapshot.getKey());
                if (dataSnapshot.hasChild(getResources().getString(R.string.xPos))
                        && dataSnapshot.hasChild(getResources().getString(R.string.yPos))) {
                    xPosPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.xPos)).getValue().toString());
                    yPosPacman = Integer.parseInt(dataSnapshot.child(getResources().getString(R.string.yPos)).getValue().toString());
                    Log.d(TAG, "onDataChange() x/y pacman = "+xPosPacman+"/"+yPosPacman);
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
           // Log.d(TAG, "run: Pacman X = " + xPosPacman +" Pacman Y = "+yPosPacman +" Ghost X = "+xPosGhost +" Ghost Y = "+yPosGhost);
            if(gameMode == GameMode.VS_PC && xPosGhost == xPosPacman && yPosGhost == yPosPacman){
                Log.d(TAG, "run: Game Over!");
                canDraw = false;
            }

            if (enemyBlockSize == 0) {
                enemyBlockSize = Utils.getEnemyBlockSize();
                initEnemyPosVariables();
                Log.d(TAG, "run: inside the if enemyblocksize =0 , enemyblock="+enemyBlockSize);
            }
            Canvas canvas = surfaceHolder.lockCanvas();
            // Set background color to Transparent
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);

                drawMap(canvas);

                updateFrame(System.currentTimeMillis());

                if (gameMode == GameMode.PACMAN || gameMode == GameMode.GHOST) {
                    if (gameMode == GameMode.PACMAN) {
                        moveCharacter(canvas, xPosPacman, yPosPacman, gameMode);
                    } else {
                        moveCharacter(canvas, xPosGhost, yPosGhost, gameMode);
                    }
                }
                else{
                moveGhost(canvas);
                // Moves the pacman based on his direction
                movePacman(canvas);
                }


                // Draw the pellets
                drawPellets(canvas);

                updatePositionsToDB();

                surfaceHolder.unlockCanvasAndPost(canvas);
                //Log.d(TAG, "run: user utils ghostX="+Utils.getUserInformation().getGhost().getxPos());
            }
        }
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

        } else if(gameMode == GameMode.GHOST) {
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
    private void drawPellets(Canvas canvas) {
        float x;
        float y;
        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 17; j++) {
                x = j * blockSize;
                y = i * blockSize;
                // Draws pellet in the middle of a block
                if ((leveldata1[i][j] & 16) != 0)
                    canvas.drawCircle(x + blockSize / 2, y + blockSize / 2, blockSize / 10, paint);
            }
        }
    }

    // Updates the character sprite and handles collisions
    public void moveCharacter(Canvas canvas, int charXPos, int charYPos, GameMode isPacman) {
        short ch;

        // Check if xPos and yPos of pacman is both a multiple of block size
        if ((charXPos % blockSize == 0) && (charYPos % blockSize == 0)) {

            // When pacman goes through tunnel on
            // the right reappear at left tunnel
            if (charXPos >= blockSize * 17) {
                charXPos = 0;
            }

            // Is used to find the number in the level array in order to
            // check wall placement, pellet placement, and candy placement
            ch = leveldata1[charYPos / blockSize][charXPos / blockSize];

            // If there is a pellet, eat it
            if ((ch & 16) != 0) {
                // Toggle pellet so it won't be drawn anymore
                leveldata1[charYPos / blockSize][charXPos / blockSize] = (short) (ch ^ 16);
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
        if (charXPos < 0) {
            charXPos = blockSize * 17;
        }


        if (isPacman == GameMode.PACMAN) {
            xPosPacman = charXPos;
            yPosPacman = charYPos;

            drawPacman(canvas);
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
            drawPacman(canvas);

        }
        // Depending on the direction move the position of pacman
        if (isPacman == GameMode.PACMAN && enemyBlockSize != 0) {
            Log.d(TAG, "moveCharacter enemyblock size = "+enemyBlockSize);
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
        } else if (isPacman == GameMode.GHOST && enemyBlockSize != 0){
            Log.d(TAG, "moveCharacter enemyblock size = "+enemyBlockSize);
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

//    public void setEnemyGhostDirection() {
//        virtualRoomGhostReference.child("direction").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    setEnemyGhostDirection(Integer.parseInt(dataSnapshot.getValue().toString()));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

//    public void setEnemyGhostDirection(int enemyGhostDirection) {
//        this.isGhostDirectionChanged = true;
//        this.mEnemyGhostDirection = enemyGhostDirection;
//    }
//
//    private void determineEnemyGhostXY(int direction) {
//        if (direction == 0) {
//            yPosGhost += -blockSize / 15;
//        } else if (direction == 1) {
//            xPosGhost += blockSize / 15;
//        } else if (direction == 2) {
//            yPosGhost += blockSize / 15;
//        } else if (direction == 3) {
//            xPosGhost += -blockSize / 15;
//        }
//    }
//
//    public void setEnemyPacmanDirection() {
//        virtualRoomPacmanReference.child("direction").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    setEnemyPacmanDirection(Integer.parseInt(dataSnapshot.getValue().toString()));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    public void setEnemyPacmanDirection(int enemyPacmanDirection) {
//        this.isPacmanDirectionChanged = true;
//        this.mEnemyPacmanDirection = enemyPacmanDirection;
//    }
//
//    private void determineEnemyPacmanXY(int direction) {
//        if (direction == 0) {
//            yPosPacman += -blockSize / 15;
//        } else if (direction == 1) {
//            xPosPacman += blockSize / 15;
//        } else if (direction == 2) {
//            yPosPacman += blockSize / 15;
//        } else if (direction == 3) {
//            xPosPacman += -blockSize / 15;
//        }
//    }



    // Updates the character sprite and handles collisions
    public void movePacman(Canvas canvas) {
        short ch;

        // Check if xPos and yPos of pacman is both a multiple of block size
        if ( (xPosPacman % blockSize == 0) && (yPosPacman  % blockSize == 0) ) {

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
                // Toggle pellet so it won't be drawn anymore
                leveldata1[yPosPacman / blockSize][xPosPacman / blockSize] = (short) (ch ^ 16);
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

        drawPacman(canvas);

        // Depending on the direction move the position of pacman
        if (direction == 0) {
            yPosPacman += -blockSize/15;
        } else if (direction == 1) {
            xPosPacman += blockSize/15;
        } else if (direction == 2) {
            yPosPacman += blockSize/15;
        } else if (direction == 3) {
            xPosPacman += -blockSize/15;
        }

    }

    // Method that draws pacman based on his viewDirection
    public void drawPacman(Canvas canvas) {
        switch (viewDirection) {
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

    private void moveGhost(Canvas canvas) {
        short ch;

            xDistance = xPosPacman - xPosGhost;
            yDistance = yPosPacman - yPosGhost;

            if ((xPosGhost % blockSize == 0) && (yPosGhost % blockSize == 0)) {
                ch = leveldata1[yPosGhost / blockSize][xPosGhost / blockSize];

                if (xPosGhost >= blockSize * 17) {
                    xPosGhost = 0;
                }
                if (xPosGhost < 0) {
                    xPosGhost = blockSize * 17;
                }


                if (xDistance >= 0 && yDistance >= 0) { // Move right and down
                    if ((ch & 4) == 0 && (ch & 8) == 0) {
                        if (Math.abs(xDistance) > Math.abs(yDistance)) {
                            ghostDirection = 1;
                        } else {
                            ghostDirection = 2;
                        }
                    }
                    else if ((ch & 4) == 0) {
                        ghostDirection = 1;
                    }
                    else if ((ch & 8) == 0) {
                        ghostDirection = 2;
                    }
                    else
                        ghostDirection = 3;
                }
                if (xDistance >= 0 && yDistance <= 0) { // Move right and up
                    if ((ch & 4) == 0 && (ch & 2) == 0 ) {
                        if (Math.abs(xDistance) > Math.abs(yDistance)) {
                            ghostDirection = 1;
                        } else {
                            ghostDirection = 0;
                        }
                    }
                    else if ((ch & 4) == 0) {
                        ghostDirection = 1;
                    }
                    else if ((ch & 2) == 0) {
                        ghostDirection = 0;
                    }
                    else ghostDirection = 2;
                }
                if (xDistance <= 0 && yDistance >= 0) { // Move left and down
                    if ((ch & 1) == 0 && (ch & 8) == 0) {
                        if (Math.abs(xDistance) > Math.abs(yDistance)) {
                            ghostDirection = 3;
                        } else {
                            ghostDirection = 2;
                        }
                    }
                    else if ((ch & 1) == 0) {
                        ghostDirection = 3;
                    }
                    else if ((ch & 8) == 0) {
                        ghostDirection = 2;
                    }
                    else ghostDirection = 1;
                }
                if (xDistance <= 0 && yDistance <= 0) { // Move left and up
                    if ((ch & 1) == 0 && (ch & 2) == 0) {
                        if (Math.abs(xDistance) > Math.abs(yDistance)) {
                            ghostDirection = 3;
                        } else {
                            ghostDirection = 0;
                        }
                    }
                    else if ((ch & 1) == 0) {
                        ghostDirection = 3;
                    }
                    else if ((ch & 2) == 0) {
                        ghostDirection = 0;
                    }
                    else ghostDirection = 2;
                }
                // Handles wall collisions
                if ( (ghostDirection == 3 && (ch & 1) != 0) ||
                        (ghostDirection == 1 && (ch & 4) != 0) ||
                        (ghostDirection == 0 && (ch & 2) != 0) ||
                        (ghostDirection == 2 && (ch & 8) != 0) ) {
                    ghostDirection = 4;
                }
            }

            if (ghostDirection == 0) {
                yPosGhost += -blockSize / 20;
            } else if (ghostDirection == 1) {
                xPosGhost += blockSize / 20;
            } else if (ghostDirection == 2) {
                yPosGhost += blockSize / 20;
            } else if (ghostDirection == 3) {
                xPosGhost += -blockSize / 20;
            }

        canvas.drawBitmap(ghostBitmap, xPosGhost, yPosGhost, paint);
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
    private void drawMap(Canvas canvas) {
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
                if (gameMode == GameMode.PACMAN ) { // need to listen to ghost path
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
