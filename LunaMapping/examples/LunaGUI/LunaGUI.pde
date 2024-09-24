import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.util.ArrayList;

ChildApplet childA;

//int prevWidth, prevHeight;

ResizablePanel leftPanel, rightPanel, scenesPanel, mainPanel;

void settings() {
  size(displayWidth * 4 / 5, displayHeight * 4 / 5);
  //prevWidth = width;
  //prevHeight = height;
}

void setup() {
  surface.setResizable(true);
  secondMonitorInit();

  leftPanel = new LeftPanel(0, 0, 150, height - 100);
  rightPanel = new RightPanel(width - 150, 0, 150, height - 100);
  scenesPanel = new ScenesPanel(0, height - 100, width, 100);
  mainPanel = new MainPanel(leftPanel.w, 0, width - leftPanel.w - rightPanel.w, height - scenesPanel.h);
}

void draw() {
  //mainScreen.show();
  //background(255);

  // Adjust panels' positions and sizes based on constraints
  adjustPanels();

  // Display panels
  leftPanel.drawContent();
  rightPanel.drawContent();
  scenesPanel.drawContent();
  mainPanel.drawContent();
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

void mousePressed() {
  leftPanel.OnMousePressed();
  rightPanel.OnMousePressed();
  scenesPanel.OnMousePressed();
}

void mouseDragged() {
  leftPanel.OnMouseDragged();
  rightPanel.OnMouseDragged();
  scenesPanel.OnMouseDragged();
}

void mouseReleased() {
  leftPanel.OnMouseReleased();
  rightPanel.OnMouseReleased();
  scenesPanel.OnMouseReleased();
}

void adjustPanels() {
  // Update the scenes panel width and position
  scenesPanel.w = width;
  scenesPanel.y = height - scenesPanel.h;

  // Update left panel height
  leftPanel.h = height - scenesPanel.h;

  // Update right panel position and height
  rightPanel.x = width - rightPanel.w;
  rightPanel.h = height - scenesPanel.h;

  // Update main panel position and size
  mainPanel.x = leftPanel.w;
  mainPanel.w = width - leftPanel.w - rightPanel.w;
  mainPanel.h = height - scenesPanel.h;
}

void fileSelected(File selection) {
  if (selection != null) {
    println("File selected");
    //leftPanel.fileSelected(selection);  // Pass the selected file to the LeftPanel
  }
}
