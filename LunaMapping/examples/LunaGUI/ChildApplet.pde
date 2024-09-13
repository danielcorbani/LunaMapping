import processing.core.PApplet;
import java.awt.Frame;
import java.awt.Window;

class ChildApplet extends PApplet {
  int id;
  int vx, vy, vw, vh;
  boolean isFullScreen;

  ChildApplet(int id, int vx, int vy, int vw, int vh, boolean isFullScreen) {
    super();
    this.id = id;
    this.vx = vx;
    this.vy = vy;
    this.vw = vw;
    this.vh = vh;
    this.isFullScreen = isFullScreen;

    PApplet.runSketch(new String[] { this.getClass().getName() }, this);
  }

  void settings() {
    if (isFullScreen) {
      fullScreen(P2D);
    } else {
      size(vw, vh, P2D);
    }
  }

  void setup() {
    windowMove(vx, vy);
    windowResizable(true);

    // Set always on top
    setAlwaysOnTop();
  }

  void draw() {
    background(0);
    fill(255);
    textAlign(CENTER, CENTER);
    textSize(32);
    text("CHILD window " + id, width / 2, height / 2);
  }

  void keyPressed() {
    if (key == ESC) {
      key = 0; // Disable default ESC behavior
      exit();
    }
  }

  void setAlwaysOnTop() {
    PSurface pSurface = getSurface();
    if (pSurface.getNative() instanceof Window) {
      Window awtWindow = (Window) pSurface.getNative();
      awtWindow.setAlwaysOnTop(true);
    } else if (pSurface.getNative() instanceof Frame) {
      Frame awtFrame = (Frame) pSurface.getNative();
      awtFrame.setAlwaysOnTop(true);
    }
  }
}
