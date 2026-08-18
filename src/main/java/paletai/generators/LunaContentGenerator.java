package paletai.generators;

import processing.opengl.PGraphics2D;

/**
 * Interface for generative content providers in the Luna Video Mapping system.
 *
 * <p>This interface defines the contract for custom content generators that can
 * be used as media sources within the Luna library. Generators create dynamic
 * visual content programmatically rather than from static media files.</p>
 *
 * <p>Implementing classes should provide:</p>
 * <ul>
 * <li>Real-time content generation</li>
 * <li>Parameter customization for dynamic effects</li>
 * <li>Graphics buffer management for efficient rendering</li>
 * <li>Unique identification for UI integration</li>
 * </ul>
 *
 * <p><strong>Implementation Notes:</strong></p>
 * <ul>
 * <li>Generators are discovered automatically through reflection</li>
 * <li>Each generator must be an inner class of the main sketch</li>
 * <li>The setup method is called once when the generator is assigned to a screen</li>
 * <li>The update method is called every frame for real-time content generation</li>
 * </ul>
 *
 * @author Daniel Corbani
 * @version 1.0
 * @see paletai.mapping.GenerativeMediaItem
 * @see PGraphics2D
 */
public interface LunaContentGenerator {
    /**
     * Returns the display name of this content generator.
     * This name is used in the UI for generator selection and identification.
     *
     * @return A unique, human-readable name for this generator
     */
    String getName();

    /**
     * Initializes the generator with the target display dimensions.
     * Called once when the generator is assigned to a screen. Use this method
     * to set up graphics buffers, initialize variables, and prepare resources.
     *
     * @param width The width of the target display in pixels
     * @param height The height of the target display in pixels
     */
    void setup(int width, int height);  // Remove PGraphics parameter

    /**
     * Updates the generator's internal state and renders content.
     * Called every frame to generate new content. Implementations should
     * update animation states, calculate new visuals, and draw to their
     * internal graphics buffer.
     */
    void update();            // Rename from draw to update

    /**
     * Returns the current graphics buffer containing the generated content.
     * This buffer is used by the video mapping system for homography transformation
     * and final rendering. The generator should maintain its own PGraphics2D buffer
     * and return it here.
     *
     * @return The PGraphics2D buffer containing the current generated frame
     */
    PGraphics2D getGraphics();          // Add this method

    /**
     * Returns the list of customizable parameters for this generator.
     * These parameters are exposed in the UI for real-time adjustment.
     * Each parameter should have a unique name that describes its function.
     *
     * @return Array of parameter names that can be adjusted
     */
    String[] getParameters();

    /**
     * Sets the value of a specific parameter.
     * Called when a parameter is adjusted in the UI. Implementations should
     * update their internal state based on the new parameter value.
     *
     * @param name The name of the parameter to set
     * @param value The new value for the parameter
     */
    void setParameter(String name, float value);
}