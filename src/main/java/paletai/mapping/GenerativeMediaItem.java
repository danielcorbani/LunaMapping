package paletai.mapping;

import paletai.generators.LunaContentGenerator;
import processing.core.*;
import processing.core.PApplet;
import processing.data.XML;

/**
 * A specialized MediaItem subclass for generative content.
 *
 * <p>Extends MediaItem to handle content generated programmatically by
 * LunaContentGenerator implementations rather than from static media files.
 * Provides custom rendering that integrates generative content with the
 * homography transformation system.</p>
 *
 * <p>Key features:</p>
 * <ul>
 * <li>Integration with LunaContentGenerator framework</li>
 * <li>Custom rendering pipeline for dynamic content</li>
 * <li>Automatic thumbnail generation from generator output</li>
 * <li>Inherits all homography transformation capabilities from MediaItem</li>
 * </ul>
 *
 * @author Daniel Corbani
 * @version 1.0
 * @see MediaItem
 * @see LunaContentGenerator
 */

public class GenerativeMediaItem extends MediaItem {

    /**
     * Constructs a new GenerativeMediaItem with the specified generator.
     *
     * @param p The parent PApplet instance
     * @param generator The content generator to use for this media item
     * @param screenIndex The screen index this media is assigned to
     * @param mediaId Unique identifier for this media item
     * @see MediaItem#MediaItem(PApplet, LunaContentGenerator, int, int)
     */
    public GenerativeMediaItem(PApplet p, LunaContentGenerator generator, int screenIndex, int mediaId) {
        super(p, generator, screenIndex, mediaId);
    }

    /**
     * Constructs a GenerativeMediaItem from XML configuration.
     * Recreates a generative media item from saved project data.
     *
     * @param p The parent PApplet instance
     * @param generator The content generator to use for this media item
     * @param mediaXML XML element containing media configuration
     * @see MediaItem#MediaItem(PApplet, LunaContentGenerator, XML)
     */
    public GenerativeMediaItem(PApplet p, LunaContentGenerator generator, XML mediaXML) {
        // Call the parent constructor that handles XML loading
        super(p, generator, mediaXML);
    }

    /**
     * Renders the generative content with homography transformation bypassing mediaCanvas.
     * Updates the generator, applies homography transformation to the generator's
     * output graphics, and generates thumbnails when needed.
     *
     * <p>This override provides a custom rendering pipeline that integrates
     * the dynamic generator output with the VidMap transformation system.</p>
     *
     * @see LunaContentGenerator#update()
     * @see LunaContentGenerator#getGraphics()
     * @see VidMap#render(PImage)
     * @see #generateThumbnail()
     */
    @Override
    public void render() {
        // Use the proven VidMap approach that worked in your test
        this.generator.update();
        if (vm != null) {
            vm.render(this.generator.getGraphics());
        }

        // Thumbnail generation can stay the same
        if (!thumbnailGenerated) {
            generateThumbnail();
        }
    }
}
