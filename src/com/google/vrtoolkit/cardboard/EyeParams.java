package com.google.vrtoolkit.cardboard;

// Referenced classes of package com.google.vrtoolkit.cardboard:
//			Viewport, FieldOfView, EyeTransform

public class EyeParams {
	public static class Eye {

		public static final int MONOCULAR = 0;
		public static final int LEFT = 1;
		public static final int RIGHT = 2;

	}

	private final int mEye;
	private final Viewport mViewport = new Viewport();
	private final FieldOfView mFov = new FieldOfView();
	private final EyeTransform mEyeTransform = new EyeTransform(this);

	public EyeParams(int eye) {
		mEye = eye;
	}

	public int getEye() {
		return mEye;
	}

	public Viewport getViewport() {
		return mViewport;
	}

	public FieldOfView getFov() {
		return mFov;
	}

	public EyeTransform getTransform() {
		return mEyeTransform;
	}
}
