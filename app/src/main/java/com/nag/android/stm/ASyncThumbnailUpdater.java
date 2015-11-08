package com.nag.android.stm;

import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

public class ASyncThumbnailUpdater extends AsyncTask<Context,ThumbnailInfo,Void>{
	private ArrayAdapter<ThumbnailInfo> adapter;
	private Point size;

	ASyncThumbnailUpdater(ArrayAdapter<ThumbnailInfo>adapter, Point size){
		this.adapter = adapter;
		this.size = size;
	}

	@Override
	protected Void doInBackground(Context... context) {
		String[] files = context[0].fileList();
		Arrays.sort(files, new Comparator<String>(){
			@Override
			public int compare(String lhs, String rhs) {
				return -lhs.compareTo(rhs);
			}
		});
		for(String filename : files){
			synchronized(ASyncPictureUpdater.lock){
				this.publishProgress(new ThumbnailInfo(context[0], size, filename));
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(ThumbnailInfo... result) {
		adapter.add(result[0]);
	}
}
