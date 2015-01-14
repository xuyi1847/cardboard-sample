package com.google.vrtoolkit.cardboard;

import android.content.Context;
import android.opengl.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// Referenced classes of package com.google.vrtoolkit.cardboard:
//			HeadMountedDisplay, CardboardDeviceParams, ScreenParams, DistortionRenderer, 
//			EyeParams, Viewport, HeadTransform, EyeTransform, 
//			FieldOfView, Distortion

public class CardboardView extends GLSurfaceView {
	
	private class StereoRendererHelper implements Renderer {

		private final StereoRenderer mStereoRenderer;
		private boolean mVRMode = false; //qs test

		public StereoRendererHelper(StereoRenderer stereoRenderer) {
			mStereoRenderer = stereoRenderer;
			mVRMode = CardboardView.this.mVRMode;
		}

		public void setVRModeEnabled(final boolean enabled) {
			queueEvent(new Runnable() {

				public void run() {
					mVRMode = enabled;
				}

			});
		}

		public void onDrawFrame(HeadTransform head, EyeParams leftEye,
				EyeParams rightEye) {
			mStereoRenderer.onNewFrame(head);
			GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
			leftEye.getViewport().setGLViewport();
			leftEye.getViewport().setGLScissor();
			mStereoRenderer.onDrawEye(leftEye.getTransform());
			if (rightEye == null) {
				return;
			}
			rightEye.getViewport().setGLViewport();
			rightEye.getViewport().setGLScissor();
			mStereoRenderer.onDrawEye(rightEye.getTransform());

		}

		public void onFinishFrame(Viewport viewport) {
			viewport.setGLViewport();
			viewport.setGLScissor();
			mStereoRenderer.onFinishFrame(viewport);
		}

		public void onSurfaceChanged(int width, int height) {
			if (mVRMode)
				mStereoRenderer.onSurfaceChanged(width / 2, height);
			else
				mStereoRenderer.onSurfaceChanged(width, height);
		}

		public void onSurfaceCreated(EGLConfig config) {
			mStereoRenderer.onSurfaceCreated(config);
		}

		public void onRendererShutdown() {
			mStereoRenderer.onRendererShutdown();
		}

	}

