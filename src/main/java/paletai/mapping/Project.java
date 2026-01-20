package paletai.mapping;

import controlP5.ControlP5;
import processing.core.*;
import java.util.ArrayList;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.io.File;
import processing.opengl.PGraphics2D;
import controlP5.*;
import paletai.generators.LunaContentGenerator;
import processing.data.XML;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * The main container class that manages multiple scenes and transitions.
 * Handles the overall project structure, scene navigation, and rendering
 * pipeline for the Luna Video Mapping system.
 *
 * <p>Key features include:</p>
 * <ul>
 * <li>Scene management and organization</li>
 * <li>Screen configuration and display management</li>
 * <li>Media file discovery and management</li>
 * <li>Content generator discovery and integration</li>
 * <li>Project serialization to/from XML</li>
 * <li>Scene transitions with fade effects</li>
 * <li>Global project state management</li>
 * </ul>
 *
 * @author Daniel Corbani
 * @version 1.0
 * @see Scene
 * @see Screen
 * @see MediaItem
 * @see LunaContentGenerator
 */
public class Project {
    /** The main PApplet instance */
    PApplet mainApplet; // needed for control P5

    /** Name of the project as specified by the user */
    String projectName;

    /** List of all scenes in the project */
    ArrayList<Scene> scenes = new ArrayList<Scene>();

    /** Index of the currently active scene */
    int currentScene = 0;

    /** List of all configured screens/displays */
    ArrayList<Screen> screens = new ArrayList<Screen>();

    /** Index of the currently selected screen */
    int currentScreen = 0;

    /** Stores available external display configurations */
    ArrayList<Rectangle> availableDisplays = new ArrayList<Rectangle>();

    /** XML configuration for project serialization */
    XML config;

    /** Main PGraphics buffer for UI rendering */
    PGraphics2D canvaUI;

    /** Main display dimensions used for UI layout */
    int mainWidth, mainHeight;

    /** Handle positions and radius for panel separation */
    int hx1, hx2, hy1, hy2, r;

    /** List of discovered media files in the data directory */
    ArrayList<String> mediaFiles = new ArrayList<String>();

    /** ControlP5 instance for UI controls */
    ControlP5 cp5;

    /** UI control groups for organization */
    Group mediaList, screenList, displaysList, sceneList, generatorList;

    /** Radio buttons for screen and scene selection */
    RadioButton screenRadio, sceneRadio;

    /** Area reserved for screen buttons */
    int screenButtonsArea = 30;

    /** Flags for UI state management */
    boolean addSelectScreenBool = false, removeScreenBol = false;
    boolean addSceneBool = false, removeSceneBol = false;

    /** Preview area dimensions for screen panel */
    float previewAreaX, previewAreaY, previewAreaWidth, previewAreaHeight,
          previewWidth, previewHeight, previewX, previewY;

    /** Flags for Preview show management */
    boolean showPreview = true;

    /** Scene transition management */
    private int nextScene = -1;
    private boolean isTransitioning = false;
    private int transitionStartTime;
    private int transitionDuration = 2000; // 2 second transition
    private float transitionProgress = 0;

    /** Available content generators discovered through reflection */
    private ArrayList<LunaContentGenerator> availableGenerators;

    /**
     * Clipboard for storing homography configuration to be copied between MediaItems
     */
    private PVector[] copiedXY = null;
    private PVector[] copiedUV = null;
    private String copiedConfigName = "";

    /**
     * Constructs a new Project instance with the specified name.
     * Initializes displays, scans for media files and generators, loads configuration,
     * and sets up the user interface.
     *
     * @param p The parent PApplet instance (typically 'this' from the sketch)
     * @param name The name of the project. If null or empty, defaults to "untitled"
     * @see #initializeDisplays()
     * @see #scanMediaFiles()
     * @see #scanGenerators()
     * @see #initXMLconfig()
     * @see #initializeButtons()
     */
	public Project(PApplet p, String name) {
		mainApplet = p;
		projectName = (name == null || name.trim().isEmpty()) ? "untitled" : name.trim();
		cp5 = new ControlP5(mainApplet); // must be called before creating buttons;
		initializeDisplays();
		mainApplet.image(canvaUI, 0, 0);
		scanMediaFiles();
        scanGenerators();
		initXMLconfig();
		initializeButtons();
	}

    /**
     * Initializes available displays and sets up the main UI canvas.
     *
     * <p>This method detects all connected displays, configures the main display for UI,
     * and stores information about external displays for potential screen configuration.
     * The main UI canvas is initialized with default dimensions and background.</p>
     *
     * @see GraphicsEnvironment
     * @see GraphicsDevice
     */
	void initializeDisplays() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		availableDisplays.clear(); // Clear previous display info

