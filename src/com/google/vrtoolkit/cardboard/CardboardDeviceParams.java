package com.google.vrtoolkit.cardboard;

import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;
import java.util.List;

// Referenced classes of package com.google.vrtoolkit.cardboard:
//			Distortion

public class CardboardDeviceParams {

	private static final String TAG = "CardboardDeviceParams";
	private static final String DEFAULT_VENDOR = "com.google";
	private static final String DEFAULT_MODEL = "cardboard";
	private static final String DEFAULT_VERSION = "1.0";
	private static final float DEFAULT_INTERPUPILLARY_DISTANCE = 0.06F;
	private static final float DEFAULT_VERTICAL_DISTANCE_TO_LENS_CENTER = 0.035F;
	private static final float DEFAULT_LENS_DIAMETER = 0.025F;
	private static final float DEFAULT_SCREEN_TO_LENS_DISTANCE = 0.037F;
	private static final float DEFAULT_EYE_TO_LENS_DISTANCE = 0.011F;
	private static final float DEFAULT_VISIBLE_VIEWPORT_MAX_SIZE = 0.06F;
	private static final float DEFAULT_FOV_Y = 65F;
	private NdefMessage mNfcTagContents;
	private String mVendor;
	private String mModel;
	private String mVersion;
	private float mInterpupillaryDistance;
	private float mVerticalDistanceToLensCenter;
	private float mLensDiameter;
	private float mScreenToLensDistance;
	private float mEyeToLensDistance;
	private float mVisibleViewportSize;
	private float mFovY;
	private Distortion mDistortion;

	public CardboardDeviceParams() {
		mVendor = DEFAULT_VENDOR;
		mModel = DEFAULT_MODEL;
		mVersion = DEFAULT_VERSION;
		mInterpupillaryDistance = DEFAULT_INTERPUPILLARY_DISTANCE;
		mVerticalDistanceToLensCenter = DEFAULT_VERTICAL_DISTANCE_TO_LENS_CENTER;
		mLensDiameter = DEFAULT_LENS_DIAMETER;
		mScreenToLensDistance = DEFAULT_SCREEN_TO_LENS_DISTANCE;
		mEyeToLensDistance = DEFAULT_EYE_TO_LENS_DISTANCE;
		mVisibleViewportSize = DEFAULT_VISIBLE_VIEWPORT_MAX_SIZE;
		mFovY = DEFAULT_FOV_Y;
		mDistortion = new Distortion();
	}

	public CardboardDeviceParams(CardboardDeviceParams params) {
		mNfcTagContents = params.mNfcTagContents;
		mVendor = params.mVendor;
		mModel = params.mModel;
		mVersion = params.mVersion;
		mInterpupillaryDistance = params.mInterpupillaryDistance;
		mVerticalDistanceToLensCenter = params.mVerticalDistanceToLensCenter;
		mLensDiameter = params.mLensDiameter;
		mScreenToLensDistance = params.mScreenToLensDistance;
		mEyeToLensDistance = params.mEyeToLensDistance;
		mVisibleViewportSize = params.mVisibleViewportSize;
		mFovY = params.mFovY;
		mDistortion = new Distortion(params.mDistortion);
	}

	public static CardboardDeviceParams createFromNfcContents(
			NdefMessage tagContents) {
		if (tagContents == null) {
			Log.w("CardboardDeviceParams",
					"Could not get contents from NFC tag.");
			return null;
		}
		CardboardDeviceParams deviceParams = new CardboardDeviceParams();
		NdefRecord arr$[] = tagContents.getRecords();
		int len$ = arr$.length;
		int i$ = 0;
		do {
			if (i$ >= len$)
				break;
			NdefRecord record = arr$[i$];
			if (deviceParams.parseNfcUri(record))
				break;
			i$++;
		} while (true);
		return deviceParams;
	}

	public NdefMessage getNfcTagContents() {
		return mNfcTagContents;
	}

	public void setVendor(String vendor) {
		mVendor = vendor;
	}

	public String getVendor() {
		return mVendor;
	}

	public void setModel(String model) {
		mModel = model;
	}

	public String getModel() {
		return mModel;
	}

	public void setVersion(String version) {
		mVersion = version;
	}

	public String getVersion() {
		return mVersion;
	}

	public void setInterpupillaryDistance(float interpupillaryDistance) {
		mInterpupillaryDistance = interpupillaryDistance;
	}

	public float getInterpupillaryDistance() {
		return mInterpupillaryDistance;
	}

	public void setVerticalDistanceToLensCenter(
			float verticalDistanceToLensCenter) {
		mVerticalDistanceToLensCenter = verticalDistanceToLensCenter;
	}

	public float getVerticalDistanceToLensCenter() {
		return mVerticalDistanceToLensCenter;
	}

	public void setVisibleViewportSize(float visibleViewportSize) {
		mVisibleViewportSize = visibleViewportSize;
	}

	public float getVisibleViewportSize() {
		return mVisibleViewportSize;
	}

	public void setFovY(float fovY) {
		mFovY = fovY;
	}

	public float getFovY() {
		return mFovY;
	}

	public void setLensDiameter(float lensDiameter) {
		mLensDiameter = lensDiameter;
	}

	public float getLensDiameter() {
		return mLensDiameter;
	}

	public void setScreenToLensDistance(float screenToLensDistance) {
		mScreenToLensDistance = screenToLensDistance;
	}

	public float getScreenToLensDistance() {
		return mScreenToLensDistance;
	}

	public void setEyeToLensDistance(float eyeToLensDistance) {
		mEyeToLensDistance = eyeToLensDistance;
	}

	public float getEyeToLensDistance() {
		return mEyeToLensDistance;
	}

	public Distortion getDistortion() {
		return mDistortion;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof CardboardDeviceParams)) {
			return false;
		} else {
			CardboardDeviceParams o = (CardboardDeviceParams) other;
			return mVendor == o.mVendor
					&& mModel == o.mModel
					&& mVersion == o.mVersion
					&& mInterpupillaryDistance == o.mInterpupillaryDistance
					&& mVerticalDistanceToLensCenter == o.mVerticalDistanceToLensCenter
					&& mLensDiameter == o.mLensDiameter
					&& mScreenToLensDistance == o.mScreenToLensDistance
					&& mEyeToLensDistance == o.mEyeToLensDistance
					&& mVisibleViewportSize == o.mVisibleViewportSize
					&& mFovY == o.mFovY && mDistortion.equals(o.mDistortion);
		}
	}

	private boolean parseNfcUri(NdefRecord record) {
		Uri uri = record.toUri();
		if (uri == null)
			return false;
		if (uri.getHost().equals("v1.0.0")) {
			mVendor = "com.google";
			mModel = "cardboard";
			mVersion = "1.0";
			return true;
		}
		List segments = uri.getPathSegments();
		if (segments.size() != 2) {
			return false;
		} else {
			mVendor = uri.getHost();
			mModel = (String) segments.get(0);
			mVersion = (String) segments.get(1);
			return true;
		}
	}
}
