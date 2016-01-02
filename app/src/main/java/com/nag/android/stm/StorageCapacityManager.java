package com.nag.android.stm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.nag.android.util.LabeledIntItem;
import com.nag.android.util.LabeledItem;
import com.nag.android.util.PreferenceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class StorageCapacityManager {
	private static final String PREF_STORAGE_CAPACITY = "storage_capacity";
	private final PreferenceHelper ph;
	private final ProtectManager pm;
	private StorageEventListener listener;

	StorageCapacityManager(PreferenceHelper ph){
		this.ph = ph;
		this.pm = new ProtectManager(ph);
	}

	public void setListener(StorageEventListener listener){
		this.listener = listener;
	}

	private void set(int capacity) {
		ph.putInt(PREF_STORAGE_CAPACITY, capacity);
	}
	private int get(){
		return ph.getInt(PREF_STORAGE_CAPACITY, 10);
	}

	void setProtected(String filename, boolean protect){
		if(protect){
			pm.add(filename);
		}else{
			pm.remove(filename);
		}
	}

	boolean isProtected(String filename){
		return pm.isProtected(filename);
	}

	boolean isProtectAvailable(){
		return pm.getCount() < get() - 1;
	}

	public void select(final Context context){
		int protectnum = pm.getCount();

		List<LabeledIntItem> temp = new ArrayList<LabeledIntItem>();
		if(protectnum < 5){temp.add(new LabeledIntItem(context.getResources().getString(R.string.action_storage_capacity_5),5));}
		if(protectnum < 10){temp.add(new LabeledIntItem(context.getResources().getString(R.string.action_storage_capacity_10),10));}
		if(protectnum < 20){temp.add(new LabeledIntItem(context.getResources().getString(R.string.action_storage_capacity_20),20));}
		final LabeledIntItem[] storagecapacityitems = temp.toArray(new LabeledIntItem[0]);

		new AlertDialog.Builder(context)
				.setTitle(context.getResources().getString(R.string.action_storage_capacity))
				.setSingleChoiceItems(storagecapacityitems, LabeledItem.indexOf(storagecapacityitems, Integer.valueOf(get())), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int capacity = storagecapacityitems[which].getValue();
						set(capacity);
						if(shrink(context, capacity)) {
							if (listener != null) {
								listener.onResetCapacity(capacity);
							}
						}
						dialog.dismiss();
					}

					private boolean shrink(Context context, int capacity){
						String[] files = context.fileList();
						if(files.length > capacity){
							Arrays.sort(files, new Comparator<String>() {
								@Override
								public int compare(String lhs, String rhs) {
									return lhs.compareTo(rhs);
								}
							});
							int cnt = files.length - capacity;
							for(int i=0 ; cnt > 0 && i < files.length; ++i) {
								if(!pm.isProtected(files[i])){
									context.deleteFile(files[i]);
									pm.remove(files[i]);
									--cnt;
								}
							}
							return true;
						}
						return false;
					}
				})
				.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
				.show();
	}
}
