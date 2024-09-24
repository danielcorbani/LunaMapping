package paletai.mapping;

import processing.core.*;
import processing.opengl.*;


public class VidMap {
	PApplet p;
	PShader mapInOut;
	PGraphics2D pgCanvas, pgInput;
	PVector[] xyN    = new PVector[4];
	PVector[] uvN    = new PVector[4];
	
	MathHomography mat;
	PMatrix3D H;
	
	boolean calibrate =  false;
	
	public VidMap(PApplet p) {
		this.p = p;
		pgCanvas = (PGraphics2D) p.createGraphics(p.width, p.height,PConstants.P2D);
		pgInput  = (PGraphics2D) p.createGraphics(p.width, p.height,PConstants.P2D);
		mapInOut = p.loadShader("homography.glsl");
		mapInOut.set("resolution", p.width, p.height);

//		xyN[0] = new PVector(0, 0);
//		xyN[1] = new PVector(1, 0);
//		xyN[2] = new PVector(1, 1);
//		xyN[3] = new PVector(0, 1);
//
//		uvN[0] = new PVector(0, 0); 
//		uvN[1] = new PVector(1, 0); 
//		uvN[2] = new PVector(1, 1); 
//		uvN[3] = new PVector(0, 1);
		
		xyN[0] = new PVector(0, 0);
		xyN[1] = new PVector(1, 0);
		xyN[2] = new PVector(1, 1);
		xyN[3] = new PVector(0, 1);

		uvN[0] = new PVector(0.1f, 0.2f); 
		uvN[1] = new PVector(0.8f, 0); 
		uvN[2] = new PVector(0.97f, 0.95f); 
		uvN[3] = new PVector(0, 1);
		
		mat = new MathHomography();
		updateHomography(xyN, uvN);
	}
	
	public void updateHomography(PVector[] xyNew, PVector[] uvNew) {
		
		for (int i=0; i< uvN.length; i++) { 
		    String xyNum = "xy" + Integer. toString(i);
		    mapInOut.set(xyNum, uvNew[i].x, uvNew[i].y); //input points set cropping mask
		  }
		float[][] h    = mat.calculateHomography(xyNew, uvNew); // get the homograhy matrix cconsidering the normalized points
		float[][] hinv = mat.invertMatrix(h);                   //the OpenGl coordinates requires the inverse Homography matrix
		hinv           = mat.transpose(hinv);                   //for some reason, it must be transposed
		H              = mat.getMatrix(hinv);                   //it gets the PMatrix#D for the OpenGL filter
		mapInOut.set("H", H, true); //true = use3x3
		
	}

	public void show(PGraphics2D input) {
		pgCanvas.beginDraw();
		pgCanvas.image(input,0,0,pgCanvas.width,pgCanvas.height);
		pgCanvas.endDraw();
		
		pgCanvas.filter(mapInOut);
		p.image(pgCanvas,0,0);
		
		if(calibrate) {
			p.stroke(0,255,0);
			p.strokeWeight(2);
			for (int i=0; i< uvN.length; i++) {
				int k = (i+1)%4;
			    p.line(p.width*uvN[i].x, p.height*uvN[i].y, p.width*uvN[k].x, p.height*uvN[k].y);
			  }
		}
	}
	
	public void show(PImage input) {
		pgInput.beginDraw();
		pgInput.image(input,0,0,pgCanvas.width,pgCanvas.height);
		pgInput.endDraw();
		
		show(pgInput);
		
		
	}
	
	public PVector Pixel2Nornal(PVector in) {
		return new PVector(in.x/p.width,in.y/p.height);
	}
	
	public PVector Nornal2Pixel(PVector in) {
		return new PVector(in.x*p.width,in.y*p.height);
	}
	public void toggleCalibration() {
		calibrate = ! calibrate;
		System.out.println("calibrate");
	}
}
