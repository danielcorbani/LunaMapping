package paletai.mapping;

import processing.core.*;
import processing.opengl.*;

/**
 * A class for managing video mapping transformations with interactive
 * calibration. Handles homography calculations, shader applications, and
 * provides UI for point adjustment.
 * 
 * <p>
 * This class combines {@link MathHomography} calculations with Processing's
 * OpenGL rendering pipeline to create perspective-corrected video mappings. It
 * supports:
 * </p>
 * <ul>
 * <li>Interactive calibration with draggable control points</li>
 * <li>Real-time homography updates</li>
 * <li>Serialization of mapping configurations</li>
 * <li>Visual feedback during calibration</li>
 * </ul>
 * 
 * @author Daniel Corbani
 * @version 1.0
 * @see MathHomography
 * @see PShader
 */

public class VidMap {
	/** The parent Processing applet */
	PApplet p;
	/** Shader for applying homography transformations */
	PShader mapInOut;
	int resolutionX, resolutionY;
	/** Graphics buffers for input and output */
	PGraphics2D pgCanvas, pgInput;
	/** Normalized coordinates for shader (0-1 range) */
	public PVector[] xyN = new PVector[4]; // Normalized coordinates for Shader
	public PVector[] uvN = new PVector[4]; // Normalized coordinates for Shader
	/** Pixel coordinates for Processing display */
	public PVector[] xyP = new PVector[4]; // Pixel coordinates for Processing (for drawing)
	public PVector[] uvP = new PVector[4]; // Pixel coordinates for Processing (for drawing)
	/** Math utility for homography calculations */
	MathHomography mat;
	/** 3D matrix for shader transformations */
	PMatrix3D H;
	/** Calibration state flags */
	boolean calibrate = false;
	public boolean checkInput = false;
	/** Point interaction tracking */
	int hoverPoint = -1; // Point index to highlight when hovering
	int selectedPoint = -1; // New variable to store the selected point for live adjustment

	/** Unique identifier for this mapping */
	String objectName; // Unique name for the VidMap object
	/** Image dragging state */
	private boolean movingImage = false;
	private PVector initialMousePos;
	private PVector[] initialCorners;
	/** Preview area parameters */
	private float previewX, previewY, previewWidth, previewHeight;
	private boolean hasPreview = false;

	/**
	 * Constructs a new VidMap instance.
	 *
	 * @param p    The parent Processing applet
	 * @param name Unique identifier for this mapping
	 */
	public VidMap(PApplet p, String name) {
		PApplet.println("Initializing VidMap");
		this.p = p;
		this.objectName = name;
		mapInOut = p.loadShader("homography.glsl");
		assignToDisplay(1280, 720);
		mat = new MathHomography();
		resetHomography();
		PApplet.println("Finishing VidMap");
		// println("Finishing VidMap");
	}

	void assignToDisplay(int w, int h) {

		this.resolutionX = w;
		this.resolutionY = h;
		pgCanvas = (PGraphics2D) p.createGraphics(resolutionX, resolutionY, PConstants.P2D);
		pgInput = (PGraphics2D) p.createGraphics(resolutionX, resolutionY, PConstants.P2D);
		// resetHomography();
		mapInOut.set("resolution", resolutionX, resolutionY);
	}


	public void setPreviewArea(float px, float py, float pw, float ph) {
		this.previewX = px;
		this.previewY = py;
		this.previewWidth = pw;
		this.previewHeight = ph;
		this.hasPreview = true;
	}

	void assignToDisplay(PGraphics2D pgScreen) {
		assignToDisplay(pgScreen.width, pgScreen.height);
	}

	/**
	 * Resets the homography to identity transformation. Initializes all points to
	 * the corners of the display.
	 */
	public void resetHomography() {
		PApplet.println("resetHomography");
		// Initialize Processing points in pixel coordinates
		xyP[0] = new PVector(0, 0);
		xyP[1] = new PVector(resolutionX, 0);
		xyP[2] = new PVector(resolutionX, resolutionY);
		xyP[3] = new PVector(0, resolutionY);

		uvP[0] = new PVector(0, 0);
		uvP[1] = new PVector(resolutionX, 0);
		uvP[2] = new PVector(resolutionY, resolutionY);
		uvP[3] = new PVector(0, resolutionY);

		// Initialize normalized points for the shader (ensure xyN and uvN are assigned
		// properly)
		for (int i = 0; i < 4; i++) {
			xyN[i] = Pixel2Nornal(xyP[i]);
			uvN[i] = Pixel2Nornal(uvP[i]);
		}

		updateHomography(xyN, uvN);
	}

