package org.leicheng.bingwallpaper.receiver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.leicheng.bingwallpaper.util.Constant;
import org.leicheng.bingwallpaper.util.PreferencesUtils;
import org.leicheng.bingwallpaper.util.AppUtil;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class WallpaperReceiver extends BroadcastReceiver {
	private static final int IMAGE_SUCCESS = 3;
	private static final int IMAGE_ERROR = 4;
	private SimpleDateFormat sdf;
	private AppUtil wallpaperUtil;
	private Handler handler;
	private File imageFile;
	private Context context;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		sdf = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
		wallpaperUtil = new AppUtil();
		if (!PreferencesUtils.getBoolean(context, "autoset", false) || sdf.format(new Date()).equals(PreferencesUtils.getString(context, "setDate")))
			return;
		imageFile = wallpaperUtil.getCacheFile(context);
		initHander();
		if (imageFile.exists() && sdf.format(new Date()).equals(PreferencesUtils.getString(context, "adate", "")))
			handler.sendEmptyMessage(IMAGE_SUCCESS);
		else
			initialWallpaper();

	}

	private void initialWallpaper() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					wallpaperUtil.getNowWallpaper(imageFile);
					handler.sendEmptyMessage(IMAGE_SUCCESS);
				} catch (Throwable e) {
					e.printStackTrace();
					handler.sendEmptyMessage(IMAGE_ERROR);
				}
			}
		}).start();
		
	}

	private void initHander() {
		handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case IMAGE_SUCCESS:
					PreferencesUtils.putString(context, "adate", sdf.format(new Date()));
					imageSuccess();
					break;
				case IMAGE_ERROR:
					break;
				}
				return false;
			}
		});
	}

	// @SuppressWarnings("deprecation")
	private void imageSuccess() {
		Bitmap decodeFile = BitmapFactory.decodeFile(imageFile.getPath());
		try {
			WallpaperManager.getInstance(context).setBitmap(decodeFile);
			// context.setWallpaper(decodeFile);
			PreferencesUtils.putString(context, "setDate", sdf.format(new Date()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PreferencesUtils.putString(context, "adate", sdf.format(new Date()));
	}
}
