package com.nag.android.stm;

import java.io.InputStream;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class ReviewFragment extends Fragment implements TranslationGestureDetector.TranslationGestureListener{

	private ScaleGestureDetector sgd;
	private TranslationGestureDetector tgd;
	private MenuHandler menuhandler;
	private float scale = 0.0f;
	private float prev_scale = 0.0f;
	private float cx, cy;
	private float px, py;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onTranslationEnd(float x, float y) {
	}

	@Override
	public void onTranslationBegin(float x, float y) {
		px = x;
		py = y;
	}

	@Override
	public void onTranslation(float x, float y) {
		cx +=  x - px;
		cy += y - py;
		px = x;
		py = y;
	}

	public static ReviewFragment newInstance(){
		final ReviewFragment instance = new ReviewFragment();
		return instance;
	}

	private Matrix matrix;

	public ReviewFragment(){
		matrix = new Matrix();
	}

	Bitmap org = null;
//	private Point size;
	public static final String ARG_FILENAME = "filename";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		MainActivity main_activity = (MainActivity)getActivity();
		InternalView rootView = new InternalView(main_activity);
		sgd = new ScaleGestureDetector(main_activity,
				new ScaleGestureDetector.SimpleOnScaleGestureListener() {
					@Override
					public boolean onScaleBegin(ScaleGestureDetector detector) {
						return super.onScaleBegin(detector);
					}
					@Override
					public void onScaleEnd(ScaleGestureDetector detector) {
						super.onScaleEnd(detector);
					}
					@Override
					public boolean onScale(ScaleGestureDetector detector) {
						prev_scale = scale;
						scale *= detector.getScaleFactor();
						return true;
					}
		});
		tgd = new TranslationGestureDetector(this);

		try{
			boolean test;
			String filename = getArguments().getString(ARG_FILENAME);
			main_activity.getMenuHandler().setFilename(filename);
//			menuitem_protect.setChecked(test = ProtectManager.getInstance(getActivity()).isProtected(filename));
			InputStream in = main_activity.openFileInput(filename);
			org = BitmapFactory.decodeStream(in);
			rootView.setBitmap(org);
			in.close();
		}catch(Exception e){
			rootView.setBitmap(BitmapFactory.decodeResource(main_activity.getResources(), R.drawable.ic_launcher));
			Toast.makeText(main_activity, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public void onDestroy() {
		if(org!=null){org.recycle();}
		super.onDestroy();
	}

	private class InternalView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener{
		private final int MAX_MAGNIFY = 8;
		private SurfaceHolder holder;
		private float scale_max;
		private float scale_min;
		private int width;
		private int height;
		private Bitmap bitmap;

		public InternalView(Context context) {
			super(context);
			getHolder().addCallback(this);
			setOnTouchListener(this);
		}

		public void setBitmap(Bitmap bitmap){
			this.bitmap = bitmap;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			present(holder, width, height, 0, 0);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}
 
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
//			bitmap.recycle();
		}

		private void present(float x, float y){
			present(holder, width, height, x, y);
		}

		private void present(SurfaceHolder holder, int width, int height, float x,float y)
		{
			this.holder = holder;
			this.width = width;
			this.height = height;

			if(scale == 0.0f){
				float sw = (float)width /bitmap.getWidth();
				float sh = (float)height /bitmap.getHeight();
				if(sw>sh){
					scale = sw;
				}else{
					scale = sh;
				}
				scale_min = prev_scale = scale;
				scale_max = scale_min*MAX_MAGNIFY;
			}

			if(scale > scale_max){
				scale = scale_max;
			}else if(scale < scale_min){
				scale = scale_min;
			}
			if(scale != prev_scale){
//				cx -= (float)(bitmap.getWidth()/2) * (scale / prev_scale);
//				cy -= (float)(bitmap.getHeight()/2) * (scale / prev_scale);
				cx -= x * (scale - prev_scale);
				cy -= y * (scale - prev_scale);
				prev_scale = scale;
			}
			if(cx > 0 ){
				cx = 0 ;
			}else if(cx<width-bitmap.getWidth()*scale){
				cx = width - bitmap.getWidth()*scale;
			}
			if(cy > 0 ){
				cy = 0 ;
			}else if(cy<height-bitmap.getHeight()*scale){
				cy = height - bitmap.getHeight()*scale;
			}
			matrix.reset();
			matrix.postScale(scale, scale);
			matrix.postTranslate(cx, cy);

			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(Color.BLACK);
			canvas.drawBitmap(bitmap, matrix, null);

			holder.unlockCanvasAndPost(canvas);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			super.onTouchEvent(event);
			sgd.onTouchEvent(event);
			tgd.onTouch(v, event);
			float x = 0;
			float y = 0;
			if(event.getPointerCount()==2){
				x = (event.getX(0) + event.getX(1)) / 2;
				y = (event.getY(0) + event.getY(1)) / 2;
			}
			present(x, y);
			return true;
		}
		@Override
		public boolean performClick() {
			return super.performClick();
		}
	}
}