		for (int i = 0; i < devices.length; i++) {
			Rectangle bounds = devices[i].getDefaultConfiguration().getBounds();
			if (i == 0) { // initialize UI
				mainWidth = bounds.width;
				mainHeight = bounds.height;
				hx1 = mainWidth / 6;
				hx2 = mainWidth - hx1;
				hy1 = 30;
				hy2 = 2 * mainHeight / 4;
				r = 10;
				previewAreaX = hx1 + 20;
				previewAreaY = hy1 + 20;
				previewAreaWidth = hx2 - hx1 - 40;
				previewAreaHeight = hy2 - hy1 - 40 - screenButtonsArea; // Room for buttons
				// Create an offscreen buffer matching main screen size
				canvaUI = (PGraphics2D) mainApplet.createGraphics(mainWidth, mainHeight, PConstants.P2D);
				canvaUI.beginDraw();
				canvaUI.background(33);
				canvaUI.textSize(20);
				canvaUI.fill(200);
				canvaUI.textAlign(PConstants.CENTER, PConstants.CENTER);
				canvaUI.text("Luna Video Mapping", (float) canvaUI.width / 2, (float) canvaUI.height / 2);
				canvaUI.endDraw();
			} else {
				// Store external display info without creating screens
				availableDisplays.add(bounds);
				PApplet.println("Found external display #" + i + ": " + bounds.width + "x" + bounds.height);
			}
		}
	}

    /**
     * Scans the data directory for supported media files.
     *
     * <p>This method searches the sketch's data folder for files with supported
     * media extensions and adds them to the mediaFiles list for later use.</p>
     *
     * @see #isMediaFile(String)
     * @see PApplet#sketchPath(String)
     */
	void scanMediaFiles() {
		File dataDir = new File(mainApplet.sketchPath("data"));
		if (dataDir.exists() && dataDir.isDirectory()) {
			File[] files = dataDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isMediaFile(file.getName())) {
                        mediaFiles.add(file.getName());
                    }
                }
            } else {
                // Optional: log a warning
                PApplet.println("Warning: Could not read files from data directory");
            }
		}
	}

    /**
     * Checks if a filename has a supported media extension.
     *
     * @param filename The name of the file to check
     * @return true if the file has a supported media extension (.mp4, .mov, .png, .jpg, .jpeg, .gif)
     */
	boolean isMediaFile(String filename) {
		String[] supportedExtensions = { ".mp4", ".mov", ".png", ".jpg", ".jpeg", ".gif" };
		String lowerName = filename.toLowerCase();
		for (String ext : supportedExtensions) {
			if (lowerName.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

    /**
     * Discovers available content generators through reflection.
     *
     * <p>This method scans the main applet's inner classes for implementations of
     * LunaContentGenerator that are not abstract or interfaces, and instantiates
     * them for use in the project.</p>
     *
     * @see LunaContentGenerator
     * @see Modifier
     * @see Constructor
     */
    private void scanGenerators() {
        availableGenerators = new ArrayList<LunaContentGenerator>();
        PApplet.println("=== Luna Generator Discovery ===");

        try {
            Class<?>[] declaredClasses = mainApplet.getClass().getDeclaredClasses();
            for (Class<?> clazz : declaredClasses) {
                if (LunaContentGenerator.class.isAssignableFrom(clazz) &&
                        !clazz.isInterface() &&
                        !Modifier.isAbstract(clazz.getModifiers())) {
                    try {
                        java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor(mainApplet.getClass());
                        constructor.setAccessible(true); // This is the key!
                        LunaContentGenerator generator = (LunaContentGenerator) constructor.newInstance(mainApplet);
                        availableGenerators.add(generator);

                    } catch (Exception e) {
                        PApplet.println("! Instantiation failed: " + e.getMessage());
                        e.printStackTrace(); // This will show the exact error
                    }
                }
            }

        } catch (Exception e) {
            PApplet.println("Error during discovery: " + e.getMessage());
        }

        PApplet.println("Total generators found: " + availableGenerators.size());
        PApplet.println("=== Discovery Complete ===\n");
    }

    /**
     * Returns the list of discovered content generators.
     *
     * @return ArrayList of available LunaContentGenerator instances
     * @see LunaContentGenerator
     */
    public ArrayList<LunaContentGenerator> getGenerators(){
        return availableGenerators;
    }

    /**
     * Initializes project configuration from XML file.
     *
     * <p>Attempts to load an existing project configuration from the data directory.
     * If no existing configuration is found, creates a new project with default
     * screen and scene.</p>
     *
     * @see #loadProjectFromXML(XML)
     * @see #addNewScreen()
     * @see #addNewScene()
     */
	void initXMLconfig() {
		String filename = "data/" + projectName + ".xml";
		File file = new File(mainApplet.sketchPath(filename));
		if (file.exists()) {
			try {
				config = mainApplet.loadXML(filename);
				loadProjectFromXML(config);
				PApplet.println("Loaded existing project: " + projectName);
			} catch (Exception e) {
				PApplet.println("Error loading project: " + e);
			}
		} else {
			PApplet.println(filename + " doesn't exist");
			addNewScreen();
			addSelectScreenBool = false; // avoids duplicated button on initialization
			addNewScene();
			addSceneBool = false; // avoids duplicated button on initialization
			PApplet.println("New Project created: " + projectName);
		}
	}

    /**
     * Loads project data from XML configuration.
     *
     * <p>Reconstructs the project state including screens, scenes, and media items
     * from the provided XML structure. Handles both traditional media files and
     * generative content.</p>
     *
     * @param xml The XML element containing project configuration
     * @see Screen
     * @see Scene
     * @see MediaItem
     * @see GenerativeMediaItem
     */
	void loadProjectFromXML(XML xml) {
		XML ScreensParent = xml.getChild("Screens");
		XML[] Screens = ScreensParent.getChildren("Screen");
		for (XML Screen : Screens) {
			addNewScreen();
		}
		addSelectScreenBool = false;
		XML ScenesParent = xml.getChild("Scenes");
		XML[] Scenes = ScenesParent.getChildren("Scene");
		for (XML Scene : Scenes) {
			Scene newScene = new Scene();
			XML[] Medias = Scene.getChildren("MediaItem");
			// PApplet.printArray(Medias);
			for (XML Media : Medias) {
                int isGen = Media.getInt("isGenerator");
                if (isGen >0) {
                    int newMediaId = newScene.mediaItems.size();
                    String MediaGeneratorName = Media.getString("name");
                    PApplet.println(MediaGeneratorName);
                    for (LunaContentGenerator generator : availableGenerators){
                        if(Objects.equals(MediaGeneratorName, generator.getName())){
                            GenerativeMediaItem newMedia = new GenerativeMediaItem(mainApplet, generator, Media);
                            int screenId = Media.getInt("Screen");
                            newMedia.assignToDisplay(screens.get(screenId).w, screens.get(screenId).h, screenId);
                            newScene.addMedia(newMedia);
                        }
                    }
                } else {
                    MediaItem newMedia = new MediaItem(mainApplet, Media);
                    int screenId = Media.getInt("Screen");
                    newMedia.assignToDisplay(screens.get(screenId).w, screens.get(screenId).h, screenId);
                    newScene.addMedia(newMedia);
                }
			}
			scenes.add(newScene);
		}
		addSceneBool = false; // avoids duplicated button on initialization
	}

    /**
     * Saves the current project state to an XML file.
     *
     * <p>Serializes all project data including screens, scenes, and media items
     * to an XML file in the data directory. The file is named after the project.</p>
     *
     * @see Screen#saveXML()
     * @see Scene#saveXML()
     * @see PApplet#saveXML(XML, String)
     */
	void saveToFile() {
//		PApplet.println("save to file");
		config = new XML(projectName);
		config.setString("name", projectName);
		config.setString("type", "Luna Video Mapping project"); // May be used to check if the existing XML file was
																// created here
		config.addChild("Screens");
		XML ScreensParent = config.getChild("Screens");
		for (Screen screen : screens) {
			ScreensParent.addChild(screen.saveXML());
		}
		config.addChild("Scenes");
		XML ScenesParent = config.getChild("Scenes");
		for (Scene scene : scenes) {
			ScenesParent.addChild(scene.saveXML());
		}
		mainApplet.saveXML(config, "data/" + projectName + ".xml");
	}

    /**
     * Creates a new screen and adds it to the project.
     * Sets the flag to update ControlP5 buttons on the next cycle.
     *
     * @see Screen
     */
	void addNewScreen() {
		int newScreenId = screens.size();
		Screen newScreen = new Screen(mainApplet, newScreenId);
		screens.add(newScreen); // add to ArrayList
		addSelectScreenBool = true; // tell ControlP5 to update next time
	}

    /**
     * Deletes the currently selected screen from the project.
     * Maintains at least one screen in the project. Updates the current screen
     * index and selects the appropriate screen after deletion.
     *
     * @see #selectScreen(int)
     */
    void DelCurScreen() {
        if (screens.size() > 1) {
            screens.remove(currentScreen);
            // RemoveSelectSceneButton(currentScene);
        }
        removeScreenBol = true;
        // Update current scene index if needed
        if (currentScreen >= screens.size()) {
            currentScreen = screens.size() - 1;
        }

        // Select the appropriate scene
        selectScreen(currentScreen);
        //PApplet.println("Delete Screen " + currentScreen);

    }

    /**
     * Creates a new scene and adds it to the project.
     * Sets the flag to update ControlP5 buttons on the next cycle.
     *
     * @see Scene
     */
	void addNewScene() {
		// int newSceneId = scenes.size();
		// println("Adding a new Scene with index " + newId);
		Scene newScene = new Scene();
		scenes.add(newScene);
		// PApplet.println("New Scene added to current Screen");
		addSceneBool = true;
	}

    /**
     * Deletes the currently selected scene from the project.
     * Maintains at least one scene in the project. Deactivates the scene before
     * removal and updates the current scene index after deletion.
     *
     * @see Scene#deactivate()
     * @see #selectScene(int)
     */
	void DelCurScene() {
		if (scenes.size() > 1 && currentScene >= 0 && currentScene < scenes.size()) {
			scenes.get(currentScene).deactivate();
			scenes.remove(currentScene);
			// RemoveSelectSceneButton(currentScene);
		}
		// PApplet.println("New Scene added to current Screen");
		removeSceneBol = true;

		// Update current scene index if needed
		if (currentScene >= scenes.size()) {
			currentScene = scenes.size() - 1;
		}

		// Select the appropriate scene
		selectScene(currentScene);
	}

    /**
     * Adds a media file to the current scene.
     * Creates a new MediaItem, assigns it to the current screen, and updates
     * the thumbnail position in the scene.
     *
     * @param name The filename of the media to add
     * @see MediaItem
     * @see Scene#addMedia(MediaItem)
     * @see MediaItem#assignToDisplay(int, int, int)
     */
	void addMedia(String name) {
		int newMediaId = scenes.get(currentScene).mediaItems.size();
		MediaItem newMedia = new MediaItem(mainApplet, name, currentScreen, newMediaId);
		newMedia.assignToDisplay(screens.get(currentScreen).w, screens.get(currentScreen).h, currentScreen);
		scenes.get(currentScene).addMedia(newMedia);
		scenes.get(currentScreen).setThumbnailPosition(2 * r, hy2);
	}

    /**
     * Adds a generative content generator to the current scene.
     * Creates a new GenerativeMediaItem using the provided generator, assigns it
     * to the current screen, and updates the thumbnail position in the scene.
     *
     * @param newGenerator The content generator to add to the scene
     * @see GenerativeMediaItem
     * @see LunaContentGenerator
     * @see Scene#addMedia(MediaItem)
     */
    void addGenerator(LunaContentGenerator newGenerator) {
        int newMediaId = scenes.get(currentScene).mediaItems.size();
        //PApplet.println("Add generator?" + newMediaId);
        GenerativeMediaItem newMedia = new GenerativeMediaItem(mainApplet, newGenerator, currentScreen, newMediaId);
        newMedia.assignToDisplay(screens.get(currentScreen).w, screens.get(currentScreen).h, currentScreen);
        scenes.get(currentScene).addMedia(newMedia);
        scenes.get(currentScene).setThumbnailPosition(2 * r, hy2);
        // saveToFile();
        // PApplet.println(name + " added to current Scene");
    }

    /**
     * Updates ControlP5 UI elements based on project state changes.
     * Handles adding new screens/scenes and removing existing ones by
     * recreating the appropriate button groups.
     *
     * @see #addSelectScreenButton(int)
     * @see #addSelectSceneButton(int)
     * @see #createSceneButtons()
     * @see #createScreenButtons()
     */
	void updateCP5() {
		if (addSelectScreenBool) {
			addSelectScreenButton(screens.size() - 1);
			addSelectScreenBool = false;
		}
		if (addSceneBool) {
			addSelectSceneButton(scenes.size() - 1);
			addSceneBool = false;
		}
		if (removeSceneBol) {
			// RemoveSelectSceneButton(currentScene);
			createSceneButtons();
			removeSceneBol = false;
		}
        if (removeScreenBol) {
            createScreenButtons();
            removeScreenBol = false;
        }

	}

    /**
     * Initializes all ControlP5 UI components for the project.
     * Creates display assignment buttons, screen controls, scene controls,
     * media selection buttons, and generator buttons.
     *
     * @see #createAssignDisplayButtons()
     * @see #createScreenButtons()
     * @see #createSceneButtons()
     * @see #createMediaButtons()
     * @see #createGeneratorButtons()
     */
	void initializeButtons() {
		// PApplet.println("Initializing buttons");
		createAssignDisplayButtons();
		createScreenButtons();
		createSceneButtons();
		createMediaButtons();
        createGeneratorButtons();
	}

    /**
     * Creates buttons for assigning screens to external displays.
     * Generates one button per available external display with appropriate
     * styling and callback functionality.
     *
     * @see #assignDisplay(Rectangle)
     * @see ControlP5
     * @see Bang
     */
	void createAssignDisplayButtons() {  
	    int DisplayButtonX = hx2 - 10 * r;
	    int DisplayButtonHeight = 40;

	    // group container
	    if (displaysList != null) displaysList.remove();
	    displaysList = cp5.addGroup("Display List")
	        .setPosition(DisplayButtonX, hy1 + 2 * r)
	        .setBackgroundHeight(hy2 - hy1)
	        .disableCollapse()
	        .hideBar();

	    // font (could be declared once globally instead)
	    PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 14, true);

	    // add one button per available display
	    for (int i = 0; i < availableDisplays.size(); i++) {
	        Rectangle b = availableDisplays.get(i);

	        Bang btn = cp5.addBang("Display " + i)
	            .setPosition(0, DisplayButtonHeight * i * 2)
	            .setSize(70, DisplayButtonHeight)  // wider
	            .setGroup(displaysList);

	        // styling
	        btn.getCaptionLabel()
	            .setText("Display " + i)
	            .setFont(myFont)
                    .toUpperCase(false)
	            .align(ControlP5.CENTER, ControlP5.CENTER);
	        btn.setColorBackground(mainApplet.color(60, 60, 60));
		    btn.setColorForeground(mainApplet.color(90, 90, 90));
		    btn.setColorActive(mainApplet.color(120, 200, 120));
		    btn.setColorLabel(mainApplet.color(255));

	        // callback
	        btn.addCallback(new CallbackListener() {
	            public void controlEvent(CallbackEvent event) {
	                if (event.getAction() == ControlP5.ACTION_RELEASE) {
	                    assignDisplay(b);
	                }
	            }
	        });
	    }
	}

    /**
     * Assigns or unassigns the current screen to/from a display.
     * If the screen is not assigned, it will be assigned to the specified display.
     * If already assigned, it will be unassigned. Ensures only one screen per display.
     *
     * @param b The display rectangle to assign to the current screen
     * @see Screen#assignToDisplay(Rectangle)
     * @see Screen#unassign()
     */
	void assignDisplay(Rectangle b) {
		if (!screens.get(currentScreen).isAssigned) {
			for (Screen screen : screens) {
				if (screen.x == b.x) { // check if this screen is assigned to display in question
					screen.unassign();
				}
			}
			screens.get(currentScreen).assignToDisplay(b);
		} else {
			screens.get(currentScreen).unassign();
		}
	}

    /**
     * Creates the screen management UI controls.
     * Includes add/delete screen buttons and radio button group for screen selection.
     * Applies consistent styling and callback handlers to all screen controls.
     *
     * @see #addNewScreen()
     * @see #DelCurScreen()
     * @see #addSelectScreenButton(int)
     * @see ControlP5
     * @see RadioButton
     */
	void createScreenButtons() {
		if (screenList != null) screenList.remove();
		
		screenList = cp5.addGroup("Screen List")
				.setPosition(hx1 + r, hy2 - 2 * r - screenButtonsArea)
				.setBackgroundHeight(screenButtonsArea)
				.disableCollapse();
				//.hideBar();

        PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 14, true);
		//PFont myFont = mainApplet.createFont("Arial", 14, true);

        // ---- Add Screen button ----
        Button showBtn = cp5.addButton("Show Screen")
                .setPosition(hx2 - hx1 - 80 - 4*r, r)
                .setSize(80, screenButtonsArea)   // wider so label fits nicely
                .setCaptionLabel("toggle Preview")
                .setGroup(screenList);

        showBtn.getCaptionLabel()
                .toUpperCase(false)
                .setFont(myFont)
                .align(ControlP5.CENTER, ControlP5.CENTER);
        showBtn.setColorBackground(mainApplet.color(60, 120, 60));
        showBtn.setColorForeground(mainApplet.color(90, 160, 90));
        showBtn.setColorActive(mainApplet.color(120, 200, 120));
        showBtn.setColorLabel(mainApplet.color(255));

        showBtn.addCallback(new CallbackListener() {
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASE) {
                    showPreview = !showPreview;
                }
            }
        });

		 // ---- Add Screen button ----
	    Button addBtn = cp5.addButton("Add Screen")
	        .setPosition(r, r)
	        .setSize(40, screenButtonsArea)   // wider so label fits nicely
	        .setCaptionLabel("+")
	        .setGroup(screenList);

	    addBtn.getCaptionLabel()
                .toUpperCase(false)
	          .setFont(myFont)
	          .align(ControlP5.CENTER, ControlP5.CENTER);
        addBtn.setColorBackground(mainApplet.color(60, 120, 60));
        addBtn.setColorForeground(mainApplet.color(90, 160, 90));
        addBtn.setColorActive(mainApplet.color(120, 200, 120));
        addBtn.setColorLabel(mainApplet.color(255));

	    addBtn.addCallback(new CallbackListener() {
	        public void controlEvent(CallbackEvent event) {
	            if (event.getAction() == ControlP5.ACTION_RELEASE) {
	                addNewScreen();
	            }
	        }
	    });

        // ---- Del Screen button ----
        Button delBtn = cp5.addButton("Del Screen")
                .setPosition(r+50, r)
                .setSize(40, screenButtonsArea)   // wider so label fits nicely
                .setCaptionLabel("-")
                .setGroup(screenList);

        delBtn.getCaptionLabel()
                .setFont(myFont)
                .align(ControlP5.CENTER, ControlP5.CENTER);

        delBtn.setColorBackground(mainApplet.color(150, 60, 60));
        delBtn.setColorForeground(mainApplet.color(200, 90, 90));
        delBtn.setColorActive(mainApplet.color(255, 120, 120));
        delBtn.setColorLabel(mainApplet.color(255));

        delBtn.addCallback(new CallbackListener() {
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASE) {
                    DelCurScreen();
                }
            }
        });
	 // ---- Screen selector (radio buttons) ----
	    if (screenRadio != null) screenRadio.remove();
	    
	    screenRadio = cp5.addRadioButton("ScreenSelector")
	            .setPosition(hx1 + 200, hy2 - r - screenButtonsArea)
	            .setSize(60, screenButtonsArea) // width x height for toggles
	            .setItemsPerRow(6)
	            .setSpacingColumn(70)
	            .setColorBackground(mainApplet.color(80))
	            .setColorForeground(mainApplet.color(120))
	            .setColorActive(mainApplet.color(0, 200, 100))
	            .setNoneSelectedAllowed(true);

		for (Screen screen : screens) {
			addSelectScreenButton(screen.id);
		}
		// PApplet.println("Create Screen buttons");
	}

    /**
     * Adds a screen selection button to the radio button group.
     * Creates a styled toggle button for the specified screen index with
     * selection callback functionality.
     *
     * @param index The screen index to create a button for
     * @see #selectScreen(int)
     * @see Toggle
     * @see RadioButton
     */
	void addSelectScreenButton(int index) {
		// PApplet.println("Entered addSelectScreenButtons");
		String optionName = "" + index;
		screenRadio.addItem(optionName, index)
                .setSize(30, 20)
                .setColorActive(mainApplet.color(0, 150, 255))
				.setColorBackground(mainApplet.color(100))
                .setColorForeground(mainApplet.color(60)).hideBar();

        PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 14, true);
        //PFont myFont = mainApplet.createFont("Arial", 14, true);
		
		// Style the new radio button's label
		screenRadio.getItem(optionName)
			.getCaptionLabel()
			.setFont(myFont)
                .toUpperCase(false)
			.align(ControlP5.CENTER, ControlP5.CENTER)
			.setColor(mainApplet.color(255))
			.setSize(14);
		Toggle t = screenRadio.getItem(optionName);
		if (index == 0) {
			screenRadio.activate(index); // ensures this Toggle is ON
			selectScene(index); // call your logic as if it was clicked
		}
		t.addCallback(new CallbackListener() {
			public void controlEvent(CallbackEvent ev) {
				if (ev.getAction() == ControlP5.ACTION_RELEASE && t.getState()) {
					// println("Screen " + index + " " + t.getState());
					selectScreen(index);
				}
			}
		});
		// PApplet.println("Add select Screen button");
	}

    /**
     * Selects a screen by index and makes it the current active screen.
     *
     * @param index The index of the screen to select
     */
	void selectScreen(int index) {
		if (index >= 0 && index < screens.size()) {
			currentScreen = index;
			// println("Select Screen: " + currentScreen);
		}
	}

    /**
     * Creates the scene management UI controls.
     * Includes add/delete scene buttons and radio button group for scene selection.
     * Applies consistent styling and callback handlers to all scene controls.
     *
     * @see #addNewScene()
     * @see #DelCurScene()
     * @see #addSelectSceneButton(int)
     * @see ControlP5
     * @see RadioButton
     */
	void createSceneButtons() {
	    // remove previous group if it exists
	    if (sceneList != null) sceneList.remove();

	    // group container
	    sceneList = cp5.addGroup("Scenes")
	        .setPosition(r, mainHeight - 8 * r)
	        .setBackgroundHeight(200)
	        .disableCollapse().hideBar();


	 // load a font (you could make this global so it’s reused)
	    //PFont myFont = mainApplet.createFont("Arial", 14, true);
        PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 14, true);
	    sceneList.getCaptionLabel()
	    .setFont(myFont)
	    .setColor(120)
	    .setColorBackground(44).hide();
	    

	    // ---- Add Scene button ----
	    Button addBtn = cp5.addButton("Add Scene")
	        .setPosition(r, r)
	        .setSize(40, screenButtonsArea)    // wider for readability
	        .setCaptionLabel("+")
	        .setGroup(sceneList);

	    addBtn.getCaptionLabel()
	        .setFont(myFont)
	        .align(ControlP5.CENTER, ControlP5.CENTER);
	    addBtn.setColorBackground(mainApplet.color(60, 120, 60));
	    addBtn.setColorForeground(mainApplet.color(90, 160, 90));
	    addBtn.setColorActive(mainApplet.color(120, 200, 120));
	    addBtn.setColorLabel(mainApplet.color(255));

	    addBtn.addCallback(new CallbackListener() {
	        public void controlEvent(CallbackEvent event) {
	            if (event.getAction() == ControlP5.ACTION_RELEASE) {
	                addNewScene();
	            }
	        }
	    });

	    // ---- Delete Scene button ----
	    Button delBtn = cp5.addButton("Del Scene")
	        .setPosition(r + 60, r)  // spaced apart
	        .setSize(40, screenButtonsArea)
	        .setCaptionLabel("-")
	        .setGroup(sceneList);

	    delBtn.getCaptionLabel()
	        .setFont(myFont)
	        .align(ControlP5.CENTER, ControlP5.CENTER);
	    delBtn.setColorBackground(mainApplet.color(150, 60, 60));
	    delBtn.setColorForeground(mainApplet.color(200, 90, 90));
	    delBtn.setColorActive(mainApplet.color(255, 120, 120));
	    delBtn.setColorLabel(mainApplet.color(255));

	    delBtn.addCallback(new CallbackListener() {
	        public void controlEvent(CallbackEvent event) {
	            if (event.getAction() == ControlP5.ACTION_RELEASE) {
	                DelCurScene();
	            }
	        }
	    });

	    // ---- Scene selector (radio button) ----
	    if (sceneRadio != null) sceneRadio.remove();

	    sceneRadio = cp5.addRadioButton("SceneSelector")
	        .setPosition(r + 200, mainHeight - 4 * r)
	        .setSize(40, 25) // width, height of each toggle
	        .setItemsPerRow(20)
	        .setSpacingColumn(50)
	        .setNoneSelectedAllowed(false)
	        .setColorBackground(mainApplet.color(80))
	        .setColorForeground(mainApplet.color(120))
	        .setColorActive(mainApplet.color(0, 200, 100));

	    // apply font/color to all toggles once they’re created
	    for (int i = 0; i < scenes.size(); i++) {
	        addSelectSceneButton(i);
	        Toggle t = sceneRadio.getItem("Scene " + i);
	        t.getCaptionLabel()
	            .setFont(myFont)
	            .align(ControlP5.CENTER, ControlP5.CENTER);
	        t.setColorLabel(mainApplet.color(255));
	    }
	}

    /**
     * Adds a scene selection button to the radio button group.
     * Creates a styled toggle button for the specified scene index with
     * selection callback functionality. Automatically activates the first scene.
     *
     * @param index The scene index to create a button for
     * @see #selectScene(int)
     * @see Toggle
     * @see RadioButton
     */
	void addSelectSceneButton(int index) {
	    String optionName = "Scene " + index;

	    // Create toggle inside the RadioButton group
	    sceneRadio.addItem(optionName, index)
	        .setSize(40, 25)  // width, height of each toggle button
	        .setColorBackground(mainApplet.color(80))   // normal background
	        .setColorForeground(mainApplet.color(120))  // hover
	        .setColorActive(mainApplet.color(171, 128, 255)) // active/selected
	        ; 

	    // Access the toggle just created
	    Toggle t = sceneRadio.getItem(optionName);

	    // ---- Styling label ----
        PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 20, true);
        //PFont myFont = mainApplet.createFont("Arial", 20, true); // load once globally if you prefer
