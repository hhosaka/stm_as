package com.nag.android.stm;

import com.nag.android.util.PreferenceHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

class ThumbnailAdapter extends ArrayAdapter<ThumbnailInfo>{

	private LayoutInflater inflater = null;
	private Point size = null;
	private boolean side;
	private static ThumbnailAdapter instance = null;
	private final PreferenceHelper ph;
	private final ProtectManager pm;

	public static ThumbnailAdapter getInstance(Context context,boolean side, Point size){
		if(instance==null){
			instance = new ThumbnailAdapter(context, side, size);
		}
		return instance;
	}

	public static void reset(){
		instance = null;
	}

	public ThumbnailAdapter(Context context, boolean side, Point size) {
		super(context, R.layout.item_thumbnail_right);
		ph = PreferenceHelper.getInstance(context);
		pm = new ProtectManager(ph);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.size = size;
		this.side = side;
		new ASyncThumbnailUpdater(this, size).execute(context);
	}

	public void insertImage(Context context, byte[]data){
		new ASyncPictureUpdater(context, this).execute(data);
	}

	public void updatePicture(Context context, Bitmap bitmap){
		try{
			insert(new ThumbnailInfo(context, size, bitmap), 0);
			adjust(context);
			bitmap.recycle();
		}catch(Exception e){
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private static final String PREF_STORAGE_CAPACITY = "storage_capacity";//TODO[H]tentative


	private int getCapacity(){
		return ph.getInt(PREF_STORAGE_CAPACITY, 10);
	}

	private void adjust(Context context){
		int count = getCount();
		for(int i = count - 1; getCapacity() < getCount() && i >= 0; --i){
			ThumbnailInfo info = getItem(i);
			if(!pm.isProtected(info.getFilename())){
				context.deleteFile(info.getFilename());
				this.remove(info);
			}
		}
	}

	private int getLayoutID(){
		if(side){
			return R.layout.item_thumbnail_right;
		}else{
			return R.layout.item_thumbnail_left;
		}
	}
	@Override
	 public View getView(int position, View convertView, ViewGroup parent) {
		ThumbnailInfo info = getItem(position);
		assert(convertView!=null);

		if (null == convertView) {
			 convertView = inflater.inflate(getLayoutID(), parent, false);
		}
		((ImageView)convertView.findViewById(R.id.imageViewThumbnail)).setImageBitmap(info.getThumbnail());

		TextView label = ((TextView)convertView.findViewById(R.id.textViewLabel));
		label.setText(info.getLabel());
		label.setBackgroundColor(pm.isProtected(info.getFilename()) ? Color.RED: Color.BLACK);
		return convertView;
	}

	public void resetCapacity(Context context){
		while(getCount()>0){remove(getItem(0));}
		new ASyncThumbnailUpdater(this, size).execute(context);
	}
}
