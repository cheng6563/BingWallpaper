package org.leicheng.bingwallpaper.activity;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.leicheng.bingwallpaper.R;
import org.leicheng.bingwallpaper.activity.subgroup.CurrentImagePagerAdapter;
import org.leicheng.bingwallpaper.bean.BingImage;
import org.leicheng.bingwallpaper.util.AppUtil;
import org.leicheng.bingwallpaper.util.Constant;
import org.leicheng.bingwallpaper.util.PreferencesUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int MENU_SET_CURRENT_WALLPAPER = 1;
	private static final int MENU_AUTO_SET = 2;
	private static final int MENU_AD = 3;
	private static final int IMAGE_SUCCESS = 3;
	private static final int IMAGE_ERROR = 4;
	private String userId = "";

	// private static final int IMAGE_APPEND = 4;
	private Handler handler;
	// private ImageView imageView;
	// private ViewGroup imageLayout;
	// private ProgressBar progressBar;
	private ViewPager currentViewPager;
	private AppUtil wallpaperUtil = new AppUtil();
	// private File imageFile;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);

	// private ViewGroup adViewGroup;
	// private List<BingImage> result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setTheme(android.R.style.Theme_Material_Light);
		}
		setTitle("Bing壁纸");

//        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
//        }


		setContentView(R.layout.activity_main);


		currentViewPager = (ViewPager) findViewById(R.id.currentViewPager);

		initHander();
		currentViewPager.setAdapter(new CurrentImagePagerAdapter(this, new ArrayList<BingImage>()));
		List<BingImage> result = wallpaperUtil.readImageInfos(this);
		if (result != null && result.size() > 0 && sdf.format(new Date()).equals(String.valueOf(result.get(0).getEnddate()))
				&& sdf.format(new Date()).equals(PreferencesUtils.getString(this, "xmlDate"))) {
			Message msg = new Message();
			msg.what = IMAGE_SUCCESS;
			msg.obj = result;
			handler.sendMessage(msg);
		} else {
			initialWallpaper();
		}
		initAd();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initAd() {

	}

	private void initHander() {
		handler = new Handler(new Handler.Callback() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case IMAGE_SUCCESS:
					List<BingImage> result = (List<BingImage>) msg.obj;
					if (result == null || result.size() == 0) {
						imageError(null, msg.arg1);
						break;
					}
					PreferencesUtils.putString(MainActivity.this, "xmlDate", sdf.format(new Date()));
					wallpaperUtil.writeImageInfos(MainActivity.this, result);
					imageSuccess(result);
					break;
				case IMAGE_ERROR:
					imageError((String) msg.obj, msg.arg1);
					break;
				}
				return false;
			}
		});
	}

	private void imageSuccess(List<BingImage> result) {

		CurrentImagePagerAdapter adapter = (CurrentImagePagerAdapter) currentViewPager.getAdapter();
		adapter.appendItem(result);
	}

	private void imageError(String msg, final int index) {
		Log.e("error", msg == null ? "null." : msg);
		new AlertDialog.Builder(this).setTitle("提示").setMessage("网络出现错误").setPositiveButton("重试", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				initialWallpaper();

			}
		}).setNegativeButton("退出", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).show();
	}

	private void initialWallpaper() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Message msg = new Message();
					msg.obj = wallpaperUtil.getNowBingImages();
					msg.what = IMAGE_SUCCESS;
					handler.sendMessage(msg);
				} catch (Throwable e) {
					e.printStackTrace();
					Message msg = new Message();
					msg.arg1 = 0;
					msg.what = IMAGE_ERROR;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem set = menu.add(0, MENU_SET_CURRENT_WALLPAPER, 1, "设置壁纸");
		MenuItem autoSet = menu.add(0, MENU_AUTO_SET, 2, "自动设置");
		set.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		autoSet.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SET_CURRENT_WALLPAPER:
			setCurrentWallpaper();
			break;
		case MENU_AUTO_SET:
			autoSet();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void autoSet() {
		new AlertDialog.Builder(this).setTitle("提示")
				.setMessage(Html.fromHtml("您当前 " + (PreferencesUtils.getBoolean(this, "autoset", false) ? "<font color='green'>已经</font>" : "<font color='red'>没有</font>") + " 开启自动设置"))
				.setPositiveButton("开启", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PreferencesUtils.putBoolean(MainActivity.this, "autoset", true);
					}
				}).setNegativeButton("关闭", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PreferencesUtils.putBoolean(MainActivity.this, "autoset", false);
					}
				}).show();

	}

	private void setCurrentWallpaper() {
		new AlertDialog.Builder(MainActivity.this).setTitle("提示").setMessage("设置桌面背景?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					CurrentImagePagerAdapter adapter = (CurrentImagePagerAdapter) currentViewPager.getAdapter();
					BingImage bingImage = adapter.getItem(currentViewPager.getCurrentItem());
					File imageFile = new File(MainActivity.this.getCacheDir().getPath() + "/" + bingImage.getStartdate());
					setWallpaper(new FileInputStream(imageFile));

					System.gc();
				} catch (Throwable e) {
					Log.e("leicheng", e.toString());
				}
			}
		}).setNeutralButton("取消", null).show();
	}
}