//
	    t.getCaptionLabel().setFont(myFont);
	    t.setColorLabel(mainApplet.color(255)); // label text color (white)
	    t.getCaptionLabel()
	     .setText(""+ index)
	     .align(ControlP5.CENTER, ControlP5.CENTER);  // horizontal + vertical center
	    
	    // ---- Behavior ----
	    if (index == 0) {
	        sceneRadio.activate(index); // ensures first button is ON
	        selectScene(index);         // trigger selection
	    }

	    t.addCallback(new CallbackListener() {
	        public void controlEvent(CallbackEvent ev) {
	            if (ev.getAction() == ControlP5.ACTION_RELEASE && t.getState()) {
	                selectScene(index);
	            }
	        }
	    });
	}

    /**
     * Selects a scene by index and activates it.
     * Deactivates all other scenes and updates all screens to reference
     * the newly selected scene.
     *
     * @param index The index of the scene to select
     * @see Scene#activate()
     * @see Scene#deactivate()
     */
	void selectScene(int index) {
		if (index >= 0 && index < scenes.size()) {
			currentScene = index;
			// PApplet.println("Scene " + index + " selected");
			for (Scene scene : scenes) {
				scene.deactivate();
			}
			scenes.get(currentScene).activate();
			// int numMedias = scenes.get(currentScene).mediaItems.size();
			// PApplet.println("has " + numMedias + " medias");
			for (Screen screen : screens) {
				screen.currentScene = currentScene;
			}
		}
	}

    /**
     * Creates media file selection buttons.
     * Generates one button for each discovered media file in the data directory
     * with appropriate styling and callback to add media to the current scene.
     *
     * @see #addMedia(String)
     * @see ControlP5
     * @see Button
     */
	void createMediaButtons() {
	    // Create the group container
	    mediaList = cp5.addGroup("Media List")
	        .setPosition(r, hy1 + r)
	        .setBackgroundHeight(hy2 - hy1 - 2 * r)
	        .disableCollapse();
	    
	    mediaList.getCaptionLabel().setVisible(false);
	    mediaList.hideBar();

	    // Load a custom font (adjust name/size to your liking)
        PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 16, true);
        //PFont myFont = mainApplet.createFont("Arial", 12, true);

	    for (int i = 0; i < mediaFiles.size(); i++) {
	        String name = mediaFiles.get(i);
	        int buttonHeight = 40;             // custom height
	        int buttonWidth  = hx1 - 2 * r;    // custom width
	        int buttonY      = i * buttonHeight;

	        Button b = cp5.addButton(name)
	            .setPosition(0, buttonY)              // relative to the group
	            .setSize(buttonWidth, buttonHeight)   // size of button
	            .setCaptionLabel(name)                // label text
	            .setGroup(mediaList);                 // attach to group

	        // ---- Styling ----
	        b.getCaptionLabel().setFont(myFont).toUpperCase(false);      // font
	        b.setColorBackground(mainApplet.color(91, 91, 91)); // normal background
	        b.setColorForeground(mainApplet.color(202, 195, 226)); // hover color
	        b.setColorActive(mainApplet.color(171, 128, 255));   // pressed color
	        b.setColorLabel(mainApplet.color(255));              // text color

	        // ---- Callback ----
	        b.addCallback(new CallbackListener() {
	            public void controlEvent(CallbackEvent event) {
	                if (event.getAction() == ControlP5.ACTION_RELEASE) {
	                    addMedia(name);
                        PApplet.println("media " + name + " added");
	                }
	            }
	        });
	    }
	}

    /**
     * Creates content generator selection buttons.
     * Generates one button for each discovered content generator with
     * appropriate styling and callback to add the generator to the current scene.
     *
     * @see #addGenerator(LunaContentGenerator)
     * @see LunaContentGenerator
     * @see ControlP5
     * @see Button
     */
    void createGeneratorButtons() {
        // Create the group container
        generatorList = cp5.addGroup("Generator List")
                .setPosition(hx2+r, hy1 + r)
                .setBackgroundHeight(hy2 - hy1 - 2 * r)
                .disableCollapse();

        PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 14, true);
        generatorList.getCaptionLabel().setFont(myFont).setVisible(false).toUpperCase(false);
        generatorList.hideBar();

        // Load a custom font (adjust name/size to your liking)
        //PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 14, true);
        //PFont myFont = mainApplet.createFont("Arial", 12, true);

        for (int i = 0; i < availableGenerators.size(); i++) {
            LunaContentGenerator generator = availableGenerators.get(i);
            String name = generator.getName();
            int buttonHeight = 30;             // custom height
            int buttonWidth  = mainWidth - hx2 - 2 * r;    // custom width
            int buttonY      = i * buttonHeight;

            Button b = cp5.addButton(name)
                    .setPosition(0, buttonY)              // relative to the group
                    .setSize(buttonWidth, buttonHeight)   // size of button
                    .setCaptionLabel(name)                // label text
                    .setGroup(generatorList);                 // attach to group

            // ---- Styling ----
            b.getCaptionLabel().setFont(myFont).toUpperCase(false);      // font
            b.setColorBackground(mainApplet.color(91, 91, 91)); // normal background
            b.setColorForeground(mainApplet.color(202, 195, 226)); // hover color
            b.setColorActive(mainApplet.color(171, 128, 255));   // pressed color
            b.setColorLabel(mainApplet.color(255));              // text color

            // ---- Callback ----
            b.addCallback(new CallbackListener() {
                public void controlEvent(CallbackEvent event) {
                    if (event.getAction() == ControlP5.ACTION_RELEASE) {
                        addGenerator(generator);
                        PApplet.println("Button pressed: Generator " + name + " added");
                    }
                }
            });
        }
    }

    /**
     * Main rendering method that draws the entire project interface.
     * Handles both normal rendering and scene transitions, updates UI controls,
     * and renders all screens with their associated media content.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see #drawUI()
     * @see #updateCP5()
     * @see Scene#render()
     * @see Screen#render(int, int)
     */
	public void render(int mousex, int mousey) {
		drawUI();
		updateCP5();

        // Process homography requests before rendering
        processHomographyRequests();

		if (isTransitioning) {
			// Update transition progress
			transitionProgress = (float) (mainApplet.millis() - transitionStartTime) / transitionDuration;
			transitionProgress = PApplet.constrain(transitionProgress, 0, 1);

			// Render both scenes during transition
			scenes.get(currentScene).render(); // Current scene
			scenes.get(nextScene).render(); // Next scene

			// Complete transition
			if (transitionProgress >= 1.0f) {
				completeTransition();
			}
		} else {
			// Normal rendering
			scenes.get(currentScene).render();
		}

		// Update screens with appropriate media list based on transition state
		if (isTransitioning) {
			// During transition, screens need access to both scenes' media
			screens.get(currentScreen).setTransitionState(scenes.get(currentScene).mediaItems,
					scenes.get(nextScene).mediaItems, transitionProgress);
		} else {
			screens.get(currentScreen).updateMediaList(scenes.get(currentScene).mediaItems);
		}

		for (Screen screen : screens) {
			screen.render(mousex, mousey);
		}
	}

    /**
     * Draws the main user interface including all panels and preview areas.
     * Renders the project title, FPS counter, media panel, stage preview,
     * effects panel, and timeline. Updates the main display with the rendered UI.
     *
     * @see #drawPanel(int, int, int, int, PGraphics2D)
     * @see #drawStagePanel(int)
     * @see #drawTimelinePanel(int)
     */
	void drawUI() {
        PFont myFont = mainApplet.createFont("NeueMachina-Regular.otf", 20, true);
		canvaUI.beginDraw();
		canvaUI.background(33);

		// title
		canvaUI.textSize(20);
        canvaUI.textFont(myFont);
		canvaUI.fill(200);
		canvaUI.textAlign(PConstants.CENTER, PConstants.CENTER);
		canvaUI.text(projectName, (float) mainWidth / 2, (float) hy1 / 2);
		canvaUI.text("fps: " + (int) (mainApplet.frameRate), (float) mainWidth / 3, (float) hy1 / 2);

		// media
		drawPanel(0, hy1, hx1, hy2, canvaUI);

		// stage
		drawStagePanel(currentScreen); // Pass the index here

		// effects
		drawPanel(hx2, hy1, mainWidth, hy2, canvaUI);

		// timeline
		drawTimelinePanel(currentScene);

        //drawGenerativePanel();

		canvaUI.endDraw();
		mainApplet.image(canvaUI, 0, 0);
	}

    /**
     * Draws a rounded rectangle panel with the specified dimensions.
     * Used as a background for various UI sections throughout the interface.
     *
     * @param x1 Left coordinate of the panel
     * @param y1 Top coordinate of the panel
     * @param x2 Right coordinate of the panel
     * @param y2 Bottom coordinate of the panel
     * @param input The PGraphics2D context to draw on
     */
	void drawPanel(int x1, int y1, int x2, int y2, PGraphics2D input) {
		input.noStroke();
		input.fill(44);
		input.rect(x1 + r, y1 + r, x2 - x1 - 2 * r, y2 - y1 - 2 * r, r);
	}

    /**
     * Updates the preview area dimensions and position based on current screen size.
     * Calculates the optimal scale to fit the screen content within the preview area
     * while maintaining aspect ratio, and centers the preview within the available space.
     *
     * @see Screen#w
     * @see Screen#h
     */
	void updatePreviewArea() {
		// Calculate scale to fit
		float scale = PApplet.min(previewAreaWidth / screens.get(currentScreen).w,
				previewAreaHeight / screens.get(currentScreen).h);

		previewWidth = screens.get(currentScreen).w * scale;
		previewHeight = screens.get(currentScreen).h * scale;

		// Center in preview area
		previewX = previewAreaX + (previewAreaWidth - previewWidth) / 2;
		previewY = previewAreaY + (previewAreaHeight - previewHeight) / 2;
	}

    /**
     * Draws the stage preview panel showing the current screen content.
     * Displays the screen output scaled to fit the preview area with a border,
     * and shows the screen index number overlaid on the preview.
     *
     * @param index The index of the screen to display in the preview
     * @see #updatePreviewArea()
     * @see Screen#getScreen()
     * @see Screen#setPreviewArea(float, float, float, float)
     */
	void drawStagePanel(int index) {
		// 1. Draw panel background
		drawPanel(hx1, hy1, hx2, hy2, canvaUI);

		// 3. Draw preview content
		if (index >= 0 && index < screens.size()) {
			Screen screen = screens.get(index);
			updatePreviewArea();
			screens.get(currentScreen).setPreviewArea(previewX, previewY, previewWidth, previewHeight);
			// Draw (with border)
			canvaUI.fill(0);
			canvaUI.rect(previewX - 2, previewY - 2, previewWidth + 4, previewHeight + 4);
			if(showPreview) canvaUI.image(screen.getScreen(), previewX, previewY, previewWidth, previewHeight);
			canvaUI.fill(200, 100);
			canvaUI.textSize(48);
			canvaUI.textAlign(PConstants.CENTER, PConstants.CENTER);
			canvaUI.text(index, (float) (hx2 + hx1) / 2, (float) (hy1 + hy2) / 2);
			// canvaUI.text(previewAreaWidth, (hx2 + hx1)/2, (hy1 +hy2)/2);
		}

		// 4. Draw UI elements on top
		canvaUI.fill(200);
		canvaUI.textSize(16);
		canvaUI.textAlign(PConstants.LEFT, PConstants.TOP);
		canvaUI.text("Stage Preview", hx1 + 20, hy1 + 15);
	}

    /**
     * Draws the timeline panel at the bottom of the interface.
     * Displays thumbnails of all media items in the current scene,
     * positioned according to the scene's thumbnail layout.
     *
     * @param index The current scene index (unused in current implementation)
     * @see Scene#mediaItems
     * @see MediaItem#thumbnail
     * @see MediaItem#thumbnailGenerated
     * @see Scene#setThumbnailPosition(int, int)
     */
	void drawTimelinePanel(int index) {
		drawPanel(0, hy2, mainWidth, mainHeight, canvaUI);
		// 4. Draw UI elements on top
		for (int i = 0; i < scenes.get(currentScene).mediaItems.size(); i++) {
			MediaItem media = scenes.get(currentScene).mediaItems.get(i);
			scenes.get(currentScene).setThumbnailPosition(2 * r, hy2);
			if (media.thumbnailGenerated) {
				canvaUI.image(media.thumbnail, media.thumbnailX, media.thumbnailY);
			}
		}
	}

    /**
     * Updates hover point position for all screens based on mouse coordinates.
     * Used for interactive elements that respond to mouse movement.
     *
     * @param mousex The current x-coordinate of the mouse
     * @param mousey The current y-coordinate of the mouse
     * @see Screen#moveHoverPoint(float, float)
     */
	public void moveHoverPoint(float mousex, float mousey) {
		for (Screen screen : screens) {
			screen.moveHoverPoint(mousex, mousey);
		}
	}

    /**
     * Handles keyboard input for project controls.
     * Saves the project on any key release and triggers scene transitions
     * when the spacebar is pressed.
     *
     * @param k The character of the key released
     * @param kc The key code of the key released
     * @see #saveToFile()
     * @see #startTransition(int)
     */
	public void keyreleased(char k, int kc) {
		saveToFile();
		if (k == ' ' && !isTransitioning) {
//			sceneRadio.deactivate(currentScene);
//			currentScene = PApplet.constrain(currentScene+1,0,scenes.size()-1);
//			sceneRadio.activate(currentScene);
//			selectScene(currentScene);
			startTransition(currentScene + 1);
			//PApplet.println("Next scene : currentScene + 1");
		}
	}

    /**
     * Initiates a transition to a target scene with fade effect.
     * Activates both current and target scenes during the transition period
     * and starts the transition timer.
     *
     * @param targetScene The index of the scene to transition to
     * @see #completeTransition()
     * @see Scene#activate()
     */
	private void startTransition(int targetScene) {
		if (targetScene >= 0 && targetScene < scenes.size() && targetScene != currentScene) {
			isTransitioning = true;
			nextScene = targetScene;
			transitionStartTime = mainApplet.millis();
			transitionProgress = 0;

			// Activate both scenes during transition
			scenes.get(nextScene).activate();
		}
	}

    /**
     * Completes an ongoing scene transition.
     * Deactivates the previous scene, updates the current scene index,
     * and cleans up transition state. Updates the scene radio button selection.
     *
     * @see #startTransition(int)
     * @see Scene#deactivate()
     * @see RadioButton#activate(int)
     * @see RadioButton#deactivate(int)
     */
	private void completeTransition() {
		// Clean up transition state
		scenes.get(currentScene).deactivate();
		sceneRadio.deactivate(currentScene);

		currentScene = nextScene;
		nextScene = -1;
		isTransitioning = false;
		transitionProgress = 0;

		sceneRadio.activate(currentScene);
	}

    /**
     * Copies the homography configuration from a source MediaItem to the project clipboard.
     * Stores the corner points (xyN) and texture coordinates (uvN) for later pasting.
     *
     * @param source The MediaItem to copy homography configuration from
     * @see #pasteHomography(MediaItem)
     * @see MediaItem#vm
     */
    public void copyHomography(MediaItem source) {
        if (source != null && source.vm != null && source.vm.xyN != null && source.vm.uvN != null) {
            this.copiedXY = deepCopyPVectorArray(source.vm.xyN);
            this.copiedUV = deepCopyPVectorArray(source.vm.uvN);
            this.copiedConfigName = source.getFileName();
            PApplet.println("✓ Copied homography configuration from: " + copiedConfigName);
        } else {
            PApplet.println("✗ Cannot copy homography: source is invalid or not calibrated");
        }
    }

    /**
     * Pastes the previously copied homography configuration to a target MediaItem.
     * Applies the stored corner points and texture coordinates to the target's VidMap.
     * Requires that copyHomography() was called first with a valid configuration.
     *
     * @param target The MediaItem to apply the homography configuration to
     * @see #copyHomography(MediaItem)
     * @see MediaItem#updateHomography(PVector[], PVector[])
     */
    public void pasteHomography(MediaItem target) {
        if (target != null && copiedXY != null && copiedUV != null) {
            if (target.vm != null) {
                //PApplet.println("Handles before: " + Arrays.toString(target.vm.xyN));
                target.updateHomography(deepCopyPVectorArray(copiedXY), deepCopyPVectorArray(copiedUV));
                //PApplet.println("Handles after: " + Arrays.toString(target.vm.xyN));
                //PApplet.println("✓ Pasted homography configuration to: " + target.getFileName());
                PApplet.println("  Source: " + copiedConfigName);
            } else {
                PApplet.println("✗ Cannot paste homography: target VidMap is not initialized");
            }
        } else {
            if (copiedXY == null || copiedUV == null) {
                PApplet.println("✗ Cannot paste homography: no configuration copied to clipboard");
            } else {
                PApplet.println("✗ Cannot paste homography: target is null");
            }
        }
    }

    /**
     * Creates a deep copy of a PVector array to prevent reference sharing.
     * Essential for ensuring copied homography points are independent of the source.
     *
     * @param source The original PVector array to copy
     * @return A new array containing copies of all PVectors, or null if source is null
     * @see PVector#copy()
     */
    private PVector[] deepCopyPVectorArray(PVector[] source) {
        if (source == null) return null;
        PVector[] copy = new PVector[source.length];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                copy[i] = source[i].copy();
            }
        }
        return copy;
    }

    /**
     * Gets the current status of the homography clipboard for UI display.
     * Indicates whether a configuration is available and from which MediaItem.
     *
     * @return A string describing the clipboard status, suitable for UI display
     * @see #copyHomography(MediaItem)
     */
    public String getClipboardStatus() {
        if (copiedConfigName.isEmpty()) {
            return "Clipboard: Empty";
        } else {
            return "Clipboard: " + copiedConfigName;
        }
    }

    /**
     * Clears the homography clipboard, freeing the stored configuration.
     * Useful for resetting the copy/paste system or managing memory.
     */
    public void clearHomographyClipboard() {
        this.copiedXY = null;
        this.copiedUV = null;
        this.copiedConfigName = "";
        PApplet.println("✓ Homography clipboard cleared");
    }

    /**
     * Processes homography copy/paste requests from all MediaItems in the current scene.
     * Should be called regularly (e.g., in render loop) to handle UI events.
     *
     * @see MediaItem#copyHomographyRequested
     * @see MediaItem#pasteHomographyRequested
     */
    public void processHomographyRequests() {
        Scene current = scenes.get(currentScene);

        for (MediaItem media : current.mediaItems) {
            // Handle copy requests
            if (media.copyHomographyRequested) {
                copyHomography(media);
                media.copyHomographyRequested = false; // Reset flag
            }

            // Handle paste requests
            if (media.pasteHomographyRequested) {
                pasteHomography(media);
                media.pasteHomographyRequested = false; // Reset flag
            }
        }
    }

}