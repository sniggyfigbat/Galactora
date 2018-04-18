package net.stefancbauer.galactora.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.stefancbauer.galactora.Controller.BitmapManager;
import net.stefancbauer.galactora.Model.Game;
import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.LocalMaths.Vector2i;

/**
 * Created by P13186907 on 23/02/2018.
 */

public class GameSurfaceView extends SurfaceView implements Runnable {
    public static float GUtoDP; //!< The factor by which a game unit measure must be multiplied to get the same quantity in dp.
    public static float DPtoGU; //!< The factor by which a dp measure must be multiplied to get the same quantity in GU.
    public static Vector2f viewOrigin = new Vector2f(); //!< The current 0,0 point of the view, in Game Units. Game Unit origin is in the centre of the screen, at the top of the UI, just below the player's ship.

    public static Vector2i GUtoDP(Vector2f gameUnits) //!< Translates a point in Game-Space to a point in screenspace, correctly translating the origin and scale.
    {
        Vector2f fromScreenOrigin = Vector2f.subtract(gameUnits, viewOrigin);
        fromScreenOrigin.x *= GUtoDP;
        fromScreenOrigin.y *= -GUtoDP;
        return fromScreenOrigin.toInteger();
    }

    public static Vector2f DPtoGU(Vector2i screenPoint) //!< Translates a point in screenspace to a point in Game-Space, correctly translating the origin and scale.
    {
        Vector2f fromScreenOrigin = screenPoint.toFloat();
        fromScreenOrigin.x *= DPtoGU;
        fromScreenOrigin.y *= -DPtoGU;
        return Vector2f.add(fromScreenOrigin, viewOrigin);
    }

    private final static int    MAX_FPS = 60;                   // desired fps
    private final static int    MAX_FRAME_SKIPS = 5;            // maximum number of frames to be skipped
    private final static int    FRAME_PERIOD = 1000 / MAX_FPS;  // the frame period
    private long beginTime;                                     // the time when the cycle began
    private long timeDiff;                                      // the time it took for the cycle to execute
    private long deltaTime = 0;                                 // the time taken by the whole of the last cycle, including sleep, useful for updating.
    private long timeDiffCanvas;
    private int sleepTime;                                      // ms to sleep
    private int framesSkipped;                                  // number of frames being skipped

    Paint paint = new Paint();
    Point screenSize; //!< Holds the dimensions, in dp, of the screen.
    SurfaceHolder holder; //!< Holds the canvas.
    private boolean paused = false; //!< Toggled to determine paused/playing.
    Thread t = null; //!< Thread being used for game logic.

    Game game; //!< Game object, holding as much model content as possible.

    public GameSurfaceView(Context context, Point screenSize) {
        super(context);
        holder = getHolder();
        this.screenSize = screenSize;

        if (!BitmapManager.createInstance(context)) {
            Log.d("ERROR", "Attempted to call BitmapManager.createInstance() with an instance already created!");
        }

        // Time to work out the dp to gu ratio.
        // Desired horizontal span is 14gu.
        // Minimum vertical span is 21gu.
        // 5gu of that vertical span are UI
        // Game Unit origin is in the x-Axis centre of the screen, at the top of the UI, just below the player's ship at start.
        // All game action will take place within the bounds of -7 to 7 x, 0 to 16 y, even if the available playing field is larger.
        if ((float)screenSize.y < (float)screenSize.x * 1.5) {
            // Screen is somehow not able to achieve a 2:3 aspect ratio.
            GUtoDP = (float)screenSize.y / 21.0f; // MAGIC
            DPtoGU = 1 / GUtoDP;

            float screenWidthGU = screenSize.x * DPtoGU;
            viewOrigin = new Vector2f((screenWidthGU * -0.5f), 16.0f); // MAGIC
        } else {
            // Screen is a sensible aspect ratio.
            GUtoDP = (float)screenSize.x / 14.0f; // MAGIC
            DPtoGU = 1 / GUtoDP;

            float screenHeightGU = screenSize.y * DPtoGU;
            viewOrigin.set(-7.0f, (screenHeightGU - 5f)); // MAGIC
        }

        this.game = new Game(screenSize, context);
    }

    private void updateCanvas (){
        //Update the items in the canvas
        game.update(deltaTime);
    }

    protected void drawCanvas(Canvas canvas){
        //Draw the items to the canvas
        canvas.drawARGB(255, 0, 0, 0);

        game.draw(canvas, paint);
    }

    public void run() {
        //Remove conflict between the UI thread and the game thread.
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while (!this.paused) {
            //Release all of the sounds
            //clearSound(); // TODO: Work this out.
            //perform canvas drawing
            if (!holder.getSurface().isValid()) {//if surface is not valid
                continue;//skip anything below it
            }
            Canvas c = holder.lockCanvas(); //Lock canvas, paint canvas, unlock canvas
            synchronized (holder) {
                beginTime = System.currentTimeMillis();
                framesSkipped = 0;  // resetting the frames skipped

                // update game state
                this.updateCanvas();
                timeDiffCanvas = System.currentTimeMillis() - beginTime;

                // render state to the screen
                // draws the canvas on the panel
                this.drawCanvas(c);

                // calculate how long did the cycle take
                timeDiff = System.currentTimeMillis() - beginTime;

                // calculate sleep time
                sleepTime = (int) (FRAME_PERIOD - timeDiff);
                if (sleepTime > 0) {
                    // if sleepTime > 0 put to sleep for short period of time
                    try {
                        // send the thread to sleep for a short period
                        // very useful for battery saving
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }

                //ADD THIS IF WE ARE DOING LOTS OF WORK
                //If sleeptime is greater than a frame length, skip a number of frames
                while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
                    // we need to catch up
                    // update without rendering
                    this.updateCanvas();
                    // add frame period to check if in next frame
                    sleepTime += FRAME_PERIOD;
                    framesSkipped++;
                }

                holder.unlockCanvasAndPost(c);
                deltaTime = System.currentTimeMillis() - beginTime;
            }
        }
    }

    public void pause(){
        paused = true;
        while(true){
            try{
                t.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            break;
        }
        t = null;
    }

    public void resume(){
        paused = false;
        t = new Thread(this);
        t.start();
    }


}
