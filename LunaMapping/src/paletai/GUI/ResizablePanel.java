package paletai.GUI;

import processing.core.PApplet;
import processing.opengl.PGraphics2D;

public class ResizablePanel {
    private PApplet p;
    private float x, y, w, h;
    private PGraphics2D panelGraphics; // Each panel has its own PGraphics2D

    public ResizablePanel(PApplet p, float x, float y, float w, float h) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        panelGraphics = (PGraphics2D) p.createGraphics((int) w, (int) h, PApplet.P2D);
    }
    
    public float getHeight() {
        return this.h;
    }
    public float getWidth() {
        return this.w;
    }

    // Display the panel's graphics
    public void display(PGraphics2D sceneCanvas) {
        // Draw the panel graphics onto the sceneCanvas
        sceneCanvas.image(panelGraphics, x, y);
    }

    // Custom drawing logic for the panel
    public void drawContent() {
        panelGraphics.beginDraw();
        panelGraphics.background(200); // Example background
        panelGraphics.fill(0);
        panelGraphics.textAlign(PApplet.CENTER, PApplet.CENTER);
        panelGraphics.text("Panel", w / 2, h / 2); // Example placeholder text
        panelGraphics.endDraw();
    }

    public void setPositionAndSize(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;

        // Only recreate the graphics if the size actually changes
        if (this.w != w || this.h != h) {
            this.w = w;
            this.h = h;

            // Only recreate if dimensions are valid
            if (w > 0 && h > 0) {
                panelGraphics.dispose(); // Free resources of the old graphics
                panelGraphics = (PGraphics2D) p.createGraphics((int) w, (int) h, PApplet.P2D);
                //System.out.println("Recreated panelGraphics with new size: " + w + "x" + h);
            }
        }
    }


    // Getter for panel graphics
    public PGraphics2D getGraphics() {
        return panelGraphics;
    }
}
