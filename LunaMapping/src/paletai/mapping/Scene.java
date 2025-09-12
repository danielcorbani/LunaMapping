package paletai.mapping;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.data.XML;

/**
 * A class that manages a collection of MediaItems as a single scene.
 * Handles rendering, media control, and calibration for multiple media elements.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Media item management and organization</li>
 *   <li>Active media selection for calibration</li>
 *   <li>Scene activation/deactivation</li>
 *   <li>Batch operations on all media items</li>
 * </ul>
 * 
 * @author Daniel Corbani
 * @version 1.0
 * @see MediaItem
 */
public class Scene {
	  int id;
	  public ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
	  public XML sceneXML;
	  boolean isActive;
	  int currentMediaCalibration = -1;

	  public Scene(int id) {
	    this.id = id;
	    sceneXML = new XML("Scene");
	    sceneXML.setInt("id", id);
	    sceneXML.addChild("Medias");
	    isActive = false;
	  }

	  Scene(XML scene) {
	    id = scene.getInt("id");
	    sceneXML = scene;
	    isActive = false;
	    
	  }

	  void render() {
	    if (isActive) {
	      for (MediaItem media : mediaItems) {
	        media.render();
	      }
	    }
	  }

	  void addMedia(MediaItem newMedia) {
	    mediaItems.add(newMedia);
	    if (currentMediaCalibration <0) currentMediaCalibration = 0;
	    if (!newMedia.fromxml) {
	      XML mediasXML = sceneXML.getChild("Medias");
	      mediasXML.addChild(newMedia.mediaXML);
	      //println("new Media added successfully");
	    }
	  }

	  void updateXML() {
	    XML mediasParent = sceneXML.getChild("Medias");
	    XML[] mediasXML = mediasParent.getChildren("MediaItem");
	    for (XML mediaXML : mediasXML) {
	      int i = mediaXML.getInt("id");
	      for (MediaItem media : mediaItems) {
	        if (media.mediaId == i) {
	          mediasParent.removeChild(mediaXML);
	          mediasParent.addChild(media.mediaXML);
	        }
	      }
	    }
	    //println("Scene XML updated?");
	  }

	  public void toggleCalibration() {
	    //for (MediaItem media : mediaItems) {
	    //  media.toggleCalibration();
	    //}
	    mediaItems.get(currentMediaCalibration).toggleCalibration();
	    PApplet.println("currentMediaCalibration: " + currentMediaCalibration);
	  }

	  public void changeMediaToCalibrate() {
	    if (mediaItems.get(currentMediaCalibration).calibrate) {
	      mediaItems.get(currentMediaCalibration).offCalibration();
	      currentMediaCalibration = (currentMediaCalibration+1)%mediaItems.size();
	      mediaItems.get(currentMediaCalibration).onCalibration();
	    }
	  }

	  public void deactivate() {
	    for (MediaItem media : mediaItems) {
	      media.stopMedia();
	      media.offCalibration();
	    }
	    isActive = false;
	  }

	  public void activate() {
	    for (MediaItem media : mediaItems) {
	      media.playMedia();
	    }
	    isActive = true;
	  }

	  public void toggleActivation() {
	    isActive = !isActive;
	    if (isActive) {
	      for (MediaItem media : mediaItems) {
	        media.playMedia();
	      }
	    } else {
	      for (MediaItem media : mediaItems) {
	        media.stopMedia();
	      }
	    }
	  }
	}