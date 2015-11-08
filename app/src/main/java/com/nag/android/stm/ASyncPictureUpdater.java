package com.nag.android.stm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

public class ASyncPictureUpdater extends AsyncTask<Object, Void, Bitmap>{

	public static final Object lock = new Object();

	private Context context = null;
	private ThumbnailAdapter adapter;

	ASyncPictureUpdater(Context context, ThumbnailAdapter adapter){
		this.context = context;
		this.adapter = adapter;
	}

	@Override
	protected Bitmap doInBackground(Object... args) {
		synchronized(lock){
			byte[] data = (byte[])args[0];
			Bitmap org = BitmapFactory.decodeByteArray(data, 0, data.length, null);
			Matrix mat = new Matrix();
			mat.postRotate(90);
			Bitmap bmp = Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), mat, true);
			org.recycle();
			return bmp;
		}
	}

	@Override
	protected void onPostExecute(Bitmap result){
		adapter.updatePicture(context, result);
	}
}
