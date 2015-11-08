//package com.nag.android.stm_free;
//
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.nag.android.stm.CaptureFragment;
//
//import android.app.Activity;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
//import android.os.Bundle;
//import android.view.Gravity;
//import android.widget.FrameLayout;
//
//
//public class MainActivity extends Activity {
//
//	private AdView adView;
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		adView = new AdView(this);
//		adView.setAdUnitId(this.getResources().getString(R.string.admob_id));
//		adView.setAdSize(AdSize.BANNER);
//		FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//		adParams.gravity = (Gravity.TOP|Gravity.CENTER);
//		addContentView(adView, adParams);
//
//		adView.loadAd(new AdRequest.Builder().build());
//		FragmentManager manager = getFragmentManager();
//		FragmentTransaction t = manager.beginTransaction();
//		t.add(R.id.fragmentMain, new CaptureFragment());
//		t.commit();
//	}
//}
