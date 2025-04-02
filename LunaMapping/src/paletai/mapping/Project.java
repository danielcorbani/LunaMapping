package paletai.mapping;

import processing.core.*;
import java.util.ArrayList;

/**
 * The main container class that manages multiple scenes and transitions.
 * Handles the overall project structure, scene navigation, and rendering pipeline.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Scene management and organization</li>
 *   <li>Scene transitions with fade effects</li>
 *   <li>Project serialization (future implementation)</li>
 *   <li>Global project state management</li>
 * </ul>
 * 
 * @author Daniel Corbani
 * @version 1.0
 * @see Scene
 */
public class Project {
	/** Parent Processing applet */
	private PApplet p;
	/** List of scenes in this project */
    private ArrayList<Scene> scenes;
    /** Index of currently active scene */
    private int activeSceneIndex;
    /** Transition state flag */
    private boolean transitioning;
    /** Current transition opacity (0-255) */
    private float transitionAlpha;
    /** Initialization state flag */
    private boolean isInitialized;
	
    /**
     * Constructs a new Project container.
     * 
     * @param p Parent Processing applet
     */
    public Project(PApplet p) {
        this.p = p;
        this.scenes = new ArrayList<>();
        this.activeSceneIndex = -1; // No scene active at start
        this.transitioning = false;
        this.transitionAlpha = 0;
        this.isInitialized = false;
    }
	

    /**
     * Adds an existing scene to the project.
     * 
     * @param scene Scene to add
     */
	public void addScene(Scene scene) {
		scenes.add(scene);
	}
	
	/**
     * Creates and adds a new empty scene to the project.
     * Automatically assigns a unique ID based on current scene count.
     */
	public void addEmptyScene() {
	    Scene newScene = new Scene(p, scenes.size() + 1); // Unique ID for new Scene
	    scenes.add(newScene);
	    System.out.println("New scene added! Total scenes: " + scenes.size());
	}
	
	/**
     * Activates the first scene in the project.
     * Only effective if no scene is currently active.
     */
	public void startFirstScene() {
        if (!scenes.isEmpty() && activeSceneIndex == -1) {
            activeSceneIndex = 0;
            scenes.get(activeSceneIndex).setActive(true);
        }
    }
	/**
     * Main rendering method for the project.
     * Handles both regular rendering and transition effects.
     * 
     * @param mouseX Current mouse X position (for hover detection)
     * @param mouseY Current mouse Y position (for hover detection)
     */
	public void render(int mouseX, int mouseY) {
        if (!scenes.isEmpty()) {
            if (!isInitialized) {
                // Display waiting message before activation
                if (activeSceneIndex == -1) {
                    p.textAlign(PApplet.CENTER, PApplet.CENTER);
                    p.textSize(24);
                    p.fill(255);
                    p.text("Press SPACE to start", p.width / 2, p.height / 2);
                    return;
                }
                isInitialized = true;
            }

            // Render active scene
            scenes.get(activeSceneIndex).render(mouseX, mouseY);

            // Handle transitions
            if (transitioning) {
                transitionEffect(mouseX, mouseY);
            }
        }
    }
	/**
     * Initiates transition to the next scene.
     * Uses a fade effect between scenes.
     */
	private void transitionEffect(int mouseX, int mouseY) {
        transitionAlpha += 5;
        int nextSceneIndex = (activeSceneIndex + 1) % scenes.size();

        // Activate next scene while still transitioning
        scenes.get(nextSceneIndex).setActive(true);

        p.pushStyle();
        p.tint(255, transitionAlpha);
        scenes.get(nextSceneIndex).render(mouseX, mouseY);
        p.popStyle();

        if (transitionAlpha >= 255) {
            scenes.get(activeSceneIndex).setActive(false); // Deactivate previous scene
            activeSceneIndex = nextSceneIndex;
            transitioning = false;
            transitionAlpha = 0;
        }
    }
	
	/**
     * Initiates transition to the next scene.
     * Uses a fade effect between scenes.
     */
    public void nextScene() {
        if (!scenes.isEmpty() && !transitioning) {
            transitioning = true;
            transitionAlpha = 0;
        }
    }
	
    /**
     * Toggles calibration mode for the active scene.
     */
	public void toggleCalibration() {
		if (!scenes.isEmpty() && activeSceneIndex != -1) {
            scenes.get(activeSceneIndex).toggleCalibration();
        }
	}

	public void moveHoverPoint(int mouseX, int mouseY) {
		if (!scenes.isEmpty() && activeSceneIndex != -1) {
            scenes.get(activeSceneIndex).moveHoverPoint(mouseX, mouseY);
        }
	}

	/**
     * Saves the current project configuration.
     * (Future implementation placeholder)
     */
	public void saveProject() {
		System.out.println("Project saved!");
		// Future implementation will save:
        // - Scene configurations
        // - Media paths
        // - Homography data
	}

	// ✅ Load a saved project
	public void loadProject() {
		// Future implementation: Load previous scene configurations
		System.out.println("Project loaded!");
	}
	
	public ArrayList<Scene> getScenes() {
	    return scenes;
	}

	public int getActiveSceneIndex() {
	    return activeSceneIndex;
	}

	public void setActiveScene(int index) {
		if (!scenes.isEmpty() && activeSceneIndex == -1) {
            activeSceneIndex = index;
            scenes.get(activeSceneIndex).setActive(true);
        }
	}
	
	
    // ===== PRIVATE METHODS =====

    /**
     * Renders the startup prompt before first activation.
     */
    private void renderStartPrompt() {
        if (activeSceneIndex == -1) {
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.textSize(24);
            p.fill(255);
            p.text("Press SPACE to start", p.width / 2, p.height / 2);
        }
    }
}
