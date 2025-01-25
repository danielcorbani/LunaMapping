package paletai.GUI;

import processing.core.PApplet;
import processing.opengl.PGraphics2D;

public class Scene {
	private PApplet p;
	private PGraphics2D sceneCanvas;

	// Edges/handles
	private float leftEdge, rightEdge, bottomEdge;
	private float resizeMargin = 10;

	// Resizing flags
	private boolean resizingVertical = false;
	private boolean resizingHorizontal = false;
	private String activeEdge = "";

	private ResizablePanel leftPanel;
	private ResizablePanel centerPanel;
	private ResizablePanel rightPanel;
	private ResizablePanel bottomPanel;

	public Scene(PApplet p) {
		this.p = p;
		sceneCanvas = (PGraphics2D) p.createGraphics(p.width, p.height, PApplet.P2D);

		// Initialize limit lines
		leftEdge = p.width / 4f;
		rightEdge = 3 * p.width / 4f;
		bottomEdge = p.height - p.height / 3f;

		// Initialize panels
		leftPanel = new ResizablePanel(p, 0, 0, leftEdge, bottomEdge);
		centerPanel = new ResizablePanel(p, leftEdge, 0, rightEdge - leftEdge, bottomEdge);
		rightPanel = new ResizablePanel(p, rightEdge, 0, p.width - rightEdge, bottomEdge);
		bottomPanel = new ResizablePanel(p, 0, bottomEdge, p.width, p.height - bottomEdge);

		// Initial drawing of panels
		leftPanel.drawContent();
		centerPanel.drawContent();
		rightPanel.drawContent();
		bottomPanel.drawContent();
	}

	public void display() {
		sceneCanvas.beginDraw();
		sceneCanvas.background(220);

		// Draw panels
		leftPanel.display(sceneCanvas);
		centerPanel.display(sceneCanvas);
		rightPanel.display(sceneCanvas);
		bottomPanel.display(sceneCanvas);

		// Draw edge handles
		drawLimitHandles(sceneCanvas);

		sceneCanvas.endDraw();
		p.image(sceneCanvas, 0, 0);
	}

	// Draw visible limit handles
	private void drawLimitHandles(PGraphics2D pg) {
		pg.noStroke();

		// Vertical left handle
		if (activeEdge == "left")
			pg.fill(150, 50, 50);
		else
			pg.fill(150);
		pg.rect(leftEdge - resizeMargin / 2, 0, resizeMargin, bottomEdge);

		// Vertical handles
		if (activeEdge == "right")
			pg.fill(150, 50, 50);
		else
			pg.fill(150);
		pg.rect(rightEdge - resizeMargin / 2, 0, resizeMargin, bottomEdge);

		// Horizontal handle
		if (activeEdge == "bottom")
			pg.fill(150, 50, 50);
		else
			pg.fill(150);
		pg.rect(0, bottomEdge - resizeMargin / 2, p.width, resizeMargin);
	}

	// Handle mouse press
	public void mousePressed() {
		if (Math.abs(p.mouseX - leftEdge) < resizeMargin / 2) {
			resizingVertical = true;
			activeEdge = "left";
			//System.out.println("Resizing left edge");
		} else if (Math.abs(p.mouseX - rightEdge) < resizeMargin / 2) {
			resizingVertical = true;
			activeEdge = "right";
			//System.out.println("Resizing right edge");
		} else if (Math.abs(p.mouseY - bottomEdge) < resizeMargin / 2) {
			resizingHorizontal = true;
			activeEdge = "bottom";
			//System.out.println("Resizing bottom edge");
		}
	}

	// Handle mouse drag
	public void mouseDragged() {
	    System.out.println("Mouse dragged");

	    // Update edge positions during dragging
	    if (resizingVertical) {
	        if ("left".equals(activeEdge)) {
	            leftEdge = PApplet.constrain(p.mouseX, 50, rightEdge - 50);
	        } else if ("right".equals(activeEdge)) {
	            rightEdge = PApplet.constrain(p.mouseX, leftEdge + 50, p.width - 50);
	        }
	    }

	    if (resizingHorizontal) {
	        bottomEdge = PApplet.constrain(p.mouseY, 50, p.height - 50);
	    }
	}


	public void mouseReleased() {
	    resizingVertical = false;
	    resizingHorizontal = false;
	    activeEdge = "";

	    // Call updatePanels() only once after dragging
	    updatePanels();
	    System.out.println("Mouse released and panels updated");
	}

	private void updatePanels() {
	    System.out.println("Updating panels");
	    leftPanel.setPositionAndSize(0, 0, leftEdge, bottomEdge);
	    centerPanel.setPositionAndSize(leftEdge, 0, rightEdge - leftEdge, bottomEdge);
	    rightPanel.setPositionAndSize(rightEdge, 0, p.width - rightEdge, bottomEdge);
	    bottomPanel.setPositionAndSize(0, bottomEdge, p.width, p.height - bottomEdge);

	    // Update panel contents after resizing
	    leftPanel.drawContent();
	    centerPanel.drawContent();
	    rightPanel.drawContent();
	    bottomPanel.drawContent();
	}
}