	/**
	 * Updates the homography matrix from pixel-space coordinates.
	 *
	 * @param xyPP Source points in pixel coordinates
	 * @param uvPP Destination points in pixel coordinates
	 * @throws IllegalArgumentException If arrays don't contain exactly 4 points
	 */
	public void updateHomographyFromPixel(PVector[] xyPP, PVector[] uvPP) {
		for (int i = 0; i < 4; i++) {
			xyP[i] = xyPP[i];
			uvP[i] = uvPP[i];
			xyN[i] = Pixel2Nornal(xyPP[i]);
			uvN[i] = Pixel2Nornal(uvPP[i]);
		}

		updateHomography(xyN, uvN);
	}

	/**
	 * Updates the homography transformation from normalized coordinates.
	 *
	 * @param xyNew Source points in normalized coordinates (0-1)
	 * @param uvNew Destination points in normalized coordinates (0-1)
	 */
	public void updateHomography(PVector[] xyNew, PVector[] uvNew) {

		for (int i = 0; i < uvN.length; i++) {
			String xyNum = "xy" + Integer.toString(i);
			mapInOut.set(xyNum, uvNew[i].x, uvNew[i].y); // input points set cropping mask
		}
		float[][] h = mat.calculateHomography(xyNew, uvNew); // get the homograhy matrix cconsidering the normalized
		// points
		float[][] hinv = mat.invertMatrix(h); // the OpenGl coordinates requires the inverse Homography matrix
		hinv = mat.transpose(hinv); // for some reason, it must be transposed
		H = mat.getMatrix(hinv); // it converts float[][] into the PMatrix#D for the OpenGL filter
		mapInOut.set("H", H, true); // true = use3x3
	}

	public void toggleInput() {
		checkInput = !checkInput;
		// System.out.println("checkInput = " + vidMap.checkInput);
	}

	private void makeGrid(PVector[] corners, boolean isInput) {
		int gridSize = 10; // Number of cells in the grid
		pgCanvas.stroke(0, 255, 0);
		if (isInput)
			pgCanvas.stroke(0, 0, 255);
		pgCanvas.strokeWeight(1);
		pgCanvas.noFill();

		// Interpolating horizontal and vertical grid lines
		for (int i = 0; i <= gridSize; i++) {
			float t = i / (float) gridSize;

			// Horizontal lines interpolation
			PVector startH = PVector.lerp(corners[0], corners[1], t);
			PVector endH = PVector.lerp(corners[3], corners[2], t);
			pgCanvas.line(startH.x, startH.y, endH.x, endH.y);

			// Vertical lines interpolation
			PVector startV = PVector.lerp(corners[0], corners[3], t);
			PVector endV = PVector.lerp(corners[1], corners[2], t);
			pgCanvas.line(startV.x, startV.y, endV.x, endV.y);
		}
	}

	public void render(PGraphics2D input) {
		if (pgCanvas == null) {
			// System.out.println("Initializing pgCanvas late...");
			pgCanvas = (PGraphics2D) p.createGraphics(resolutionX, resolutionY, PConstants.P2D);
		}
		pgCanvas.beginDraw();
		pgCanvas.image(input, 0, 0, pgCanvas.width, pgCanvas.height); // it should be offset to media.width/height

		if (calibrate) {
			makeGrid(xyP, checkInput); // Draw the green grid inside pgCanvas
		}
		pgCanvas.endDraw();

		if (!checkInput) {
			pgCanvas.filter(mapInOut);
		}

		if (calibrate && hasPreview) {
			drawCalibrationOnPreview();
		}
	}

