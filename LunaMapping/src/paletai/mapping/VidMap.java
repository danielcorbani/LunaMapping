package paletai.mapping;

import processing.core.*;
import processing.opengl.*;
import processing.data.XML;

public class VidMap {
	PApplet p;
	PShader mapInOut;
	PGraphics2D pgCanvas, pgInput;

	PVector[] xyN = new PVector[4]; // Normalized coordinates for Shader
	PVector[] uvN = new PVector[4]; // Normalized coordinates for Shader
	PVector[] xyP = new PVector[4]; // Pixel coordinates for Processing (for drawing)
	PVector[] uvP = new PVector[4]; // Pixel coordinates for Processing (for drawing)

	MathHomography mat;
	PMatrix3D H;

	boolean calibrate = false;
	int hoverPoint = -1; // Point index to highlight when hovering
	int selectedPoint = -1; // New variable to store the selected point for live adjustment

	String objectName; // Unique name for the VidMap object

	private boolean movingImage = false;
	private PVector initialMousePos;
	private PVector[] initialCorners;

	public VidMap(PApplet p, String name) {
		this.p = p;
		this.objectName = name;
		pgCanvas = (PGraphics2D) p.createGraphics(p.width, p.height, PConstants.P2D);
		pgInput = (PGraphics2D) p.createGraphics(p.width, p.height, PConstants.P2D);
		mapInOut = p.loadShader("homography.glsl");
		mapInOut.set("resolution", p.width, p.height);

		mat = new MathHomography();
		resetHomography();
	}

	public void resetHomography() {
		// Initialize Processing points in pixel coordinates
		xyP[0] = new PVector(0, 0);
		xyP[1] = new PVector(p.width, 0);
		xyP[2] = new PVector(p.width, p.height);
		xyP[3] = new PVector(0, p.height);

		uvP[0] = new PVector(0, 0);
		uvP[1] = new PVector(p.width, 0);
		uvP[2] = new PVector(p.width, p.height);
		uvP[3] = new PVector(0, p.height);

		// Initialize normalized points for the shader (ensure xyN and uvN are assigned
		// properly)
		for (int i = 0; i < 4; i++) {
			xyN[i] = Pixel2Nornal(xyP[i]);
			uvN[i] = Pixel2Nornal(uvP[i]);
		}

		// mat = new MathHomography();
		updateHomography(xyN, uvN);
	}

	public void updateHomographyFromPixel(PVector[] xyPP, PVector[] uvPP) {
		for (int i = 0; i < 4; i++) {
			xyP[i] = xyPP[i];
			uvP[i] = uvPP[i];
			xyN[i] = Pixel2Nornal(xyPP[i]);
			uvN[i] = Pixel2Nornal(uvPP[i]);
		}

		// mat = new MathHomography();
		updateHomography(xyN, uvN);
	}

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

	public void show(PGraphics2D input) {
		pgCanvas.beginDraw();
		pgCanvas.image(input, 0, 0, pgCanvas.width, pgCanvas.height);

		if (calibrate) {
			// Draw the green grid inside pgCanvas
			int gridSize = 10; // Number of cells in the grid
			pgCanvas.stroke(0, 255, 0);
			pgCanvas.strokeWeight(1);
			pgCanvas.noFill();

			for (int i = 0; i <= gridSize; i++) {
				float x = i * (pgCanvas.width / (float) gridSize);
				pgCanvas.line(x, 0, x, pgCanvas.height); // Vertical lines
				float y = i * (pgCanvas.height / (float) gridSize);
				pgCanvas.line(0, y, pgCanvas.width, y); // Horizontal lines
			}
		}

		pgCanvas.endDraw();
		pgCanvas.filter(mapInOut);
		p.image(pgCanvas, 0, 0);

		if (calibrate) {
			// Highlight the corners on the main canvas
			p.beginShape();
			p.stroke(0, 255, 0);
			p.strokeWeight(2);
			p.noFill();
			for (int i = 0; i < uvN.length; i++) {
				if (i == hoverPoint) {
					p.fill(255, 0, 0); // Highlight hovered point
					p.ellipse(p.width * uvN[i].x, p.height * (1 - uvN[i].y), 10, 10);
					p.noFill();
				}
				p.vertex(p.width * uvN[i].x, p.height * (1 - uvN[i].y));
			}
			p.endShape(PConstants.CLOSE);
		}
	}

	public void show(PImage input) {
		pgInput.beginDraw();
		pgInput.image(input, 0, 0, pgCanvas.width, pgCanvas.height);
		pgInput.endDraw();

		show(pgInput);
	}

	public PGraphics2D getMediaCanvas() {
		return pgCanvas;
	}

	public PVector Pixel2Nornal(PVector in) {
		return new PVector(in.x / p.width, 1.0f - (in.y / p.height)); // Normalize and invert Y-axis for shader
	}

	public PVector Nornal2Pixel(PVector in) {
		return new PVector(in.x * p.width, (1.0f - in.y) * p.height); // Convert back to Processing coordinates
	}

	public void toggleCalibration() {
		calibrate = !calibrate;
		System.out.println("calibrate " + objectName + "= " + calibrate);
	}

