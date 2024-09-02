package paletai.mapping;

import processing.core.*;

public class Homography {

	// PApplet parent;

	float[][] hh;
	float[][] hhInv;

	MathHomography mat;

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

	public void updateHomographyMatrix(float[][] h) {
		hh = mat.copyMatrix(h);
		hhInv = mat.invertMatrix(h);
	}

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
