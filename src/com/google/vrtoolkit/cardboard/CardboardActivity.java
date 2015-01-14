package com.google.vrtoolkit.cardboard;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.view.*;
import com.google.vrtoolkit.cardboard.sensors.MagnetSensor;
import com.google.vrtoolkit.cardboard.sensors.NfcSensor;

// Referenced classes of package com.google.vrtoolkit.cardboard:
//			CardboardDeviceParams, CardboardView

public class CardboardActivity extends Activity
		implements
		com.google.vrtoolkit.cardboard.sensors.MagnetSensor.OnCardboardTriggerListener,
		com.google.vrtoolkit.cardboard.sensors.NfcSensor.OnCardboardNfcListener {
	
	public static class VolumeKeys {

		public static final int NOT_DISABLED = 0;
		public static final int DISABLED = 1;
		public static final int DISABLED_WHILE_IN_CARDBOARD = 2;

	}
	
	private static final boolean NFC_DISABLED = true;
	private static final boolean MAGNET_DISABLED = true;
	
	private static final int NAVIGATION_BAR_TIMEOUT_MS = 2000;
	private CardboardView mCardboardView;
	private MagnetSensor mMagnetSensor;
	private NfcSensor mNfcSensor;
	private int mVolumeKeysMode;

	public void setCardboardView(CardboardView cardboardView) {
		mCardboardView = cardboardView;
		if (cardboardView != null && mNfcSensor != null) {
			CardboardDeviceParams cardboardDeviceParams = mNfcSensor
					.getCardboardDeviceParams();
			if (cardboardDeviceParams == null)
				cardboardDeviceParams = new CardboardDeviceParams();
			cardboardView.updateCardboardDeviceParams(cardboardDeviceParams);
		}
	}

	public CardboardView getCardboardView() {
		return mCardboardView;
	}

	public void setVolumeKeysMode(int mode) {
		mVolumeKeysMode = mode;
	}

	public int getVolumeKeysMode() {
		return mVolumeKeysMode;
	}

	public boolean areVolumeKeysDisabled() {
		switch (mVolumeKeysMode) {
		case VolumeKeys.NOT_DISABLED: // '\0'
			return false;
			
		case VolumeKeys.DISABLED: // '\001'
			return true;
			
		case VolumeKeys.DISABLED_WHILE_IN_CARDBOARD: // '\002'
			return isDeviceInCardboard();
		
		}
		throw new IllegalStateException("Invalid volume keys mode "
				+ mVolumeKeysMode);
	}

	public boolean isDeviceInCardboard() {
		if(NFC_DISABLED) {
			return false;
		} else {
			return mNfcSensor.isDeviceInCardboard();
		}
	}

	public void onInsertedIntoCardboard(CardboardDeviceParams deviceParams) {
		if (mCardboardView != null)
			mCardboardView.updateCardboardDeviceParams(deviceParams);
	}

	public void onRemovedFromCardboard() {
	}

	public void onCardboardTrigger() {
	}

	protected void onNfcIntent(Intent intent) {
		if(!NFC_DISABLED) 
			mNfcSensor.onNfcIntent(intent);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if(!MAGNET_DISABLED) {
			mMagnetSensor = new MagnetSensor(this);
			mMagnetSensor.setOnCardboardTriggerListener(this);
		}
		if(!NFC_DISABLED) {
			mNfcSensor = NfcSensor.getInstance(this);
			mNfcSensor.addOnCardboardNfcListener(this);
		}
		onNfcIntent(getIntent());
		setVolumeKeysMode(VolumeKeys.DISABLED_WHILE_IN_CARDBOARD);
		if (android.os.Build.VERSION.SDK_INT < 19) {
			final Handler handler = new Handler();
			getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
					new android.view.View.OnSystemUiVisibilityChangeListener() {

						public void onSystemUiVisibilityChange(int visibility) {
							if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)
								handler.postDelayed(new Runnable() {

									public void run() {
										setFullscreenMode();
									}

								}, 2000L);
						}

					});
		}
	}

	protected void onResume() {
		super.onResume();
		if (mCardboardView != null)
			mCardboardView.onResume();
		if(mMagnetSensor != null)
			mMagnetSensor.start();
		if(mNfcSensor != null)
			mNfcSensor.onResume(this);
	}

	protected void onPause() {
		super.onPause();
		if (mCardboardView != null)
			mCardboardView.onPause();
		if(mMagnetSensor != null)
			mMagnetSensor.stop();
		if(mNfcSensor != null)
			mNfcSensor.onPause(this);
	}

	protected void onDestroy() {
		if(mNfcSensor != null)
			mNfcSensor.removeOnCardboardNfcListener(this);
		super.onDestroy();
	}

	public void setContentView(View view) {
		if (view instanceof CardboardView)
			setCardboardView((CardboardView) view);
		super.setContentView(view);
	}

	public void setContentView(View view,
			android.view.ViewGroup.LayoutParams params) {
		if (view instanceof CardboardView)
			setCardboardView((CardboardView) view);
		super.setContentView(view, params);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode ==  KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) && areVolumeKeysDisabled())
			return true;
		else
			return super.onKeyDown(keyCode, event);
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) && areVolumeKeysDisabled())
			return true;
		else
			return super.onKeyUp(keyCode, event);
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
			setFullscreenMode();
	}

	private void setFullscreenMode() {
		getWindow().getDecorView().setSystemUiVisibility(5894);
	}

}
