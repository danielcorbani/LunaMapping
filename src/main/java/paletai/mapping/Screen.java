package paletai.mapping;

import java.awt.Rectangle;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.data.XML;
import processing.opengl.PGraphics2D;

/**
 * Represents a physical or virtual display screen for video mapping.
 *
 * <p>The Screen class manages the rendering of media content to a specific display,
 * handling both normal rendering and transitions between scenes. Each screen can
 * be assigned to a physical display or used as a virtual screen for preview.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 * <li>Display assignment and configuration</li>
 * <li>Media content rendering with transition effects</li>
 * <li>Offscreen buffer management for efficient rendering</li>
 * <li>Mouse interaction handling for media items</li>
 * <li>XML serialization for project saving/loading</li>
 * </ul>
 *
 * @author Daniel Corbani
 * @version 1.0
 * @see MediaItem
 * @see Project
 * @see Rectangle
 */
public class Screen {
    /** The parent PApplet instance for rendering */
    private PApplet p;

    /** Unique identifier for this screen */
    int id;

    /** Screen dimensions and position in virtual space */
    int w, h, x, y;

    /** XML configuration for serialization */
    XML screenXML;

    /** Media items currently displayed on this screen */
    ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();

    /** Media items for the next scene during transitions */
    ArrayList<MediaItem> nextMediaItems = new ArrayList<MediaItem>();

    /** Offscreen buffers for current and next scene rendering */
    public PGraphics2D pg, nextPg;

    /** Whether this screen is assigned to a physical display */
    boolean isAssigned;

    /** Currently active scene index */
    int currentScene;

    /** Transition state management */
    private boolean inTransition = false;
    private float transitionAlpha = 1.0f;

    /**
     * Constructs a new Screen with the specified ID.
     * Initializes with default unassigned dimensions and creates offscreen buffers.
     *
     * @param p The parent PApplet instance
     * @param id The unique identifier for this screen
     */

	public Screen(PApplet p, int id) {
		this.p = p;
		this.id = id;
		unassign();
		screenXML = new XML("Screen");
		screenXML.setInt("id", id);
		// screenXML.addChild("Medias");
		currentScene = 0;
	}

    /**
     * Constructs a Screen from XML configuration.
     * Recreates a screen from saved project data.
     *
     * @param p The parent PApplet instance
     * @param screen XML element containing screen configuration
     */
	Screen(PApplet p, XML screen) {
		this.p = p;
		this.id = screen.getInt("id");
		unassign();
		screenXML = screen;
		currentScene = 0;
	}

    /**
     * Serializes the screen configuration to XML for project saving.
     *
     * @return XML element containing screen ID and configuration
     * @see Project#saveToFile()
     */
	XML saveXML() {
		XML xml = new XML("Screen");
		xml.setInt("id", id);
		return xml;
	}

    /**
     * Updates the media list for normal rendering without transitions.
     * Replaces the current media items and exits transition state.
     *
     * @param m The new list of media items to display
     */
	public void updateMediaList(ArrayList<MediaItem> m) {
		this.mediaItems = new ArrayList<MediaItem>(m);
		this.inTransition = false;
	}

    /**
     * Sets up transition state between two sets of media items.
     * Stores both current and next scene media for cross-fade transition.
     *
     * @param currentMedia Media items from the current scene
     * @param nextMedia Media items from the next scene
     * @param progress Transition progress (0.0 to 1.0)
     * @see #updateMediaList(ArrayList)
     */
	public void setTransitionState(ArrayList<MediaItem> currentMedia, ArrayList<MediaItem> nextMedia, float progress) {
		this.mediaItems = new ArrayList<MediaItem>(currentMedia);
		this.nextMediaItems = new ArrayList<MediaItem>(nextMedia);
		this.inTransition = true;
		this.transitionAlpha = progress;
	}

    /**
     * Unassigns this screen from any physical display.
     * Resets to default dimensions (1280x720) and recreates offscreen buffers.
     * Updates all assigned media items with the new dimensions.
     *
     * @see #assignToDisplay(Rectangle)
     */
	void unassign() {
		this.w = 1280;
		this.h = 720;
		this.x = 0;
		this.y = 0;
		isAssigned = false;
		//if (pg != null) pg.dispose();
		//if (nextPg != null) nextPg.dispose();
		pg = (PGraphics2D) p.createGraphics(w, h, PConstants.P2D);
		nextPg = (PGraphics2D) p.createGraphics(w, h, PConstants.P2D);
		for (MediaItem media : mediaItems) {
			media.assignToDisplay(w, h, id);
		}
	}

