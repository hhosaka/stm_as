package com.nag.android.stm;

import android.content.Context;

import com.nag.android.util.PreferenceHelper;

import java.util.HashMap;
import java.util.Map;

public class ProtectManager {
	private static final String PROP_PROTECTED_FILE = "protected_file";
	private static final String PROP_PROTECTED_FILE_COUNT = "protected_file_count";
	private static int MAX_PROP = 999;
	private final PreferenceHelper ph;

	ProtectManager(PreferenceHelper ph){
		this.ph = ph;
	}

	public int getCount(){
		return ph.getInt(PROP_PROTECTED_FILE_COUNT, 0);
	}

	boolean isProtected(String filename){
		int count = getCount();
		for(int i=0;count > 0; ++i){
			String buf = ph.getString(PROP_PROTECTED_FILE+i, null);
			if(buf != null){
				if(filename.equals(buf)){
					return true;
				}
				--count;
			}
		}
		return false;
	}

	boolean add(String filename){
		for(int i=0;i<MAX_PROP; ++i){
			String label = PROP_PROTECTED_FILE + i;
			if(ph.getString(label, null) == null){
				ph.putString(label, filename);
				ph.putInt(PROP_PROTECTED_FILE_COUNT, ph.getInt(PROP_PROTECTED_FILE_COUNT, 0) + 1);
				return true;
			}
		}
		return false;
	}

	boolean remove(String filename){
		for(int i=0;i<MAX_PROP; ++i){
			String label = PROP_PROTECTED_FILE + i;
			String buf = ph.getString(label, null);
			if(buf != null && filename.equals(buf)){
				ph.remove(label);
				ph.putInt(PROP_PROTECTED_FILE_COUNT, ph.getInt(PROP_PROTECTED_FILE_COUNT, 0) - 1);
				return true;
			}
		}
		return false;
	}
}
