package saintcat.game;

import java.util.Properties;
import java.util.logging.Logger;

public class ConfParmSubsystem {

    private static ConfParmSubsystem instance;

    public synchronized static ConfParmSubsystem getInstance() {
        if(instance == null) {
            instance = new ConfParmSubsystem();
        }
        return instance;
    }

    private ConfParmSubsystem() {}

    private AppContext ctxt;
    private static Logger log;

    public void initialize() {


        ctxt = AppContext.loadContext();
    }

    public AppContext getCtxt() {
        return ctxt;
    }

    public static class AppContext {
        private AppContext(Properties props) {
            String currentProperty;

            currentProperty = props.getProperty("initialWidth");
            if(currentProperty != null) {
                WIDTH = Integer.parseInt(currentProperty);
            } else {
                WIDTH = DEFAULT_WIDTH;
            }

            currentProperty = props.getProperty("initialHeight");
            if(currentProperty != null) {
                HEIGHT = Integer.parseInt(currentProperty);
            } else {
                HEIGHT = DEFAULT_HEIGHT;
            }

            currentProperty = props.getProperty("title");
            if(currentProperty != null) {
                TITLE = currentProperty;
            } else {
                TITLE = DEFAULT_TITLE;
            }
        }

        static AppContext loadContext() {
            Properties props = new Properties();
            return new AppContext(props);
        }

        private final int WIDTH;
        private final int DEFAULT_WIDTH = 800;

        public int getWIDTH() {
            return WIDTH;
        }

        private final int HEIGHT;
        private final int DEFAULT_HEIGHT = 600;

        public int getHEIGHT() {
            return HEIGHT;
        }

        private final String TITLE;
        private final String DEFAULT_TITLE = "Title didn't get loaded...";

        public String getTITLE() {
            return TITLE;
        }

        public float cameraAngleX=0.0f, cameraAngleY=0.0f, cameraAngleZ=0.0f;

        public float cameraOffsetX=0.0f, cameraOffsetY=0.0f, cameraOffsetZ=-6.0f;

        public float leftWingAngle=-30.0f, rightWingAngle=90.0f;
        
        public Point3D firstPoint = new Point3D(-10, 0, 0);
        
        public Point3D secondPoint = new Point3D(10, 0, 0);;
        
        public float clipPlaneRotateAngle = 0f;
        
        public float firstCubeAngle = 0f;
        
        public float secondCubeAngle = 0f;
        
        public float thirdCubeAngle = 0f;
    }
}
