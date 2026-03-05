package paletai.mapping;

import controlP5.*;
import paletai.generators.LunaContentGenerator;
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
 * <p>Key features include:</p>
 * <ul>
 * <li>Automatic aspect ratio correction</li>
 * <li>Video playback control</li>
 * <li>Thumbnail generation</li>
 * <li>Homography transformation via {@link VidMap}</li>
 * <li>Media file management</li>
 * <li>Generative content support</li>
 * <li>UI controls for calibration and playback</li>
 * </ul>
 *
 * @author Daniel Corbani
 * @version 1.0
 * @see VidMap
 * @see Movie
 * @see LunaContentGenerator
 */

public class MediaItem {
    // XML mediaXML;
    /**
     * Parent Processing applet
     */
    private final PApplet p;
    /**
     * Full path to media file
     */
    private String filePath;
    /**
     * Base filename with scene index
     */
    public String fileName;
    /**
     * Image object (for static images)
     */
    private PImage img;
    /**
     * Thumbnail representation
     */
    public PImage thumbnail;

    /**
     * Thumbnail display position
     */
    public float thumbnailX, thumbnailY;

    /**
     * Video flag
     */
    private final boolean isVideo;

    /**
     * Media loading status
     */
    private boolean loaded = false;

    /**
     * Video looping status
     */
    private volatile boolean isLooping = false;

    /**
     * Video object (for movies)
     */
    private Movie movie;

    /**
     * Graphics buffer for rendering
     */
    private PGraphics2D mediaCanvas;

    /**
     * Homography transformation handler
     */
    public VidMap vm; // Homography transformation

    /**
     * Original media dimensions
     */
    public int mediaWidth, mediaHeight;

    /**
     * Assigned screen index and media identifier
     */
    public int assignedScreen, mediaId;

    /**
     * Display resolution for this media
     */
    int resolutionX, resolutionY;

    /**
     * Calibration mode status
     */
    public boolean calibrate = false;

    /**
     * Flag indicating whether thumbnail has been generated
     */
    boolean thumbnailGenerated = false;

    /**
     * ControlP5 instance for UI controls
     */
    ControlP5 cp5;

    /**
     * Group containing media controls
     */
    Group controlGroup;

    /**
     * Controls visibility state
     */
    boolean controlsVisible = false;

    /**
     * Controls visibility state
     */
    boolean toBeDeleted = false;

    /**
     * Content generator for generative media
     */
    public LunaContentGenerator generator;

    /**
     * Flag indicating generative content
     */
    private final boolean isGenerative;

    /**
     * Event flags for homography copy/paste operations.
     * When true, indicates that the UI has requested this action.
     * Should be checked and reset by the managing class (Scene/Project).
     */
    public boolean copyHomographyRequested = false;
    public boolean pasteHomographyRequested = false;

    /**
     * Font
     */
    private PFont mediaFont;


    /**
     * Constructs a new MediaItem from a file path.
     *
     * @param p           The parent PApplet instance
     * @param filePath    Path to media file
     * @param screenIndex The screen index this media is assigned to
     * @param mediaId     Unique identifier for this media item
     * @throws RuntimeException If media file cannot be loaded
     */
    public MediaItem(PApplet p, String filePath, int screenIndex, int mediaId) {
        this.p = p;
        this.filePath = filePath;
        this.fileName = extractFileName(filePath); // NEED TO CHECK THIS!!!!!
        this.isVideo = isVideoFile(filePath);
        this.assignedScreen = screenIndex;
        this.mediaId = mediaId;
        this.vm = new VidMap(p, fileName); // Pass fileName to vm
        initVariables();
        isGenerative = false;
    }

    /**
     * Constructs a MediaItem from XML configuration.
     * Recreates a media item from saved project data.
     *
     * @param p        The parent PApplet instance
     * @param mediaXML XML element containing media configuration
     */
    public MediaItem(PApplet p, XML mediaXML) {
        // PApplet.println("Start MediaItem");
        this.p = p;
        this.filePath = mediaXML.getString("name");
        // PApplet.println("filePath " + filePath);
        this.fileName = extractFileName(filePath); // NEED TO CHECK THIS!!!!!
        // PApplet.println(fileName);
        this.isVideo = isVideoFile(filePath);
        // this.sceneIndex = sceneIndex;
        this.assignedScreen = mediaXML.getInt("Screen");
        this.mediaId = mediaXML.getInt("id");
        this.vm = new VidMap(p, fileName); // Pass fileName to vm
        initVariables();
        updateFromXML(mediaXML);
        loaded = true;
        isGenerative = false;
    }

