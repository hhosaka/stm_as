package com.nag.android.stm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nag.android.stm.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.widget.Toast;

public class ThumbnailInfo {
//	private final int THUMBNAIL_SIZE = 128;
	private final float THUMBNAIL_RATIO = 5;
	private Bitmap image;
	private long tp;

	ThumbnailInfo(Context context, Point size, Bitmap bitmap) throws IOException{
		tp = System.currentTimeMillis();
		OutputStream out = context.openFileOutput(getFilename(), Context.MODE_PRIVATE);
		bitmap.compress(CompressFormat.JPEG, 100, out);
		out.close();
		this.image = createThumbnail(context, size, bitmap);
	}

	ThumbnailInfo(Context context, Point size, String filename){
		this.image = createThumbnail(context, size, filename);
		tp = Long.parseLong(filename.substring(0,filename.length()-4));
	}
	
	public Bitmap getThumbnail(){
		return image;
	}

	public String getFilename(){
		return tp + ".jpg";
	}

	public String getLabel(){
		return new SimpleDateFormat("yyyy/MM/dd").format(new Date(tp));
	}

	private Bitmap createThumbnail(Context context, Point size, Bitmap src){
		Bitmap bmp = Bitmap.createBitmap((int)(size.x/THUMBNAIL_RATIO), (int)(size.y/THUMBNAIL_RATIO), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		Rect rc = new Rect(0, 0, src.getWidth(), src.getHeight());
		Rect dst = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		c.drawBitmap(src, rc, dst, null);
		return bmp;
	}

	private Bitmap createThumbnail(Context context, Point size, String filename) {
		InputStream in;
		try {
			in = context.openFileInput(filename);
			Bitmap ret = createThumbnail(context, size, BitmapFactory.decodeStream(in));
			in.close();
			return ret;
		} catch (FileNotFoundException e) {
			Toast.makeText(context, "File Not Found",Toast.LENGTH_LONG);
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
		} catch (IOException e) {
			Toast.makeText(context, "File IO error",Toast.LENGTH_LONG);
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
		}
	}
}
