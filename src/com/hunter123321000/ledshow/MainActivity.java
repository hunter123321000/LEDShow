package com.hunter123321000.ledshow;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsoluteLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsoluteLayout.LayoutParams;

import com.example.android.BluetoothChat.BluetoothChatService;
import com.example.android.BluetoothChat.DeviceListActivity;

public class MainActivity extends Activity {
	MarqueeTextView mtv_msg;
	AutoResizeTextView tv_msg;
	ImageView img, ig1, ig2, ig3, ig4;
	ImageView btn_msg1, btn_msg2, btn_msg3, btn_msg4;
	BTN_Click btn_onclick;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	private ArrayAdapter<String> mConversationArrayAdapter;
	// private ListView mConversationView;
	// private EditText mOutEditText;
	private StringBuffer mOutStringBuffer;
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int CALL_OUT = 6;
	public static final int Hang_UP = 7;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	public static final byte[] MSG1 = { 0x01 }; // lower than 128
	public static final byte[] MSG2 = { 0x02 }; // lower than 128
	public static final byte[] MSG3 = { 0x03 }; // lower than 128
	public static final byte[] MSG4 = { 0x04 }; // lower than 128
	// 文字顯示
	AbsoluteLayout rl_btn;
	private Timer timer;
	private TimerTask timerTask;
	private int i_count = 0, bb;
	SoundPool sound;
	boolean b_flash = false;
	// 手勢判斷
	float upX, upY, downX, downY;
	//
	float f_max_high, f_max_width;

	float m_lastTouchX, m_lastTouchY, m_posX, m_posY, m_prevX, m_prevY,
			m_imgXB, m_imgYB, m_imgXC, m_imgYC, m_dx, m_dy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// mtv_msg = (MarqueeTextView) findViewById(R.id.mtv_msg);
		// AssetManager mgr = getAssets();
		// Typeface face = Typeface.createFromAsset(mgr, "The 2K12.ttf");
		// mtv_msg.setTypeface(face);

		tv_msg = (AutoResizeTextView) findViewById(R.id.tv_msg);
		AssetManager mgr = getAssets();
		Typeface face = Typeface.createFromAsset(mgr, "fonts/LEDBOARD.TTF");
		tv_msg.setTypeface(face);

