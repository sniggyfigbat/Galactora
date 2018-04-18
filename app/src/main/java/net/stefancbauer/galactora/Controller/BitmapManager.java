package net.stefancbauer.galactora.Controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import net.stefancbauer.galactora.R;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Semi-Singleton that manages all texture loading. Heavily based on code by Kieran Clare, many thanks.
 */

public class BitmapManager {
    private static boolean instanceFlag = false;
    private static BitmapManager instance;

    private Map<String, Bitmap> bitmaps;
    private static Map<String, Integer> drawableIDs;

    public static boolean createInstance(Context context) {
        if (!instanceFlag) {
            instance = new BitmapManager(context);
            instanceFlag = true;
            return true;
        } else { return false; }
    }

    public static BitmapManager getInstance() {
        if (!instanceFlag) {
            Log.d("ERROR", "Attempted to call BitmapManager.getInstance() with no instance available!");
        }
        return instance;
    }

    private BitmapManager(Context context) {
        bitmaps = new HashMap<>();
        drawableIDs = new HashMap<>();
        loadDrawables(R.drawable.class);
        setUpBitmaps(context);
    }

    private static void loadDrawables(Class<?> clz){
        final Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            try {
                drawableIDs.put(field.getName(), field.getInt(clz));
            } catch (Exception e) {
                continue;
            }
        }
    }

    private void setUpBitmaps(Context context){
        for(String key : drawableIDs.keySet()){
            bitmaps.put(key, BitmapFactory.decodeResource(context.getResources(), drawableIDs.get(key)));
        }
    }

    public Bitmap getBitmap(String s){
        return bitmaps.get(s);
    }
}