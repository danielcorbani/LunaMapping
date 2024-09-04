package paletai.mapping;

import processing.core.*;
import processing.opengl.*;
import processing.core.PConstants;


public class VidMap {
	PApplet p;
	PShader mapInOut;
	PGraphics2D pgCanvas;
	PVector[] xyN    = new PVector[4];
	PVector[] uvN    = new PVector[4];
	
	
	public VidMap(PApplet p) {
		this.p = p;
		pgCanvas = (PGraphics2D) p.createGraphics(p.width, p.height,PConstants.P2D);
		mapInOut = p.loadShader("homography.glsl");
	}

	public void show(PGraphics2D input) {
		p.image(input,0,0);
		
	}
	
	public void show(PImage input) {
		p.image(input,0,0);
		
	}

	
	
}
