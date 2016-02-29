package com.nag.android.stm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import android.widget.EditText;
import android.widget.Toast;

import com.nag.android.util.LabeledItem;
import com.nag.android.util.LabeledStringItem;
import com.nag.android.util.PreferenceHelper;

public class MenuHandler {

	private static final String PREF_FLASH_MODE = "flash_mode";
	private static final String PREF_PICTURE_SIZE = "picture_size";
	private static final String PREF_THUMBNAIL_LOCATION = "thumbnail_location";
	private final PreferenceHelper ph;
//	private final ProtectManager protectmanager;
	private final StorageCapacityManager scm;
	private CaptureListener capture_listener;
	private String filename = null;

	MenuHandler(Context context){
		assert(capture_listener !=null);
		ph = PreferenceHelper.getInstance(context);
		scm = new StorageCapacityManager(ph);

	}

	void setCaptureListener(CaptureListener listener, StorageEventListener slistener){
		capture_listener = listener;
		scm.setListener(slistener);
	}

	public void setFilename(String filename){
		this.filename = filename;
	}

	public void initialize(Context context, Menu menu){
		if(filename != null) {
			menu.findItem(R.id.action_protected).setVisible(true);
			setProtectStatus(menu.findItem(R.id.action_protected), scm.isProtected(filename));
			menu.findItem(R.id.action_export).setVisible(true);
			menu.findItem(R.id.action_send_mail).setVisible(true);
			menu.findItem(R.id.action_flash_setting).setVisible(false);
			menu.findItem(R.id.action_picture_size).setVisible(false);
			menu.findItem(R.id.action_thumbnail_location).setVisible(false);
		}else{
			menu.findItem(R.id.action_protected).setVisible(false);
			menu.findItem(R.id.action_export).setVisible(false);
			menu.findItem(R.id.action_send_mail).setVisible(false);
			menu.findItem(R.id.action_flash_setting).setVisible(true);
			menu.findItem(R.id.action_picture_size).setVisible(true);
			menu.findItem(R.id.action_thumbnail_location).setVisible(true);
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
			Toast.makeText(context, context.getString(R.string.message_exported) + filename, Toast.LENGTH_LONG).show();
		}catch(IOException e) {
			Toast.makeText(context, context.getString(R.string.error_fail_to_export_file), Toast.LENGTH_LONG).show();
		}
		registAndroidDB(context, imgPath);
		return imgPath;
	}

	private void sendMail(Context context, String filename){

		String imgPath = export(context, filename);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.string_email_subject));
		intent.putExtra(Intent.EXTRA_TEXT, filename);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imgPath)));
		context.startActivity(intent);
	}

	private void setProtectStatus(MenuItem item, boolean is_protected){
		item.setEnabled(is_protected || scm.isProtectAvailable());
		item.setChecked(is_protected);
	}

	public boolean onOptionsItemSelected(Context context, Camera camera, MenuItem menuitem){
		switch (menuitem.getItemId()){
		case R.id.action_protected:
			boolean is_protected = !menuitem.isChecked();
			scm.setProtected(filename, is_protected );
			setProtectStatus(menuitem, is_protected);
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
			selectPictureSize(context, camera);
			return true;
		case R.id.action_thumbnail_location:
			selectThumbnailSide(context);
			return true;
		case R.id.action_help:
			EditText e = new EditText(context);
			e.setText(Logger.read(context));
			e.setFocusable(false);
			new AlertDialog.Builder(context)
					.setTitle("debug")
					.setView(e)
					.show();
//			context.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(context.getResources().getString(R.string.string_url_help))));
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

		String mode = ph.getString(PREF_FLASH_MODE, Camera.Parameters.FLASH_MODE_AUTO);
		new AlertDialog.Builder(context)
		.setTitle(context.getResources().getString(R.string.action_flash))
		.setSingleChoiceItems(items, LabeledItem.indexOf(items,mode), new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String mode = items[which].getValue();
				ph.putString(PREF_FLASH_MODE, mode);
				capture_listener.onUpdateFlash(mode);
				dialog.dismiss();
			}
		})
		.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
		.show();
	}

	public String getFlashMode(Context context){
		return ph.getString(PREF_FLASH_MODE, Camera.Parameters.FLASH_MODE_AUTO);
	}

	private void selectPictureSize(final Context context, final Camera camera){
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
				Camera.Parameters params = camera.getParameters();
				params.setPictureSize(item.getSize().x, item.getSize().y);
				camera.setParameters(params);
				dialog.dismiss();
			}
		})
		.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
		.show();
	}

	public Point getPictureSize(Context context, Camera camera){
		String buf = PreferenceHelper.getInstance(context).getString(PREF_PICTURE_SIZE, null);
		if(buf!=null){
			return new SizeItem(buf).getSize();
		}else{
			Size size = getMinimumSize(camera.getParameters().getSupportedPictureSizes().toArray(new Size[0]));
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
		.setSingleChoiceItems(items, getThumbnailSide()?0:1, new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LabeledItem<Integer> item = (LabeledItem<Integer>)items[which];
				PreferenceHelper.getInstance(context).putBoolean(PREF_THUMBNAIL_LOCATION, item.getValue()==0);
				ThumbnailAdapter.reset();
				capture_listener.onUpdateThumbnailSide(item.getValue());
				dialog.dismiss();
			}
		})
		.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
		.show();
	}
	
	public boolean getThumbnailSide(){
		return ph.getBoolean(PREF_THUMBNAIL_LOCATION, true);
	}
}
