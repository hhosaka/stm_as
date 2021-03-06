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

public class CaptureFragment extends Fragment implements OnClickListener, SurfaceHolder.Callback, PictureCallback,AutoFocusCallback, OnItemClickListener,Camera.ShutterCallback, CaptureListener, StorageEventListener {
	public static CaptureFragment newInstance(){
		final CaptureFragment instance = new CaptureFragment();
		return instance;
	}

//	private Camera camera = null;
	private ThumbnailAdapter adapter = null;
	private Button shutter;

	private Camera getCamera(){
		return ((MainActivity)getActivity()).getCamera();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		MenuHandler menuhandler = ((MainActivity)getActivity()).getMenuHandler();
		menuhandler.setCaptureListener(this, this);
		menuhandler.setFilename(null);

		View rootView = inflater.inflate(getLayoutID(menuhandler.getThumbnailSide()), container, false);
		SurfaceView preview=(SurfaceView)rootView.findViewById(R.id.surfaceView);
		SurfaceHolder holder = preview.getHolder();
		holder.addCallback(this);
		ListView thumbnail = (ListView)rootView.findViewById(R.id.listViewThumbnail);
		Point size=new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(size);
		adapter = ThumbnailAdapter.getInstance(getActivity(), menuhandler.getThumbnailSide(), size);
		thumbnail.setAdapter(adapter);// TODO
		thumbnail.setOnItemClickListener(this);
		shutter =(Button) rootView.findViewById(R.id.buttonTakePicture);
		shutter.setOnClickListener(this);
		return rootView;
	}

	private int getLayoutID(boolean side){
		if(side){
			return R.layout.fragment_preview_right;
		}else{
			return R.layout.fragment_preview_left;
		}
	}

	@Override
	public void onClick(View v) {
		shutter.setOnClickListener(null);
		getView().findViewById(R.id.textViewTaking).setVisibility(View.VISIBLE);
		getCamera().autoFocus(CaptureFragment.this);
	}

	@Override
	public void onUpdateFlash(String mode) {
		Camera.Parameters params = getCamera().getParameters();
		params.setFlashMode(mode);
		getCamera().setParameters(params);
	}

	@Override
	public void onAutoFocus(boolean arg0, Camera arg1) {
		getCamera().takePicture(this, null, CaptureFragment.this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Logger.write(getActivity(), "serfaceCreated");
		try {
			MenuHandler menuhandler = ((MainActivity) getActivity()).getMenuHandler();
//			camera = Camera.open();
			Camera camera = getCamera();
			camera.setDisplayOrientation(90);
			Camera.Parameters params = camera.getParameters();
			Point picturesize = menuhandler.getPictureSize(getActivity(), getCamera());
			params.setPictureSize(picturesize.x, picturesize.y);
			Size previewsize = menuhandler.getMinimumSize(camera.getParameters().getSupportedPreviewSizes().toArray(new Size[0]));
			params.setPreviewSize(previewsize.width, previewsize.height);
			params.setFlashMode(menuhandler.getFlashMode(getActivity()));
			camera.setParameters(params);
			previewsize = camera.getParameters().getPreviewSize();
			camera.setPreviewDisplay(holder);
		} catch (Exception e) {
			Toast.makeText(getActivity(), R.string.error_fail_to_open_camera, Toast.LENGTH_LONG).show();
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
		Logger.write(getActivity(), "serfaceChanged");
		getCamera().startPreview();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Logger.write(getActivity(), "serfaceDestroyed");
		getCamera().stopPreview();
//		camera.release();
//		camera = null;
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

//	@Override
//	public Camera getCamera() {
//		return camera;
//	}

	@Override
	public void onUpdateThumbnailSide(int side) {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		t.replace(R.id.fragmentMain, new CaptureFragment());
		t.commit();
	}
	@Override
	public void onResetCapacity(int size) {
		adapter.resetCapacity(getActivity());
	}
}
