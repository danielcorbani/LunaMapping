void secondMonitorInit(){
  // Determine the size and position for the second window
  GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  GraphicsDevice[] screens = ge.getScreenDevices();
  //println(screens[0].getDisplayMode().getWidth());
  if (screens.length > 1) {
    // Second monitor is available, use it for the second window
    GraphicsDevice secondMonitor = screens[1];
    java.awt.DisplayMode dm = secondMonitor.getDisplayMode();
    int secondX = secondMonitor.getDefaultConfiguration().getBounds().x;
    int secondY = secondMonitor.getDefaultConfiguration().getBounds().y;
    int secondWidth = dm.getWidth();
    int secondHeight = dm.getHeight();

    // Create the second window full screen on the second monitor
    childA = new ChildApplet(2, secondX, secondY, secondWidth, secondHeight, true);
  } else {
    // Second monitor is not available, use a default size on the primary monitor
    childA = new ChildApplet(2, 0, 0, 640, 480, false);
  }
  
}