	private void drawCalibrationOnPreview() {
		if (!checkInput) {
			// Draw output calibration (uvN points) on preview
			p.beginShape();
			p.stroke(0, 255, 0);
			p.strokeWeight(2);
			p.noFill();
			for (int i = 0; i < uvN.length; i++) {
				PVector previewPoint = normalizedToPreview(uvN[i]);
				if (i == hoverPoint) {
					p.fill(255, 0, 0);
					p.ellipse(previewPoint.x, previewPoint.y, 10, 10);
					p.noFill();
				}
				p.vertex(previewPoint.x, previewPoint.y);
			}
			p.endShape(PConstants.CLOSE);
		} else {
			// Draw input calibration (xyN points) on preview
			p.beginShape();
			p.stroke(0, 0, 255);
			p.strokeWeight(2);
			p.noFill();
			for (int i = 0; i < xyN.length; i++) {
				PVector previewPoint = normalizedToPreview(xyN[i]);
				if (i == hoverPoint) {
					p.fill(255, 0, 0);
					p.ellipse(previewPoint.x, previewPoint.y, 10, 10);
					p.noFill();
				}
				p.vertex(previewPoint.x, previewPoint.y);
			}
			p.endShape(PConstants.CLOSE);
		}
	}

	private PVector normalizedToPreview(PVector normalized) {
		float x = previewX + normalized.x * previewWidth;
		float y = previewY + (1 - normalized.y) * previewHeight; // Invert Y for preview
		return new PVector(x, y);
	}

	private PVector previewToNormalized(PVector previewPoint) {
		float x = (previewPoint.x - previewX) / previewWidth;
		float y = 1 - ((previewPoint.y - previewY) / previewHeight); // Invert Y conversion
		return new PVector(x, y);
	}

	private PVector previewToPixel(PVector previewPoint) {
		PVector normalized = previewToNormalized(previewPoint);
		return Nornal2Pixel(normalized);
	}

	// public void show(PImage input) {
	// pgInput.beginDraw();
	// pgInput.image(input, 0, 0, pgCanvas.width, pgCanvas.height);
	// pgInput.endDraw();

	// show(pgInput);
	// }

	public void render(PImage input) {
		pgInput.beginDraw();
		pgInput.image(input, 0, 0, pgCanvas.width, pgCanvas.height);
		pgInput.endDraw();

		render(pgInput);
	}

	public PGraphics2D getMediaCanvas() {
		return pgCanvas;
	}

	/**
	 * Converts pixel coordinates to normalized shader coordinates.
	 *
	 * @param in Input point in pixel coordinates
	 * @return Point in normalized coordinates (0-1, Y inverted)
	 */
	public PVector Pixel2Nornal(PVector in) {
		return new PVector(in.x / pgCanvas.width, 1.0f - (in.y / pgCanvas.height)); // Normalize and invert Y-axis for
																					// shader
	}

	/**
	 * Converts normalized coordinates back to pixel space.
	 *
	 * @param in Input point in normalized coordinates
	 * @return Point in pixel coordinates
	 */
	public PVector Nornal2Pixel(PVector in) {
		return new PVector(in.x * pgCanvas.width, (1.0f - in.y) * pgCanvas.height); // Convert back to Processing
																					// coordinates
	}

	/**
	 * Toggles calibration mode on/off.
	 */
	public void toggleCalibration() {
		calibrate = !calibrate;
		// System.out.println("calibrate " + objectName + "= " + calibrate);
	}

	public void offCalibration() {
		calibrate = false;
		// System.out.println("calibrate " + objectName + "= " + calibrate);
	}

	public void onCalibration() {
		calibrate = true;
		// System.out.println("calibrate " + objectName + "= " + calibrate);
	}

	// Helper method to check if mouse is inside the quadrilateral formed by uvP[]
	private boolean isMouseInsideImage(PVector mouse, PVector[] cc) {
		float minX = Math.min(Math.min(cc[0].x, cc[1].x), Math.min(cc[2].x, cc[3].x));
		float maxX = Math.max(Math.max(cc[0].x, cc[1].x), Math.max(cc[2].x, cc[3].x));
		float minY = Math.min(Math.min(cc[0].y, cc[1].y), Math.min(cc[2].y, cc[3].y));
		float maxY = Math.max(Math.max(cc[0].y, cc[1].y), Math.max(cc[2].y, cc[3].y));

		return mouse.x > minX && mouse.x < maxX && mouse.y > minY && mouse.y < maxY;
	}

