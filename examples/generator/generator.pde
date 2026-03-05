/**
 *
 * Luna Video Mapping | https://luna.art.br/
 * A Processing/Java library for Video Mapping.
 *
 * Created by Daniel Corbani
 * GPL 2.0 licence: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.en
 *
 */

/* In this example, generators are included.
 *       You need to have a webcam 
 */
 
import paletai.mapping.*;

//Luna need this two complementary libraries to work
//You can install them from the Processing Contrinution Manager
import controlP5.*;        
import processing.video.*;


Project project;

import processing.opengl.PGraphics2D;

// Called every time a new frame is available to read
void movieEvent(Movie m) {
  m.read();
}

void setup() {
  fullScreen(P2D, SPAN); //Always FullScreen, P2D and SPAN

  project = new Project(this, "generators");  //Name your project here
}

void draw() {
  background(0);
  
  project.render(mouseX,mouseY);
}

// press any key to save your project
// press space bar to go to next scene
void keyReleased() {
  project.keyreleased(key, keyCode);  // call method on the instance
}

void mouseDragged() {
  project.moveHoverPoint(mouseX, mouseY);  // Move hovered point while dragging
}
