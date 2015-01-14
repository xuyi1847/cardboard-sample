package com.google.vrtoolkit.cardboard;

import android.view.Display;

// Referenced classes of package com.google.vrtoolkit.cardboard:
//			ScreenParams, CardboardDeviceParams

public class HeadMountedDisplay {

	private ScreenParams mScreen;
	private CardboardDeviceParams mCardboard;

	public HeadMountedDisplay(Display display) {
		mScreen = new ScreenParams(display);
		mCardboard = new CardboardDeviceParams();
	}

	public HeadMountedDisplay(HeadMountedDisplay hmd) {
		mScreen = new ScreenParams(hmd.mScreen);
		mCardboard = new CardboardDeviceParams(hmd.mCardboard);
	}

	public void setScreen(ScreenParams screen) {
		mScreen = new ScreenParams(screen);
	}

	public ScreenParams getScreen() {
		return mScreen;
	}

	public void setCardboard(CardboardDeviceParams cardboard) {
		mCardboard = new CardboardDeviceParams(cardboard);
	}

	public CardboardDeviceParams getCardboard() {
		return mCardboard;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof HeadMountedDisplay)) {
			return false;
		} 
			HeadMountedDisplay o = (HeadMountedDisplay) other;
			return mScreen.equals(o.mScreen) && mCardboard.equals(o.mCardboard);

	}
}
