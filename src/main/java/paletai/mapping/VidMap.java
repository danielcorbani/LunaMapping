package paletai.mapping;

import processing.core.*;
import processing.opengl.*;

/**
 * Handles homography transformations for video mapping with interactive calibration.
 * Provides UI for point adjustment and real-time perspective correction.
 *
 * <p>This class combines {@link MathHomography} calculations with Processing's
 * OpenGL rendering pipeline to create perspective-corrected video mappings. It
 * supports:</p>
 * <ul>
 * <li>Interactive calibration with draggable control points</li>
 * <li>Real-time homography updates</li>
 * <li>Serialization of mapping configurations</li>
 * <li>Visual feedback during calibration</li>
 * <li>Preview area integration for UI display</li>
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

    /** Display resolution for this mapping */
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
     * Initializes the homography shader, display buffers, and mathematical utilities.
     *
     * @param p The parent Processing applet
     * @param name Unique identifier for this mapping
     * @see #assignToDisplay(int, int)
     * @see #resetHomography()
     */
	public VidMap(PApplet p, String name) {
		//PApplet.println("Initializing VidMap");
		this.p = p;
		this.objectName = name;
		mapInOut = p.loadShader("homography.glsl");
		assignToDisplay(1280, 720);
		mat = new MathHomography();
		resetHomography();
		//PApplet.println("Finishing VidMap");
		// println("Finishing VidMap");
	}

    /**
     * Assigns this VidMap to a display with specific dimensions.
     * Creates graphics buffers and configures the shader for the target resolution.
     *
     * @param w The width of the target display
     * @param h The height of the target display
     * @see PGraphics2D
     * @see PShader#set(String, float, float)
     */
	public void assignToDisplay(int w, int h) {

		this.resolutionX = w;
		this.resolutionY = h;
		//if (pgCanvas != null) pgCanvas.dispose();
		//if (pgInput != null) pgInput.dispose();
		pgCanvas = (PGraphics2D) p.createGraphics(resolutionX, resolutionY, PConstants.P2D);
		pgInput = (PGraphics2D) p.createGraphics(resolutionX, resolutionY, PConstants.P2D);
		mapInOut.set("resolution", resolutionX, resolutionY);
	}

    /**
     * Sets the preview area for calibration point display.
     * Defines the screen region where calibration points and grids will be rendered.
     *
     * @param px Preview area x-coordinate
     * @param py Preview area y-coordinate
     * @param pw Preview area width
     * @param ph Preview area height
     */
	public void setPreviewArea(float px, float py, float pw, float ph) {
		this.previewX = px;
		this.previewY = py;
		this.previewWidth = pw;
		this.previewHeight = ph;
		this.hasPreview = true;
	}

    /**
     * Assigns this VidMap to a display using an existing PGraphics buffer.
     * Uses the dimensions of the provided graphics buffer for configuration.
     *
     * @param pgScreen The PGraphics2D buffer to use for display assignment
     * @see #assignToDisplay(int, int)
     */
	void assignToDisplay(PGraphics2D pgScreen) {
		assignToDisplay(pgScreen.width, pgScreen.height);
	}

    /**
     * Resets the homography to identity transformation.
     * Initializes all points to the corners of the display and updates
     * the homography matrix accordingly.
     *
     * @see #updateHomography(PVector[], PVector[])
     * @see #Pixel2Nornal(PVector)
     */
	public void resetHomography() {
		//PApplet.println("resetHomography");
		// Initialize Processing points in pixel coordinates
		xyP[0] = new PVector(0, 0);
		xyP[1] = new PVector(resolutionX, 0);
		xyP[2] = new PVector(resolutionX, resolutionY);
		xyP[3] = new PVector(0, resolutionY);

		uvP[0] = new PVector(0, 0);
		uvP[1] = new PVector(resolutionX, 0);
		uvP[2] = new PVector(resolutionX, resolutionY);
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
     * Converts pixel coordinates to normalized coordinates and updates
     * the transformation accordingly.
     *
     * @param xyPP Source points in pixel coordinates
     * @param uvPP Destination points in pixel coordinates
     * @throws IllegalArgumentException If arrays don't contain exactly 4 points
     * @see #Pixel2Nornal(PVector)
     * @see #updateHomography(PVector[], PVector[])
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
     * Calculates the homography matrix and its inverse, then configures
     * the shader with the transformed coordinates.
     *
     * @param xyNew Source points in normalized coordinates (0-1)
     * @param uvNew Destination points in normalized coordinates (0-1)
     * @see MathHomography#calculateHomography(PVector[], PVector[])
     * @see MathHomography#invertMatrix(float[][])
     * @see MathHomography#transpose(float[][])
     * @see PShader#set(String, float, float)
     */
	public void updateHomography(PVector[] xyNew, PVector[] uvNew) {
		for (int i = 0; i < 4; i++) {
			xyP[i] = Normal2Pixel(xyNew[i]);
			uvP[i] = Normal2Pixel(uvNew[i]);
            xyN[i] = Pixel2Nornal(xyP[i]);
            uvN[i] = Pixel2Nornal(uvP[i]);
		}

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

    /**
     * Toggles input checking mode for calibration.
     * Switches between input (source) and output (destination) point manipulation.
     */
	public void toggleInput() {
		checkInput = !checkInput;
		// System.out.println("checkInput = " + vidMap.checkInput);
	}

    /**
     * Draws a calibration grid on the specified corners.
     * Creates a grid visualization to help with homography point alignment.
     *
     * @param corners The four corner points defining the grid area
     * @param isInput Whether this is an input (source) grid (affects color)
     */
	private void makeGrid(PVector[] corners, boolean isInput) {
		int gridSize = 10; // Number of cells in the grid
		pgCanvas.stroke(0, 255, 0);
		if (isInput)
			pgCanvas.stroke(0, 0, 255);
		pgCanvas.strokeWeight(3);
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

    /**
     * Renders the input image with homography transformation applied.
     * Processes the input through the homography shader and draws calibration
     * visuals if calibration mode is active.
     *
     * @param input The input graphics to transform
     * @see #makeGrid(PVector[], boolean)
     * @see #drawCalibrationOnPreview()
     * @see PGraphics#filter(PShader)
     */
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

    /**
     * Draws calibration points and shapes on the preview area.
     * Visualizes the current homography points in the UI preview with
     * different colors for input and output points.
     *
     * @see #normalizedToPreview(PVector)
     */
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
			p.strokeWeight(4);
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

    /**
     * Converts normalized coordinates to preview screen coordinates.
     *
     * @param normalized Normalized coordinates (0-1 range)
     * @return Preview coordinates in screen space
     */
	private PVector normalizedToPreview(PVector normalized) {
		float x = previewX + normalized.x * previewWidth;
		float y = previewY + (1 - normalized.y) * previewHeight; // Invert Y for preview
		return new PVector(x, y);
	}

    /**
     * Converts preview screen coordinates to normalized coordinates.
     *
     * @param previewPoint Preview coordinates in screen space
     * @return Normalized coordinates (0-1 range)
     */
	private PVector previewToNormalized(PVector previewPoint) {
		float x = (previewPoint.x - previewX) / previewWidth;
		float y = 1 - ((previewPoint.y - previewY) / previewHeight); // Invert Y conversion
		return new PVector(x, y);
	}

    /**
     * Converts preview screen coordinates to pixel coordinates.
     *
     * @param previewPoint Preview coordinates in screen space
     * @return Pixel coordinates in display space
     * @see #previewToNormalized(PVector)
     * @see #Normal2Pixel(PVector)
     */
	private PVector previewToPixel(PVector previewPoint) {
		PVector normalized = previewToNormalized(previewPoint);
		return Normal2Pixel(normalized);
	}

    /**
     * Renders a PImage with homography transformation applied.
     * Wrapper method that converts PImage to PGraphics2D before processing.
     *
     * @param input The input image to transform
     * @see #render(PGraphics2D)
     */
	public void render(PImage input) {
		pgInput.beginDraw();
		pgInput.image(input, 0, 0, pgCanvas.width, pgCanvas.height);
		pgInput.endDraw();

		render(pgInput);
	}

    /**
     * Returns the transformed media canvas after homography processing.
     *
     * @return PGraphics2D containing the transformed output
     */
	public PGraphics2D getMediaCanvas() {
		return pgCanvas;
	}

    /**
     * Converts pixel coordinates to normalized shader coordinates.
     * Normalizes coordinates to 0-1 range and inverts Y-axis for shader compatibility.
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
     * Denormalizes coordinates and inverts Y-axis back to Processing coordinate system.
     *
     * @param in Input point in normalized coordinates
     * @return Point in pixel coordinates
     */
	public PVector Normal2Pixel(PVector in) {
		return new PVector(in.x * pgCanvas.width, (1.0f - in.y) * pgCanvas.height); // Convert back to Processing
																					// coordinates
	}

    /**
     * Toggles calibration mode on/off.
     * Enables or disables the interactive calibration interface.
     */
	public void toggleCalibration() {
		calibrate = !calibrate;
		// System.out.println("calibrate " + objectName + "= " + calibrate);
	}

    /**
     * Turns off calibration mode.
     * Disables the interactive calibration interface.
     */
	public void offCalibration() {
		calibrate = false;
		// System.out.println("calibrate " + objectName + "= " + calibrate);
	}

    /**
     * Turns on calibration mode.
     * Enables the interactive calibration interface.
     */
	public void onCalibration() {
		calibrate = true;
		// System.out.println("calibrate " + objectName + "= " + calibrate);
	}

    /**
     * Checks if mouse position is inside the quadrilateral formed by the given corners.
     * Uses bounding box approximation for quick containment check.
     *
     * @param mouse The mouse position to check
     * @param cc The four corner points defining the quadrilateral
     * @return true if mouse is within the bounding box of the corners
     */
	private boolean isMouseInsideImage(PVector mouse, PVector[] cc) {
		float minX = Math.min(Math.min(cc[0].x, cc[1].x), Math.min(cc[2].x, cc[3].x));
		float maxX = Math.max(Math.max(cc[0].x, cc[1].x), Math.max(cc[2].x, cc[3].x));
		float minY = Math.min(Math.min(cc[0].y, cc[1].y), Math.min(cc[2].y, cc[3].y));
		float maxY = Math.max(Math.max(cc[0].y, cc[1].y), Math.max(cc[2].y, cc[3].y));

		return mouse.x > minX && mouse.x < maxX && mouse.y > minY && mouse.y < maxY;
	}

    /**
     * Checks hover state for calibration points and image dragging.
     * Determines if mouse is hovering over calibration points or inside the
     * transformable image area in the preview.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see #normalizedToPreview(PVector)
     * @see #isMouseInsideImage(PVector, PVector[])
     */
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

    /**
     * Moves hover points or drags the entire image during calibration.
     * Updates homography points based on mouse movement in preview space.
     * Supports individual point movement and whole-image translation.
     *
     * @param x The current x-coordinate of the mouse
     * @param y The current y-coordinate of the mouse
     * @see #previewToPixel(PVector)
     * @see #Pixel2Nornal(PVector)
     * @see #updateHomography(PVector[], PVector[])
     */
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

    /**
     * Handles mouse release events for calibration interaction.
     * Completes dragging operations and resets movement states.
     */
	public void mouseReleased() {
		movingImage = false;
	}


}