    /**
     * Constructs a generative MediaItem from a content generator.
     *
     * @param p           The parent PApplet instance
     * @param generator   The content generator to use
     * @param screenIndex The screen index this media is assigned to
     * @param mediaId     Unique identifier for this media item
     */
    public MediaItem(PApplet p, LunaContentGenerator generator, int screenIndex, int mediaId) {
        this.p = p;
        this.fileName = generator.getName();
        this.isVideo = false;
        isGenerative = true;
        this.generator = generator;
        this.assignedScreen = screenIndex;
        this.mediaId = mediaId;
        this.vm = new VidMap(p, fileName); // Pass fileName to vm

        initVariables();
    }

    /**
     * Constructs a generative MediaItem from XML configuration.
     * Recreates a generative media item from saved project data.
     *
     * @param p         The parent PApplet instance
     * @param generator The content generator to use
     * @param mediaXML  XML element containing media configuration
     */
    public MediaItem(PApplet p, LunaContentGenerator generator, XML mediaXML) {
        this.p = p;
        this.fileName = generator.getName();
        this.isVideo = false;
        isGenerative = true;
        this.generator = generator;
        this.assignedScreen = mediaXML.getInt("Screen");
        this.mediaId = mediaXML.getInt("id");
        this.vm = new VidMap(p, fileName); // Pass fileName to vm
        initVariables();
        updateFromXML(mediaXML);
        loaded = true;
    }

    /**
     * Checks if this media item uses generative content.
     *
     * @return true if this is a generative media item, false for file-based media
     */
    boolean isGenerative() {
        return isGenerative;
    }

    /**
     * Serializes the media item configuration to XML for project saving.
     * Includes file information, screen assignment, homography data, and generator flag.
     *
     * @return XML element containing complete media configuration
     * @see #arrayToXML(String, PVector[])
     */
    XML saveXML() {
        XML xml = new XML("MediaItem");
        xml.setString("name", fileName);
        xml.setInt("Screen", assignedScreen);
        xml.setInt("id", mediaId);
        xml.setInt("width", mediaWidth);
        xml.setInt("height", mediaHeight);
        int intGen;
        if (isGenerative) intGen = 1;
        else intGen = 0;
        xml.setInt("isGenerator", intGen);
        if (vm.xyN[0] != null)
            xml.addChild(arrayToXML("xyN", vm.xyN));
        if (vm.uvN[0] != null)
            xml.addChild(arrayToXML("uvN", vm.uvN));
        return xml;
    }

    /**
     * Converts a PVector array to XML format for serialization.
     *
     * @param tag The XML tag name for the array
     * @param arr The PVector array to convert
     * @return XML element containing the vector array data
     */
    XML arrayToXML(String tag, PVector[] arr) {
        XML arrayXML = new XML(tag);
        for (int i = 0; i < arr.length; i++) {
            XML v = new XML("point");
            v.setInt("index", i);
            v.setFloat("x", arr[i].x);
            v.setFloat("y", arr[i].y);
            arrayXML.addChild(v);
        }
        return arrayXML;
    }