		setView();
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
		bb = sound.load(MainActivity.this, R.raw.bb, 1);

	}

	void setView() {
		btn_msg1 = (ImageView) findViewById(R.id.btn_msg1);
		btn_msg2 = (ImageView) findViewById(R.id.btn_msg2);
		btn_msg3 = (ImageView) findViewById(R.id.btn_msg3);
		btn_msg4 = (ImageView) findViewById(R.id.btn_msg4);

		rl_btn = (AbsoluteLayout) findViewById(R.id.rl_btn);
		ig1 = new TouchImageView(MainActivity.this, null);
		ig1.setId(1);
		ig1.setBackgroundResource(R.drawable.right);
		rl_btn.addView(ig1, new LayoutParams(50, 50, 0, 0));

		ig2 = new TouchImageView(MainActivity.this, null);
		ig2.setId(2);
		ig2.setBackgroundResource(R.drawable.left);
		rl_btn.addView(ig2, new LayoutParams(50, 50, 200, 0));

		ig3 = new TouchImageView(MainActivity.this, null);
		ig3.setId(3);
		ig3.setBackgroundResource(R.drawable.goahead);
		rl_btn.addView(ig3, new LayoutParams(50, 50, 0, 200));
		ig4 = new TouchImageView(MainActivity.this, null);
		ig4.setId(4);
		ig4.setBackgroundResource(R.drawable.stop);
		rl_btn.addView(ig4, new LayoutParams(50, 50, 200, 200));

		// ig1.setOnTouchListener(new TouchListener());
		// ig2.setOnTouchListener(new TouchListener());
		ig1.setOnTouchListener(m_onTouchListener);
		ig2.setOnTouchListener(m_onTouchListener);
		ig3.setOnTouchListener(m_onTouchListener);
		ig4.setOnTouchListener(m_onTouchListener);

		img = (ImageView) findViewById(R.id.img);
		// img.setOnTouchListener(new TouchListener());
		btn_msg1.setOnTouchListener(new TouchListener());
		btn_msg2.setOnTouchListener(new TouchListener());
		btn_msg3.setOnTouchListener(new TouchListener());
		btn_msg4.setOnTouchListener(new TouchListener());

		btn_onclick = new BTN_Click();
		btn_msg1.setOnClickListener(btn_onclick);
		btn_msg2.setOnClickListener(btn_onclick);
		btn_msg3.setOnClickListener(btn_onclick);
		btn_msg4.setOnClickListener(btn_onclick);

		if (function.isPad(MainActivity.this) == true) {
			rl_btn.setVisibility(View.GONE);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv_msg
					.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			tv_msg.setLayoutParams(layoutParams);
		} else {
			tv_msg.setVisibility(View.GONE);
			img.setVisibility(View.GONE);
		}
		setBrightness(1);
	}

	class BTN_Click implements OnClickListener {
		@Override
		public void onClick(View v) {
			// mtv_msg.startScroll();
			switch (v.getId()) {
			case R.id.btn_msg1:
				tv_msg.setTextSize(60);
				tv_msg.setBackgroundColor(getResources().getColor(
						R.color.light_green));
				tv_msg.setText(getResources().getString(R.string.moving));
				mChatService.write(MSG1);
				mOutStringBuffer.setLength(0);
				break;
			case R.id.btn_msg2:
				tv_msg.setTextSize(60);
				tv_msg.setBackgroundColor(getResources().getColor(R.color.red));
				tv_msg.setText(getResources().getString(R.string.stop));
				mChatService.write(MSG2);
				mOutStringBuffer.setLength(0);
				break;
			case R.id.btn_msg3:
				tv_msg.setTextSize(60);
				tv_msg.setBackgroundColor(getResources().getColor(
						R.color.light_green));
				tv_msg.setText(getResources().getString(R.string.turn_left));
				mChatService.write(MSG3);
				mOutStringBuffer.setLength(0);
				break;
			case R.id.btn_msg4:
				tv_msg.setTextSize(60);
				tv_msg.setBackgroundColor(getResources().getColor(
						R.color.light_green));
				tv_msg.setText(getResources().getString(R.string.turn_right));
				mChatService.write(MSG4);
				mOutStringBuffer.setLength(0);
				break;
			}

		}
	}

	private void setupChat() {
		// Initialize the array adapter for the conversation thread
		mConversationArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.message);
		// mConversationView = (ListView) findViewById(R.id.in);
		// mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		// mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		// mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
				return true;
			}
			return true;
		}
	};

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);

		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);

			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.i("0.0", "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
		determinaterange();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
	}

	private void ensureDiscoverable() {

		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				Log.i("0.0", "writeMessage=" + writeMessage);
				mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				Log.i("0.0", "msg=" + msg);
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
						+ readMessage);
				switch (msg.arg1) {
				case 1:
					pauseTimer();
					if (timerTask == null) {
						i_count = 0;
						timer = new Timer();
						timerTask = new TimerTask() {
							@Override
							public void run() {
								i_count++;
								if (i_count < 3) {
									handler.sendMessage(handler.obtainMessage(
											1, i_count));
								} else {
									i_count = 0;
									handler.sendMessage(handler.obtainMessage(
											1, i_count));
								}
							}
						};
						timer.schedule(timerTask, 500, 500);
					} else {
						i_count = 0;
						handler.sendMessage(handler.obtainMessage(1, 0));
					}

					tv_msg.setBackgroundColor(getResources().getColor(
							R.color.light_green));
					tv_msg.setText(getResources().getString(R.string.moving));
					break;
				case 2:
					sound.release();// 可立即STOP 音效
					sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
					bb = sound.load(MainActivity.this, R.raw.bb, 1);
					tv_msg.setBackgroundColor(getResources().getColor(
							R.color.red));
					tv_msg.setText(getResources().getString(R.string.stop));
					handler.sendMessage(handler.obtainMessage(0, i_count));
					break;
				case 3:
					pauseTimer();
					if (timerTask == null) {
						i_count = 2;
						timer = new Timer();
						timerTask = new TimerTask() {
							@Override
							public void run() {
								i_count++;
								if (i_count < 6) {
									handler.sendMessage(handler.obtainMessage(
											1, i_count));
								} else {
									i_count = 3;
									handler.sendMessage(handler.obtainMessage(
											1, i_count));
								}
							}
						};
						timer.schedule(timerTask, 500, 500);
					} else {
						handler.sendMessage(handler.obtainMessage(1, 3));
					}

					tv_msg.setBackgroundColor(getResources().getColor(
							R.color.light_green));
					tv_msg.setText(getResources().getString(R.string.turn_left));
					break;
				case 4:
					pauseTimer();
					if (timerTask == null) {
						i_count = 5;
						timer = new Timer();
						timerTask = new TimerTask() {
							@Override
							public void run() {
								i_count++;
								if (i_count < 9) {
									handler.sendMessage(handler.obtainMessage(
											1, i_count));
								} else {
									i_count = 6;
									handler.sendMessage(handler.obtainMessage(
											1, i_count));
								}
							}
						};
						timer.schedule(timerTask, 500, 500);
					} else {
						handler.sendMessage(handler.obtainMessage(1, 6));
					}

					tv_msg.setBackgroundColor(getResources().getColor(
							R.color.light_green));
					tv_msg.setText(getResources()
							.getString(R.string.turn_right));
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				sound.release();
				finish();
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int myCount = Integer.valueOf(msg.obj.toString());
			switch (msg.what) {
			case 0:
				stopTimer();
				break;
			case 1:
				setImageViewSrc(myCount);
				switch (myCount) {
				case 0:
				case 1:
				case 2:
					if (b_flash == false) {
						b_flash = true;
						tv_msg.setText(getResources()
								.getString(R.string.moving));
					} else {
						b_flash = false;
						tv_msg.setText(getResources().getString(R.string.non));
					}
					break;
				case 3:
				case 4:
				case 5:
					if (b_flash == false) {
						b_flash = true;
						tv_msg.setText(getResources().getString(
								R.string.turn_left));
					} else {
						b_flash = false;
						tv_msg.setText(getResources().getString(R.string.non));
					}
					break;
				case 6:
				case 7:
				case 8:
					if (b_flash == false) {
						b_flash = true;
						tv_msg.setText(getResources().getString(
								R.string.turn_right));
					} else {
						b_flash = false;
						tv_msg.setText(getResources().getString(R.string.non));
					}
					break;
				}
				break;
			}
		}
	};

	private void stopTimer() {
		img.setImageResource(R.drawable.bike);
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}

		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void pauseTimer() {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}

		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void setImageViewSrc(int count) {
		int myCount = count % 10;
		switch (myCount) {
		case 0:
			img.setImageResource(R.drawable.bike);
			break;
		case 1:
			img.setImageResource(R.drawable.bike2);
			break;
		case 2:
			img.setImageResource(R.drawable.bike3);
			break;
		case 3:
			sound.play(bb, 1, 1, 0, 0, 1);
			img.setImageResource(R.drawable.r1);
			break;
		case 4:
			img.setImageResource(R.drawable.r2);
			break;
		case 5:
			img.setImageResource(R.drawable.r3);
			break;
		case 6:
			sound.play(bb, 1, 1, 0, 0, 1);
			img.setImageResource(R.drawable.bike);
			break;
		case 7:
			img.setImageResource(R.drawable.bike2);
			break;
		case 8:
			img.setImageResource(R.drawable.bike3);
			break;
		case 9:
			break;
		}
	}

	public boolean onTouchEvent(MotionEvent event) {

		float X = event.getX(); // 觸控的 X 軸位置
		float Y = event.getY(); // 觸控的 Y 軸位置

		switch (event.getAction()) { // 判斷觸控的動作

		case MotionEvent.ACTION_DOWN: // 按下
			downX = event.getX();
			downY = event.getY();

			return true;
		case MotionEvent.ACTION_MOVE: // 拖曳

			return true;
		case MotionEvent.ACTION_UP: // 放開
			Log.d("onTouchEvent-ACTION_UP", "UP");
			upX = event.getX();
			upY = event.getY();
			float x = Math.abs(upX - downX);
			float y = Math.abs(upY - downY);
			double z = Math.sqrt(x * x + y * y);
			int jiaodu = Math.round((float) (Math.asin(y / z) / Math.PI * 180));// 角度

			if (upY < downY && jiaodu > 45) {// 上
				btn_msg1.performClick();
				Log.d("onTouchEvent-ACTION_UP", "角度:" + jiaodu + ", 動作:上");
			} else if (upY > downY && jiaodu > 45) {// 下
				btn_msg2.performClick();
				Log.d("onTouchEvent-ACTION_UP", "角度:" + jiaodu + ", 動作:下");
			} else if (upX < downX && jiaodu <= 45) {// 左
				Log.d("onTouchEvent-ACTION_UP", "角度:" + jiaodu + ", 動作:左");
				btn_msg3.performClick();
				// // 原方向不是向右時，方向轉右
				// if (mDirection != EAST) {
				// mNextDirection = WEST;
				// }
			} else if (upX > downX && jiaodu <= 45) {// 右
				Log.d("onTouchEvent-ACTION_UP", "角度:" + jiaodu + ", 動作:右");
				btn_msg4.performClick();
				// 原方向不是向左時，方向轉右
				// if (mDirection ! = WEST) {
				// mNextDirection = EAST;
				// }
			}
			return true;
		}

		return super.onTouchEvent(event);
	}

	public void setBrightness(float f) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = f;
		getWindow().setAttributes(lp);
	}

	private final class TouchListener implements OnTouchListener {

		private PointF startPoint = new PointF();
		private Matrix matrix = new Matrix();
		private Matrix currentMatrix = new Matrix();
		private int mode = 0;
		private static final int DRAG = 1;
		private static final int ZOOM = 2;
		private float startDis;
		private PointF midPoint;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// RelativeLayout.LayoutParams param = new
			// RelativeLayout.LayoutParams(
			// LayoutParams.FILL_PARENT,
			// LayoutParams.FILL_PARENT);
			// btn_msg1.setLayoutParams(param);
			// btn_msg2.setLayoutParams(param);
			// btn_msg3.setLayoutParams(param);
			// btn_msg4.setLayoutParams(param);

			ImageView img_show = new ImageView(MainActivity.this);
			Log.i("0.0", "id=" + v.getId() + "\r\nig1=" + ig1);
			switch (v.getId()) {
			case 1:
				img_show = ig1;
				break;
			case 2:
				img_show = ig2;
				break;
			case R.id.btn_msg1:
				img_show = btn_msg1;
				break;
			case R.id.btn_msg2:
				img_show = btn_msg2;
				break;
			case R.id.btn_msg3:
				img_show = btn_msg3;
				break;
			case R.id.btn_msg4:
				img_show = btn_msg4;
				break;
			}
			Log.i("0.0", "Action=" + event.getAction() + "   img_show="
					+ img_show);
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mode = DRAG;
				currentMatrix.set(img_show.getImageMatrix());
				startPoint.set(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					float dx = event.getX() - startPoint.x;
					float dy = event.getY() - startPoint.y;

					matrix.set(currentMatrix);
					matrix.postTranslate(dx, dy);
				} else if (mode == ZOOM) {
					float endDis = distance(event);
					if (endDis > 10f) {
						float scale = endDis / startDis;
						matrix.postScale(scale, scale, midPoint.x, midPoint.y);
					}

				}

				break;
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode = 0;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = ZOOM;
				startDis = distance(event);
				if (startDis > 10f) {
					midPoint = mid(event);
					currentMatrix.set(img_show.getImageMatrix());
				}
				break;
			}

			float[] f = new float[9];
			matrix.getValues(f);
			for (int i = 0; i < 9; i++) {
				Log.i("0.0", "f[" + i + "]=" + f[i]);
			}
			if (f[0] < 0.1f)
				f[0] = 0.1f;
			if (f[4] < 0.1f)
				f[4] = 0.1f;
			if (f[0] > 1.1f)
				f[0] = 1.1f;
			if (f[4] > 1.1f)
				f[4] = 1.1f;
			if (f[2] < 0.0f)
				f[2] = 0.0f;
			if (f[5] < 0.0f)
				f[5] = 0.0f;
			if (f[2] > f_max_width)
				f[2] = f_max_width;
			if (f[5] > f_max_high)
				f[5] = f_max_high;

			matrix.setValues(f);
			img_show.setImageMatrix(matrix);
			return true;
		}

	}

	public static float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	private static PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}

	private void determinaterange() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;
		f_max_high = height - tv_msg.getHeight();
		f_max_width = width;
		Log.i("0.0", "height=" + height + "\r\nwidth=" + width
				+ "\r\ntv_msg.getHeight()=" + tv_msg.getHeight()
				+ "\r\ntv_msg.getWidth()=" + tv_msg.getWidth()
				+ "\r\nf_max_high=" + f_max_high + "\r\nf_max_width="
				+ f_max_width);
	}

	OnTouchListener m_onTouchListener = new OnTouchListener() {
		private PointF startPoint = new PointF();
		private int mode = 0;
		private static final int DRAG = 1;
		private static final int ZOOM = 2;
		private float startDis, dx, dy, scale;
		private PointF midPoint;

		@Override
		public boolean onTouch(View p_v, MotionEvent p_event) {
			switch (p_event.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN: {
				m_lastTouchX = p_event.getX();
				m_lastTouchY = p_event.getY();

				mode = DRAG;
				startPoint.set(p_event.getX(), p_event.getY());

				break;
			}
			case MotionEvent.ACTION_UP: {
				mode = 0;
				break;
			}

			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					dx = p_event.getX() - startPoint.x;
					dy = p_event.getY() - startPoint.y;

					m_dx = p_event.getX() - m_lastTouchX;
					m_dy = p_event.getY() - m_lastTouchY;

					m_posX = m_prevX + m_dx;
					m_posY = m_prevY + m_dy;

					if (m_posX > 0 && m_posY > 0
							&& (m_posX + p_v.getWidth()) < rl_btn.getWidth()
							&& (m_posY + p_v.getHeight()) < rl_btn.getHeight()) {
						p_v.setLayoutParams(new AbsoluteLayout.LayoutParams(p_v
								.getMeasuredWidth(), p_v.getMeasuredHeight(),
								(int) m_posX, (int) m_posY));

						m_prevX = m_posX;
						m_prevY = m_posY;

					}

				} else if (mode == ZOOM) {
					Log.i("0.0", "ACTION_MOVE > ZOOM");
					float endDis = distance(p_event);
					if (endDis > 10f) {
						scale = endDis / startDis;
					}

				}

				break;

			case MotionEvent.ACTION_POINTER_UP:
				Log.i("0.0", "ACTION_POINTER_UP > scale=" + scale);
				mode = 0;
				if ((rl_btn.getWidth() < (int) (p_v.getMeasuredWidth() * scale))
						|| (rl_btn.getHeight() < (int) (p_v.getMeasuredHeight() * scale))) {
					scale = 1;
					p_v.setLayoutParams(new AbsoluteLayout.LayoutParams(
							(int) (p_v.getMeasuredWidth() * scale), (int) (p_v
									.getMeasuredHeight() * scale),
							(int) p_event.getX(), (int) p_event.getY()));
				} else if ((20 > (int) (p_v.getMeasuredWidth() * scale))
						|| (20 > (int) (p_v.getMeasuredHeight() * scale))) {
					scale = 1;
					p_v.setLayoutParams(new AbsoluteLayout.LayoutParams(
							(int) (p_v.getMeasuredWidth() * scale), (int) (p_v
									.getMeasuredHeight() * scale),
							(int) p_event.getX(), (int) p_event.getY()));
				} else {
					p_v.setLayoutParams(new AbsoluteLayout.LayoutParams(
							(int) (p_v.getMeasuredWidth() * scale), (int) (p_v
									.getMeasuredHeight() * scale),
							(int) midPoint.x, (int) midPoint.y));
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				Log.i("0.0", "ACTION_POINTER_DOWN");
				mode = ZOOM;
				startDis = distance(p_event);
				if (startDis > 10f) {
					midPoint = mid(p_event);

				}
				break;

			}

			return true;
		}
	};

}