	private class RendererHelper implements
			android.opengl.GLSurfaceView.Renderer {

		private final HeadTransform mHeadTransform = new HeadTransform();
		private final EyeParams mMonocular = new EyeParams(0);
		private final EyeParams mLeftEye = new EyeParams(1);
		private final EyeParams mRightEye = new EyeParams(2);
		private final float mLeftEyeTranslate[] = new float[16];
		private final float mRightEyeTranslate[] = new float[16];
		private final Renderer mRenderer;
		private boolean mShuttingDown;
		private HeadMountedDisplay mHmd;
		private boolean mVRMode = false; //qs test
		private boolean mDistortionCorrectionEnabled;
		private float mDistortionCorrectionScale;
		private float mZNear;
		private float mZFar;
		private boolean mProjectionChanged;
		private boolean mInvalidSurfaceSize;

		public void shutdown() {
			queueEvent(new Runnable() {

				public void run() {
					synchronized (RendererHelper.this) {
						mShuttingDown = true;
						mRenderer.onRendererShutdown();
						RendererHelper.this.notifyAll();
					}
				}

			});
		}

		public void setCardboardDeviceParams(CardboardDeviceParams newParams) {
			final CardboardDeviceParams deviceParams = new CardboardDeviceParams(
					newParams);
			queueEvent(new Runnable() {

				public void run() {
					mHmd.setCardboard(deviceParams);
					mProjectionChanged = true;
				}

			});
		}

		public void setScreenParams(ScreenParams newParams) {
			final ScreenParams screenParams = new ScreenParams(newParams);
			queueEvent(new Runnable() {

				public void run() {
					mHmd.setScreen(screenParams);
					mProjectionChanged = true;
				}

			});
		}

		public void setInterpupillaryDistance(final float interpupillaryDistance) {
			queueEvent(new Runnable() {

				public void run() {
					mHmd.getCardboard().setInterpupillaryDistance(
							interpupillaryDistance);
					mProjectionChanged = true;
				}

			});
		}

		public void setFOV(final float fovY) {
			queueEvent(new Runnable() {

				public void run() {
					mHmd.getCardboard().setFovY(fovY);
					mProjectionChanged = true;
				}

			});
		}

		public void setZPlanes(final float zNear, final float zFar) {
			queueEvent(new Runnable() {

				public void run() {
					mZNear = zNear;
					mZFar = zFar;
					mProjectionChanged = true;
				}

			});
		}

		public void setDistortionCorrectionEnabled(final boolean enabled) {
			queueEvent(new Runnable() {

				public void run() {
					mDistortionCorrectionEnabled = enabled;
					mProjectionChanged = true;
				}

			});
		}

		public void setDistortionCorrectionScale(final float scale) {
			queueEvent(new Runnable() {

				public void run() {
					mDistortionCorrectionScale = scale;
					mDistortionRenderer.setResolutionScale(scale);
				}

			});
		}

		public void setVRModeEnabled(final boolean enabled) {
			queueEvent(new Runnable() {

				public void run() {
					if (mVRMode == enabled)
						return;
					mVRMode = enabled;
					if (mRenderer instanceof StereoRendererHelper) {
						StereoRendererHelper stereoHelper = (StereoRendererHelper) mRenderer;
						stereoHelper.setVRModeEnabled(enabled);
					}
					mProjectionChanged = true;
					onSurfaceChanged((GL10) null, mHmd.getScreen().getWidth(),
							mHmd.getScreen().getHeight());
				}

			});
		}

		public void onDrawFrame(GL10 gl) {
			if (mShuttingDown || mInvalidSurfaceSize)
				return;
			ScreenParams screen = mHmd.getScreen();
			CardboardDeviceParams cdp = mHmd.getCardboard();
			mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);
			float halfInterpupillaryDistance = cdp.getInterpupillaryDistance() * 0.5F;
			if (mVRMode) {
				Matrix.setIdentityM(mLeftEyeTranslate, 0);
				Matrix.setIdentityM(mRightEyeTranslate, 0);
				Matrix.translateM(mLeftEyeTranslate, 0,
						halfInterpupillaryDistance, 0.0F, 0.0F);
				Matrix.translateM(mRightEyeTranslate, 0,
						-halfInterpupillaryDistance, 0.0F, 0.0F);
				Matrix.multiplyMM(mLeftEye.getTransform().getEyeView(), 0,
						mLeftEyeTranslate, 0, mHeadTransform.getHeadView(), 0);
				Matrix.multiplyMM(mRightEye.getTransform().getEyeView(), 0,
						mRightEyeTranslate, 0, mHeadTransform.getHeadView(), 0);
			} else {
				System.arraycopy(mHeadTransform.getHeadView(), 0, mMonocular
						.getTransform().getEyeView(), 0, mHeadTransform
						.getHeadView().length);
			}
			if (mProjectionChanged) {
				mMonocular.getViewport().setViewport(0, 0, screen.getWidth(),
						screen.getHeight());
				if (!mVRMode) {
					float aspectRatio = screen.getWidth() / screen.getHeight();
					Matrix.perspectiveM(mMonocular.getTransform()
							.getPerspective(), 0, cdp.getFovY(), aspectRatio,
							mZNear, mZFar);
				} else if (mDistortionCorrectionEnabled) {
					updateFieldOfView(mLeftEye.getFov(), mRightEye.getFov());
					mDistortionRenderer.onProjectionChanged(mHmd, mLeftEye,
							mRightEye, mZNear, mZFar);
				} else {
					float distEyeToScreen = cdp.getVisibleViewportSize()
							/ 2.0F
							/ (float) Math
									.tan(Math.toRadians(cdp.getFovY()) / 2D);
					float left = screen.getWidthMeters() / 2.0F
							- halfInterpupillaryDistance;
					float right = halfInterpupillaryDistance;
					float bottom = cdp.getVerticalDistanceToLensCenter()
							- screen.getBorderSizeMeters();
					float top = (screen.getBorderSizeMeters() + screen
							.getHeightMeters())
							- cdp.getVerticalDistanceToLensCenter();
					FieldOfView leftEyeFov = mLeftEye.getFov();
					leftEyeFov.setLeft((float) Math.toDegrees(Math.atan2(left,
							distEyeToScreen)));
					leftEyeFov.setRight((float) Math.toDegrees(Math.atan2(
							right, distEyeToScreen)));
					leftEyeFov.setBottom((float) Math.toDegrees(Math.atan2(
							bottom, distEyeToScreen)));
					leftEyeFov.setTop((float) Math.toDegrees(Math.atan2(top,
							distEyeToScreen)));
					FieldOfView rightEyeFov = mRightEye.getFov();
					rightEyeFov.setLeft(leftEyeFov.getRight());
					rightEyeFov.setRight(leftEyeFov.getLeft());
					rightEyeFov.setBottom(leftEyeFov.getBottom());
					rightEyeFov.setTop(leftEyeFov.getTop());
					leftEyeFov.toPerspectiveMatrix(mZNear, mZFar, mLeftEye
							.getTransform().getPerspective(), 0);
					rightEyeFov.toPerspectiveMatrix(mZNear, mZFar, mRightEye
							.getTransform().getPerspective(), 0);
					mLeftEye.getViewport().setViewport(0, 0,
							screen.getWidth() / 2, screen.getHeight());
					mRightEye.getViewport().setViewport(screen.getWidth() / 2,
							0, screen.getWidth() / 2, screen.getHeight());
				}
				mProjectionChanged = false;
			}
			if (mVRMode) {
				if (mDistortionCorrectionEnabled) {
					mDistortionRenderer.beforeDrawFrame();
					if (mDistortionCorrectionScale == 1.0F) {
						mRenderer.onDrawFrame(mHeadTransform, mLeftEye,
								mRightEye);
					} else {
						int leftX = mLeftEye.getViewport().x;
						int leftY = mLeftEye.getViewport().y;
						int leftWidth = mLeftEye.getViewport().width;
						int leftHeight = mLeftEye.getViewport().height;
						int rightX = mRightEye.getViewport().x;
						int rightY = mRightEye.getViewport().y;
						int rightWidth = mRightEye.getViewport().width;
						int rightHeight = mRightEye.getViewport().height;
						mLeftEye.getViewport()
								.setViewport(
										(int) ((float) leftX * mDistortionCorrectionScale),
										(int) ((float) leftY * mDistortionCorrectionScale),
										(int) ((float) leftWidth * mDistortionCorrectionScale),
										(int) ((float) leftHeight * mDistortionCorrectionScale));
						mRightEye
								.getViewport()
								.setViewport(
										(int) ((float) rightX * mDistortionCorrectionScale),
										(int) ((float) rightY * mDistortionCorrectionScale),
										(int) ((float) rightWidth * mDistortionCorrectionScale),
										(int) ((float) rightHeight * mDistortionCorrectionScale));
						mRenderer.onDrawFrame(mHeadTransform, mLeftEye,
								mRightEye);
						mLeftEye.getViewport().setViewport(leftX, leftY,
								leftWidth, leftHeight);
						mRightEye.getViewport().setViewport(rightX, rightY,
								rightWidth, rightHeight);
					}
					mDistortionRenderer.afterDrawFrame();
				} else {
					mRenderer.onDrawFrame(mHeadTransform, mLeftEye, mRightEye);
				}
			} else {
				mRenderer.onDrawFrame(mHeadTransform, mMonocular, null);
			}
			mRenderer.onFinishFrame(mMonocular.getViewport());
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			if (mShuttingDown)
				return;
			ScreenParams screen = mHmd.getScreen();
			if (width != screen.getWidth() || height != screen.getHeight()) {
				if (!mInvalidSurfaceSize) {
					GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
					Log.w("CardboardView", "Surface size " + width + "x"
							+ height
							+ " does not match the expected screen size "
							+ screen.getWidth() + "x" + screen.getHeight()
							+ ". Rendering is disabled.");
				}
				mInvalidSurfaceSize = true;
			} else {
				mInvalidSurfaceSize = false;
			}
			mRenderer.onSurfaceChanged(width, height);
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			if (mShuttingDown) {
				return;
			}
			mRenderer.onSurfaceCreated(config);
		}

