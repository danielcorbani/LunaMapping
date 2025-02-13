package paletai.mapping;

import processing.core.*;
import processing.opengl.*;
import java.util.ArrayList;

public class Scene {
    private PApplet p;
    private PGraphics2D sceneCanvas;
    private ArrayList<MediaItem> mediaItems;
    private MediaItem activeMedia; // Track active media for calibration
    private int indexNum;
    
    public Scene(PApplet p, int index) {
        this.p = p;
        this.sceneCanvas = (PGraphics2D) p.createGraphics(p.width, p.height, PConstants.P2D);
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
//        sceneCanvas.beginDraw();
//        sceneCanvas.background(0);

        for (MediaItem item : mediaItems) {
            item.checkHover(mouseX, mouseY); // Pass explicit mouse position
            item.render();
            //sceneCanvas.image(item.getMediaCanvas(), 0, 0);
        }

//        sceneCanvas.endDraw();
//        p.image(sceneCanvas, 0, 0);
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
    }

    // **🔹 Getters**
    public ArrayList<MediaItem> getMediaItems() {
        return mediaItems;
    }
}
