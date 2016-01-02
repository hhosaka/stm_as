package com.nag.android.stm;

import android.hardware.Camera;

interface CaptureListener {
	void onUpdateFlash(String mode);
	Camera getCamera();
	void onUpdateThumbnailSide(int side);
}
