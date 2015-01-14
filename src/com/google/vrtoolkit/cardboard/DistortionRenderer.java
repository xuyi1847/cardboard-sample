package com.google.vrtoolkit.cardboard;

import android.opengl.GLES20;
import android.util.Log;
import java.nio.*;

// Referenced classes of package com.google.vrtoolkit.cardboard:
//			HeadMountedDisplay, FieldOfView, ScreenParams, CardboardDeviceParams, 
//			EyeParams, EyeTransform, Viewport, Distortion

public class DistortionRenderer {
	

	private static final String TAG = "DistortionRenderer";
	public  static final boolean GPU_32F = false;  //jz Mali 16 bits
	private int mTextureId;
	private int mRenderbufferId;
	private int mFramebufferId;
	private IntBuffer mOriginalFramebufferId;
	private IntBuffer mCullFaceEnabled;
	private IntBuffer mScissorTestEnabled;
	private IntBuffer mViewport;
	private float mResolutionScale;
	private DistortionMesh mLeftEyeDistortionMesh;
	private DistortionMesh mRightEyeDistortionMesh;
	private HeadMountedDisplay mHmd;
	private FieldOfView mLeftEyeFov;
	private FieldOfView mRightEyeFov;
	private ProgramHolder mProgramHolder;
	private final String VERTEX_SHADER = "attribute vec2 aPosition;\nattribute float aVignette;\nattribute vec2 aTextureCoord;\nvarying vec2 vTextureCoord;\nvarying float vVignette;\nuniform float uTextureCoordScale;\nvoid main() {\n    gl_Position = vec4(aPosition, 0.0, 1.0);\n    vTextureCoord = aTextureCoord.xy * uTextureCoordScale;\n    vVignette = aVignette;\n}\n";
	private final String FRAGMENT_SHADER = "precision mediump float;\nvarying vec2 vTextureCoord;\nvarying float vVignette;\nuniform sampler2D uTextureSampler;\nvoid main() {\n    gl_FragColor = vVignette * texture2D(uTextureSampler, vTextureCoord);\n}\n";

	public DistortionRenderer() {
		mTextureId = -1;
		mRenderbufferId = -1;
		mFramebufferId = -1;
		mOriginalFramebufferId = IntBuffer.allocate(1);
		mCullFaceEnabled = IntBuffer.allocate(1);
		mScissorTestEnabled = IntBuffer.allocate(1);
		mViewport = IntBuffer.allocate(4);
		mResolutionScale = 1.0F;
	}

