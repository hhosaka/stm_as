package com.nag.android.stm;

import com.nag.android.stm.CaptureFragment;
import com.nag.android.stm.R;
import com.nag.android.util.PreferenceHelper;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity {

	private PreferenceHelper ph;
	private ProtectManager pm;
	private StorageCapacityManager scm;
	private MenuHandler menuhandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ph = PreferenceHelper.getInstance(this);
		pm = new ProtectManager(ph);
		scm = new StorageCapacityManager(ph, pm);
		menuhandler = new MenuHandler(this);

		FragmentManager manager = getFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		t.add(R.id.fragmentMain, new CaptureFragment());
		t.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.review, menu);
		menuhandler.initialize(this, menu);
		return true;
	}

	MenuHandler getMenuHandler(){
		return menuhandler;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuhandler.onOptionsItemSelected(this, item);
	}

	@Override
	public void openOptionsMenu() {
		super.openOptionsMenu();
	}

	public void onCreateOptionsMenu()
	{

	}
}
