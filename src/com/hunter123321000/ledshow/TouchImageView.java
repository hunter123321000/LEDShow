package com.hunter123321000.ledshow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

@SuppressLint("ViewConstructor")
public class TouchImageView extends ImageView {

	private static final String TAG = "Touch";
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	Context context;
	View parentView;

	int frameWidth = 0;
	int frameHeight = 0;
	int bitmapWidth = 0;
	int bitmapHeight = 0;

	public int maxWidth = 0; // 放大後最大可以寬到多少
	public int minWidth = 0; // 縮小後最小可以小到多少
	float minScale = 0;

	public TouchImageView(Context context, final View parentView) {
		super(context);
		// TODO 自動產生的建構子 Stub
		super.setClickable(true);
		this.context = context;
		this.parentView = parentView;

		this.setScaleType(ScaleType.MATRIX);
		this.setOnTouchListener(imgOnTouchListener);
	}

	OnTouchListener imgOnTouchListener = new OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			// Handle touch events here...
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				// Log.d(TAG, "TouchImage -- mode=DRAG");
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				// Log.d(TAG, "TouchImage -- oldDist=" + oldDist);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
					Log.d(TAG, "TouchImage -- mode=ZOOM");
				}
				break;
			case MotionEvent.ACTION_UP:
				// 做最後確認
				getRebuildMatrix();
				mode = NONE;
				Log.d(TAG, "TouchImage -- mode = NONE");
				break;
			case MotionEvent.ACTION_POINTER_UP:

				mode = NONE;
				Log.d(TAG, "TouchImage -- mode = NONE");
				break;
			case MotionEvent.ACTION_MOVE:
				Matrix _m = new Matrix();
				if (mode == DRAG) { // && getMatrixScale(matrix) > maxScale
					// ...
					float xx = event.getX() - start.x;
					float yy = event.getY() - start.y;
					_m.set(savedMatrix);
					_m.postTranslate(xx, yy);
					matrix.set(_m);
					setImageMatrix(matrix);

				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					// Log.d(TAG, "TouchImage -- newDist=" + newDist);
					if (newDist > 10f) {
						float scale = newDist / oldDist;

						_m.set(savedMatrix);
						_m.postScale(scale, scale, mid.x, mid.y);

						if (checkScale(_m, false)) {
							matrix.set(_m);
							setImageMatrix(matrix);
						}
					}
				}

				break;
			}
			return true;
		}

	};

	public void setImage(Bitmap bm, int displayWidth, int displayHeight) {
		super.setImageBitmap(bm);

		// Fit to screen.
		frameWidth = displayWidth;
		frameHeight = displayHeight;

		bitmapWidth = bm.getWidth();
		bitmapHeight = bm.getHeight();

		savedMatrix.set(matrix);
		matrix.set(savedMatrix);
		float scale = setImageNormalScale(bm.getWidth(), bm.getHeight());
		minScale = scale;
		// Center the image
		setCenterImage();

	}

	private float setImageNormalScale(int imageWidth, int imageHeight) {

		float scale;
		if ((frameHeight / imageHeight) >= (frameWidth / imageWidth)) {
			scale = (float) frameWidth / (float) imageWidth;
		} else {
			scale = (float) frameHeight / (float) imageHeight;
		}
		matrix.postScale(scale, scale, mid.x, mid.y);
		setImageMatrix(matrix);

		return scale;
	}

	private void setCenterImage() {
		// Center the image
		float redundantYSpace = (float) frameHeight
				- (minScale * (float) bitmapHeight);
		float redundantXSpace = (float) frameWidth
				- (minScale * (float) bitmapWidth);

		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;

		matrix.postTranslate(redundantXSpace, redundantYSpace);
		setImageMatrix(matrix);
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	// -----------------------------------------------------------
	private void getRebuildMatrix() {
		// 確認大小
		boolean scaleProblem = checkScale(matrix, true);
		if (!scaleProblem) {
			// 改變大小
			// matrix.postScale(minScale, minScale, mid.x, mid.y);
			matrix.setScale(minScale, minScale);
			setCenterImage();
		} else {
			// 確認邊界
			float[] check = checkBorder(matrix);
			// x1,x2,y1,y2
			Log.v(TAG, "確認誰越界 -- " + check[0] + "," + check[1] + "," + check[2]
					+ "," + check[3]);
			if (check[0] != 0f) {
				matrix.postTranslate(check[0], 0);
				setImageMatrix(matrix);
			}
			if (check[1] != 0f) {
				matrix.postTranslate(check[1], 0);
				setImageMatrix(matrix);
			}
			if (check[2] != 0f) {
				matrix.postTranslate(0, check[2]);
				setImageMatrix(matrix);
			}
			if (check[3] != 0f) {
				matrix.postTranslate(0, check[3]);
				setImageMatrix(matrix);
			}
		}

	}

	private boolean checkScale(Matrix _m, boolean smallcheck) {
		float[] f = new float[9];
		_m.getValues(f);
		// 圖片頂點四座標
		float x1 = f[0] * 0 + f[1] * 0 + f[2];
		float y1 = f[3] * 0 + f[4] * 0 + f[5];
		float x2 = f[0] * bitmapWidth + f[1] * 0 + f[2];
		float y2 = f[3] * bitmapWidth + f[4] * 0 + f[5];
		// 圖片現在寬度
		double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		float maxWidth = frameWidth * 3;
		// Log.w(TAG," -- -- -- 比較小? " + frameWidth + " 或比較大?" + maxWidth);
		if (smallcheck) {
			if (width < frameWidth)
				return false;
		}
		if (width > maxWidth)
			return false;

		return true;
	}

	private float[] checkBorder(Matrix _m) {
		float[] check = { 0f, 0f, 0f, 0f };
		float[] f = new float[9];
		_m.getValues(f);

		// 圖片頂點四座標
		float x1 = f[0] * 0 + f[1] * 0 + f[2];
		float y1 = f[3] * 0 + f[4] * 0 + f[5];
		float x4 = f[0] * bitmapWidth + f[1] * bitmapHeight + f[2];
		float y4 = f[3] * bitmapWidth + f[4] * bitmapHeight + f[5];

		// 出界判斷
		Log.i(TAG, "出界判斷 -- frameWidth : " + frameWidth + "  frameHeight  : "
				+ frameHeight);
		Log.v(TAG, "座標判斷2 -- (" + x1 + ", " + y1 + ") , (" + x4 + ", " + y4
				+ ")");

		if (x1 > 0)
			check[0] = -x1;
		if (x4 < frameWidth)
			check[1] = frameWidth - x4;

		if (y1 > 0)
			check[2] = -y1;
		Log.w(TAG, "-- " + y4 + " vs " + frameHeight + " ( " + this.getHeight()
				+ " )");
		if (y4 < frameHeight)
			check[3] = frameHeight - y4;

		return check;
	}

}