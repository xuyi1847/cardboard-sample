
package com.google.vrtoolkit.cardboard;

import android.annotation.SuppressLint;
import android.util.DisplayMetrics;
import android.view.Display;

@SuppressLint("NewApi")
public class ScreenParams {

	public static final float METERS_PER_INCH = 0.0254F;
	private static final float DEFAULT_BORDER_SIZE_METERS = 0.003F;
	private int mWidth;
	private int mHeight;
	private float mXMetersPerPixel;
	private float mYMetersPerPixel;
	private float mBorderSizeMeters;

	public ScreenParams(Display display) {
		DisplayMetrics metrics = new DisplayMetrics();
		try {
			display.getRealMetrics(metrics);
		} catch (NoSuchMethodError e) {
			display.getMetrics(metrics);
		}
		mXMetersPerPixel = 0.0254F / metrics.xdpi;
		mYMetersPerPixel = 0.0254F / metrics.ydpi;
		mWidth = metrics.widthPixels;
		mHeight = metrics.heightPixels;
		mBorderSizeMeters = DEFAULT_BORDER_SIZE_METERS;
		if (mHeight > mWidth) {
			int tempPx = mWidth;
			mWidth = mHeight;
			mHeight = tempPx;
			float tempMetersPerPixel = mXMetersPerPixel;
			mXMetersPerPixel = mYMetersPerPixel;
			mYMetersPerPixel = tempMetersPerPixel;
		}
	}

	public ScreenParams(ScreenParams params) {
		mWidth = params.mWidth;
		mHeight = params.mHeight;
		mXMetersPerPixel = params.mXMetersPerPixel;
		mYMetersPerPixel = params.mYMetersPerPixel;
		mBorderSizeMeters = params.mBorderSizeMeters;
	}

	public void setWidth(int width) {
		mWidth = width;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setHeight(int height) {
		mHeight = height;
	}

	public int getHeight() {
		return mHeight;
	}

	public float getWidthMeters() {
		return (float) mWidth * mXMetersPerPixel;
	}

	public float getHeightMeters() {
		return (float) mHeight * mYMetersPerPixel;
	}

	public void setBorderSizeMeters(float screenBorderSize) {
		mBorderSizeMeters = screenBorderSize;
	}

	public float getBorderSizeMeters() {
		return mBorderSizeMeters;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof ScreenParams)) {
			return false;
		} else {
			ScreenParams o = (ScreenParams) other;
			return (mWidth == o.mWidth) && (mHeight == o.mHeight)
					&& mXMetersPerPixel == o.mXMetersPerPixel
					&& mYMetersPerPixel == o.mYMetersPerPixel
					&& mBorderSizeMeters == o.mBorderSizeMeters;
		}
	}
}
