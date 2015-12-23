package com.nag.android.stm;

import com.nag.android.stm.R;

import android.content.Context;
import android.graphics.Bitmap;
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
	private int capacity;
	private static ThumbnailAdapter instance = null;

	public static ThumbnailAdapter getInstance(Context context, int capacity,boolean side, Point size){
		if(instance==null){
			instance = new ThumbnailAdapter(context, capacity, side, size);
		}
		return instance;
	}

	public static void reset(){
		instance = null;
	}

	public ThumbnailAdapter(Context context, int capacity, boolean side, Point size) {
		super(context, R.layout.item_thumbnail_right);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.size = size;
		this.side = side;
		this.capacity = capacity;
		new ASyncThumbnailUpdater(this, size).execute(context);
	}

	public void insertImage(Context context, byte[]data){
		new ASyncPictureUpdater(context, this).execute(data);
	}

	public void updatePicture(Context context, Bitmap bitmap){
		try{
			insert(new ThumbnailInfo(context, size, bitmap), 0);
			recycle(context);
			bitmap.recycle();
		}catch(Exception e){
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void recycle(Context context){
		if(this.getCount()>capacity){
			for(int i=0; i<getCount(); ++i){
				ThumbnailInfo info = this.getItem(this.getCount() - i - 1);
				if(!ProtectManager.getInstance(context).isProtected(info.getFilename())){
					context.deleteFile(info.getFilename());
					//ProtectManager.getInstance(context).remove(info.getFilename());
					this.remove(info);
				}
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
		((TextView)convertView.findViewById(R.id.textViewLabel)).setText(info.getLabel());
		
		return convertView;
	}

	public void resetCapacity(int capacity){
		this.capacity = capacity;
		while(getCount()>capacity){
			remove(getItem(getCount()-1));
		}
	}
}
