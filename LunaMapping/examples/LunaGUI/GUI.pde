class mainGUI {
  int dWidth, dHeight;
  color BG;
  resizableWindowGUI library, tools, scenes;

  mainGUI() {
    BG = color(150);
    scenes  = new resizableWindowGUI(width, 80, "SCENES");
    library = new resizableWindowGUI(300, 800, "LIBRARY");
    tools   = new resizableWindowGUI(300, 800, "TOOLS");
  }

  void show() {
    background(BG);
    textAlign(CENTER, CENTER);
    textSize(32);
    fill(255);
    text("Main Window", width / 2, height / 2);
    library.show();
    tools.show();
    scenes.show();
  }

  void reconfigureGUI() {
    library.resizeWindow();
    tools.resizeWindow();
    scenes.resizeWindow();
  }
  
}

class resizableWindowGUI {
  float w, h, x, y, resizeMargin;
  color LC;
  String canvas;

  resizableWindowGUI(float w, float h, String canvas) {
    LC = color(100);
    this.w    = w;
    this.h    = h;
    this.canvas = canvas;
    resizeMargin = 10;
    resizeWindow();
  }

  void show() {
    fill(LC);
    stroke(255);
    rect(x, y, w, h);
    textAlign(CENTER, CENTER);
    textSize(24);
    fill(255);
    text(canvas + " Window", x + w/2, y + h/2);
  }

  void resizeWindow() {
    switch(canvas) {
    case "LIBRARY":
      this.x    = 0;
      this.y    = 0;
      break;
    case "TOOLS":
      this.x    = width-w;
      this.y    = 0;
      break;
    case "SCENES":
      this.x    = 0;
      this.y    = height-h;
      this.w    = width;
      break;
    }
  }
  
  boolean overResizeHandle() {
    return mouseX > x + w - resizeMargin && mouseX < x + w &&
           mouseY > y + h - resizeMargin && mouseY < y + h;
  }
}
