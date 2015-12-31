package com.nag.android.stm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nag.android.util.LabeledIntItem;
import com.nag.android.util.LabeledItem;
import com.nag.android.util.LabeledStringItem;
import com.nag.android.util.PreferenceHelper;

public class MenuHandler {
	public interface Listener{
		void onUpdateFlash(String mode);
		Camera getCamera();
		void onUpdateThumbnailSide(int side);
	}

	private static final String PREF_FLASH_MODE = "flash_mode";
	private static final String PREF_PICTURE_SIZE = "picture_size";
	private static final String PREF_THUMBNAIL_LOCATION = "thumbnail_location";
	private final PreferenceHelper ph;
	private final ProtectManager protectmanager;
	private final StorageCapacityManager scm;
	private final Listener listener;

	MenuHandler(Context context, Listener listener, StorageCapacityManager.Listener scmlistener){
		assert(listener!=null);
		this.listener = listener;
		ph = PreferenceHelper.getInstance(context);
		protectmanager = new ProtectManager(ph);
		scm = new StorageCapacityManager(ph, protectmanager, scmlistener);

	}

	private boolean isProtectAvailable(Context context){
		return protectmanager.getCount() < scm.get(context) - 1;
	}

	MenuItem itemProtect = null;

	private void setProtectStatus(Context context, String filename){
		if (itemProtect != null) {
			boolean isProtected = protectmanager.isProtected(filename);
			itemProtect.setEnabled(isProtected || isProtectAvailable(context));
			itemProtect.setChecked(isProtected);
		}
	}

	public void initialize(Context context, Menu menu, String filename){
		if(filename!=null) {
			itemProtect = (MenuItem) menu.findItem(R.id.action_protected);
			setProtectStatus(context, filename);
		}
	}

	private static void copy(FileInputStream src, FileOutputStream dest) throws IOException{

		FileChannel srcChannel = src.getChannel();
		FileChannel destChannel = dest.getChannel();

		srcChannel.transferTo(0, srcChannel.size(), destChannel);

		destChannel.close();
		srcChannel.close();
		dest.close();
		src.close();
	}

	private void registAndroidDB(Context context, String path) {
		ContentValues values = new ContentValues();
		ContentResolver contentResolver = context.getContentResolver();
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		values.put("_data", path);
		contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}

