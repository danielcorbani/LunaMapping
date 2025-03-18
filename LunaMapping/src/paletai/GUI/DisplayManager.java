package paletai.GUI;

import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;

public class DisplayManager {
    private boolean secondMonitorAvailable;
    private int primaryMonitorIndex;
    private int secondaryMonitorIndex;

    public DisplayManager() {
        detectMonitors();
    }

    // Detect available monitors
    private void detectMonitors() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        primaryMonitorIndex = 0; // Default to first monitor
        secondMonitorAvailable = screens.length > 1;

        if (secondMonitorAvailable) {
            secondaryMonitorIndex = 1; // Assume second monitor is index 1
        } else {
            secondaryMonitorIndex = -1; // No second monitor
        }
    }

    // Returns true if a second monitor is detected
    public boolean isSecondMonitorAvailable() {
        return secondMonitorAvailable;
    }

    // Returns the index of the primary monitor
    public int getPrimaryMonitorIndex() {
        return primaryMonitorIndex;
    }

    // Returns the index of the secondary monitor, or -1 if not available
    public int getSecondaryMonitorIndex() {
        return secondaryMonitorIndex;
    }
}
