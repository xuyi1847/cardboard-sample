package com.google.vrtoolkit.cardboard;

import android.opengl.GLES20;

public class Viewport {

	public int x;
	public int y;
	public int width;
	public int height;

	public void setViewport(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void setGLViewport() {
		GLES20.glViewport(x, y, width, height);
	}

	public void setGLScissor() {
		GLES20.glScissor(x, y, width, height);
	}

	public void getAsArray(int array[], int offset) {
		if (offset + 4 > array.length) {
			throw new IllegalArgumentException(
					"Not enough space to write the result");
		} 
			array[offset] = x;
			array[offset + 1] = y;
			array[offset + 2] = width;
			array[offset + 3] = height;
		}

	public String toString() {
		return "Viewport {x:" + this.x + " y:" + this.y + " width:" + this.width + " height:" + this.height + "}";
	}
}
