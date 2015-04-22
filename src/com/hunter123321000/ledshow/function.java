package com.hunter123321000.ledshow;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class function {

	public static boolean isPad(Context cnt) {
		WindowManager wm = (WindowManager) cnt
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		// �e��
		float screenWidth = display.getWidth();
		// ����
		float screenHeight = display.getHeight();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
		// �ؤo
		double screenInches = Math.sqrt(x + y);
		// �j��6�T�h�����O
		if (screenInches >= 6.0) {
			return false;
		}
		return false;
	}


}
