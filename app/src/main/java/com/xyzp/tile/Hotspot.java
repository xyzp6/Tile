package com.xyzp.tile;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Hotspot extends TileService {
    private void startTethering() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            File outputDir = getCodeCacheDir();
            Class classOnStartTetheringCallback = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
            Method startTethering = connectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, classOnStartTetheringCallback);
            Object proxy = ProxyBuilder.forClass(classOnStartTetheringCallback).dexCache(outputDir).handler(new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    return null;
                }
            }).build();
            startTethering.invoke(connectivityManager, 0, false, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopTethering() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Method stopTethering = connectivityManager.getClass().getDeclaredMethod("stopTethering", int.class);
            stopTethering.invoke(connectivityManager, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick() {
        super.onClick();
        if (!Settings.System.canWrite(Hotspot.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            if (getQsTile().getState()==Tile.STATE_ACTIVE) {
                stopTethering();
                getQsTile().setState(Tile.STATE_INACTIVE);  // 设置磁贴为活动状态
                getQsTile().updateTile();
            }
            else if (getQsTile().getState()==Tile.STATE_INACTIVE){
                startTethering();
                getQsTile().setState(Tile.STATE_ACTIVE);  // 设置磁贴为活动状态
                getQsTile().updateTile();
            }
        }
    }
}