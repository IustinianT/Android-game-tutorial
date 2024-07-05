package com.example.tutorialgame;

import static com.example.tutorialgame.MainActivity.GAME_HEIGHT;
import static com.example.tutorialgame.MainActivity.GAME_WIDTH;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.camera2.params.TonemapCurve;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.tutorialgame.entities.GameCharacters;
import com.example.tutorialgame.helpers.GameConstants;
import com.example.tutorialgame.inputs.TouchEvents;

import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private Paint redPaint = new Paint();
    private SurfaceHolder holder;
    private float x,y;
    private boolean movePlayer;
    private PointF lastTouchDiff;

    private Random rand = new Random();
    private GameLoop gameLoop;
    private TouchEvents touchEvents;

//    private ArrayList<PointF> skeletons = new ArrayList<PointF>();
    private PointF skeletonPos;
    private int skeletonDir =GameConstants.Face_Dir.DOWN;
    private long lastDirChange = System.currentTimeMillis();

    private int playerAniIndexY, playerFaceDir = GameConstants.Face_Dir.RIGHT;
    private int aniTick;
    private int aniSpeed = 10;

    public GamePanel(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        redPaint.setColor(Color.RED);

        touchEvents = new TouchEvents(this);

        gameLoop = new GameLoop(this);

        skeletonPos = new PointF(rand.nextInt(GAME_WIDTH), rand.nextInt(GAME_HEIGHT));

//        for(int i = 0; i < 50; i++) {
//            skeletons.add(new PointF(rand.nextInt(1080), rand.nextInt(1920)));
//        }
    }

    public void render(){
        Canvas c = holder.lockCanvas();
        // set canvas color to black
        c.drawColor(Color.BLACK);

        touchEvents.draw(c);

        c.drawBitmap(GameCharacters.PLAYER.getSprite(playerAniIndexY, playerFaceDir), x, y, null);
        c.drawBitmap(GameCharacters.SKELETON.getSprite(playerAniIndexY, skeletonDir), skeletonPos.x, skeletonPos.y, null);
        //for(PointF pos: skeletons) {
        //    c.drawBitmap(GameCharacters.SKELETON.getSprite(0, 0), pos.x, pos.y, null);
        //}

        // send canvas to be rendered
        holder.unlockCanvasAndPost(c);
    }

    public void update(double delta) {

        if (System.currentTimeMillis() - lastDirChange >= 5000) {
            skeletonDir = rand.nextInt(4);
            lastDirChange = System.currentTimeMillis();
        }

        switch (skeletonDir) {
            case GameConstants.Face_Dir.DOWN:
                skeletonPos.y += delta * 300;
                if(skeletonPos.y >= GAME_HEIGHT) {
                    skeletonDir = GameConstants.Face_Dir.UP;
                }
                break;
            case GameConstants.Face_Dir.UP:
                skeletonPos.y -= delta * 300;
                if(skeletonPos.y <= 0) {
                    skeletonDir = GameConstants.Face_Dir.DOWN;
                }
                break;
            case GameConstants.Face_Dir.RIGHT:
                skeletonPos.x += delta * 300;
                if(skeletonPos.x >= GAME_WIDTH) {
                    skeletonDir = GameConstants.Face_Dir.LEFT;
                }
                break;
            case GameConstants.Face_Dir.LEFT:
                skeletonPos.x -= delta * 300;
                if(skeletonPos.x <= 0) {
                    skeletonDir = GameConstants.Face_Dir.RIGHT;
                }
                break;
        }
        updatePlayerMove(delta);

        updateAnimation();
    }

    private void updatePlayerMove(double delta) {
        if(!movePlayer) {
            return;
        }

        float baseSpeed = (float) (delta * 300);
        float ratio = Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x);
        double angle = Math.atan(ratio); // radians

        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);

        if(xSpeed > ySpeed) {
            if(lastTouchDiff.x > 0) playerFaceDir = GameConstants.Face_Dir.RIGHT;
            else playerFaceDir = GameConstants.Face_Dir.LEFT;
        }
        else {
            if(lastTouchDiff.y > 0) playerFaceDir = GameConstants.Face_Dir.DOWN;
            else playerFaceDir = GameConstants.Face_Dir.UP;
        }

        if(lastTouchDiff .x < 0) {
            xSpeed *= -1;
        }
        if(lastTouchDiff.y < 0) {
            ySpeed *= -1;
        }
        x += xSpeed * baseSpeed;
        y += ySpeed * baseSpeed;

    }

    private void updateAnimation() {
        if (!movePlayer) {
            return;
        }
        aniTick++;
        if(aniTick >= aniSpeed) {
            aniTick = 0;
            playerAniIndexY++;
            if(playerAniIndexY >= 4)
                playerAniIndexY = 0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchEvents.touchEvent(event);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        gameLoop.startGameLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    public void setPlayerMoveTrue(PointF lastTouchDiff) {
        movePlayer = true;
        this.lastTouchDiff = lastTouchDiff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        resetAnimation();
    }

    private void resetAnimation() {
        aniTick = 0;
        playerAniIndexY = 0;
    }
}
