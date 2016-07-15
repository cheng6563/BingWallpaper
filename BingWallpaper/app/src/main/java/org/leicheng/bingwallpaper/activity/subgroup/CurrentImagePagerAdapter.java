package org.leicheng.bingwallpaper.activity.subgroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.leicheng.bingwallpaper.bean.BingImage;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class CurrentImagePagerAdapter extends PagerAdapter {

	private List<BingImage> images;
	private Map<BingImage,View> views=new HashMap<BingImage, View>();
	private Context context;
	//private android.view.LayoutInflater inflater;
	
	public CurrentImagePagerAdapter(Context context,List<BingImage> images){
		if(context==null||images==null){
			throw new NullPointerException();
		}
		this.context=context;
		this.images=images;
		//inflater=android.view.LayoutInflater.from(context);
		for(BingImage item:images){
			CurrentImageView view=new CurrentImageView(this.context, item);
			views.put(item, view);
		}
	}
	
	@Override
	public int getCount() {
		return images.size();
	}
	
	public BingImage getItem(int index){
		return images.get(index);
	}
	
	public void appendItem(Collection<BingImage> items){
		images.addAll(items);
		for(BingImage item:images){
			CurrentImageView view=new CurrentImageView(this.context, item);
			views.put(item, view);
		}
		notifyDataSetChanged();
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		BingImage bingImage=images.get(position);
		CurrentImageView imageView=(CurrentImageView) views.get(bingImage);
		imageView.loadImage();
		container.addView(imageView,0);
		return imageView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		BingImage bingImage=images.get(position);
		CurrentImageView imageView=(CurrentImageView) views.get(bingImage);
		imageView.removeImage();
		container.removeView(imageView);
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0==arg1;
	}
}
