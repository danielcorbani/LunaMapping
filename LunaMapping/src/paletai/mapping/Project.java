package paletai.mapping;

import processing.core.*;
import java.util.ArrayList;

public class Project {
	private PApplet p;
    private ArrayList<Scene> scenes;
    private int activeSceneIndex;
    private boolean transitioning;
    private float transitionAlpha;
    private boolean isInitialized;
	
    public Project(PApplet p) {
        this.p = p;
        this.scenes = new ArrayList<>();
        this.activeSceneIndex = -1; // No scene active at start
        this.transitioning = false;
        this.transitionAlpha = 0;
        this.isInitialized = false;
    }
	

	// ✅ Add a Scene to the Project
	public void addScene(Scene scene) {
		scenes.add(scene);
	}
	
	public void addEmptyScene() {
	    Scene newScene = new Scene(p, scenes.size() + 1); // Unique ID for new Scene
	    scenes.add(newScene);
	    System.out.println("New scene added! Total scenes: " + scenes.size());
	}
	
	public void startFirstScene() {
        if (!scenes.isEmpty() && activeSceneIndex == -1) {
            activeSceneIndex = 0;
            scenes.get(activeSceneIndex).setActive(true);
        }
    }

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

    public void nextScene() {
        if (!scenes.isEmpty() && !transitioning) {
            transitioning = true;
            transitionAlpha = 0;
        }
    }
	
	// ✅ Mouse Interaction
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

	// ✅ Save the project (Scenes & Configurations)
	public void saveProject() {
		// Future implementation: Save scene configurations, media paths, homography,
		// etc.
		System.out.println("Project saved!");
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
}
