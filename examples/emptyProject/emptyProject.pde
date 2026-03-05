/**
 *
 * Luna Video Mapping | https://luna.art.br/
 * A Processing/Java library for Video Mapping.
 *
 * Created by Daniel Corbani
 * GPL 2.0 licence: https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.en
 *
 */


/* Befor running this sketch, include all necessary media files
 *        in the data folder. The library will find them and made
 *        them available in the left panel.
 */
 
import paletai.mapping.*;

//Luna need this two complementary libraries to work
//You can install them from the Processing Contrinution Manager
import controlP5.*;
import processing.video.*;


Project project;

// Called every time a new frame is available to read
void movieEvent(Movie m) {
  m.read();
}

void setup() {
  fullScreen(P2D, SPAN); //This should always be FullScreen, P2D and SPAN
  project = new Project(this, "NewProject");  //Name your project here
}

void draw() {
  background(0);
  project.render(mouseX, mouseY);
}

// press any key to save your project
// press 'space bar' to go to next scene
void keyReleased() {
  project.keyreleased(key, keyCode);  // call method on the instance
}

void mouseDragged() {
  project.moveHoverPoint(mouseX, mouseY);  // Move hovered point while dragging
}
