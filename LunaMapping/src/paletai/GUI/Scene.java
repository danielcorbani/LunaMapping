package paletai.mapping;

import processing.core.*;
import processing.opengl.*;
import java.util.ArrayList;

public class Scene {
	private PApplet p;
	private ArrayList<MediaItem> mediaItems;
	private MediaItem activeMedia; // Track active media for calibration
	private int indexNum;
	private boolean isActive = false;

	public Scene(PApplet p, int index) {
		this.p = p;
		this.mediaItems = new ArrayList<>();
		this.indexNum = index;
	}

	// **🔹 Add a media item to the scene**
	public void addMediaItem(MediaItem item) {
		mediaItems.add(item);
		if (activeMedia == null) {
			activeMedia = item; // Set first added item as active
		}
	}

	// **🔹 Render Scene**
	public void render(int mouseX, int mouseY) {
		for (MediaItem item : mediaItems) {
			item.checkHover(mouseX, mouseY); // Pass explicit mouse position
			item.render();
		}

	}

	// **🔹 Switch Active Media for Calibration**
	public void switchActiveMedia(int index) {
		if (index >= 0 && index < mediaItems.size()) {
			activeMedia = mediaItems.get(index);
			System.out.println("Switched to MediaItem: " + activeMedia.getFileName());
		}
	}

	// **🔹 Toggle Calibration for Active Media**
	public void toggleCalibration() {
		if (activeMedia != null) {
			activeMedia.toggleCalibration();
		}
	}

	// **🔹 Mouse Interaction for Calibration**
	public void mouseDragged(int mouseX, int mouseY) {
		if (activeMedia != null) {
			activeMedia.moveHoverPoint(mouseX, mouseY);
		}
	}

	public void mouseReleased() {
		if (activeMedia != null) {
			activeMedia.mouseReleased();
		}
		//saveAll();
	}

	// **🔹 Getters**
	public ArrayList<MediaItem> getMediaItems() {
		return mediaItems;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
		//loadAll();
		for (MediaItem item : mediaItems) {
			if (isActive) {
				item.playMedia(); // Start video when scene is active
			} else {
				item.stopMedia(); // Pause video when scene is inactive
			}
		}
	}

	public void saveAll() {
		this.isActive = isActive;
		for (MediaItem item : mediaItems) {
			item.saveHomography(); // Start video when scene is active
		}
	}
	
	public void loadAll() {
		this.isActive = isActive;
		for (MediaItem item : mediaItems) {
			item.loadHomography(); // Start video when scene is active
		}
	}
}
