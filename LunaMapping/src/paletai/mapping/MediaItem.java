package paletai.mapping;

import processing.core.*;
import processing.data.XML;
import processing.video.*;
import processing.opengl.*;
import java.io.File;

/**
 * A class for managing media items (images/videos) with homography
 * transformation capabilities. Handles loading, playback, and rendering of
 * media files with perspective correction.
 * 
 * <p>
 * Key features include:
 * </p>
 * <ul>
 * <li>Automatic aspect ratio correction</li>
 * <li>Video playback control</li>
 * <li>Thumbnail generation</li>
 * <li>Homography transformation via {@link VidMap}</li>
 * <li>Media file management</li>
 * </ul>
 * 
 * @author Daniel Corbani
 * @version 1.0
 * @see VidMap
 * @see Movie
 */

public class MediaItem {
	XML mediaXML, fromXML;
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
	public VidMap vm; // Homography transformation
	/** Original media dimensions */
	public int mediaWidth, mediaHeight;
	public int assignedScreen, sceneIndex, mediaId;
	int resolutionX, resolutionY;
	public boolean calibrate = false;
	public boolean fromxml = false;

	/**
	 * Constructs a new MediaItem.
	 *
	 * @param p          Parent Processing applet
	 * @param filePath   Path to media file
	 * @param sceneIndex Identifier for this media instance
	 * @throws RuntimeException If media file cannot be loaded
	 */

	public MediaItem(PApplet p, String filePath, int sceneIndex, int screenIndex, int mediaId) {
		PApplet.println("Start MediaItem");
		this.p = p;
		this.filePath = filePath;
		// println("filePath " + filePath);
		this.fileName = extractFileName(filePath); // NEED TO CHECK THIS!!!!!
		// println(fileName);
		this.isVideo = isVideoFile(filePath);
		this.sceneIndex = sceneIndex;
		this.assignedScreen = screenIndex;
		this.mediaId = mediaId;
		this.vm = new VidMap(p, fileName); // Pass fileName to vm
		initVariables();
		toXML();
		PApplet.println(mediaXML);
		// initXML();
	}

	public MediaItem(PApplet p, XML mXML) {
		PApplet.println("Initializing MediaItem from XML");
		this.p = p;
		// println(fromXML);
		this.filePath = mXML.getString("name");
		// println("filePath " + filePath);
		this.fileName = extractFileName(filePath); // NEED TO CHECK THIS!!!!!
		// println("fileName : " + fileName);
		this.isVideo = isVideoFile(filePath);
		this.sceneIndex = mXML.getInt("Scene");
		this.assignedScreen = mXML.getInt("Screen");
		this.mediaId = mXML.getInt("id");
		this.vm = new VidMap(p, fileName); // Pass fileName to vm
		this.fromXML = mXML;
		PApplet.println("What is fromXML");
		PApplet.println(this.fromXML);
		PApplet.println("What is mediaXML");
		PApplet.println(this.mediaXML);
		initVariables();
		PApplet.println("updating from XML");
		updateFromXML(this.fromXML);
		// PApplet.println(this.fromXML);
		// toXML();
		// initXML();
		// PApplet.println(this.mediaXML);

	}

	void initXML() {
		mediaXML = new XML("MediaItem");
		mediaXML.setString("name", fileName);
		mediaXML.setInt("Screen", assignedScreen);
		mediaXML.setInt("Scene", sceneIndex);
		mediaXML.setInt("id", mediaId);
	}

	void initVariables() {
		PApplet.println("initVariables");
		if (isVideo) {
			PApplet.println("is video");
			this.movie = new Movie(p, filePath);
			movie.loop(); // Preload the movie (optional)
			mediaWidth = movie.width;
			mediaHeight = movie.height;
			// movie.stop();
			PApplet.println("video loaded");
		} else {
			PApplet.println("MediaItem is picture");
			img = p.loadImage(filePath);

			if (img != null) {
				this.thumbnail = img.copy();
				this.thumbnail.resize(150, 100); // Resize for thumbnail display
				mediaWidth = img.width;
				mediaHeight = img.height;
			} else {
				mediaWidth = 720;
				mediaHeight = 480;
			}
		}
	}

	void toXML() {
		PApplet.println("MediaItem toXML");
		initXML();
		if (vm.xyN[0] != null)
			mediaXML.addChild(arrayToXML("xyN", vm.xyN));
		if (vm.xyN[0] != null)
			mediaXML.addChild(arrayToXML("uvN", vm.uvN));
		if (vm.xyN[0] != null)
			mediaXML.addChild(arrayToXML("xyP", vm.xyP));
		if (vm.xyN[0] != null)
			mediaXML.addChild(arrayToXML("uvP", vm.uvP));
	}