    /**
     * Assigns this screen to a physical display with specified bounds.
     * Configures screen dimensions and position based on display geometry,
     * recreates offscreen buffers, and updates all assigned media items.
     *
     * @param bounds The display bounds to assign this screen to
     * @see #unassign()
     * @see Rectangle
     */
	public void assignToDisplay(Rectangle bounds) {
		this.w = bounds.width;
		this.h = bounds.height;
		this.x = bounds.x;
		this.y = bounds.y;
		isAssigned = true;
		//if (pg != null) pg.dispose();
		//if (nextPg != null) nextPg.dispose();
		pg = (PGraphics2D) p.createGraphics(w, h, PConstants.P2D);
		nextPg = (PGraphics2D) p.createGraphics(w, h, PConstants.P2D);
		for (MediaItem media : mediaItems) {
			media.assignToDisplay(w, h, id);
		}
	}

    /**
     * Renders the screen content and displays it at the correct position.
     * Updates the offscreen buffer with current media and shows the result.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see #updateGraphics(int, int)
     * @see #show()
     */
	public void render(int mousex, int mousey) {
		int localMousex = mousex;
		int localMousey = mousey;
		// Update the offscreen buffer
		updateGraphics(localMousex, localMousey);
		show();
	}

    /**
     * Displays the screen buffer at its assigned position.
     * Only renders if the screen is assigned to a physical display.
     */
	public void show() {
		// Draw the buffer at the correct position
		if (isAssigned) {
			p.image(pg, x, y);
		}
	}

    /**
     * Updates the offscreen graphics buffer with current media content.
     * Handles both normal rendering and transition effects based on current state.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see #renderNormalMedia(int, int)
     * @see #renderTransitionMedia(int, int)
     */
	void updateGraphics(int mousex, int mousey) {
		pg.beginDraw();
		pg.background(0);
		if (inTransition) {
            // Render transition: current scene fading out, next scene fading in
            renderTransitionMedia(mousex, mousey);
        } else {
            // Normal rendering
            renderNormalMedia(mousex, mousey);
        }
		
		pg.endDraw();
	}

    /**
     * Renders media items in normal (non-transition) state.
     * Applies hover effects and draws all assigned media to the screen buffer.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see MediaItem#checkHover(int, int)
     * @see MediaItem#getMediaCanvas()
     */
	private void renderNormalMedia(int mousex, int mousey) {
        for (MediaItem media : mediaItems) {
            if (media.assignedScreen == id) {
                media.checkHover(mousex, mousey);
                pg.image(media.getMediaCanvas(), 0, 0, pg.width, pg.height);
            }
        }
    }

    /**
     * Renders media items during scene transitions with cross-fade effect.
     * Fades out current scene media while fading in next scene media.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see MediaItem#checkHover(int, int)
     * @see MediaItem#getMediaCanvas()
     */
	private void renderTransitionMedia(int mousex, int mousey) {
        // Render current scene media (fading out)
        for (MediaItem media : mediaItems) {
            if (media.assignedScreen == id) {
                media.checkHover(mousex, mousey);
                pg.tint(255, 255 * (1 - transitionAlpha)); // Fade out
                pg.image(media.getMediaCanvas(), 0, 0, pg.width, pg.height);
                pg.noTint();
            }
        }
        
        // Render next scene media (fading in)
        for (MediaItem media : nextMediaItems) {
            if (media.assignedScreen == id) {
                media.checkHover(mousex, mousey);
                pg.tint(255, 255 * transitionAlpha); // Fade in
                pg.image(media.getMediaCanvas(), 0, 0, pg.width, pg.height);
                pg.noTint();
            }
        }
    }

    /**
     * Sets the preview area dimensions for media item preview rendering.
     * Propagates the preview area configuration to all assigned media items.
     *
     * @param px Preview area x-coordinate
     * @param py Preview area y-coordinate
     * @param pw Preview area width
     * @param ph Preview area height
     * @see MediaItem#setPreviewArea(float, float, float, float)
     */
	public void setPreviewArea(float px, float py, float pw, float ph) {
		for (MediaItem media : mediaItems) {
			media.setPreviewArea(px, py, pw, ph);
			// media.moveHoverPoint(mousex, mousey);
		}
	}

    /**
     * Updates hover point position for all media items on this screen.
     * Used for interactive media elements that respond to mouse movement.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see MediaItem#moveHoverPoint(float, float)
     */
	public void moveHoverPoint(float mousex, float mousey) {
		for (MediaItem media : mediaItems) {
			media.moveHoverPoint(mousex, mousey);
		}
	}

    /**
     * Returns the offscreen graphics buffer for this screen.
     *
     * @return PGraphics2D buffer containing the rendered screen content
     */
	public PGraphics2D getScreen() {
		return pg;
	}

}