    /**
     * Creates the UI control group for this media item.
     * Includes buttons for calibration, input toggling, and video controls.
     * The control group is positioned relative to the thumbnail.
     *
     * @see ControlP5
     * @see Group
     */
    private void createControlGroup() {
        cp5 = new ControlP5(p);
        // Create a group for this media item's controls
        controlGroup = cp5.addGroup("media_" + System.currentTimeMillis()) // Unique name
                .setPosition(thumbnailX, thumbnailY).setWidth(150) // Match thumbnail width
                .setHeight(50) // Slightly taller to fit controls
                .setBackgroundColor(p.color(50, 220)) // Semi-transparent background
                .setBackgroundHeight(20).disableCollapse().hideBar();
        // .hide(); // Start hidden

        // Add controls to the group
        Button b1 = cp5.addButton("calibrate" + controlGroup.getName())
                .setPosition(10, 10)
                .setSize(60, 20)
                .setCaptionLabel("Mapping")
                .setGroup(controlGroup);
        b1.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
        b1.addCallback(new CallbackListener() {
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASE) {
                    // println("Bang released: " + event.getController().getName());
                    // Your function call here
                    toggleCalibration();
                }
            }
        });

        Button b2 = cp5.addButton("in/out" + controlGroup.getName())
                .setPosition(80, 10)
                .setSize(60, 20)
                .setCaptionLabel("in/out")
                .setGroup(controlGroup);
        b2.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
        b2.addCallback(new CallbackListener() {
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASE) {
                    // println("Bang released: " + event.getController().getName());
                    // Your function call here
                    toggleInput();
                }
            }
        });


        Button b3 = cp5.addButton("copyHomography" + controlGroup.getName())
                .setPosition(10, 40) // Position below existing buttons
                .setSize(60, 20)
                .setCaptionLabel("Copy")
                .setGroup(controlGroup);
        b3.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
        b3.addCallback(new CallbackListener() {
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASE) {
                    copyHomographyRequested = true;
                }
            }
        });

        Button b4 = cp5.addButton("pasteHomography" + controlGroup.getName())
                .setPosition(80, 40)
                .setSize(60, 20)
                .setCaptionLabel("Paste")
                .setGroup(controlGroup);
        b4.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
        b4.addCallback(new CallbackListener() {
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASE) {
                    pasteHomographyRequested = true;
                }
            }
        });

        Button b8 = cp5.addButton("deleteThisMedia" + controlGroup.getName())
                .setPosition(130, 100)
                .setSize(20, 20)
                .setCaptionLabel("x")
                .setGroup(controlGroup);
        b8.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
        b8.addCallback(new CallbackListener() {
            public void controlEvent(CallbackEvent event) {
                if (event.getAction() == ControlP5.ACTION_RELEASE) {
                    //pasteHomographyRequested = true;
                    toBeDeleted = true;
                    PApplet.println("media deleted");
                }
            }
        });

        // Video controls
        if (isVideo) {
            Button b5 = cp5.addButton("play" + controlGroup.getName())
                    .setPosition(10, 80)
                    .setSize(30, 20)
                    .setCaptionLabel("Play")
                    .setGroup(controlGroup);
            b5.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
            b5.addCallback(new CallbackListener() {
                public void controlEvent(CallbackEvent event) {
                    if (event.getAction() == ControlP5.ACTION_RELEASE) {
                        // println("Bang released: " + event.getController().getName());
                        // Your function call here
                        playMedia();
                    }
                }
            });
            Button b6 = cp5.addButton("stop" + controlGroup.getName())
                    .setPosition(45, 80)
                    .setSize(30, 20)
                    .setCaptionLabel("Stop")
                    .setGroup(controlGroup);
            b6.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
            b6.addCallback(new CallbackListener() {
                public void controlEvent(CallbackEvent event) {
                    if (event.getAction() == ControlP5.ACTION_RELEASE) {
                        // println("Bang released: " + event.getController().getName());
                        // Your function call here
                        stopMedia();
                    }
                }
            });
            Button b7 = cp5.addButton("loop" + controlGroup.getName())
                    .setPosition(80, 80)
                    .setSize(30, 20)
                    .setCaptionLabel("Loop")
                    .setGroup(controlGroup);
            b7.getCaptionLabel().setFont(mediaFont).toUpperCase(false);      // font
            b7.addCallback(new CallbackListener() {
                public void controlEvent(CallbackEvent event) {
                    if (event.getAction() == ControlP5.ACTION_RELEASE) {
                        // println("Bang released: " + event.getController().getName());
                        // Your function call here
                        loopMedia();
                    }
                }
            });
        }
    }

    /**
     * Shows the media item's control group.
     * Makes the calibration and playback controls visible.
     */
    public void deleteControls() {
        controlGroup.remove();
    }

    /**
     * Shows the media item's control group.
     * Makes the calibration and playback controls visible.
     */
    public void showControls() {
        controlGroup.show();
    }

    /**
     * Hides the media item's control group.
     * Removes the calibration and playback controls from display.
     */
    public void hideControls() {
        controlGroup.hide();
    }

    /**
     * Sets the thumbnail position and updates control group placement.
     * Positions the thumbnail and aligns the control group above it.
     *
     * @param x The x-coordinate for thumbnail placement
     * @param y The y-coordinate for thumbnail placement
     */
    public void setThumbnailPosition(int x, int y) {
        this.thumbnailX = x;
        this.thumbnailY = y;
        // PApplet.println("Thumnail position: " + thumbnailX + "," + thumbnailY);
        controlGroup.setPosition(x, y); // Position above thumbnail
    }

    /**
     * Initializes media variables and creates thumbnails.
     * Loads media files, creates thumbnails, and sets up initial dimensions.
     * For generative content, prepares the generator system.
     */
    void initVariables() {
        mediaFont = p.createFont("NeueMachina-Regular.otf", 10, true);
        if (isVideo) {
            initMovie();
            thumbnail = p.createImage(150, 100, PConstants.RGB);
        } else if (isGenerative) {
            thumbnail = p.createImage(150, 100, PConstants.RGB);
        } else {
            img = p.loadImage(filePath);
            mediaWidth = img.width;
            mediaHeight = img.height;
            thumbnail = p.createImage(150, 100, PConstants.RGB);
        }
        thumbnailX = 0;// (thumbnail.width+50)*mediaId;
        thumbnailY = 0;
        createControlGroup();
    }

    /**
     * Initializes the movie object once without starting playback.
     * Called during initVariables() instead of creating Movie inline.
     */
    private void initMovie() {
        if (isVideo) {
            movie = new Movie(p, filePath);
            //movie.play(); // must call play() once so GStreamer pipeline initializes
            //movie.pause(); // immediately pause — we just needed the pipeline up
        }
    }

    // -------------------------------------------------------------------------
    // Movie event handling (called from Project.movieEvent on GStreamer thread)
    // -------------------------------------------------------------------------

