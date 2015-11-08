package com.nag.android.stm;

import com.nag.android.stm.CaptureFragment;
import com.nag.android.stm.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentManager manager = getFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		t.add(R.id.fragmentMain, new CaptureFragment());
		t.commit();
	}
}
