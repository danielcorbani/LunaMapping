

enum PanelType {
  VERTICAL, HORIZONTAL, MAIN;
}

abstract class ResizablePanel {
  float x, y, w, h;
  boolean isResizing = false;
  PanelType type;
  float resizeMargin = 40;

  ResizablePanel(float x, float y, float w, float h, PanelType type) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.type = type;
  }

  void drawContent() {
  }

  // Check if the mouse is over the resize handle
  boolean overResizeHandle() {
    switch (type) {
    case VERTICAL:
      // Left panel: right edge; Right panel: left edge
      return (this == leftPanel && mouseX > x + w - resizeMargin && mouseX < x + w && mouseY > y && mouseY < y + h) ||
        (this == rightPanel && mouseX > x && mouseX < x + resizeMargin && mouseY > y && mouseY < y + h);
    case HORIZONTAL:
      // Scenes panel: top edge
      return mouseY > y && mouseY < y + resizeMargin && mouseX > x && mouseX < x + w;
    default:
      return false;
    }
  }

  void OnMousePressed() {
    if (overResizeHandle()) {
      isResizing = true;
    }
  }

  void OnMouseDragged() {
    if (isResizing) {
      switch (type) {
      case VERTICAL:
        if (this == leftPanel) {
          // Left panel: resize width
          //w = mouseX - x;
          w = constrain(mouseX - x, 0, 250);  // Width 0 to 150
        } else if (this == rightPanel) {
          // Right panel: resize by changing its x position
          //float newWidth = width - mouseX;
          float newWidth = constrain(width - mouseX, 0, 250);
          x = width - newWidth;
          w = newWidth;
        }
        break;
      case HORIZONTAL:
        // Scenes Panel: resize height from top edge
        //h = height - mouseY;
        h = constrain(height - mouseY, 40, 200);  // Height 40 to 200
        break;
      case MAIN:
        // Scenes Panel: resize height from top edge
        //h = height - mouseY;
        h = constrain(height - mouseY, 40, 200);  // Height 40 to 200
        break;
      }
    }
  }

  void OnMouseReleased() {
    isResizing = false;
  }
}

class LeftPanel extends ResizablePanel {
  ArrayList<String> fileList;
  float buttonX, buttonY, buttonW, buttonH;

  LeftPanel(float x, float y, float w, float h) {
    super(x, y, w, h, PanelType.VERTICAL);
    fileList = new ArrayList<String>();
    // Button properties
    buttonW = 100;
    buttonH = 30;
    buttonX = 10;
    buttonY = 10;
  }

  @Override
    void drawContent() {
    fill(120);
    stroke(0);
    rect(x, y, w, h);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Left Panel", x + w / 2, y + h / 2);

    // Draw the import button
    drawButton();
    // Display the list of imported files
    displayFileList();

    // Add more functionalities specific to the left panel here
  }

  void drawButton() {
    fill(180);
    rect(buttonX, buttonY, buttonW, buttonH);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Import", buttonX + buttonW / 2, buttonY + buttonH / 2);
  }

  void displayFileList() {
    fill(0);
    textAlign(LEFT, CENTER);
    float textY = buttonY + buttonH + 10;
    for (String file : fileList) {
      text(file, 10, textY);
      textY += 20; // Move to the next line
    }
  }

  @Override  //needed to include the button, yet keeping the original
    void OnMousePressed() {
    super.OnMousePressed();
    if (mouseOverButton()) {
      // Open file dialog
      selectInput("Select a file to import:", "fileSelected");
    }
  }

  boolean mouseOverButton() {
    return mouseX > x + buttonX && mouseX < x + buttonX + buttonW &&
      mouseY > y + buttonY && mouseY < y + buttonY + buttonH;
  }

  public void fileSelected(File selection) {
    if (selection != null) {
      fileList.add(selection.getName());
    }
  }
}

class RightPanel extends ResizablePanel {
  RightPanel(float x, float y, float w, float h) {
    super(x, y, w, h, PanelType.VERTICAL);
  }

  @Override
    void drawContent() {
    fill(120);
    stroke(0);
    rect(x, y, w, h);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Right Panel", x + w / 2, y + h / 2);
    // Add more functionalities specific to the right panel here
  }
}

class ScenesPanel extends ResizablePanel {
  ScenesPanel(float x, float y, float w, float h) {
    super(x, y, w, h, PanelType.HORIZONTAL);
  }

  @Override
    void drawContent() {
    fill(120);
    stroke(0);
    rect(x, y, w, h);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Scenes Panel", x + w / 2, y + h / 2);
    // Add more functionalities specific to the scenes panel here
  }
}

class MainPanel extends ResizablePanel {
  MainPanel(float x, float y, float w, float h) {
    super(x, y, w, h, PanelType.MAIN);
  }

  @Override
    void drawContent() {
    fill(120);
    stroke(0);
    rect(x, y, w, h);
    fill(0);
    textAlign(CENTER, CENTER);
    text("Main Panel", x + w / 2, y + h / 2);
    // Add more functionalities specific to the main panel here
  }
}
