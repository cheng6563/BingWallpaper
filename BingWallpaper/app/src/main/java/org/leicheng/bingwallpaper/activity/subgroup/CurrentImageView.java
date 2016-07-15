package org.leicheng.bingwallpaper.activity.subgroup;

import java.io.File;

import org.leicheng.bingwallpaper.R;
import org.leicheng.bingwallpaper.bean.BingImage;
import org.leicheng.bingwallpaper.util.AppUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CurrentImageView extends FrameLayout {
	private static final int IMAGE_SUCCESS = 3;
	private static final int IMAGE_ERROR = 4;
	private BingImage bingImage;
	private View currentView;
	private ImageView imageView;
	private ViewGroup imageLayout;
	private View title_layout;
	private TextView title;
	private ProgressBar progressBar;
	private File imageFile;
	public Handler handler;
	private AppUtil wallpaperUtil = new AppUtil();
	private Bitmap decodeImageBitmap;

	/**
	 * 
	 * @param context
	 * @param bingImage :封装了下载地址，文件名等
	 */
	public CurrentImageView(Context context, BingImage bingImage) {
		super(context);
		this.bingImage = bingImage;
		// 初始化
		LayoutInflater inflater = LayoutInflater.from(context);
		currentView = inflater.inflate(R.layout.viewpager_current, null);
		addView(currentView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		imageView = (ImageView) findViewById(R.id.imageView);
		imageLayout = (ViewGroup) findViewById(R.id.imageLayout);
		title_layout = findViewById(R.id.title_layout);
		title = (TextView) findViewById(R.id.title);
		imageFile = new File(context.getCacheDir().getPath() + "/" + this.bingImage.getStartdate());
		initHander();
	}

	private void initialWallpaper() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 下载图像
					wallpaperUtil.getBingWallpaperFile(bingImage.getUrl(), imageFile);
					//无异常则下载成功
					handler.sendEmptyMessage(IMAGE_SUCCESS);
				} catch (Throwable e) {
					handler.sendEmptyMessage(IMAGE_ERROR);
				}
			}
		}).start();
	}

	/**
	 * 外部调用，开始下载图像
	 */
	public void loadImage() {
		if (imageFile.exists())
			handler.sendEmptyMessage(IMAGE_SUCCESS);
		else
			initialWallpaper();
	}

	/**
	 * 外部调用，销毁图像
	 */
	public void removeImage() {

		imageView.setImageDrawable(null);
		if (decodeImageBitmap != null && !decodeImageBitmap.isRecycled()) {
			decodeImageBitmap.recycle();
			decodeImageBitmap = null;
		}
		System.gc();
	}

	private void initHander() {
		handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case IMAGE_SUCCESS:
					imageSuccess();
					break;
				case IMAGE_ERROR:
					imageError((String) msg.obj);
					break;
				}
				return false;
			}
		});
	}

	private void imageError(String msg) {
		Toast.makeText(getContext(), "载入失败，正在重试。", Toast.LENGTH_LONG).show();
		progressBar.setVisibility(View.VISIBLE);
		title_layout.setVisibility(View.GONE);
		imageLayout.setVisibility(View.GONE);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (imageFile.exists())
					imageFile.delete();
				loadImage();
			}
		}).start();

	}

	private void imageSuccess() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;// 图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一
		decodeImageBitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);

		imageView.setImageBitmap(decodeImageBitmap);

		title.setText(bingImage.getCopyright());
		progressBar.setVisibility(View.GONE);
		title_layout.setVisibility(View.VISIBLE);
		imageLayout.setVisibility(View.VISIBLE);
		System.gc();
	}

}
