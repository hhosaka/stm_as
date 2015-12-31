package com.nag.android.stm;

import java.io.IOException;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class CaptureFragment extends Fragment implements OnClickListener, SurfaceHolder.Callback, PictureCallback,AutoFocusCallback, OnItemClickListener,Camera.ShutterCallback, MenuHandler.Listener, StorageCapacityManager.Listener {
	public static CaptureFragment newInstance(){
		final CaptureFragment instance = new CaptureFragment();
		return instance;
	}

	private Camera camera = null;
	private ThumbnailAdapter adapter = null;
	private Button shutter;
	private MenuHandler menuhandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menuhandler = new MenuHandler(getActivity(), this, this);
		setHasOptionsMenu(true);
	}
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(getLayoutID(getActivity()), container, false);
		SurfaceView preview=(SurfaceView)rootView.findViewById(R.id.surfaceView);
		SurfaceHolder holder = preview.getHolder();
		holder.addCallback(this);
		ListView thumbnail = (ListView)rootView.findViewById(R.id.listViewThumbnail);
		Point size=new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(size);
		adapter = ThumbnailAdapter.getInstance(getActivity(), menuhandler.getThumbnailSide(getActivity()), size);
		thumbnail.setAdapter(adapter);// TODO
		thumbnail.setOnItemClickListener(this);
		shutter =(Button) rootView.findViewById(R.id.buttonTakePicture);
		shutter.setOnClickListener(this);
		return rootView;
	}

	private int getLayoutID(Context context){
		if(menuhandler.getThumbnailSide(context)){
			return R.layout.fragment_preview_right;
		}else{
			return R.layout.fragment_preview_left;
		}
	}

	@Override
	public void onClick(View v) {
		shutter.setOnClickListener(null);
		getView().findViewById(R.id.textViewTaking).setVisibility(View.VISIBLE);
		camera.autoFocus(CaptureFragment.this);
	}

	@Override
	public void onUpdateFlash(String mode) {
		Camera.Parameters params = camera.getParameters();
		params.setFlashMode(mode);
		camera.setParameters(params);
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {
		camera.takePicture(this, null, CaptureFragment.this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		camera.setDisplayOrientation(90);
		Camera.Parameters params = camera.getParameters();
		Point picturesize = menuhandler.getPictureSize(getActivity());
		params.setPictureSize(picturesize.x, picturesize.y);
		Size previewsize = menuhandler.getMinimumSize(camera.getParameters().getSupportedPreviewSizes().toArray(new Size[0]));
		params.setPreviewSize(previewsize.width, previewsize.height);
		params.setFlashMode(menuhandler.getFlashMode(getActivity()));
		camera.setParameters(params);
		previewsize = camera.getParameters().getPreviewSize();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ListView listView = (ListView) parent;
		FragmentManager manager = getFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		Fragment fragment = new ReviewFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ReviewFragment.ARG_FILENAME, ((ThumbnailInfo)listView.getItemAtPosition(position)).getFilename());
		fragment.setArguments(bundle);
		t.replace(R.id.fragmentMain, fragment);
		t.addToBackStack(null);
		t.commit();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		camera.startPreview();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		camera.stopPreview();
		camera.release();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		adapter.insertImage(getActivity(), data);
		this.getView().findViewById(R.id.textViewTaking).setVisibility(View.GONE);
		shutter.setOnClickListener(this);
		camera.startPreview();
	}

	@Override
	public void onShutter() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.capture, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(menuhandler.onOptionsItemSelected(getActivity(), null, item)){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResetCapacity(int size) {
		adapter.resetCapacity(getActivity());
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	@Override
	public void onUpdateThumbnailSide(int side) {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		t.replace(R.id.fragmentMain, new CaptureFragment());
		t.commit();
	}
}
