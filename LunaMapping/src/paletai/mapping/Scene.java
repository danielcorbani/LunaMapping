package paletai.mapping;

import processing.core.*;
import processing.opengl.*;
import java.util.ArrayList;

/**
 * A class that manages a collection of MediaItems as a single scene.
 * Handles rendering, media control, and calibration for multiple media elements.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Media item management and organization</li>
 *   <li>Active media selection for calibration</li>
 *   <li>Scene activation/deactivation</li>
 *   <li>Batch operations on all media items</li>
 * </ul>
 * 
 * @author Daniel Corbani
 * @version 1.0
 * @see MediaItem
 */
public class Scene {
	/** Parent Processing applet */
	private PApplet p;
	/** List of media items in this scene */
	private ArrayList<MediaItem> mediaItems;
	/** Currently active media for calibration */
	private MediaItem activeMedia; // Track active media for calibration
	/** Scene index identifier */
	private int indexNum;
	/** Scene activation state */
	private boolean isActive = false;
	/**
     * Constructs a new Scene.
     * 
     * @param p Parent Processing applet
     * @param index Unique identifier for this scene
     */
	public Scene(PApplet p, int index) {
		this.p = p;
		this.mediaItems = new ArrayList<>();
		this.indexNum = index;
	}
	
	/**
     * Adds a media item to this scene.
     * First added item becomes the default active media.
     * 
     * @param item MediaItem to add
     */
	public void addMediaItem(MediaItem item) {
		mediaItems.add(item);
		if (activeMedia == null) {
			activeMedia = item; // Set first added item as active
		}
	}
	
	/**
     * Checks if all media items in the scene are ready for display.
     * 
     * @return true if all media items report being loaded
     */
	public boolean isReady() {
	    for (MediaItem item : mediaItems) {
	        if (!item.isLoaded()) {
	            return false; // Wait until all media items confirm they are loaded
	        }
	    }
	    return true;
	}

	/**
     * Renders all media items in the scene.
     * Also handles hover detection for calibration points.
     * 
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
	public void render(int mouseX, int mouseY) {
		//System.out.println("Scene Render Running...");
		for (MediaItem item : mediaItems) {
			//System.out.println("Rendering MediaItem: " + item.getFileName());
			item.checkHover(mouseX, mouseY); // Pass explicit mouse position
			item.render();
		}

	}

	/**
     * Changes the active media item for calibration.
     * 
     * @param index Position of media item in the list
     * @throws IndexOutOfBoundsException if index is invalid
     */
	public void switchActiveMedia(int index) {
		if (index >= 0 && index < mediaItems.size()) {
			activeMedia = mediaItems.get(index);
			System.out.println("Switched to MediaItem: " + activeMedia.getFileName());
		}
	}
	/**
     * Toggles the input mode for the active media's calibration.
     */
	public void toggleInput() {
		activeMedia.toggleInput();
	}
	
	/**
     * Toggles calibration mode for the active media.
     */
	public void toggleCalibration() {
		if (activeMedia != null) {
			activeMedia.toggleCalibration();
		}
	}
	
	/**
     * Disables calibration for the active media.
     */
	public void offCalibration() {
		if (activeMedia != null) {
			activeMedia.offCalibration();
		}
	}
	
	/**
     * Enables calibration for the active media.
     */
	public void onCalibration() {
		if (activeMedia != null) {
			activeMedia.onCalibration();
		}
	}
	/**
     * Moves the hovered calibration point for active media.
     * 
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
	public void moveHoverPoint(int mouseX, int mouseY) {
		if (activeMedia != null) {
			activeMedia.moveHoverPoint(mouseX, mouseY);
		}
	}
	
	/**
     * Handles mouse release events during calibration.
     */
	public void mouseReleased() {
		if (activeMedia != null) {
			activeMedia.mouseReleased();
		}
	}

	// **🔹 Getters**
	public ArrayList<MediaItem> getMediaItems() {
		return mediaItems;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
		//loadAll();
		for (MediaItem item : mediaItems) {
			if (this.isActive) {
				item.playMedia(); // Start video when scene is active
			} else {
				item.stopMedia(); // Pause video when scene is inactive
			}
		}
		//System.out.println("Setting Scene Active: " + this.isActive);
	}
	/**
     * Saves homography configurations for all media items.
     */
	public void saveAll() {
		this.isActive = isActive;
		for (MediaItem item : mediaItems) {
			item.saveHomography(); // Start video when scene is active
		}
	}
	
	/**
     * Loads homography configurations for all media items.
     */
	public void loadAll() {
		this.isActive = isActive;
		for (MediaItem item : mediaItems) {
			item.loadHomography(); // Start video when scene is active
		}
	}
	
	/**
     * Toggles loop mode for all video media items.
     */
	public void toggleLoop() {
		this.isActive = isActive;
		//loadAll();
		for (MediaItem item : mediaItems) {
			if (this.isActive) {
				item.toggleLoop(); // Start video when scene is active
			}
		}
		//System.out.println("Setting Scene Active: " + this.isActive);
		
	}
}