	public void beforeDrawFrame() {
		GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mOriginalFramebufferId);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
	}

	public void afterDrawFrame() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mOriginalFramebufferId.array()[0]);
		GLES20.glViewport(0, 0, mHmd.getScreen().getWidth(), mHmd.getScreen()
				.getHeight());
		GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, mViewport);
		GLES20.glGetIntegerv(GLES20.GL_CULL_FACE, mCullFaceEnabled);
		GLES20.glGetIntegerv(GLES20.GL_SCISSOR_TEST, mScissorTestEnabled);
		GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glUseProgram(mProgramHolder.program);
		GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
		GLES20.glScissor(0, 0, mHmd.getScreen().getWidth() / 2, mHmd
				.getScreen().getHeight());
		renderDistortionMesh(mLeftEyeDistortionMesh);
		GLES20.glScissor(mHmd.getScreen().getWidth() / 2, 0, mHmd.getScreen()
				.getWidth() / 2, mHmd.getScreen().getHeight());
		renderDistortionMesh(mRightEyeDistortionMesh);
		GLES20.glDisableVertexAttribArray(mProgramHolder.aPosition);
		GLES20.glDisableVertexAttribArray(mProgramHolder.aVignette);
		GLES20.glDisableVertexAttribArray(mProgramHolder.aTextureCoord);
		GLES20.glUseProgram(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
		if (mCullFaceEnabled.array()[0] == 1)
			GLES20.glEnable(GLES20.GL_CULL_FACE);
		if (mScissorTestEnabled.array()[0] == 1)
			GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
		GLES20.glViewport(mViewport.array()[0], mViewport.array()[1],
				mViewport.array()[2], mViewport.array()[3]);
	}

	public void setResolutionScale(float scale) {
		mResolutionScale = scale;
	}

	public void onProjectionChanged(HeadMountedDisplay hmd, EyeParams leftEye,
			EyeParams rightEye, float zNear, float zFar) {
		mHmd = new HeadMountedDisplay(hmd);
		mLeftEyeFov = new FieldOfView(leftEye.getFov());
		mRightEyeFov = new FieldOfView(rightEye.getFov());
		ScreenParams screen = mHmd.getScreen();
		CardboardDeviceParams cdp = mHmd.getCardboard();
		if (mProgramHolder == null)
			mProgramHolder = createProgramHolder();
		EyeViewport leftEyeViewport = initViewportForEye(leftEye, 0.0F);
		EyeViewport rightEyeViewport = initViewportForEye(rightEye,
				leftEyeViewport.width);
		leftEye.getFov().toPerspectiveMatrix(zNear, zFar,
				leftEye.getTransform().getPerspective(), 0);
		rightEye.getFov().toPerspectiveMatrix(zNear, zFar,
				rightEye.getTransform().getPerspective(), 0);
		float textureWidthM = leftEyeViewport.width + rightEyeViewport.width;
		float textureHeightM = Math.max(leftEyeViewport.height,
				rightEyeViewport.height);
		float xPxPerM = (float) screen.getWidth() / screen.getWidthMeters();
		float yPxPerM = (float) screen.getHeight() / screen.getHeightMeters();
		int textureWidthPx = Math.round(textureWidthM * xPxPerM);
		int textureHeightPx = Math.round(textureHeightM * yPxPerM);
		float xEyeOffsetMScreen = screen.getWidthMeters() / 2.0F
				- cdp.getInterpupillaryDistance() / 2.0F;
		float yEyeOffsetMScreen = cdp.getVerticalDistanceToLensCenter()
				- screen.getBorderSizeMeters();
		mLeftEyeDistortionMesh = createDistortionMesh(leftEye, leftEyeViewport,
				textureWidthM, textureHeightM, xEyeOffsetMScreen,
				yEyeOffsetMScreen);
		xEyeOffsetMScreen = screen.getWidthMeters() - xEyeOffsetMScreen;
		mRightEyeDistortionMesh = createDistortionMesh(rightEye,
				rightEyeViewport, textureWidthM, textureHeightM,
				xEyeOffsetMScreen, yEyeOffsetMScreen);
		setupRenderTextureAndRenderbuffer(textureWidthPx, textureHeightPx);
	}

	private EyeViewport initViewportForEye(EyeParams eye, float xOffsetM) {
		ScreenParams screen = mHmd.getScreen();
		CardboardDeviceParams cdp = mHmd.getCardboard();
		float eyeToScreenDistanceM = cdp.getEyeToLensDistance()
				+ cdp.getScreenToLensDistance();
		float leftM = (float) Math.tan(Math.toRadians(eye.getFov().getLeft()))
				* eyeToScreenDistanceM;
		float rightM = (float) Math
				.tan(Math.toRadians(eye.getFov().getRight()))
				* eyeToScreenDistanceM;
		float bottomM = (float) Math.tan(Math.toRadians(eye.getFov()
				.getBottom())) * eyeToScreenDistanceM;
		float topM = (float) Math.tan(Math.toRadians(eye.getFov().getTop()))
				* eyeToScreenDistanceM;
		EyeViewport vp = new EyeViewport();
		vp.x = xOffsetM;
		vp.y = 0.0F;
		vp.width = leftM + rightM;
		vp.height = bottomM + topM;
		vp.eyeX = leftM + xOffsetM;
		vp.eyeY = bottomM;
		float xPxPerM = (float) screen.getWidth() / screen.getWidthMeters();
		float yPxPerM = (float) screen.getHeight() / screen.getHeightMeters();
		eye.getViewport().x = Math.round(vp.x * xPxPerM);
		eye.getViewport().y = Math.round(vp.y * xPxPerM);
		eye.getViewport().width = Math.round(vp.width * xPxPerM);
		eye.getViewport().height = Math.round(vp.height * xPxPerM);
		return vp;
	}

	private DistortionMesh createDistortionMesh(EyeParams eye,
			EyeViewport eyeViewport, float textureWidthM, float textureHeightM,
			float xEyeOffsetMScreen, float yEyeOffsetMScreen) {
		return new DistortionMesh(eye, mHmd.getCardboard().getDistortion(),
				mHmd.getScreen().getWidthMeters(), mHmd.getScreen()
						.getHeightMeters(), xEyeOffsetMScreen,
				yEyeOffsetMScreen, textureWidthM, textureHeightM,
				eyeViewport.eyeX, eyeViewport.eyeY, eyeViewport.x,
				eyeViewport.y, eyeViewport.width, eyeViewport.height);
	}

	private void renderDistortionMesh(DistortionMesh mesh)
	{
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.mArrayBufferId);
		mesh.getClass();
		mesh.getClass();

		GLES20.glVertexAttribPointer(mProgramHolder.aPosition, 3, GLES20.GL_FLOAT, false, DistortionMesh.DATA_STRIDE_BYTES, 0 * 4);
		GLES20.glEnableVertexAttribArray(mProgramHolder.aPosition);
		mesh.getClass();
		mesh.getClass();
		GLES20.glVertexAttribPointer(mProgramHolder.aVignette, 1, GLES20.GL_FLOAT, false, DistortionMesh.DATA_STRIDE_BYTES, 2 * 4);
		GLES20.glEnableVertexAttribArray(mProgramHolder.aVignette);
		mesh.getClass();
		mesh.getClass();
		GLES20.glVertexAttribPointer(mProgramHolder.aTextureCoord, 2, GLES20.GL_FLOAT, false, DistortionMesh.DATA_STRIDE_BYTES, 3 * 4);
		GLES20.glEnableVertexAttribArray(mProgramHolder.aTextureCoord);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
		GLES20.glUniform1i(mProgramHolder.uTextureSampler, 0);
		GLES20.glUniform1f(mProgramHolder.uTextureCoordScale, mResolutionScale);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mesh.mElementBufferId);
		
		// jz changed
		if(GPU_32F){
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mesh.nIndices, GLES20.GL_UNSIGNED_INT, 0);
		} else {
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mesh.nIndices, GLES20.GL_UNSIGNED_SHORT, 0);
		}
	}

	private float computeDistortionScale(Distortion distortion,
			float screenWidthM, float interpupillaryDistanceM) {
		return distortion
				.distortionFactor((screenWidthM / 2.0F - interpupillaryDistanceM / 2.0F)
						/ (screenWidthM / 4F));
	}

	private int createTexture(int width, int height) {
		int textureIds[] = new int[1];
		GLES20.glGenTextures(1, textureIds, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, null);
		return textureIds[0];
	}

	private int setupRenderTextureAndRenderbuffer(int width, int height) {
		if (mTextureId != -1)
			GLES20.glDeleteTextures(1, new int[] { mTextureId }, 0);
		if (mRenderbufferId != -1)
			GLES20.glDeleteRenderbuffers(1, new int[] { mRenderbufferId }, 0);
		if (mFramebufferId != -1)
			GLES20.glDeleteFramebuffers(1, new int[] { mFramebufferId }, 0);
		mTextureId = createTexture(width, height);
		checkGlError("setupRenderTextureAndRenderbuffer: create texture");
		int renderbufferIds[] = new int[1];
		GLES20.glGenRenderbuffers(1, renderbufferIds, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbufferIds[0]);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
		mRenderbufferId = renderbufferIds[0];
		checkGlError("setupRenderTextureAndRenderbuffer: create renderbuffer");
		int framebufferIds[] = new int[1];
		GLES20.glGenFramebuffers(1, framebufferIds, 0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferIds[0]);
		mFramebufferId = framebufferIds[0];
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId, 0);
		GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER,
				renderbufferIds[0]);
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Framebuffer is not complete: " + Integer.toHexString(status));
		} 
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			return framebufferIds[0];
		
	}

	private int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int compiled[] = new int[1];
			GLES20.glGetShaderiv(shader, 35713, compiled, 0);
			if (compiled[0] == 0) {
				Log.e("DistortionRenderer", "Could not compile shader " + shaderType + ":");
				Log.e("DistortionRenderer", GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	private int createProgram(String vertexSource, String fragmentSource) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == 0)
			return 0;
		int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (pixelShader == 0)
			return 0;
		int program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, vertexShader);
			checkGlError("glAttachShader");
			GLES20.glAttachShader(program, pixelShader);
			checkGlError("glAttachShader");
			GLES20.glLinkProgram(program);
			int linkStatus[] = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != 1) {
				Log.e("DistortionRenderer", "Could not link program: ");
				Log.e("DistortionRenderer", GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}

	private ProgramHolder createProgramHolder() {
		ProgramHolder holder = new ProgramHolder();
		holder.program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		if (holder.program == 0)
			throw new RuntimeException("Could not create program");
		holder.aPosition = GLES20.glGetAttribLocation(holder.program,
				"aPosition");
		checkGlError("glGetAttribLocation aPosition");
		if (holder.aPosition == -1)
			throw new RuntimeException(
					"Could not get attrib location for aPosition");
		holder.aVignette = GLES20.glGetAttribLocation(holder.program,
				"aVignette");
		checkGlError("glGetAttribLocation aVignette");
		if (holder.aVignette == -1)
			throw new RuntimeException(
					"Could not get attrib location for aVignette");
		holder.aTextureCoord = GLES20.glGetAttribLocation(holder.program,
				"aTextureCoord");
		checkGlError("glGetAttribLocation aTextureCoord");
		if (holder.aTextureCoord == -1)
			throw new RuntimeException(
					"Could not get attrib location for aTextureCoord");
		holder.uTextureCoordScale = GLES20.glGetUniformLocation(holder.program,
				"uTextureCoordScale");
		checkGlError("glGetUniformLocation uTextureCoordScale");
		if (holder.uTextureCoordScale == -1)
			throw new RuntimeException(
					"Could not get attrib location for uTextureCoordScale");
		holder.uTextureSampler = GLES20.glGetUniformLocation(holder.program,
				"uTextureSampler");
		checkGlError("glGetUniformLocation uTextureSampler");
		if (holder.uTextureSampler == -1)
			throw new RuntimeException(
					"Could not get attrib location for uTextureSampler");
		else
			return holder;
	}

	private void checkGlError(String op) {
		int error;
		if ((error = GLES20.glGetError()) != 0) {
			Log.e("DistortionRenderer", op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	private static float clamp(float val, float min, float max) {
		return Math.max(min, Math.min(max, val));
	}


private class DistortionMesh {

		private static final String TAG = "DistortionMesh";
		public static final int BYTES_PER_FLOAT = 4;
		public static final int BYTES_PER_INT = 4;
		public final int COMPONENTS_PER_VERT = 5;
		public static final int DATA_STRIDE_BYTES = 20;
		public final int DATA_POS_OFFSET = 0;
		public final int DATA_VIGNETTE_OFFSET = 2;
		public final int DATA_UV_OFFSET = 3;
		public final int ROWS = 40;
		public final int COLS = 40;
		public final float VIGNETTE_SIZE_M_SCREEN = 0.002F;
		public int nIndices;
		public int mArrayBufferId;
		public int mElementBufferId;

		public DistortionMesh(EyeParams eye, Distortion distortion,
				float screenWidthM, float screenHeightM,
				float xEyeOffsetMScreen, float yEyeOffsetMScreen,
				float textureWidthM, float textureHeightM,
				float xEyeOffsetMTexture, float yEyeOffsetMTexture,
				float viewportXMTexture, float viewportYMTexture,
				float viewportWidthMTexture, float viewportHeightMTexture) {
	

			mArrayBufferId = -1;
			mElementBufferId = -1;
			float mPerUScreen = screenWidthM;
			float mPerVScreen = screenHeightM;
			float mPerUTexture = textureWidthM;
			float mPerVTexture = textureHeightM;
			float vertexData[] = new float[8000];
			int vertexOffset = 0;
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					float uTexture = ((float) col / 39F)
							* (viewportWidthMTexture / textureWidthM)
							+ viewportXMTexture / textureWidthM;
					float vTexture = ((float) row / 39F)
							* (viewportHeightMTexture / textureHeightM)
							+ viewportYMTexture / textureHeightM;
					float xTexture = uTexture * mPerUTexture;
					float yTexture = vTexture * mPerVTexture;
					float xTextureEye = xTexture - xEyeOffsetMTexture;
					float yTextureEye = yTexture - yEyeOffsetMTexture;
					float rTexture = (float) Math.sqrt(xTextureEye
							* xTextureEye + yTextureEye * yTextureEye);
					float textureToScreen = rTexture <= 0.0F ? 1.0F
							: distortion.distortInverse(rTexture) / rTexture;
					float xScreen = xTextureEye * textureToScreen
							+ xEyeOffsetMScreen;
					float yScreen = yTextureEye * textureToScreen
							+ yEyeOffsetMScreen;
					float uScreen = xScreen / mPerUScreen;
					float vScreen = yScreen / mPerVScreen;
					float vignetteSizeMTexture = VIGNETTE_SIZE_M_SCREEN / textureToScreen;
					float dxTexture = xTexture
							- DistortionRenderer.clamp(xTexture,
									viewportXMTexture + vignetteSizeMTexture,
									(viewportXMTexture + viewportWidthMTexture)
											- vignetteSizeMTexture);
					float dyTexture = yTexture
							- DistortionRenderer
									.clamp(yTexture,
											viewportYMTexture
													+ vignetteSizeMTexture,
											(viewportYMTexture + viewportHeightMTexture)
													- vignetteSizeMTexture);
					float drTexture = (float) Math.sqrt(dxTexture * dxTexture
							+ dyTexture * dyTexture);
					float vignette = 1.0F - DistortionRenderer.clamp(drTexture
							/ vignetteSizeMTexture, 0.0F, 1.0F);
					vertexData[vertexOffset + 0] = 2.0F * uScreen - 1.0F;
					vertexData[vertexOffset + 1] = 2.0F * vScreen - 1.0F;
					vertexData[vertexOffset + 2] = vignette;
					vertexData[vertexOffset + 3] = uTexture;
					vertexData[vertexOffset + 4] = vTexture;
					vertexOffset += COMPONENTS_PER_VERT;
				}

			}

			nIndices = 3158;
			
			// jz changed
			if(GPU_32F){
				int indexData[] = new int[nIndices];
				int indexOffset = 0;
				vertexOffset = 0;
				for (int row = 0; row < (ROWS-1); row++) {
					if (row > 0) {
						indexData[indexOffset] = indexData[indexOffset - 1];
						indexOffset++;
					}
					for (int col = 0; col < COLS; col++) {
						if (col > 0)
							if (row % 2 == 0)
								vertexOffset++;
							else
								vertexOffset--;
						indexData[indexOffset++] = vertexOffset;
						indexData[indexOffset++] = vertexOffset + 40;
					}
	
					vertexOffset += 40;
				}
	
				FloatBuffer vertexBuffer = ByteBuffer
						.allocateDirect(vertexData.length * 4)
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				vertexBuffer.put(vertexData).position(0);
				IntBuffer indexBuffer = ByteBuffer
						.allocateDirect(indexData.length * 4)
						.order(ByteOrder.nativeOrder()).asIntBuffer();
				indexBuffer.put(indexData).position(0);
				int bufferIds[] = new int[2];
				GLES20.glGenBuffers(2, bufferIds, 0);
				mArrayBufferId = bufferIds[0];
				mElementBufferId = bufferIds[1];
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mArrayBufferId);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, vertexBuffer,
						GLES20.GL_STATIC_DRAW);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mElementBufferId);
				GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexData.length * 4, indexBuffer, GLES20.GL_STATIC_DRAW);
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
			} else {
				
				short indexData[] = new short[nIndices];
				int indexOffset = 0;
				vertexOffset = 0;
				
				for (int row = 0; row < (ROWS-1); row++) {
					if (row > 0) {
						indexData[indexOffset] = indexData[indexOffset - 1];
						indexOffset++;
					}
					for (int col = 0; col < COLS; col++) {
						if (col > 0)
							if (row % 2 == 0)
								vertexOffset++;
							else
								vertexOffset--;
						indexData[indexOffset++] = (short)vertexOffset;
						indexData[indexOffset++] = (short)(vertexOffset + 40);
					}
	
					vertexOffset += 40;
				}
	
				FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				vertexBuffer.put(vertexData).position(0);
				ShortBuffer indexBuffer = ByteBuffer.allocateDirect(indexData.length * 4)
						.order(ByteOrder.nativeOrder()).asShortBuffer();
				indexBuffer.put(indexData).position(0);
				int bufferIds[] = new int[2];
				GLES20.glGenBuffers(2, bufferIds, 0);
				mArrayBufferId = bufferIds[0];
				mElementBufferId = bufferIds[1];
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mArrayBufferId);
				GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, vertexBuffer,
						GLES20.GL_STATIC_DRAW);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mElementBufferId);
				GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexData.length * 4, indexBuffer, GLES20.GL_STATIC_DRAW);
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
				GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
			}
		}
	}

	private class EyeViewport {

		public float x;
		public float y;
		public float width;
		public float height;
		public float eyeX;
		public float eyeY;

		public String toString() {
			return "EyeViewport {x:" + this.x + " y:" + this.y + " width:" + this.width + " height:" + this.height + " eyeX: " + this.eyeX + " eyeY: " + this.eyeY + "}";
		}


	}

	private class ProgramHolder {

		public int program;
		public int aPosition;
		public int aVignette;
		public int aTextureCoord;
		public int uTextureCoordScale;
		public int uTextureSampler;

	}
}
