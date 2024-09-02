package paletai.mapping;

import processing.core.PVector;
import processing.core.PMatrix3D;

public class MathHomography {

	float[][] hh;

	public MathHomography() {
		hh = new float[3][3];
		for (int i = 0; i < 3; i++) {
			hh[i][i] = 1;
		}
	}

	public float[][] calculateHomography(PVector[] xy, PVector[] uv, int w, int h) {
		float x0 = xy[0].x / w;
		float y0 = xy[0].y / h;
		float u0 = uv[0].x / w;
		float v0 = uv[0].y / h;

		float x1 = xy[1].x / w;
		float y1 = xy[1].y / h;
		float u1 = uv[1].x / w;
		float v1 = uv[1].y / h;

		float x2 = xy[2].x / w;
		float y2 = xy[2].y / h;
		float u2 = uv[2].x / w;
		float v2 = uv[2].y / h;

		float x3 = xy[3].x / w;
		float y3 = xy[3].y / h;
		float u3 = uv[3].x / w;
		float v3 = uv[3].y / h;

		float[] p0 = { -x0, -y0, -1, 0, 0, 0, x0 * u0, y0 * u0, u0, 0 };
		float[] p1 = { 0, 0, 0, -x0, -y0, -1, x0 * v0, y0 * v0, v0, 0 };
		float[] p2 = { -x1, -y1, -1, 0, 0, 0, x1 * u1, y1 * u1, u1, 0 };
		float[] p3 = { 0, 0, 0, -x1, -y1, -1, x1 * v1, y1 * v1, v1, 0 };
		float[] p4 = { -x2, -y2, -1, 0, 0, 0, x2 * u2, y2 * u2, u2, 0 };
		float[] p5 = { 0, 0, 0, -x2, -y2, -1, x2 * v2, y2 * v2, v2, 0 };
		float[] p6 = { -x3, -y3, -1, 0, 0, 0, x3 * u3, y3 * u3, u3, 0 };
		float[] p7 = { 0, 0, 0, -x3, -y3, -1, x3 * v3, y3 * v3, v3, 0 };
		float[] p8 = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 };

		float[][] H = new float[9][9];

		H[0] = p0;
		H[1] = p1;
		H[2] = p2;
		H[3] = p3;
		H[4] = p4;
		H[5] = p5;
		H[6] = p6;
		H[7] = p7;
		H[8] = p8;

		H = GaussJordan(H);
		int end = H[0].length - 1;
		hh[0][0] = H[0][end];
		hh[0][1] = H[1][end];
		hh[0][2] = H[2][end];
		hh[1][0] = H[3][end];
		hh[1][1] = H[4][end];
		hh[1][2] = H[5][end];
		hh[2][0] = H[6][end];
		hh[2][1] = H[7][end];
		hh[2][2] = H[8][end];

