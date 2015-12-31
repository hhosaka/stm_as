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
	interface Listener{
		void onResetCapacity(int capacity);
	}
	private static final String PREF_STORAGE_CAPACITY = "storage_capacity";
	private final PreferenceHelper ph;
	private final ProtectManager pm;
	private final Listener listener;

	StorageCapacityManager(PreferenceHelper ph, ProtectManager pm, Listener listener){
		this.ph = ph;
		this.pm = pm;
		this.listener = listener;
	}

	public void set(Context context, int capacity) {
		ph.putInt(PREF_STORAGE_CAPACITY, capacity);
	}
	public int get(Context context){
		return ph.getInt(PREF_STORAGE_CAPACITY, 10);
	}
	public void select(final Context context){
		int protectnum = pm.getCount();

		List<LabeledIntItem> temp = new ArrayList<LabeledIntItem>();
		if(protectnum < 5){temp.add(new LabeledIntItem(context.getResources().getString(R.string.action_storage_capacity_5),5));}
		if(protectnum < 10){temp.add(new LabeledIntItem(context.getResources().getString(R.string.action_storage_capacity_10),10));}
		if(protectnum < 20){temp.add(new LabeledIntItem(context.getResources().getString(R.string.action_storage_capacity_20),20));}
		final LabeledIntItem[] storagecapacityitems = temp.toArray(new LabeledIntItem[0]);

		int size = get(context);

		new AlertDialog.Builder(context)
				.setTitle(context.getResources().getString(R.string.action_storage_capacity))
				.setSingleChoiceItems(storagecapacityitems, LabeledItem.indexOf(storagecapacityitems, Integer.valueOf(size)), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int capacity = storagecapacityitems[which].getValue();
						if(pm.getCount() <= capacity) {
							set(context, capacity);
							shrink(context, capacity);
							if (listener != null) {
								listener.onResetCapacity(capacity);
							}
						}
						dialog.dismiss();
					}

					private void shrink(Context context, int capacity){
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
						}
					}
				})
				.setNegativeButton(context.getResources().getString(R.string.string_cancel), null)
				.show();
	}

}
