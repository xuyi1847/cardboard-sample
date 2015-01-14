package com.google.vrtoolkit.cardboard;

import android.opengl.Matrix;

// Referenced classes of package com.google.vrtoolkit.cardboard:
//			EyeParams

public class EyeTransform {

	private final EyeParams mEyeParams;
	private final float mEyeView[] = new float[16];
	private final float mPerspective[] = new float[16];

	public EyeTransform(EyeParams params) {
		mEyeParams = params;
		Matrix.setIdentityM(mEyeView, 0);
		Matrix.setIdentityM(mPerspective, 0);
	}

	public float[] getEyeView() {
		return mEyeView;
	}

	public float[] getPerspective() {
		return mPerspective;
	}

	public EyeParams getParams() {
		return mEyeParams;
	}
}
