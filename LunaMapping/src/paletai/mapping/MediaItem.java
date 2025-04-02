package paletai.mapping;

import processing.core.*;
import processing.video.*;
import processing.opengl.*;
import java.io.File;

/**
 * A class for managing media items (images/videos) with homography transformation capabilities.
 * Handles loading, playback, and rendering of media files with perspective correction.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Automatic aspect ratio correction</li>
 *   <li>Video playback control</li>
 *   <li>Thumbnail generation</li>
 *   <li>Homography transformation via {@link VidMap}</li>
 *   <li>Media file management</li>
 * </ul>
 * 
 * @author Daniel Corbani
 * @version 1.0
 * @see VidMap
 * @see Movie
 */
public class MediaItem {
	/** Parent Processing applet */
	private PApplet p;
	/** Full path to media file */
	private String filePath;
	/** Base filename with scene index */
	private String fileName; 
	/** Image object (for static images) */
	private PImage img;
	/** Thumbnail representation */
	private PImage thumbnail;
	/** Video flag */
	private boolean isVideo;
	private boolean loaded = false;
	private boolean isLooping = true;
	/** Video object (for movies) */
	private Movie movie;
	/** Graphics buffer for rendering */
	private PGraphics2D mediaCanvas;
	/** Homography transformation handler */
	private VidMap vidMap; // Homography transformation
	/** Original media dimensions */
	public int mediaWidth, mediaHeight;
	
	/**
     * Constructs a new MediaItem.
     * 
     * @param p Parent Processing applet
     * @param filePath Path to media file
     * @param sceneIndex Identifier for this media instance
     * @throws RuntimeException If media file cannot be loaded
     */
	public MediaItem(PApplet p, String filePath, int sceneIndex) {
		this.p = p;
		this.filePath = filePath;
		this.fileName = extractFileName(filePath) + "_scene" + String.valueOf(sceneIndex); // NEED TO CHECK THIS!!!!!
		// System.out.println("fileName = " + fileName);
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

		if (mediaHeight != 0)
			applyAspectRatioCorrection(mediaWidth, mediaHeight);
	}
	
	/**
     * Checks if media is successfully loaded.
     * @return true if media is ready for display
     */
	public boolean isLoaded() {
		return loaded;
	}

	/**
     * Adjusts media display to maintain aspect ratio.
     * Automatically updates homography points to fit media properly.
     * 
     * @param mediaWidth Original media width
     * @param mediaHeight Original media height
     */
	public void applyAspectRatioCorrection(int mediaWidth, int mediaHeight) {
		float screenAspect = (float) p.width / p.height;
		// System.out.println("screenAspect = " + screenAspect); //1.3334
		float mediaAspect = (float) mediaWidth / mediaHeight;
		// System.out.println("mediaAspect = " + mediaAspect); //0.5625
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
		PVector[] uvP = { new PVector(offsetX, offsetY), new PVector(offsetX + newWidth, offsetY),
				new PVector(offsetX + newWidth, offsetY + newHeight), new PVector(offsetX, offsetY + newHeight) };

		PVector[] xyP = { new PVector(0, 0), new PVector(p.width, 0), new PVector(p.width, p.height),
				new PVector(0, p.height) };

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

	public void offCalibration() {
		vidMap.offCalibration();
	}

	public void onCalibration() {
		vidMap.onCalibration();
	}

	public void toggleInput() {
		vidMap.checkInput = !vidMap.checkInput;
		System.out.println("checkInput = " + vidMap.checkInput);
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
	
	public void resetHomography() {
		vidMap.resetHomography();
		applyAspectRatioCorrection(mediaWidth, mediaHeight);
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

	/**
     * Renders the media with homography transformation.
     * Handles both static images and video playback.
     */
	public void render() {
		// System.out.println("Rendering file: " + fileName);
		mediaCanvas.beginDraw();
		// System.out.println("All good " + fileName);
		mediaCanvas.background(0); // Clear previous frame

		if (isVideo && movie.available()) {
			movie.read();
			if (mediaHeight == 0) {
				mediaWidth = movie.width;
				mediaHeight = movie.height;
				applyAspectRatioCorrection(mediaWidth, mediaHeight);
			}

		}
		if (isVideo) {
			mediaCanvas.image(movie, 0, 0, mediaCanvas.width, mediaCanvas.height);
			if (thumbnail != null) {
				mediaCanvas.image(thumbnail, 0, 0);
			}
		} else {
			mediaCanvas.image(img, 0, 0, mediaCanvas.width, mediaCanvas.height);
		}

		mediaCanvas.endDraw();

		// Apply homography transformation using VidMap
		vidMap.show(mediaCanvas);

		loaded = true;
	}

	/**
     * Toggles video playback state.
     * No effect on static images.
     */
	public void togglePlayback() {
		if (isVideo) {
			if (movie.isPlaying()) {
				movie.pause();
			} else {
				playMedia();
			}
		}
	}
	
	/**
     * Toggles video loop mode.
     */
	public void toggleLoop() {
		isLooping = !isLooping;
		System.out.println("isLooping = " + isLooping);
	}
	/**
     * Starts media playback.
     * For videos: begins playback according to loop mode.
     */
	public void playMedia() {
		if (isVideo && movie != null && !movie.isPlaying()) {
			if (isLooping) {
				movie.loop();
			} else {
				movie.play();
			}

		}
	}
	
	/**
     * Stops media playback.
     * For videos: stops and clears the display.
     */
	public void stopMedia() {
		if (isVideo && movie != null && movie.isPlaying()) {
			movie.stop();
			mediaCanvas.beginDraw();
			mediaCanvas.clear();
			mediaCanvas.endDraw();
		}
	}

	public void muteMedia() {
		if (isVideo && movie != null && !movie.isPlaying()) {
			movie.volume(0);
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