	public void checkHover(float mousex, float mousey) {
		PVector mouse = new PVector(mousex, mousey);
		hoverPoint = -1;
		movingImage = false;

		if (calibrate && hasPreview) {
			// Check if mouse is within preview area
			if (mousex >= previewX && mousex <= previewX + previewWidth && mousey >= previewY
					&& mousey <= previewY + previewHeight) {

				if (!checkInput) {
					// Check corners in preview space
					for (int i = 0; i < uvN.length; i++) {
						PVector previewPoint = normalizedToPreview(uvN[i]);
						float dist = PVector.dist(mouse, previewPoint);
						if (dist < 10) {
							hoverPoint = i;
							break;
						}
					}
					// Check if clicking inside the image in preview
					PVector[] previewCorners = new PVector[4];
					for (int i = 0; i < 4; i++) {
						previewCorners[i] = normalizedToPreview(uvN[i]);
					}
					if (isMouseInsideImage(mouse, previewCorners)) {
						movingImage = true;
						initialMousePos = new PVector(mouse.x, mouse.y);
						initialCorners = new PVector[4];
						for (int i = 0; i < 4; i++) {
							initialCorners[i] = uvP[i].copy();
						}
					}
				} else {
					// Similar logic for input mode
					for (int i = 0; i < xyN.length; i++) {
						PVector previewPoint = normalizedToPreview(xyN[i]);
						float dist = PVector.dist(mouse, previewPoint);
						if (dist < 10) {
							hoverPoint = i;
							break;
						}
					}
					PVector[] previewCorners = new PVector[4];
					for (int i = 0; i < 4; i++) {
						previewCorners[i] = normalizedToPreview(xyN[i]);
					}
					if (isMouseInsideImage(mouse, previewCorners)) {
						movingImage = true;
						initialMousePos = new PVector(mouse.x, mouse.y);
						initialCorners = new PVector[4];
						for (int i = 0; i < 4; i++) {
							initialCorners[i] = xyP[i].copy();
						}
					}
				}
			}
		}
	}

	public void moveHoverPoint(float x, float y) {
		if (calibrate && hasPreview) {
			PVector mouse = new PVector(x, y);

			if (!checkInput) {
				if (hoverPoint != -1) {
					// Convert preview coordinates back to pixel coordinates
					PVector pixelPoint = previewToPixel(mouse);
					uvP[hoverPoint] = pixelPoint;
					uvN[hoverPoint] = Pixel2Nornal(uvP[hoverPoint]);
					updateHomography(xyN, uvN);
				} else if (movingImage) {
					PVector delta = new PVector(x - initialMousePos.x, y - initialMousePos.y);
					for (int i = 0; i < 4; i++) {
						PVector initialPreview = normalizedToPreview(Pixel2Nornal(initialCorners[i]));
						PVector newPreview = PVector.add(initialPreview, delta);
						PVector newPixel = previewToPixel(newPreview);
						uvP[i] = newPixel;
						uvN[i] = Pixel2Nornal(uvP[i]);
					}
					updateHomography(xyN, uvN);
				}
			} else {
				// Similar logic for input mode
				if (hoverPoint != -1) {
					PVector pixelPoint = previewToPixel(mouse);
					xyP[hoverPoint] = pixelPoint;
					xyN[hoverPoint] = Pixel2Nornal(xyP[hoverPoint]);
					updateHomography(xyN, uvN);
				} else if (movingImage) {
					PVector delta = new PVector(x - initialMousePos.x, y - initialMousePos.y);
					for (int i = 0; i < 4; i++) {
						PVector initialPreview = normalizedToPreview(Pixel2Nornal(initialCorners[i]));
						PVector newPreview = PVector.add(initialPreview, delta);
						PVector newPixel = previewToPixel(newPreview);
						xyP[i] = newPixel;
						xyN[i] = Pixel2Nornal(xyP[i]);
					}
					updateHomography(xyN, uvN);
				}
			}
		}
	}

	public void mouseReleased() {
		movingImage = false;
	}
}