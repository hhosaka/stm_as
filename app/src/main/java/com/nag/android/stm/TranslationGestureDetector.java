package com.nag.android.stm;

import android.view.MotionEvent;
import android.view.View;

public class TranslationGestureDetector {
	public interface TranslationGestureListener {
		void onTranslation(float x, float y);
		void onTranslationBegin(float x, float y);
		void onTranslationEnd(float x, float y);
	}

	private TranslationGestureListener mListener;
	private int pid;

	public TranslationGestureDetector(TranslationGestureListener listener){
		mListener = listener;
	}
 
	public boolean onTouch(View v, MotionEvent event) {
		int pointerIndex = event.getActionIndex();
		float x = event.getX(pointerIndex);
		float y = event.getY(pointerIndex);

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			pid = event.getPointerId(pointerIndex);
			mListener.onTranslationBegin(x, y);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
//			if (mPointerID2 == -1){
//				mPointerID2 = event.getPointerId(pointerIndex);
//			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (pid == event.getPointerId(pointerIndex)){
				pid = -1;
				mListener.onTranslationEnd(x, y);
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (pid != -1){
				mListener.onTranslationEnd(x, y);
			}
			pid = -1;
			break;
		case MotionEvent.ACTION_MOVE:
			if (pid >= 0){
				int ptrIndex = event.findPointerIndex(pid);
				mListener.onTranslation(event.getX(ptrIndex), event.getY(ptrIndex));
			}
			break;
		}
		return true;
	}
}
