import paletai.mapping.*;
import processing.opengl.*;
import processing.video.Movie;

PImage img;
VidMap vm1, vm2;
Movie mov;
float duration = 0;

void setup(){
  //size(640,480,P2D);
  fullScreen(P2D,2);
  img = loadImage("https://t3.ftcdn.net/jpg/02/43/25/90/360_F_243259090_crbVsAqKF3PC2jk2eKiUwZHBPH8Q6y9Y.jpg");
  //mov = new Movie(this,"beach01.mp4");
  //mov.loop();
  //duration = mov.duration()*0.99;
  
  vm1 = new VidMap(this,"vm1");
  //vm2 = new VidMap(this,"vm2");
}


void draw(){
  background(100,50,50);
  //image(img,0,0);
  //if(mov.available()) mov.read(); 
  vm1.checkHover(mouseX, mouseY);  // Check if mouse is hovering over a point
  vm1.show(img);
  //vm2.checkHover(mouseX, mouseY);  // Check if mouse is hovering over a point
  //vm2.show(mov);
  //if(mov.time()>duration) mov.jump(0);
}

void keyReleased(){
  if (key == 'c') {
    vm1.toggleCalibration();
    //vm2.toggleCalibration();
  }
  if (key == 's'){
    vm1.save();
    //vm2.save();
  }
  if (key == 'l'){
    vm1.load();
    //vm2.load();
  }
}

void mouseDragged() {
  vm1.moveHoverPoint(mouseX, mouseY);  // Move hovered point while dragging
  //vm2.moveHoverPoint(mouseX, mouseY);  // Move hovered point while dragging
}
