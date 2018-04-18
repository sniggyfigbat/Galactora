package net.stefancbauer.galactora.View;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

public class GameActivity extends AppCompatActivity {

    private GameSurfaceView gsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int nUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(nUIFlag);

        //Pass the metrics to the screensize point
        Point screenSize = new Point();
        this.getWindowManager().getDefaultDisplay().getRealSize(screenSize);
        gsv = new GameSurfaceView(this, screenSize);

        //setContentView(R.layout.activity_game);
        setContentView(gsv);
    }

    private void _lockOrientation() {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
    }

    private void _unlockOrientation() {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gsv.resume();
        _lockOrientation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gsv.pause();
        _unlockOrientation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        _unlockOrientation();
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();
        Vector2f cursorPos = new Vector2f(event.getX(), event.getY());

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                gsv.game.addEvent(cursorPos, true);
                break;
            case MotionEvent.ACTION_UP:
                gsv.game.addEvent(cursorPos, false);
                break;
        }
        return true;
    }
}
