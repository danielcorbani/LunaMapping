package paletai.mapping;

import processing.core.*;
import processing.video.*;
import processing.opengl.*;
import java.io.File;

public class MediaItem {
	private PApplet p;
	private String filePath;
	private String fileName; // Extracted file name
	private PImage img;
	private PImage thumbnail;
	private boolean isVideo;
	private Movie movie;
	private PGraphics2D mediaCanvas;
	private VidMap vidMap; // Homography transformation
	public int mediaWidth, mediaHeight;
	
	public MediaItem(PApplet p, String filePath, int sceneIndex) {
		this.p = p;
		this.filePath = filePath;
		this.fileName = extractFileName(filePath) + "scene" + String.valueOf(sceneIndex); //NEED TO CHECK THIS!!!!!
		System.out.println("fileName = " + fileName);
		this.isVideo = isVideoFile(filePath);
		this.mediaCanvas = (PGraphics2D) p.createGraphics(p.width, p.height, PConstants.P2D);
		this.vidMap = new VidMap(p, fileName); // Pass fileName to VidMap		
		
		if (isVideo) {
			this.movie = new Movie(p, filePath);
			movie.loop(); // Preload the movie (optional)
			mediaWidth = movie.width;
			mediaHeight = movie.height;
			movie.stop();
		} else {
			img = p.loadImage(filePath);

			if (img != null) {
				this.thumbnail = img.copy();
				this.thumbnail.resize(150, 100); // Resize for thumbnail display
				mediaWidth = img.width;
				mediaHeight = img.height;
			} else {
				mediaWidth = p.width;
				mediaHeight = p.height;
			}
		}
		// Apply aspect ratio correction
		
	    if(mediaHeight != 0) applyAspectRatioCorrection(mediaWidth, mediaHeight);
	}
	
	// **🔹 Aspect Ratio Correction**
	private void applyAspectRatioCorrection(int mediaWidth, int mediaHeight) {
	    float screenAspect = (float) p.width / p.height;
	    //System.out.println("screenAspect = " + screenAspect); //1.3334
	    float mediaAspect = (float) mediaWidth / mediaHeight;
	    //System.out.println("mediaAspect = " + mediaAspect);   //0.5625
	    float newWidth, newHeight;
	    float offsetX = 0, offsetY = 0;

	    if (mediaAspect > screenAspect) {
	        // Fit to width
	        newWidth = p.width;
	        newHeight = p.width / mediaAspect;
	        offsetY = (p.height - newHeight) / 2;
	    } else {
	        // Fit to height
	        newHeight = p.height;
	        newWidth = p.height * mediaAspect;
	        offsetX = (p.width - newWidth) / 2;
	        
	    }
	    
	    // Update homography points
	    PVector[] uvP = {
	        new PVector(offsetX, offsetY),
	        new PVector(offsetX + newWidth, offsetY),
	        new PVector(offsetX + newWidth, offsetY + newHeight),
	        new PVector(offsetX, offsetY + newHeight)
	    };

	    PVector[] xyP = {
	        new PVector(0, 0),
	        new PVector(p.width, 0),
	        new PVector(p.width, p.height),
	        new PVector(0, p.height)
	    };

	    vidMap.updateHomographyFromPixel(xyP, uvP);
	}
	// **🔹 VidMap Wrapper Methods**

	private void updateHomographyFromPixel(PVector[] xyPP, PVector[] uvPP) {
		vidMap.updateHomographyFromPixel(xyPP, uvPP);
	}

	public void updateHomography(PVector[] xyNew, PVector[] uvNew) {
		vidMap.updateHomography(xyNew, uvNew);
	}

	public void toggleCalibration() {
		vidMap.toggleCalibration();
	}

	public void checkHover(float x, float y) {
		vidMap.checkHover(x, y);
	}

	public void moveHoverPoint(float x, float y) {
		vidMap.moveHoverPoint(x, y);
	}

	public void mouseReleased() {
		vidMap.mouseReleased();
    }
	
	public void saveHomography() {
		vidMap.save();
	}

	public void loadHomography() {
		vidMap.load();
	}

	// Extracts the file name from the full path
	private String extractFileName(String path) {
		File file = new File(path);
		return file.getName();
	}

	// Check if a file is a video
	private boolean isVideoFile(String filename) {
		filename = filename.toLowerCase();
		return filename.endsWith(".mp4") || filename.endsWith(".avi") || filename.endsWith(".mov");
	}

	// Generate a thumbnail from a video (first frame) - Now manually callable
	public void generateVideoThumbnail() {
		if (isVideo && movie != null) {
			thumbnail = p.createImage(movie.width, movie.height, PConstants.RGB);
			thumbnail.copy(movie, 0, 0, movie.width, movie.height, 0, 0, thumbnail.width, thumbnail.height);
			thumbnail.resize(150, 100);
		}
	}

	// Render media using VidMap transformation
	public void render() {
		mediaCanvas.beginDraw();
		mediaCanvas.background(0); // Clear previous frame

		if (isVideo && movie.available()) {
			movie.read();
			if(mediaHeight == 0) {
				mediaWidth = movie.width;
				mediaHeight = movie.height;
				applyAspectRatioCorrection(mediaWidth, mediaHeight);
			}
			
		}
		if (isVideo) {
			mediaCanvas.image(movie, 0, 0, mediaCanvas.width, mediaCanvas.height);
			if (thumbnail != null)
				mediaCanvas.image(thumbnail, 0, 0);
		} else {
			mediaCanvas.image(img, 0, 0, mediaCanvas.width, mediaCanvas.height);
		}

		mediaCanvas.endDraw();

		// Apply homography transformation using VidMap
		vidMap.show(mediaCanvas);
	}

	// Toggle video playback
	public void togglePlayback() {
		if (isVideo) {
			if (movie.isPlaying()) {
				movie.pause();
			} else {
				movie.loop();
			}
		}
	}

	public void playMedia() {
	    if (isVideo && movie != null && !movie.isPlaying()) {
	        movie.loop();
	    }
	}
	
	public void stopMedia() {
	    if (isVideo && movie != null && movie.isPlaying()) {
	        movie.stop();
	    }
	}

	// Getters
	public String getFilePath() {
		return filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public PImage getThumbnail() {
		return thumbnail;
	}

	public boolean isVideo() {
		return isVideo;
	}

	public PGraphics2D getMediaCanvas() {
		return vidMap.getMediaCanvas();
	}
}