		private void updateFieldOfView(FieldOfView leftEyeFov,
				FieldOfView rightEyeFov) {
			CardboardDeviceParams cdp = mHmd.getCardboard();
			ScreenParams screen = mHmd.getScreen();
			Distortion distortion = cdp.getDistortion();
			float idealFovAngle = (float) Math.toDegrees(Math.atan2(
					cdp.getLensDiameter() / 2.0F, cdp.getEyeToLensDistance()));
			float eyeToScreenDist = cdp.getEyeToLensDistance()
					+ cdp.getScreenToLensDistance();
			float outerDist = (screen.getWidthMeters() - cdp
					.getInterpupillaryDistance()) / 2.0F;
			float innerDist = cdp.getInterpupillaryDistance() / 2.0F;
			float bottomDist = cdp.getVerticalDistanceToLensCenter()
					- screen.getBorderSizeMeters();
			float topDist = (screen.getHeightMeters() + screen
					.getBorderSizeMeters())
					- cdp.getVerticalDistanceToLensCenter();
			float outerAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(outerDist), eyeToScreenDist));
			float innerAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(innerDist), eyeToScreenDist));
			float bottomAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(bottomDist), eyeToScreenDist));
			float topAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(topDist), eyeToScreenDist));
			leftEyeFov.setLeft(Math.min(outerAngle, idealFovAngle));
			leftEyeFov.setRight(Math.min(innerAngle, idealFovAngle));
			leftEyeFov.setBottom(Math.min(bottomAngle, idealFovAngle));
			leftEyeFov.setTop(Math.min(topAngle, idealFovAngle));
			rightEyeFov.setLeft(Math.min(innerAngle, idealFovAngle));
			rightEyeFov.setRight(Math.min(outerAngle, idealFovAngle));
			rightEyeFov.setBottom(Math.min(bottomAngle, idealFovAngle));
			rightEyeFov.setTop(Math.min(topAngle, idealFovAngle));
		}

		public RendererHelper(Renderer renderer) {
			mRenderer = renderer;
			mHmd = new HeadMountedDisplay(CardboardView.this.mHmd);
			updateFieldOfView(mLeftEye.getFov(), mRightEye.getFov());
			mDistortionRenderer = new DistortionRenderer();
			mVRMode = CardboardView.this.mVRMode;
			mDistortionCorrectionEnabled = CardboardView.this.mDistortionCorrectionEnabled;
			mDistortionCorrectionScale = CardboardView.this.mDistortionCorrectionScale;
			mZNear = CardboardView.this.mZNear;
			mZFar = CardboardView.this.mZFar;
			mProjectionChanged = true;
		}
	}

	public static interface CardboardDeviceParamsObserver {

		public abstract void onCardboardDeviceParamsUpdate(
				CardboardDeviceParams cardboarddeviceparams);
	}

	public static interface StereoRenderer {

		public abstract void onNewFrame(HeadTransform headtransform);

		public abstract void onDrawEye(EyeTransform eyetransform);

		public abstract void onFinishFrame(Viewport viewport);

		public abstract void onSurfaceChanged(int i, int j);

		public abstract void onSurfaceCreated(EGLConfig eglconfig);

		public abstract void onRendererShutdown();
	}

	public static interface Renderer {

		public abstract void onDrawFrame(HeadTransform headtransform,
				EyeParams eyeparams, EyeParams eyeparams1);

		public abstract void onFinishFrame(Viewport viewport);

		public abstract void onSurfaceChanged(int i, int j);

		public abstract void onSurfaceCreated(EGLConfig eglconfig);

		public abstract void onRendererShutdown();
	}

	private static final String TAG = "CardboardView";
	private static final float DEFAULT_Z_NEAR = 0.1F;
	private static final float DEFAULT_Z_FAR = 100F;
	private RendererHelper mRendererHelper;
	private HeadTracker mHeadTracker;
	private HeadMountedDisplay mHmd;
	private DistortionRenderer mDistortionRenderer;
	private CardboardDeviceParamsObserver mCardboardDeviceParamsObserver;
	private boolean mVRMode = false; //qs test
	private volatile boolean mDistortionCorrectionEnabled;
	private volatile float mDistortionCorrectionScale;
	private float mZNear;
	private float mZFar;

	public CardboardView(Context context) {
		super(context);
		mVRMode = false; //qs test true;
		mDistortionCorrectionEnabled = false; //qs test true;
		mDistortionCorrectionScale = 1.0F;
		mZNear = DEFAULT_Z_NEAR;
		mZFar = DEFAULT_Z_FAR;
		init(context);
	}

	public CardboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mVRMode = false; //qs test true;
		mDistortionCorrectionEnabled = false; //qs test true;
		mDistortionCorrectionScale = 1.0F;
		mZNear = DEFAULT_Z_NEAR;
		mZFar = DEFAULT_Z_FAR;
		init(context);
	}

	public void setRenderer(Renderer renderer) {
		mRendererHelper = renderer == null ? null
				: new RendererHelper(renderer);
		super.setRenderer(mRendererHelper);
	}

	public void setRenderer(StereoRenderer renderer) {
		setRenderer(((Renderer) (renderer == null ? (Renderer) null
				: ((Renderer) (new StereoRendererHelper(renderer))))));
	}

	public void setVRModeEnabled(boolean enabled) {
		//qs test
		if(true) {
			mVRMode = false;
			if (mRendererHelper != null)
				mRendererHelper.setVRModeEnabled(false);
		} else {
			mVRMode = enabled;
			if (mRendererHelper != null)
				mRendererHelper.setVRModeEnabled(enabled);
		}
	}

	public boolean getVRMode() {
		return mVRMode;
	}

	public HeadMountedDisplay getHeadMountedDisplay() {
		return mHmd;
	}

	public void updateCardboardDeviceParams(
			CardboardDeviceParams cardboardDeviceParams) {
		if (cardboardDeviceParams == null
				|| cardboardDeviceParams.equals(mHmd.getCardboard()))
			return;
		if (mCardboardDeviceParamsObserver != null)
			mCardboardDeviceParamsObserver
					.onCardboardDeviceParamsUpdate(cardboardDeviceParams);
		mHmd.setCardboard(cardboardDeviceParams);
		if (mRendererHelper != null)
			mRendererHelper.setCardboardDeviceParams(cardboardDeviceParams);
	}

	public void setCardboardDeviceParamsObserver(
			CardboardDeviceParamsObserver observer) {
		mCardboardDeviceParamsObserver = observer;
	}

	public CardboardDeviceParams getCardboardDeviceParams() {
		return mHmd.getCardboard();
	}

	public void updateScreenParams(ScreenParams screenParams) {
		if (screenParams == null || screenParams.equals(mHmd.getScreen()))
			return;
		mHmd.setScreen(screenParams);
		if (mRendererHelper != null)
			mRendererHelper.setScreenParams(screenParams);
	}

	public ScreenParams getScreenParams() {
		return mHmd.getScreen();
	}

	public void setInterpupillaryDistance(float distance) {
		mHmd.getCardboard().setInterpupillaryDistance(distance);
		if (mRendererHelper != null)
			mRendererHelper.setInterpupillaryDistance(distance);
	}

	public float getInterpupillaryDistance() {
		return mHmd.getCardboard().getInterpupillaryDistance();
	}

	public void setFovY(float fovY) {
		mHmd.getCardboard().setFovY(fovY);
		if (mRendererHelper != null)
			mRendererHelper.setFOV(fovY);
	}

	public float getFovY() {
		return mHmd.getCardboard().getFovY();
	}

	public void setZPlanes(float zNear, float zFar) {
		mZNear = zNear;
		mZFar = zFar;
		if (mRendererHelper != null)
			mRendererHelper.setZPlanes(zNear, zFar);
	}

	public float getZNear() {
		return mZNear;
	}

	public float getZFar() {
		return mZFar;
	}

	public void setDistortionCorrectionEnabled(boolean enabled) {
		mDistortionCorrectionEnabled = enabled;
		if (mRendererHelper != null)
			mRendererHelper.setDistortionCorrectionEnabled(enabled);
	}

	public boolean getDistortionCorrectionEnabled() {
		return mDistortionCorrectionEnabled;
	}

	public void setDistortionCorrectionScale(float scale) {
		mDistortionCorrectionScale = scale;
		if (mRendererHelper != null)
			mRendererHelper.setDistortionCorrectionScale(scale);
	}

	public float getDistortionCorrectionScale() {
		return mDistortionCorrectionScale;
	}

	public void onResume() {
		if (mRendererHelper == null) {
			return;
		}
		super.onResume();
		mHeadTracker.startTracking();

	}

	public void onPause() {
		if (mRendererHelper == null) {
			return;
		}
		super.onPause();
		mHeadTracker.stopTracking();

	}

	public void setRenderer(android.opengl.GLSurfaceView.Renderer renderer) {
		throw new RuntimeException(
				"Please use the CardboardView renderer interfaces");
	}

	public void onDetachedFromWindow() {
		if (mRendererHelper != null)
			synchronized (mRendererHelper) {
				mRendererHelper.shutdown();
				try {
					mRendererHelper.wait();
				} catch (InterruptedException e) {
					Log.e("CardboardView",
							(new StringBuilder())
									.append("Interrupted during shutdown: ")
									.append(e.toString()).toString());
				}
			}
		super.onDetachedFromWindow();
	}

	private void init(Context context) {
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		WindowManager windowManager = (WindowManager) context
				.getSystemService("window");
		mHeadTracker = new HeadTracker(context);
		mHmd = new HeadMountedDisplay(windowManager.getDefaultDisplay());
	}

}
