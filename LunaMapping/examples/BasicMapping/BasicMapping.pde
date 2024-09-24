import paletai.mapping.*;
import processing.opengl.*;

PImage img;
VidMap vm;


void setup(){
  size(640,480,P2D);
  img = loadImage("https://t3.ftcdn.net/jpg/02/43/25/90/360_F_243259090_crbVsAqKF3PC2jk2eKiUwZHBPH8Q6y9Y.jpg");
  
  vm = new VidMap(this);
}


void draw(){
  background(100,50,50);
  //image(img,0,0);
  vm.show(img);
}

void keyReleased(){
  if (key == 'c') vm.toggleCalibration();
}
