import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;

ChildApplet childA;
mainGUI mainScreen;
int prevWidth, prevHeight;

void settings() {
  size(displayWidth * 4 / 5, displayHeight * 4 / 5);
  prevWidth = width;
  prevHeight = height;
}

void setup() {
  // Set the main window to full size on the primary monitor
  surface.setResizable(true);

  secondMonitorInit();
  mainScreen = new mainGUI();
}

void draw() {
  mainScreen.show();
  checkResizedWindow();
}

void exit() {
  if (childA != null) {
    childA.dispose();
  }
  super.exit();
}

void keyPressed() {
  if (key == ESC) {
    key = 0; // Disable default ESC behavior
    exit();
  }
}

void checkResizedWindow() {
  if (width != prevWidth || height != prevHeight) {
    mainScreen.reconfigureGUI();
    prevWidth = width;
    prevHeight = height;
  }
}