		return hh;
	}

	public float[][] calculateHomography(PVector[] xy, PVector[] uv) {
		float x0 = xy[0].x;
		float y0 = xy[0].y;
		float u0 = uv[0].x;
		float v0 = uv[0].y;

		float x1 = xy[1].x;
		float y1 = xy[1].y;
		float u1 = uv[1].x;
		float v1 = uv[1].y;

		float x2 = xy[2].x;
		float y2 = xy[2].y;
		float u2 = uv[2].x;
		float v2 = uv[2].y;

		float x3 = xy[3].x;
		float y3 = xy[3].y;
		float u3 = uv[3].x;
		float v3 = uv[3].y;

		float[] p0 = { -x0, -y0, -1, 0, 0, 0, x0 * u0, y0 * u0, u0, 0 };
		float[] p1 = { 0, 0, 0, -x0, -y0, -1, x0 * v0, y0 * v0, v0, 0 };
		float[] p2 = { -x1, -y1, -1, 0, 0, 0, x1 * u1, y1 * u1, u1, 0 };
		float[] p3 = { 0, 0, 0, -x1, -y1, -1, x1 * v1, y1 * v1, v1, 0 };
		float[] p4 = { -x2, -y2, -1, 0, 0, 0, x2 * u2, y2 * u2, u2, 0 };
		float[] p5 = { 0, 0, 0, -x2, -y2, -1, x2 * v2, y2 * v2, v2, 0 };
		float[] p6 = { -x3, -y3, -1, 0, 0, 0, x3 * u3, y3 * u3, u3, 0 };
		float[] p7 = { 0, 0, 0, -x3, -y3, -1, x3 * v3, y3 * v3, v3, 0 };
		float[] p8 = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 };

		float[][] H = new float[9][9];

		H[0] = p0;
		H[1] = p1;
		H[2] = p2;
		H[3] = p3;
		H[4] = p4;
		H[5] = p5;
		H[6] = p6;
		H[7] = p7;
		H[8] = p8;

		H = GaussJordan(H);
		int end = H[0].length - 1;
		hh[0][0] = H[0][end];
		hh[0][1] = H[1][end];
		hh[0][2] = H[2][end];
		hh[1][0] = H[3][end];
		hh[1][1] = H[4][end];
		hh[1][2] = H[5][end];
		hh[2][0] = H[6][end];
		hh[2][1] = H[7][end];
		hh[2][2] = H[8][end];

		return hh;
	}

	public float[][] GaussJordan(float[][] m) {
		float[][] out = copyMatrix(m);
		for (int i = 0; i < out.length; i++) {
			out = findNextNonZeroLine(out, i);
			out = normLine(out, i);
			out = zeroNextLines(out, i);
		}
		for (int i = out.length - 1; i >= 0; i--) {
			out = zeroPreviousLines(out, i);
		}

		return out;
	}

	public float[][] copyMatrix(float[][] m) {
		float[][] out = new float[m.length][m[0].length];
		for (int i = 0; i < out.length; i++) {
			for (int j = 0; j < out[0].length; j++) {
				out[i][j] = m[i][j];
			}
		}
		return out;
	}

	private float[][] findNextNonZeroLine(float[][] m, int k) {
		float num = m[k][k];
		float[][] out = copyMatrix(m);
		if (num == 0) {
			for (int i = k + 1; i < out.length; i++) {
				float aux = out[i][k];
				if (aux != 0) {
					out = swapLines(out, k, i);
					break;
				}
			}
		}
		return out;
	}

	private float[][] swapLines(float[][] m, int a, int b) {
		float[][] out = copyMatrix(m);
		float[] pa = m[a];
		float[] pb = m[b];
		out[a] = pb;
		out[b] = pa;
		return out;
	}

	private float[][] normLine(float[][] m, int k) {
		float[][] out = copyMatrix(m);
		float pivot = out[k][k];
		for (int i = k; i < out[k].length; i++) {
			out[k][i] /= pivot;
		}
		return out;
	}

	private float[][] zeroNextLines(float[][] m, int k) {
		float[][] out = copyMatrix(m);
		for (int i = k + 1; i < out.length; i++) {
			float[] p = out[i];
			float pivot = p[k];
			for (int j = k; j < p.length; j++) {
				p[j] -= pivot * out[k][j];
			}
			out[i] = p;
		}
		return out;
	}

	private float[][] zeroPreviousLines(float[][] m, int k) { // k = line
		float[][] out = copyMatrix(m);
		for (int i = k - 1; i >= 0; i--) { // starts at k-1, the line up
			float[] p = out[i]; // the line to be zeroed
			float pivot = p[k]; // the pivot
			for (int j = p.length - 1; j >= 0; j--) {
				p[j] -= pivot * out[k][j];
			}
			out[i] = p;
		}
		return out;
	}

	public float[][] invertMatrix(float[][] m) {
		float[][] in = copyMatrix(m);
		float[][] id = new float[in.length][in[0].length];
		for (int i = 0; i < id.length; i++)
			id[i][i] = 1;
		float[][] aux = GaussJordan(appendMatrix(in, id));
		float[][] out = disappendMatrix(aux, in[0].length);
		return out;
	}

	public float[][] transpose(float[][] in) {
		float[][] out = new float[in[0].length][in.length];
		for (int row = 0; row < in.length; row++) {
			for (int col = 0; col < in[0].length; col++) {
				out[row][col] = in[col][row];
			}
		}
		return out;
	}

	private float[][] appendMatrix(float[][] a, float[][] b) {
		float[][] out = new float[a.length][a[0].length + b[0].length];
		for (int row = 0; row < a.length; row++) {
			for (int col = 0; col < a[0].length; col++) {
				out[row][col] = a[row][col];
			}
			for (int col = 0; col < b[0].length; col++) {
				out[row][col + a[0].length] = b[row][col];
			}
		}
		return out;
	}

	private float[][] disappendMatrix(float[][] a, int cols) {
		float[][] out = new float[a.length][cols];
		for (int row = 0; row < a.length; row++) {
			for (int col = 0; col < cols; col++) {
				out[row][col] = a[row][col + cols];
			}
		}
		return out;
	}

	public PMatrix3D getMatrix(float[][] hinv) {
		PMatrix3D H = new PMatrix3D();
		float h00 = hinv[0][0];
		float h01 = hinv[0][1];
		float h02 = hinv[0][2];
		float h10 = hinv[1][0];
		float h11 = hinv[1][1];
		float h12 = hinv[1][2];
		float h20 = hinv[2][0];
		float h21 = hinv[2][1];
		float h22 = hinv[2][2];
		H.set(h00, h01, h02, 0,
		      h10, h11, h12, 0,
		      h20, h21, h22, 0,
		      0, 0, 0, 1);
		return H;

	}
	
	public PMatrix3D getMatrixInOut(PVector[] xy, PVector[] uv) {
		float[][] h = calculateHomography(xy, uv);
		h = invertMatrix(h); //the OpenGl coordinates requires the inverse Homography matrix
		h = transpose(h);
		PMatrix3D H = new PMatrix3D();
		float h00 = h[0][0];
		float h01 = h[0][1];
		float h02 = h[0][2];
		float h10 = h[1][0];
		float h11 = h[1][1];
		float h12 = h[1][2];
		float h20 = h[2][0];
		float h21 = h[2][1];
		float h22 = h[2][2];
		H.set(h00, h01, h02, 0,
		      h10, h11, h12, 0,
		      h20, h21, h22, 0,
		      0, 0, 0, 1);
		return H;

	}
	
	
}