//    /**
//     * Returns true if this MediaItem owns the given Movie instance.
//     * Used by Project.movieEvent() to route callbacks to the correct item.
//     */
//    public boolean ownsMovie(Movie m) {
//        return isVideo && this.movie == m;
//    }

//    /**
//     * Called by Project.movieEvent() when a new frame is available.
//     * Runs on the GStreamer thread — only sets flags, never draws.
//     */
//    public void handleMovieEvent() {
//        newFrameAvailable = true;
//
//        // Check end-of-video here, on the GStreamer thread,
//        // but only set a flag — don't call jump() directly
////        if (movie != null && movie.duration() > 0) {
////            float timeLeft = movie.duration() - movie.time();
////            if (timeLeft <= (1.0f / 30.0f)) {
////                if (isLooping) {
////                    rewindRequested = true;
////                }
////            }
////        }
//    }

    /**
     * Updates media item configuration from XML data.
     * Restores homography transformation points from saved project data.
     *
     * @param xml XML element containing saved media configuration
     * @see #arrayFromXML(XML)
     */
    void updateFromXML(XML xml) {
        PApplet.println(fileName);
        PVector[] xyNew = arrayFromXML(xml.getChild("xyN"));
        PVector[] uvNew = arrayFromXML(xml.getChild("uvN"));
        //PApplet.printArray(uvNew);
        vm.xyN = arrayFromXML(xml.getChild("xyN"));
        vm.uvN = arrayFromXML(xml.getChild("uvN"));
        updateHomography(xyNew, uvNew);
    }

    /**
     * Converts XML data back to a PVector array.
     * Reconstructs homography points from saved project data.
     *
     * @param arrayXML XML element containing point data
     * @return PVector array reconstructed from XML
     */
    PVector[] arrayFromXML(XML arrayXML) {
        XML[] points = arrayXML.getChildren("point");
        PVector[] arr = new PVector[points.length];
        for (int i = 0; i < points.length; i++) {
            // PApplet.println(points[i].getName());
            float x = points[i].getFloat("x");
            float y = points[i].getFloat("y");
            // float z = points[i].hasAttribute("z") ? points[i].getFloat("z") : 0;
            arr[i] = new PVector(x, y);
            // PApplet.println(arr[i]);
        }
        return arr;
    }

    /**
     * Updates an existing XML node with new PVector values.
     * Modifies the XML structure to reflect current homography points.
     *
     * @param arrayXML The XML element to update
     * @param arr      The new PVector array values
     */
    void updateArrayXML(XML arrayXML, PVector[] arr) {
        XML[] points = arrayXML.getChildren("point");
        for (int i = 0; i < arr.length && i < points.length; i++) {
            // println(arrayXML.getName() + " " + points[i]);
            points[i].setFloat("x", arr[i].x);
            points[i].setFloat("y", arr[i].y);
        }
    }

    // -------------------------------------------------------------------------
    // Display assignment
    // -------------------------------------------------------------------------

    /**
     * Assigns this media item to a display with specific dimensions.
     * Creates the media canvas, sets resolution, and configures the homography
     * transformation for the target display. For generative content, calls
     * the generator's setup method.
     *
     * @param w           The width of the target display
     * @param h           The height of the target display
     * @param screenIndex The index of the screen to assign to
     * @see LunaContentGenerator#setup(int, int)
     * @see VidMap#assignToDisplay(int, int)
     */
    public void assignToDisplay(int w, int h, int screenIndex) {

        this.resolutionX = w;
        this.resolutionY = h;
        this.mediaCanvas = (PGraphics2D) p.createGraphics(resolutionX, resolutionY, PConstants.P2D);

        this.mediaCanvas.beginDraw();
        this.mediaCanvas.clear();
        this.mediaCanvas.endDraw();
        this.assignedScreen = screenIndex;
        if (isGenerative) {
            this.generator.setup(resolutionX, resolutionY);
            mediaWidth = resolutionX;
            mediaHeight = resolutionY;
            //PApplet.println("Generator setup complete");
        }
        vm.assignToDisplay(resolutionX, resolutionY);
    }

    /**
     * Checks if media is successfully loaded and ready for display.
     *
     * @return true if media is ready for display, false otherwise
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Adjusts media display to maintain aspect ratio.
     * Automatically updates homography points to fit media properly within
     * the display canvas while preserving the original aspect ratio.
     *
     * @param mediaWidth  Original media width
     * @param mediaHeight Original media height
     * @see VidMap#updateHomographyFromPixel(PVector[], PVector[])
     */
    public void applyAspectRatioCorrection(int mediaWidth, int mediaHeight) {
        // PApplet.println("applyAspectRatioCorrection");
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
        PVector[] uvP = {new PVector(offsetX, offsetY), new PVector(offsetX + newWidth, offsetY),
                new PVector(offsetX + newWidth, offsetY + newHeight), new PVector(offsetX, offsetY + newHeight)};

        PVector[] xyP = {new PVector(0, 0), new PVector(mediaCanvas.width, 0),
                new PVector(mediaCanvas.width, mediaCanvas.height), new PVector(0, mediaCanvas.height)};

        vm.updateHomographyFromPixel(xyP, uvP);
//		if (loaded) {
//			// updateFromXML(fromXML);
//		}
    }
    // **🔹 vm Wrapper Methods**

    // -------------------------------------------------------------------------
    // Homography wrappers
    // -------------------------------------------------------------------------

    /**
     * Updates the homography transformation with new coordinate points.
     * Wrapper method for VidMap's updateHomography functionality.
     *
     * @param xyNew New destination points for transformation
     * @param uvNew New source points for transformation
     * @see VidMap#updateHomography(PVector[], PVector[])
     */
    public void updateHomography(PVector[] xyNew, PVector[] uvNew) {
        vm.updateHomography(xyNew, uvNew);
    }

    /**
     * Toggles calibration mode for this media item.
     * Wrapper method for VidMap's toggleCalibration functionality.
     *
     * @see VidMap#toggleCalibration()
     */
    public void toggleCalibration() {
        vm.toggleCalibration();
        this.calibrate = vm.calibrate;
    }

    /**
     * Turns off calibration mode for this media item.
     * Wrapper method for VidMap's offCalibration functionality.
     *
     * @see VidMap#offCalibration()
     */
    public void offCalibration() {
        vm.offCalibration();
        this.calibrate = vm.calibrate;
    }

    /**
     * Turns on calibration mode for this media item.
     * Wrapper method for VidMap's onCalibration functionality.
     *
     * @see VidMap#onCalibration()
     */
    public void onCalibration() {
        vm.onCalibration();
        this.calibrate = vm.calibrate;
    }

    /**
     * Toggles input checking for homography points.
     * Enables or disables mouse interaction with transformation points.
     *
     * @see VidMap#checkInput
     */
    public void toggleInput() {
        vm.checkInput = !vm.checkInput;
        // System.out.println("checkInput = " + vm.checkInput);
    }

    /**
     * Checks hover state for homography points at specified coordinates.
     * Wrapper method for VidMap's checkHover functionality.
     *
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @see VidMap#checkHover(float, float)
     */
    public void checkHover(float x, float y) {
        vm.checkHover(x, y);
    }

    /**
     * Moves the hover point to specified coordinates.
     * Wrapper method for VidMap's moveHoverPoint functionality.
     *
     * @param x The target x-coordinate
     * @param y The target y-coordinate
     * @see VidMap#moveHoverPoint(float, float)
     */
    public void moveHoverPoint(float x, float y) {
        vm.moveHoverPoint(x, y);
    }

    /**
     * Handles mouse release events for homography point interaction.
     * Wrapper method for VidMap's mouseReleased functionality.
     *
     * @see VidMap#mouseReleased()
     */
    public void mouseReleased() {
        vm.mouseReleased();
    }

    /**
     * Resets the homography transformation to default state.
     * Clears custom transformation points and reapplies aspect ratio correction.
     *
     * @see VidMap#resetHomography()
     * @see #applyAspectRatioCorrection(int, int)
     */
    public void resetHomography() {
        vm.resetHomography();
        applyAspectRatioCorrection(mediaWidth, mediaHeight);
    }

    /**
     * Extracts the filename from a full file path.
     *
     * @param path The full file path
     * @return The filename without directory path
     */
    private String extractFileName(String path) {
        File file = new File(path);
        return file.getName();
    }

    /**
     * Checks if a file is a supported video format.
     * Currently supports .mp4, .avi, and .mov file extensions.
     *
     * @param filename The filename to check
     * @return true if the file is a supported video format
     */
    private boolean isVideoFile(String filename) {
        filename = filename.toLowerCase();
        return filename.endsWith(".mp4") || filename.endsWith(".avi") || filename.endsWith(".mov");
    }

    // -------------------------------------------------------------------------
    // Thumbnail
    // -------------------------------------------------------------------------

    /**
     * Generates a thumbnail image for this media item.
     * For generative content: captures the current generator output.
     * For images: creates a resized copy of the original image.
     * For videos: captures the first available frame and resizes it.
     * Sets thumbnailGenerated flag to true when complete.
     *
     * @see #thumbnailGenerated
     * @see LunaContentGenerator#getGraphics()
     */
    public void generateThumbnail() {
        if (isGenerative) {
            thumbnail = p.createImage(mediaCanvas.width, mediaCanvas.height, PConstants.RGB);
            thumbnail.copy(this.generator.getGraphics(), 0, 0,
                    this.generator.getGraphics().width, this.generator.getGraphics().height,
                    0, 0, thumbnail.width, thumbnail.height);
            thumbnail.loadPixels();
            thumbnail.resize(150, 100);
        } else if (!isVideo) {
            img.loadPixels();
            thumbnail = p.createImage(img.width, img.height, PConstants.RGB);
            thumbnail.copy(img, 0, 0, img.width, img.height,
                    0, 0, thumbnail.width, thumbnail.height);
            thumbnail.loadPixels();
            thumbnail.resize(150, 100);
        } else if (isVideo && movie != null) {
            // Check if video has pixels available
            movie.loadPixels();
            thumbnail = p.createImage(movie.width, movie.height, PConstants.RGB);
            thumbnail.copy(movie, 0, 0, movie.width, movie.height,
                    0, 0, thumbnail.width, thumbnail.height);
            thumbnail.loadPixels();
            thumbnail.resize(150, 100);
            // PApplet.println(thumbnail.width);
        }
        thumbnailGenerated = true;
    }

    /**
     * Sets the preview area dimensions for homography point display.
     * Wrapper method for VidMap's setPreviewArea functionality.
     *
     * @param px Preview area x-coordinate
     * @param py Preview area y-coordinate
     * @param pw Preview area width
     * @param ph Preview area height
     * @see VidMap#setPreviewArea(float, float, float, float)
     */
    public void setPreviewArea(float px, float py, float pw, float ph) {
        vm.setPreviewArea(px, py, pw, ph);
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    /**
     * Renders the media with homography transformation.
     * Videos: consumes thread-safe flags set by handleMovieEvent().
     * Generative: updates and draws generator output.
     * Images: draws the static image.
     *
     * @see Movie#available()
     * @see Movie#read()
     * @see LunaContentGenerator#update()
     * @see LunaContentGenerator#getGraphics()
     * @see VidMap#render(PGraphics2D)
     */
    public void render() {
//        if (isVideo) PApplet.println("render: movie=" + movie +
//                " loaded=" + loaded + " newFrame=" + newFrameAvailable +
//                " isPlaying=" + (movie != null ? movie.isPlaying() : "null"));

        mediaCanvas.beginDraw();
        mediaCanvas.background(0); // Clear previous frame

        if (isVideo && movie != null) { // Check for null FIRST
//            if (movie.available()) {
//                movie.read();
//                if (mediaHeight == 0) {
//                    mediaWidth = movie.width;
//                    mediaHeight = movie.height;
//                    if (!loaded)
//                        applyAspectRatioCorrection(mediaWidth, mediaHeight);
//                    loaded = true;
//                }
//                if (movie.time() > 0.5f && !thumbnailGenerated) {
//                    generateThumbnail();
//                    //PApplet.println("Video thumbnail generated");
//                }
//
//                if (movie.time() > (movie.duration() - 0.1) && !isLooping) {
//                    stopMedia();
//                    //PApplet.println("Video thumbnail generated");
//                }
//            }
//
//            // Only try to draw if movie is not null
//            mediaCanvas.image(movie, 0, 0, mediaCanvas.width, mediaCanvas.height);

            // Dimensions and thumbnail — check once after loaded
//            if (!loaded && movie.width > 0) {
//                mediaWidth = movie.width;
//                mediaHeight = movie.height;
//                applyAspectRatioCorrection(mediaWidth, mediaHeight);
//                loaded = true;
//            }
//            if (loaded && !thumbnailGenerated && movie.time() > 0.5f) {
//                generateThumbnail();
//            }
//
//            // End-of-video handling
//            if (loaded && movie.duration() > 0 &&
//                    movie.time() >= movie.duration() - (1.0f / 30.0f)) {
//                if (isLooping) {
//                    movie.jump(0);
//                    movie.play();
//                } else {
//                    movie.pause(); // freeze on last frame
//                }
//            }
//
//            // Always draw current frame (movieEvent keeps it fresh)
//            mediaCanvas.image(movie, 0, 0, mediaCanvas.width, mediaCanvas.height);

            // Handle rewind request set by movieEvent thread
//            if (rewindRequested) {
//                rewindRequested = false;
//                movie.jump(0);
//                movie.play();
//            }

            // Capture dimensions once the first frame arrives
            if (!loaded && movie.width > 0) {
                mediaWidth = movie.width;
                mediaHeight = movie.height;
                //applyAspectRatioCorrection(mediaWidth, mediaHeight);
                loaded = true;
            }

            // Thumbnail — once only
            if (loaded && !thumbnailGenerated && movie.time() > 0.5f) {
                generateThumbnail();
            }

            if (isLooping && movie.time()>movie.duration()-0.2){
                disposeMedia();
                initMovie();
                loopMedia();
            }

            // Always draw — movieEvent keeps frame fresh, fallback keeps last frame visible
            mediaCanvas.image(movie, 0, 0, mediaCanvas.width, mediaCanvas.height);
        } else if (isGenerative) {
            //PApplet.println("Generative");
            this.generator.update();
            //generator.draw(mediaCanvas,t);
            if (generator.getGraphics() != null) {
                mediaCanvas.image(this.generator.getGraphics(), 0, 0);
                if (!thumbnailGenerated) {
                    generateThumbnail();
                    //PApplet.println("Video thumbnail generated");
                }
            }
        } else if (!isVideo) {
            if (!thumbnailGenerated) {
                generateThumbnail();
            }
            if (img != null) {
                mediaCanvas.image(img, 0, 0, mediaCanvas.width, mediaCanvas.height);
            }
        }


        mediaCanvas.endDraw();

        if (vm != null) {
            vm.render(mediaCanvas);
        }
    }

    // -------------------------------------------------------------------------
    // Playback control
    // -------------------------------------------------------------------------

    /**
     * Toggles video playback state.
     * Plays if paused, pauses if playing. No effect on static images or generative content.
     */
    public void togglePlayback() {
        if (isVideo && movie != null) {
            if (movie.isPlaying()) {
                movie.pause();
            } else {
                movie.play();
            }
        }
    }

    /**
     * Toggles loop mode without restarting playback.
     */
    public void toggleLoop() {
        isLooping = !isLooping;
        // System.out.println("isLooping = " + isLooping);
    }

    /**
     * Starts playback from the beginning in single-play mode.
     * Rebuilds the GStreamer pipeline if it was previously disposed.
     */
    public void playMedia() {
//        if (isVideo && movie != null) {
//            movie.jump(0);   // rewind to start
//            movie.play();
//            isLooping = false;
//        }
        if (isVideo) {
            if (movie == null) initMovie(); // rebuild if was disposed
            //if(loaded) movie.jump(0); // only rewind if already played before
            movie.play();
            movie.noLoop();
            isLooping = false;
        }
    }
//    public void playMedia() {
//        if (isVideo) {
//            stopMedia(); // clean up old one first
//            movie = new Movie(p, filePath);
//            movie.play();
//            isLooping = false;
//        }
//    }


    /**
     * Starts looping playback from the beginning.
     * Rebuilds the GStreamer pipeline if it was previously disposed.
     */
    public void loopMedia() {
//        if (isVideo && movie != null) {
//            movie.jump(0);
//            movie.loop();
//            isLooping = true;
//        }
        if (isVideo) {
            if (movie == null) initMovie();
            //if (loaded) movie.jump(0);
            movie.loop();
            isLooping = true;
        }
    }
//    public void loopMedia() {
//        if (isVideo) {
//            stopMedia(); // clean up old one first
//            movie = new Movie(p, filePath);
//            movie.loop();
//            isLooping = true;
//        }
//    }

    /**
     * Pauses playback, preserving the current frame on screen.
     * Used by the Stop button — does NOT dispose the pipeline.
     */
    public void stopMedia() {
        if (isVideo && movie != null) {
            movie.pause(); // pause instead of stop — preserves last frame in buffer
            // Do NOT clear the canvas here — let the last frame persist
        }
    }
//    public void stopMedia() {
//        if (isVideo && movie != null) {
//            //PApplet.println("Stop Media");
//            movie.stop();
//            movie.dispose(); // force GStreamer cleanup. It is crucial to force GStreamer to release the
//            // native pipeline before reusing
//            movie = null;
//            // movie = new Movie(p, filePath);
//            mediaCanvas.beginDraw();
//            mediaCanvas.clear();
//            mediaCanvas.endDraw();
//            //PApplet.println("finished Stop Media");
//        }
//    }

    /**
     * Fully disposes the GStreamer pipeline and releases all native resources.
     * Called by Scene.deactivate() to prevent pipeline accumulation across scene switches.
     * playMedia() / loopMedia() will call initMovie() to rebuild when needed.
     */
    public void disposeMedia() {
        if (isVideo && movie != null) {
            movie.stop();
            movie.dispose();
            movie = null;
            loaded = false;
        }
    }

    /**
     * Mutes video audio playback.
     * Sets video volume to zero if the video is not currently playing.
     * No effect on static images or generative content.
     */
    public void muteMedia() {
        if (isVideo && movie != null && !movie.isPlaying()) {
            movie.volume(0);
        }
    }

    /**
     * Returns the full file path of the media.
     *
     * @return The complete file path, or null for generative content
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the filename of the media.
     *
     * @return The filename without path, or generator name for generative content
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the thumbnail image for this media item.
     *
     * @return The thumbnail PImage, or null if not generated
     * @see #thumbnailGenerated
     */
    public PImage getThumbnail() {
        return thumbnail;
    }

    /**
     * Checks if this media item is a video file.
     *
     * @return true if this is a video media item
     */
    public boolean isVideo() {
        return isVideo;
    }

    /**
     * Returns the transformed media canvas with homography applied.
     * Wrapper method for VidMap's getMediaCanvas functionality.
     *
     * @return PGraphics2D containing the transformed media output
     * @see VidMap#getMediaCanvas()
     */
    public PGraphics2D getMediaCanvas() {
        return vm.getMediaCanvas();
    }
}