	private String export(Context context, String filename){
		String saveDir = Environment.getExternalStorageDirectory().getPath() + "/stm";
		File file = new File(saveDir);

		if (!file.exists()) {
			if (!file.mkdir()) {
				Toast.makeText(context, "Make Dir Error", Toast.LENGTH_LONG).show();
			}
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

		try {
			copy(context.openFileInput(filename), new FileOutputStream(imgPath));
		}catch(IOException e) {
			Toast.makeText(context, "fail to copy file", Toast.LENGTH_LONG);
		}
		registAndroidDB(context, imgPath);
		return imgPath;
	}

	private void sendMail(Context context, String filename){

		String imgPath = export(context, filename);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
//		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"test@exsample.com"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "picture from s.t.m.");
		intent.putExtra(Intent.EXTRA_TEXT, filename);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imgPath)));
		context.startActivity(intent);
	}

	public boolean onOptionsItemSelected(Context context, String filename, MenuItem menuitem){
		switch (menuitem.getItemId()){
		case R.id.action_protected:
			if(menuitem.isChecked()){
				protectmanager.remove(filename);
				menuitem.setChecked(false);
			}else{
				protectmanager.add(filename);
				menuitem.setChecked(true);
			}
			setProtectStatus(context, filename);
			return true;
		case R.id.action_export:
			export(context, filename);
			return true;
		case R.id.action_send_mail:
			sendMail(context, filename);
			return true;
		case R.id.action_flash_setting:
			selectFlashMode(context);
			return true;
		case R.id.action_strage_capacity:
			scm.select(context);
			return true;
		case R.id.action_picture_size:
			selectPictureSize(context);
			return true;
		case R.id.action_thumbnail_location:
			selectThumbnailSide(context);
			return true;
		case R.id.action_help:
			context.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(context.getResources().getString(R.string.string_url_help))));
			return true;
		default:
			return false;
		}
	}

	private void selectFlashMode(final Context context){
		final LabeledStringItem[] items = {
				new LabeledStringItem(context.getResources().getString(R.string.action_flash_on),Camera.Parameters.FLASH_MODE_ON),
				new LabeledStringItem(context.getResources().getString(R.string.action_flash_off),Camera.Parameters.FLASH_MODE_OFF),
				new LabeledStringItem(context.getResources().getString(R.string.action_flash_auto),Camera.Parameters.FLASH_MODE_AUTO)
		};

		String mode = PreferenceHelper.getInstance(context).getString(PREF_FLASH_MODE, Camera.Parameters.FLASH_MODE_AUTO);
		new AlertDialog.Builder(context)
		.setTitle(context.getResources().getString(R.string.action_flash))
		.setSingleChoiceItems(items, LabeledItem.indexOf(items,mode), new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String mode = items[which].getValue();
				PreferenceHelper.getInstance(context).putString(PREF_FLASH_MODE, mode);
				listener.onUpdateFlash(mode);
				dialog.dismiss();
			}
		})
		.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
		.show();
	}

	public String getFlashMode(Context context){
		return PreferenceHelper.getInstance(context).getString(PREF_FLASH_MODE, Camera.Parameters.FLASH_MODE_AUTO);
	}



	private void selectPictureSize(final Context context){
		Camera camera = listener.getCamera();
		Size[] sizes = camera.getParameters().getSupportedPictureSizes().toArray(new Size[0]);
		final SizeItem[] items=new SizeItem[sizes.length];
		Size currentsize = camera.getParameters().getPictureSize();
		int cur = -1;
		for(int i=0; i<sizes.length; ++i){
			items[i] = new SizeItem(sizes[i].width, sizes[i].height);
			if(sizes[i].equals(currentsize)){
				cur = i;
			}
		}
		new AlertDialog.Builder(context)
		.setTitle(context.getResources().getString(R.string.action_picture_size))
		.setSingleChoiceItems(items, cur, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SizeItem item = items[which];
				PreferenceHelper.getInstance(context).putString(PREF_PICTURE_SIZE, item.toString());
				Camera.Parameters params = listener.getCamera().getParameters();
				params.setPictureSize(item.getSize().x, item.getSize().y);
				listener.getCamera().setParameters(params);
				dialog.dismiss();
			}
		})
		.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
		.show();
	}

	public Point getPictureSize(Context context){
		String buf = PreferenceHelper.getInstance(context).getString(PREF_PICTURE_SIZE, null);
		if(buf!=null){
			return new SizeItem(buf).getSize();
		}else{
			Size size = getMinimumSize(listener.getCamera().getParameters().getSupportedPictureSizes().toArray(new Size[0]));
			return new Point(size.width, size.height);
		}
	}

	public Size getMinimumSize(Size[] sizes) {
		Size ret = null;
		for(Size s : sizes){
			if(ret==null || ret.height*ret.width>s.height*s.width){
				ret = s;
			}
		}
		if(ret==null)throw new IllegalArgumentException();
		return ret;
	}
	
	private static final int THUMBNAIL_RIGHT = 0;
	private static final int THUMBNAIL_LEFT = 1;
	private void selectThumbnailSide(final Context context){
		final LabeledItem<?>[] items={
				new LabeledItem<Integer>(context.getResources().getString(R.string.action_thumbnail_location_right), THUMBNAIL_RIGHT),
				new LabeledItem<Integer>(context.getResources().getString(R.string.action_thumbnail_location_left), THUMBNAIL_LEFT)
			};
		new AlertDialog.Builder(context)
		.setTitle(context.getResources().getString(R.string.action_thumbnail_location))
		.setSingleChoiceItems(items, getThumbnailSide(context)?0:1, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LabeledItem<Integer> item = (LabeledItem<Integer>)items[which];
				PreferenceHelper.getInstance(context).putBoolean(PREF_THUMBNAIL_LOCATION, item.getValue()==0);
				ThumbnailAdapter.reset();
				listener.onUpdateThumbnailSide(item.getValue());
				dialog.dismiss();
			}
		})
		.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
		.show();
	}
	
	public boolean getThumbnailSide(Context context){
		return PreferenceHelper.getInstance(context).getBoolean(PREF_THUMBNAIL_LOCATION, true);
	}
}