	// Convert a PVector[] into XML
	XML arrayToXML(String tag, PVector[] arr) {
		XML arrayXML = new XML(tag);
		for (int i = 0; i < arr.length; i++) {
			XML v = new XML("point");
			v.setInt("index", i);
			// println(arr[i]);
			v.setFloat("x", arr[i].x);
			v.setFloat("y", arr[i].y);
			arrayXML.addChild(v);
		}
		return arrayXML;
	}

	void activateFromXML() {
		fromxml = true;
		PApplet.println("activate fromxml: " + fromxml);
	}

	void updateFromXML(XML xml) {
//		XML[] points = xml.getChildren();
//		PApplet.println("points length: " + points.length);
//		for (int i = 0; i < points.length; i++) {
//			PApplet.println(points[i]);
//		}
		//PApplet.println(xml.getChild("xyN"));
		PVector[] xyNew = arrayFromXML(xml.getChild("xyN"));
		PVector[] uvNew = arrayFromXML(xml.getChild("uvN"));
		vm.xyN = arrayFromXML(xml.getChild("xyN"));
		vm.uvN = arrayFromXML(xml.getChild("uvN"));
		vm.xyP = arrayFromXML(xml.getChild("xyP"));
		vm.uvP = arrayFromXML(xml.getChild("uvP"));
		mediaXML = xml;
		updateHomography(xyNew, uvNew);
		// toXML();
	}

	// Convert XML back into a PVector[]
	PVector[] arrayFromXML(XML arrayXML) {
		XML[] points = arrayXML.getChildren("point");
		PVector[] arr = new PVector[points.length];
		for (int i = 0; i < points.length; i++) {
			PApplet.println(points[i].getName());
			float x = points[i].getFloat("x");
			float y = points[i].getFloat("y");
			// float z = points[i].hasAttribute("z") ? points[i].getFloat("z") : 0;
			arr[i] = new PVector(x, y);
			PApplet.println(arr[i]);
		}
		return arr;
	}

	// 🔹 Update the *existing* XML with current array values
	void updateXML() {
//		if (mediaXML == null) {
//			// println("Inside updateXML");
//			toXML(); // build fresh if missing
//			return;
//		}
		updateArrayXML(mediaXML.getChild("xyN"), vm.xyN);
		updateArrayXML(mediaXML.getChild("uvN"), vm.uvN);
		updateArrayXML(mediaXML.getChild("xyP"), vm.xyP);
		updateArrayXML(mediaXML.getChild("uvP"), vm.uvP);
	}

	// Update an existing XML node with new PVector values
	void updateArrayXML(XML arrayXML, PVector[] arr) {
		XML[] points = arrayXML.getChildren("point");
		for (int i = 0; i < arr.length && i < points.length; i++) {
			// println(arrayXML.getName() + " " + points[i]);
			points[i].setFloat("x", arr[i].x);
			points[i].setFloat("y", arr[i].y);
			// points[i].setFloat("z", arr[i].z);
		}
	}

	void assignToDisplay(int w, int h, int screenIndex) {
		PApplet.println("5");
		this.resolutionX = w;
		this.resolutionY = h;
		this.mediaCanvas = (PGraphics2D) p.createGraphics(resolutionX, resolutionY, PConstants.P2D);
		this.assignedScreen = screenIndex;
		if (mediaHeight != 0) {
			// println("mediaHeight = " + mediaHeight);
			applyAspectRatioCorrection(mediaWidth, mediaHeight);
		}
		vm.assignToDisplay(resolutionX, resolutionY);
		// this.fileName = extractFileName(filePath)+"C"+sceneIndex+"S"+screenIndex;
		PApplet.println("Assign to Display");
		// toXML();
	}

