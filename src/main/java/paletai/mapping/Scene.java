package paletai.mapping;

import java.util.ArrayList;

import processing.data.XML;

/**
 * A class that manages a collection of MediaItems as a single scene. Handles
 * rendering, media control, and calibration for multiple media elements.
 *
 * <p>Key features include:</p>
 * <ul>
 * <li>Media item management and organization</li>
 * <li>Active media selection for calibration</li>
 * <li>Scene activation/deactivation with media control</li>
 * <li>Batch operations on all media items</li>
 * <li>XML serialization for project saving/loading</li>
 * </ul>
 *
 * @author Daniel Corbani
 * @version 1.0
 * @see MediaItem
 * @see Project
 */
public class Scene {
    /** List of media items contained in this scene */
    public ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();

    /** XML configuration for scene serialization */
    public XML sceneXML;

    /** Whether this scene is currently active and rendering */
    boolean isActive;

    /** Index of the media item currently being calibrated, or -1 if none */
    int currentMediaCalibration = -1;

    /** Global alpha transparency for the entire scene */
    private int alpha = 255;

    /**
     * Constructs a new empty Scene.
     * Initializes with inactive state and prepares XML structure for media storage.
     */
	public Scene() {
		//this.id = id;
		sceneXML = new XML("Scene");
		//sceneXML.setInt("id", id);
		sceneXML.addChild("Medias");
		isActive = false;
	}

    /**
     * Constructs a Scene from XML configuration.
     * Recreates a scene from saved project data.
     *
     * @param scene XML element containing scene configuration
     */
	Scene(XML scene) {
		sceneXML = scene;
		isActive = false;

	}

    /**
     * Sets the thumbnail position for all media items in this scene.
     * Arranges thumbnails horizontally with 200-pixel spacing starting from
     * the specified coordinates.
     *
     * @param x The base x-coordinate for thumbnail positioning
     * @param y The base y-coordinate for thumbnail positioning
     * @see MediaItem#setThumbnailPosition(int, int)
     */
	void setThumbnailPosition(int x, int y) {
		for (MediaItem media : mediaItems) {
			int tX = 1+media.mediaId*200;
			media.setThumbnailPosition((int)(x+tX), y+30);
		}
	}

    /**
     * Renders all media items in this scene if the scene is active.
     * Only processes rendering when the scene is in active state.
     *
     * @see MediaItem#render()
     * @see #isActive
     */
	void render() {
		if (isActive) {
			for (MediaItem media : mediaItems) {
                media.render();
			}

            for (int i = 0; i<mediaItems.size();i++){
                if( mediaItems.get(i).toBeDeleted) delMedia(mediaItems.get(i).mediaId);
            }
		}
	}

    /**
     * Adds a media item to this scene.
     * If no media is currently selected for calibration, sets the new media
     * as the current calibration target.
     *
     * @param newMedia The media item to add to the scene
     */
	void addMedia(MediaItem newMedia) {
		mediaItems.add(newMedia);
		if (currentMediaCalibration < 0)
			currentMediaCalibration = 0;
	}

    /**
     * Removes a media item from this scene by index.
     *
     * @param index The index of the media item to remove
     */
    void delMedia(int index) {
        mediaItems.get(index).stopMedia();
        mediaItems.get(index).deleteControls();
        mediaItems.remove(index);
    }

    /**
     * Serializes the scene and all its media items to XML for project saving.
     *
     * @return XML element containing all scene media configuration
     * @see MediaItem#saveXML()
     * @see Project#saveToFile()
     */
	XML saveXML() {
		XML xml = new XML("Scene");
		for (MediaItem media : mediaItems) {
			xml.addChild(media.saveXML());
		}
		return xml;
	}

    /**
     * Deactivates this scene and all its media items.
     * Stops media playback, turns off calibration mode, and hides controls
     * for all media items in the scene.
     *
     * @see MediaItem#stopMedia()
     * @see MediaItem#offCalibration()
     * @see MediaItem#hideControls()
     */
	public void deactivate() {
		for (MediaItem media : mediaItems) {
			media.stopMedia();
			media.offCalibration();
			media.hideControls();
		}
		isActive = false;
	}

    /**
     * Activates this scene and all its media items.
     * Starts media playback and shows controls for all media items in the scene.
     *
     * @see MediaItem#playMedia()
     * @see MediaItem#showControls()
     */
	public void activate() {
		for (MediaItem media : mediaItems) {
			media.playMedia();
			media.showControls();
		}
		isActive = true;
	}

    /**
     * Toggles the activation state of this scene.
     * If activating, starts playback for all media items.
     * If deactivating, stops playback for all media items.
     *
     * @see #activate()
     * @see #deactivate()
     */
	public void toggleActivation() {
		isActive = !isActive;
		if (isActive) {
			for (MediaItem media : mediaItems) {
				media.playMedia();
			}
		} else {
			for (MediaItem media : mediaItems) {
				media.stopMedia();
			}
		}
	}
}