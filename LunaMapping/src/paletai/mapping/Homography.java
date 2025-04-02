package paletai.mapping;

import processing.core.*;

/**
 * A class for applying homography transformations to points.
 * Wraps the mathematical operations from {@link MathHomography} 
 * with convenient point transformation methods.
 * 
 * <p>This class maintains both a homography matrix and its inverse,
 * allowing efficient forward and backward transformations.</p>
 * 
 * @author Daniel Corbani
 * @version 1.0
 * @see MathHomography
 */
public class Homography {

	/**
     * The 3x3 homography matrix
     */
	float[][] hh;
	
	/**
     * The inverse of the homography matrix
     */
	float[][] hhInv;
	
	/**
     * The math utility class for matrix operations
     */
	MathHomography mat;
	
	/**
     * Constructs a Homography object initialized with identity matrices.
     * Creates both the homography matrix and its inverse as identity matrices.
     */
	public Homography() {

		mat = new MathHomography();
		hh = new float[3][3];
		for (int i = 0; i < 3; i++) {
			hh[i][i] = 1;
		}

		hhInv = new float[3][3];
		for (int i = 0; i < 3; i++) {
			hhInv[i][i] = 1;
		}

	}
	
	/**
     * Updates the homography matrix and automatically calculates its inverse.
     * 
     * @param h The new 3x3 homography matrix to use
     */
	
	public void updateHomographyMatrix(float[][] h) {
		hh = mat.copyMatrix(h);
		hhInv = mat.invertMatrix(h);
	}
	
	/**
     * Transforms a point using the inverse homography matrix.
     * Applies the inverse perspective transformation: out = H⁻¹ * in.
     * 
     * @param in The input point to transform
     * @return The inverse-transformed point
     */
	public PVector vectorTransform(PVector in) {
		PVector out = new PVector(0, 0);

		float k = hh[2][0] * in.x + hh[2][1] * in.y + hh[2][2];
		float u = (hh[0][0] * in.x + hh[0][1] * in.y + hh[0][2]) / k;
		float v = (hh[1][0] * in.x + hh[1][1] * in.y + hh[1][2]) / k;

		out.set(u, v);
		return out;
	}

	public PVector vectorInvertTransform(PVector in) {
		PVector out = new PVector(0, 0);

		float k = hhInv[2][0] * in.x + hhInv[2][1] * in.y + hhInv[2][2];
		float u = (hhInv[0][0] * in.x + hhInv[0][1] * in.y + hhInv[0][2]) / k;
		float v = (hhInv[1][0] * in.x + hhInv[1][1] * in.y + hhInv[1][2]) / k;

		out.set(u, v);
		return out;
	}

}