	/**
	 * Checks if media is successfully loaded.
	 * 
	 * @return true if media is ready for display
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Adjusts media display to maintain aspect ratio. Automatically updates
	 * homography points to fit media properly.
	 *
	 * @param mediaWidth  Original media width
	 * @param mediaHeight Original media height
	 */
	public void applyAspectRatioCorrection(int mediaWidth, int mediaHeight) {
		float screenAspect = (float) mediaCanvas.width / mediaCanvas.height;
		// System.out.println("screenAspect = " + screenAspect); //1.3334
		float mediaAspect = (float) mediaWidth / mediaHeight;
		// System.out.println("mediaAspect = " + mediaAspect); //0.5625
		float newWidth, newHeight;
		float offsetX = 0, offsetY = 0;

		if (mediaAspect > screenAspect) {
			// Fit to width
			newWidth = mediaCanvas.width;
			newHeight = mediaCanvas.width / mediaAspect;
			offsetY = (mediaCanvas.height - newHeight) / 2;
		} else {
			// Fit to height
			newHeight = mediaCanvas.height;
			newWidth = mediaCanvas.height * mediaAspect;
			offsetX = (mediaCanvas.width - newWidth) / 2;
		}

		// Update homography points
		PVector[] uvP = { new PVector(offsetX, offsetY), new PVector(offsetX + newWidth, offsetY),
				new PVector(offsetX + newWidth, offsetY + newHeight), new PVector(offsetX, offsetY + newHeight) };

		PVector[] xyP = { new PVector(0, 0), new PVector(mediaCanvas.width, 0),
				new PVector(mediaCanvas.width, mediaCanvas.height), new PVector(0, mediaCanvas.height) };

		vm.updateHomographyFromPixel(xyP, uvP);
		if (loaded) {
			updateFromXML(fromXML);
		}
	}
	// **🔹 vm Wrapper Methods**

//	private void updateHomographyFromPixel(PVector[] xyPP, PVector[] uvPP) {
//		vm.updateHomographyFromPixel(xyPP, uvPP);
//	}

	public void updateHomography(PVector[] xyNew, PVector[] uvNew) {
		vm.updateHomography(xyNew, uvNew);
	}

	public void toggleCalibration() {
		vm.toggleCalibration();
		this.calibrate = vm.calibrate;
	}

	public void offCalibration() {
		vm.offCalibration();
		this.calibrate = vm.calibrate;
	}

	public void onCalibration() {
		vm.onCalibration();
		this.calibrate = vm.calibrate;
	}

	public void toggleInput() {
		vm.checkInput = !vm.checkInput;
		// System.out.println("checkInput = " + vm.checkInput);
	}

	public void checkHover(float x, float y) {
		vm.checkHover(x, y);
	}

	public void moveHoverPoint(float x, float y) {
		vm.moveHoverPoint(x, y);
		updateXML();
	}

	public void mouseReleased() {
		vm.mouseReleased();
		updateXML();
	}

	public void resetHomography() {
		vm.resetHomography();
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

	public void setPreviewArea(float px, float py, float pw, float ph) {
		vm.setPreviewArea(px, py, pw, ph);
	}

	/**
	 * Renders the media with homography transformation. Handles both static images
	 * and video playback.
	 */
	public void render() {

		mediaCanvas.beginDraw();
		mediaCanvas.background(0); // Clear previous frame

		if (isVideo && movie.available()) {
			movie.read();
			if (mediaHeight == 0) {
				mediaWidth = movie.width;
				mediaHeight = movie.height;
				applyAspectRatioCorrection(mediaWidth, mediaHeight);
				if (fromxml == false && loaded == false) {
					toXML();
					PApplet.println("toXML inside render");
				}
				PApplet.println("Inside render");
				loaded = true;
			}
		}
		if (isVideo) {
			mediaCanvas.image(movie, 0, 0, mediaCanvas.width, mediaCanvas.height);
		} else {
			mediaCanvas.image(img, 0, 0, mediaCanvas.width, mediaCanvas.height);
		}
		if (fromxml == true && vm.xyN != null) {
			PApplet.println("updating from XML");
			updateFromXML(fromXML);
			fromxml = false;
		}

		mediaCanvas.endDraw();

		// Apply homography transformation using vm
		vm.render(mediaCanvas);
	}

	/**
	 * Toggles video playback state. No effect on static images.
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
		// System.out.println("isLooping = " + isLooping);
	}

	/**
	 * Starts media playback. For videos: begins playback according to loop mode.
	 */
	public void playMedia() {
		if (isVideo) {
			stopMedia(); // clean up old one first
			movie = new Movie(p, filePath);
			if (isLooping) {
				movie.loop();
			} else {
				movie.play();
			}
		}
	}

	/**
	 * Stops media playback. For videos: stops and clears the display.
	 */
	public void stopMedia() {
		if (isVideo && movie != null && movie != null) {
			movie.stop();
			movie.dispose(); // force GStreamer cleanup. It is crucial to force GStreamer to release the
								// native pipeline before reusing
			movie = null;
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
		return vm.getMediaCanvas();
	}
}