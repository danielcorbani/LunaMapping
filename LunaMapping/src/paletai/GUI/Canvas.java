package paletai.GUI;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.opengl.PGraphics2D;
import java.io.File;
import java.util.ArrayList;
import paletai.mapping.*;
import processing.video.*;


public class Canvas {
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
	
	private String sketchDir;
	private ArrayList<String> fileList; // List of files in data folder
	
	private Project project;
	
	public Canvas(PApplet p, String sketchDir, Project project) {
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
		
		this.sketchDir = sketchDir;
		fileList = new ArrayList<>();
		scanDataFolder(); // Load files on startup

		// Initial drawing of panels
		leftPanel.drawContent();
		centerPanel.drawContent();
		rightPanel.drawContent();
		bottomPanel.drawContent();

		this.project = project;
	}

	// Scan the data folder for images and videos
	private void scanDataFolder() {
	    // Get the sketch's working directory
	    //String sketchDir = p.sketchPath(""); 
	    File dataFolder = new File(sketchDir, "data"); // Ensure it points to the correct "data" folder

	    if (dataFolder.exists() && dataFolder.isDirectory()) {
	        System.out.println("Data folder found: " + dataFolder.getAbsolutePath());

	        File[] files = dataFolder.listFiles();
	        if (files != null) {
	            for (File file : files) {
	                String fileName = file.getName().toLowerCase();
	                if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov")
	                        || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) {
	                    fileList.add(file.getName());
	                }
	            }
	        }
	    } else {
	        System.out.println("No 'data' folder found at: " + dataFolder.getAbsolutePath());
	    }

	}

	public void display() {
		sceneCanvas.beginDraw();
		sceneCanvas.background(220);

		// Draw panels
		leftPanel.display(sceneCanvas);
		centerPanel.display(sceneCanvas);
		rightPanel.display(sceneCanvas);
		bottomPanel.display(sceneCanvas);

		// Draw file list inside the left panel
		drawFileList(sceneCanvas);

		// Draw edge handles
		drawLimitHandles(sceneCanvas);
		drawAddSceneButton(sceneCanvas);
		drawSceneTabs(sceneCanvas);
		sceneCanvas.endDraw();
		p.image(sceneCanvas, 0, 0);
	}

	
	// Draw the file list inside the left panel
	private void drawFileList(PGraphics2D pg) {
		pg.fill(0);
		pg.textSize(14);
		pg.textAlign(PApplet.LEFT, PApplet.TOP);
		float x = 10;
		float y = 10;

		for (String fileName : fileList) {
			pg.text(fileName, x, y);
			y += 18; // Line spacing
			if (y > leftPanel.getHeight() - 20)
				break; // Stop if out of space
		}
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
			// System.out.println("Resizing left edge");
		} else if (Math.abs(p.mouseX - rightEdge) < resizeMargin / 2) {
			resizingVertical = true;
			activeEdge = "right";
			// System.out.println("Resizing right edge");
		} else if (Math.abs(p.mouseY - bottomEdge) < resizeMargin / 2) {
			resizingHorizontal = true;
			activeEdge = "bottom";
			// System.out.println("Resizing bottom edge");
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
		//System.out.println("Mouse released and panels updated");
	}

	private void updatePanels() {
		//System.out.println("Updating panels");
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
	
	// Inside Canvas class
	void drawAddSceneButton(PGraphics2D pg) {
	    float btnX = centerPanel.x + 10;  // Position at bottom left of center panel
	    float btnY = centerPanel.y + centerPanel.h - 40;
	    float btnW = 100, btnH = 30;
	    
	    pg.fill(50, 150, 250);  // Blue color for button
	    pg.rect(btnX, btnY, btnW, btnH, 8);
	    
	    pg.fill(255);
	    pg.textAlign(PConstants.CENTER, PConstants.CENTER);
	    pg.text("New Scene", btnX + btnW / 2, btnY + btnH / 2);
	}

	// Detect button click
	public boolean isOverAddSceneButton(int mouseX, int mouseY) {
	    float btnX = centerPanel.x + 10;
	    float btnY = centerPanel.y + centerPanel.h - 40;
	    float btnW = 100, btnH = 30;

	    return (mouseX > btnX && mouseX < btnX + btnW &&
	            mouseY > btnY && mouseY < btnY + btnH);
	}

	void drawSceneTabs(PGraphics2D pg) {
	    float tabX = centerPanel.x + 10;  // Start position
	    float tabY = centerPanel.y + 10;
	    float tabW = 80, tabH = 25;
	    
	    for (int i = 0; i < project.getScenes().size(); i++) {
	        Scene scene = project.getScenes().get(i);
	        
	        // Highlight active scene
	        if (i == project.getActiveSceneIndex()) {
	            pg.fill(50, 150, 250);
	        } else {
	            pg.fill(200);
	        }

	        pg.rect(tabX, tabY, tabW, tabH, 6);
	        
	        // Scene number inside tab
	        pg.fill(0);
	        pg.textAlign(PConstants.CENTER, PConstants.CENTER);
	        pg.text("Scene " + (i + 1), tabX + tabW / 2, tabY + tabH / 2);
	        
	        tabX += tabW + 5; // Move next tab to the right
	    }
	}
	
	public int getClickedSceneTab(int mouseX, int mouseY) {
	    float tabX = centerPanel.x + 10;
	    float tabY = centerPanel.y + 10;
	    float tabW = 80, tabH = 25;
	    
	    for (int i = 0; i < project.getScenes().size(); i++) {
	        if (mouseX > tabX && mouseX < tabX + tabW &&
	            mouseY > tabY && mouseY < tabY + tabH) {
	        	System.out.println("Clicked Scene " + i);
	        	System.out.println("Scenes size " + project.getScenes().size());
	        	return i;
	        }
	        tabX += tabW + 5; // Move to next tab
	    }
	    return -1;
	}


}