	// Function to check if mouse is near any point and set the hoverPoint
	public void checkHover(float x, float y) {
		PVector mouse = new PVector(x, y); // Use Processing coordinates for checking hover
		hoverPoint = -1; // Reset hover point
		movingImage = false;

		if (calibrate) {
			for (int i = 0; i < uvP.length; i++) {
				float dist = PVector.dist(mouse, uvP[i]);
				if (dist < 10) { // Set hover if within a certain distance threshold
					hoverPoint = i;
					break;
				}
			}
			// Check if clicking inside the image
			if (isMouseInsideImage(mouse)) {
				movingImage = true;
				initialMousePos = new PVector(x, y);
				initialCorners = new PVector[4];
				for (int i = 0; i < 4; i++) {
					initialCorners[i] = uvP[i].copy(); // Store initial corners
				}
			}
		}
	}

	// Helper method to check if mouse is inside the quadrilateral formed by uvP[]
	private boolean isMouseInsideImage(PVector mouse) {
	    float minX = Math.min(Math.min(uvP[0].x, uvP[1].x), Math.min(uvP[2].x, uvP[3].x));
	    float maxX = Math.max(Math.max(uvP[0].x, uvP[1].x), Math.max(uvP[2].x, uvP[3].x));
	    float minY = Math.min(Math.min(uvP[0].y, uvP[1].y), Math.min(uvP[2].y, uvP[3].y));
	    float maxY = Math.max(Math.max(uvP[0].y, uvP[1].y), Math.max(uvP[2].y, uvP[3].y));

	    return mouse.x > minX && mouse.x < maxX && mouse.y > minY && mouse.y < maxY;
	}
	
	// Function to move the hovered point when dragging
	public void moveHoverPoint(float x, float y) {
	    if (hoverPoint != -1) {
	        uvP[hoverPoint] = new PVector(x, y); // Update in Processing coordinates
	        uvN[hoverPoint] = Pixel2Nornal(uvP[hoverPoint]); // Convert to normalized coordinates for the shader
	        updateHomography(xyN, uvN);
	    } else if (movingImage) {
	        PVector delta = new PVector(x - initialMousePos.x, y - initialMousePos.y);
	        for (int i = 0; i < 4; i++) {
	            uvP[i] = PVector.add(initialCorners[i], delta);
	            uvN[i] = Pixel2Nornal(uvP[i]);
	        }
	        updateHomography(xyN, uvN);
	    }
	}


	public void mouseReleased() {
	    movingImage = false;
	}

	public void save() {
		try {
			XML root = p.loadXML("data/homography.xml");
			if (root == null) {
				root = new XML("homographies");
			}

			// Check if an entry for this object already exists
			XML objectNode = root.getChild(objectName);
			if (objectNode != null) {
				root.removeChild(objectNode);
			}

			// Create a new node for this object
			objectNode = root.addChild(objectName);

			// Save xyP points
			XML xyPNode = objectNode.addChild("xyP");
			for (PVector point : xyP) {
				XML pointNode = xyPNode.addChild("point");
				pointNode.setFloat("x", point.x);
				pointNode.setFloat("y", point.y);
			}

			// Save uvP points
			XML uvPNode = objectNode.addChild("uvP");
			for (PVector point : uvP) {
				XML pointNode = uvPNode.addChild("point");
				pointNode.setFloat("x", point.x);
				pointNode.setFloat("y", point.y);
			}

			// Save the XML file
			p.saveXML(root, "data/homography.xml");
			PApplet.println("Homography for " + objectName + " saved.");
		} catch (Exception e) {
			PApplet.println("Failed to save homography: " + e.getMessage());
		}
	}

	public void load() {
		try {
			XML root = p.loadXML("data/homography.xml");
			if (root == null) {
				PApplet.println("No homography file found.");
				return;
			}

			// Find the node for this object
			XML objectNode = root.getChild(objectName);
			if (objectNode == null) {
				PApplet.println("No data found for " + objectName);
				return;
			}

			// Load xyP points
			XML xyPNode = objectNode.getChild("xyP");
			XML[] xyPPoints = xyPNode.getChildren("point");
			for (int i = 0; i < xyPPoints.length; i++) {
				xyP[i] = new PVector(xyPPoints[i].getFloat("x"), xyPPoints[i].getFloat("y"));
			}

			// Load uvP points
			XML uvPNode = objectNode.getChild("uvP");
			XML[] uvPPoints = uvPNode.getChildren("point");
			for (int i = 0; i < uvPPoints.length; i++) {
				uvP[i] = new PVector(uvPPoints[i].getFloat("x"), uvPPoints[i].getFloat("y"));
			}

			// Update normalized coordinates and homography
			for (int i = 0; i < xyP.length; i++) {
				xyN[i] = Pixel2Nornal(xyP[i]);
				uvN[i] = Pixel2Nornal(uvP[i]);
			}
			updateHomography(xyN, uvN);

			PApplet.println("Homography for " + objectName + " loaded.");
		} catch (Exception e) {
			PApplet.println("Failed to load homography: " + e.getMessage());
		}
	}

}