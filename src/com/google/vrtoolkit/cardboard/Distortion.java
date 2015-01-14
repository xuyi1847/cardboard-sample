package com.google.vrtoolkit.cardboard;

public class Distortion {

	private static final float DEFAULT_COEFFICIENTS[] = { 250F, 50000F };
	private float mCoefficients[];

	public Distortion() {
		mCoefficients = new float[2];
		mCoefficients[0] = DEFAULT_COEFFICIENTS[0];
		mCoefficients[1] = DEFAULT_COEFFICIENTS[1];
	}

	public Distortion(Distortion other) {
		mCoefficients = new float[2];
		mCoefficients[0] = other.mCoefficients[0];
		mCoefficients[1] = other.mCoefficients[1];
	}

	public void setCoefficients(float coefficients[]) {
		mCoefficients[0] = coefficients[0];
		mCoefficients[1] = coefficients[1];
	}

	public float[] getCoefficients() {
		return mCoefficients;
	}

	public float distortionFactor(float radius) {
		float rSq = radius * radius;
		return 1.0F + mCoefficients[0] * rSq + mCoefficients[1] * rSq * rSq;
	}

	public float distort(float radius) {
		return radius * distortionFactor(radius);
	}

	public float distortInverse(float radius) {
		float r0 = radius / 0.9F;
		float r1 = radius * 0.9F;
		float dr1;
		for (float dr0 = radius - distort(r0); (double) Math.abs(r1 - r0) > 0.0001D; dr0 = dr1) {
			dr1 = radius - distort(r1);
			float r2 = r1 - dr1 * ((r1 - r0) / (dr1 - dr0));
			r0 = r1;
			r1 = r2;
		}

		return r1;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof Distortion)) {
			return false;
		} else {
			Distortion o = (Distortion) other;
			return mCoefficients[0] == o.mCoefficients[0]
					&& mCoefficients[1] == o.mCoefficients[1];
		}
	}

	public String toString() {
		return "Distortion {" + this.mCoefficients[0] + ", " + this.mCoefficients[1] + "}";
	}

}
