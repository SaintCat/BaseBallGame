package saintcat.game;

import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.GLUT;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.GL;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_ONE;
import static javax.media.opengl.GL.GL_SMOOTH;
import static javax.media.opengl.GL.GL_SRC_ALPHA;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

public class MirrorTest3 implements GLEventListener {

    public static enum PointLocation {

        PLANEFRONT, PLANEBACK, ONPLANE;
    }

    static final int MAXTRIANGLES = 20;
    static final int BOXSIZE = 40;
    static final int FLOORSIZE = 50;
    static final int BALLRADIUS = 5;
    static final int BALLMAXCOUNT = 20;
    //uprugost
    static final float e = 0.80f;
    //trenie
    static final float k = 0.05f;
    static final float mass = 10f;
    static final float gravity = -9.8f;
    static final float dt = 0.01f;

    static Point3D first = new Point3D(30,-10,-FLOORSIZE);
    static Point3D second = new Point3D(30,-10,FLOORSIZE);
    static Point3D third = new Point3D(30,40,-FLOORSIZE);
    static Point3D forth = new Point3D(30,40,FLOORSIZE);
    
    static Integer floorCounter = new Integer(0);
    static int floorNum = 4;
    static float[][][] floorVertices = new float[][][]{{{-FLOORSIZE, -10, FLOORSIZE}, {-FLOORSIZE, -10, -FLOORSIZE}, {FLOORSIZE, -10, -FLOORSIZE}},
    {{FLOORSIZE, -10, -FLOORSIZE}, {FLOORSIZE, -10, FLOORSIZE}, {-FLOORSIZE, -10, FLOORSIZE}},
    quadPointsFirst(forth, third, second, first), quadPointsFirst(forth, third, second, first)};

    static Polygon3[] myfloor = new Polygon3[MAXTRIANGLES];
    static Object3 bola = new Object3();
    static Point3D windvelocity = new Point3D(0, 0, 0);
    

    public static float[][] quadPointsFirst(Point3D fi, Point3D s, Point3D t, Point3D f) {
        return new float[][]{
            {fi.x, fi.y, fi.z}, {s.x, s.y, s.z}, {t.x, t.y, t.z}};
    }

    public static float[][] quadPointsSecond(Point3D fi, Point3D s, Point3D t, Point3D f) {
        return new float[][]{
            {s.x, s.y, s.z}, {t.x, t.y, t.z}, {f.x, f.y, f.z}};
    }

    public static void main(String[] args) {
        MirrorTest3 rr = new MirrorTest3();
        addBall();
        rr.buildFloorDimensions(floorNum, myfloor, floorVertices);
        ConfParmSubsystem.getInstance().initialize();
        GLCapabilities capa = new GLCapabilities();
        GLCanvas glcanvas = new GLCanvas(capa);
        glcanvas.addKeyListener(new KeyboardController());
        glcanvas.addGLEventListener(rr);// initialize GLUT
        glcanvas.setSize(900, 600);
        final FPSAnimator animator = new FPSAnimator(glcanvas, 60, true);
        Frame frame = new Frame("TEST");
        frame.add(glcanvas, BorderLayout.CENTER);
        frame.setSize(glcanvas.getPreferredSize());
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClearDepth(1);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_BLEND);
        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }
    private GLU glu = new GLU();

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        ConfParmSubsystem.AppContext appContext = ConfParmSubsystem.getInstance().getCtxt();
        gl.glClearDepth(1);

        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(appContext.cameraOffsetX, appContext.cameraOffsetY, appContext.cameraOffsetZ);
        gl.glRotatef(appContext.cameraAngleX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(appContext.cameraAngleY, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(appContext.cameraAngleZ, 0.0f, 0.0f, 1.0f);
        glu.gluLookAt(0.0, 20.0, 100.0, // eyeX, eyeY, eyeZ
                0.0, 0.0, 0.0, // centerX, centerY, centerZ
                0.0, 1.0, 0.0);		// upX, upY, upZ

        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        drawFloor(gl);
        drawBall(gl);
        ballDisplacement();

    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45, width / height, 1.0, 200.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    void determinePointLocation(Plane3 plane, Point3D pStart, Point3D pDest, PointLocation pStartLoc, PointLocation pDestLoc) {
        /////////////////////////////////////////////////////////////////////////
        // Now we are going to start comparing on which side is the starting point
        // and the destination point. If they are not on the same side, that means 
        // the point crossed over to the other side of the plane.
        /////////////////////////////////////////////////////////////////////////
        float beforecollision = pStart.dotProduct(plane.getNormal()) + plane.getDistfromOrigin();
        if (beforecollision > 0) {
            pStartLoc = PointLocation.PLANEFRONT;
        } else if (beforecollision < 0) {
            pStartLoc = PointLocation.PLANEBACK;
        } else if (beforecollision == 0) {
            pStartLoc = PointLocation.ONPLANE;
        }

        ////////////////////////////////////////////////////////////////////////
        // The value of this last collision is actually enough to check for 
        // collision. If (aftercollision > 0), then ball is infront of plane (crossed over)
        // and there is a collision with plane.
        ////////////////////////////////////////////////////////////////////////
        float aftercollision = pDest.dotProduct(plane.getNormal()) + plane.getDistfromOrigin();
        if (aftercollision > 0) {
            pDestLoc = PointLocation.PLANEFRONT;
        } else if (aftercollision < 0) {
            pDestLoc = PointLocation.PLANEBACK;
        } else {
            pDestLoc = PointLocation.ONPLANE;
        }

    }

    PointLocation determinePointLocationStart(Plane3 plane, Point3D pStart, Point3D pDest) {
        float beforecollision = pStart.dotProduct(plane.getNormal()) + plane.getDistfromOrigin();
//        System.out.println(beforecollision);
        if (beforecollision > 0) {
            return PointLocation.PLANEFRONT;
        } else if (beforecollision < 0) {
            return PointLocation.PLANEBACK;
        } else if (beforecollision == 0) {
            return PointLocation.ONPLANE;
        }
        return null;
    }

    PointLocation determinePointLocationEnd(Plane3 plane, Point3D pStart, Point3D pDest) {
        float aftercollision = pDest.dotProduct(plane.getNormal()) + plane.getDistfromOrigin();
//        System.out.println("After " + aftercollision);
        if (aftercollision > 0) {
            return PointLocation.PLANEFRONT;
        } else if (aftercollision < 0) {
            return PointLocation.PLANEBACK;
        } else {
            return PointLocation.ONPLANE;
        }
    }

    Point3D findVectorFrom2Points(Point3D vec1, Point3D vec2) {
        return vec2.substract(vec1);
    }

    Wrapper findTwoVectorsOfPlane(Polygon3 pol, Point3D vec1, Point3D vec2) {
        vec1 = findVectorFrom2Points(pol.getTopLeft(), pol.getBotRight());
        vec2 = findVectorFrom2Points(pol.getTopRight(), pol.getBotRight());
        return new Wrapper(vec1, vec2);
    }

    public class Wrapper {

        Point3D vec1;
        Point3D vec2;

        public Wrapper(Point3D vec1, Point3D vec2) {
            this.vec1 = vec1;
            this.vec2 = vec2;
        }

    }

//////////////////////////////////////////////////////////////////////////////////////
//
// Functions below are used for Collision Detection
//
// I would like to acknowledge this site:
// http://gamedeveloperjourney.blogspot.com/2009/04/point-plane-collision-detection.html
// for the algorithm of collision detection, that helped me a lot with the 3 functions
// below. I think this is the best blog 
// that clearly explains the physics and math behind polygon collision.
// I can say that this is the ONLY site that made me understand the concept
// behind collision detection. It helped me a lot!!!
// 
///////////////////////////////////////////////////////////////////////////////////////
    Plane3 findPlaneAttributes(Polygon3 poly) {
        ///////////////////////////////////////////////////////////////////
        // Below is all about our Plane
        // Ax + By + Cz + D = 0
        // 1. Find two vectors on a plane			-- use three vertices of polygon
        // 2. Find cross product of those 2 vectors -- values of A,B,C
        // 3. Normalize the cross product			-- unit length of 1
        // 4. Find the distance of Plane from origin -- value of D
        //////////////////////////////////////////////////////////////////
        Plane3 plane = new Plane3();
        Point3D planeVec1 = null, planeVec2 = null;
        Wrapper wp = findTwoVectorsOfPlane(poly, planeVec1, planeVec2);
        planeVec1 = wp.vec1;
        planeVec2 = wp.vec2;
        Point3D cross = planeVec1.crossProduct(planeVec2);
        plane.setNormal(cross.normalize());

        float distFromOrigin = -(poly.getTopLeft().dotProduct(plane.getNormal()));
        plane.setDistfromOrigin(distFromOrigin);
        return plane;
    }

////////////////////////////////////////////////////////////////////////////
//
// Function to check if point is actually inside polygon
//
////////////////////////////////////////////////////////////////////////////
    boolean isPointBounded(Polygon3 poly, Point3D pStart, Point3D pDest, Plane3 plane) {
        //////////////////////////////////////////////////////////////////////////
        // Now find the bounds of poylgon
        // Use formula intersect = pStart + ray * t
        // 1. Calculate the ray = pDest - pStart
        // 2. Normalize ray
        // 3. Find t = -(DotProduct(N, pStart) + D)/ DotProduct(N, ray)
        // 4. Find intersection with plane --> pointOfIntersection
        // 5. Determine if intersection hit the collision surface of polygon
        ////////////////////////////////////////////////////////////////////////
        Point3D ray = findVectorFrom2Points(pDest, pStart);
        Point3D rayNormal = ray.normalize();

        float t = -(pStart.dotProduct(plane.getNormal()) + plane.getDistfromOrigin()) / ray.dotProduct(plane.getNormal());
        Point3D pointOfIntersection = pStart.add(ray.multiply(t));

        Point3D v1 = pointOfIntersection.substract(poly.getTopLeft());
        Point3D v2 = pointOfIntersection.substract(poly.getTopRight());
        Point3D v3 = pointOfIntersection.substract(poly.getBotRight());

        Point3D v1Normal = v1.normalize();
        Point3D v2Normal = v2.normalize();
        Point3D v3Normal = v3.normalize();

        // Angles around intersection should total 360 degrees (2 PI)
        float angle1 = (float) Math.acos(v1Normal.dotProduct(v2Normal));
        float angle2 = (float) Math.acos(v2Normal.dotProduct(v3Normal));
        float angle3 = (float) Math.acos(v3Normal.dotProduct(v1Normal));
        float thetaSum = angle1 + angle2 + angle3;

        if (Math.abs(thetaSum - (2 * Math.PI)) < 0.1) {
            System.out.println("RETURN TRUE");
            return true; //cout << "Point lies in plane" << endl; 
        } else {
            System.out.println("RETURN FALSE");
            return false; //cout << "Point lies NOT in plane" << endl; //
        }
    }

////////////////////////////////////////////////////////////
//
// Function to compute velocity after collision between 
// two points. e is elasticity coefficient whose value
// is from 0 to 1.
//
////////////////////////////////////////////////////////////
    Point3D collisionVelocity(Point3D normal, Point3D velocity, float e) {
        Point3D velocityTemp = velocity.multiply(-1);

        //calculate the projection.
        float projection = velocityTemp.dotProduct(normal);

        //Take the length of the projection against the normal.
        Point3D lengthVector = normal.multiply(projection);

        ///////////////////////////////////////////////////////////////////////
        // Lets obtain the final velocity vector.
        // We can stop here because we found the final velocity after collision.
        // But this is the collision on the plane of the polygon and not the 
        // polygon itself! We still need to figure out if the ball actually hit 
        // the polygon itself.
        ////////////////////////////////////////////////////////////////////////
        Point3D reflection = lengthVector.multiply(1 + e);
        Point3D velocityFinal = reflection.add(velocity);
        return velocityFinal;

    }

////////////////////////////////////////////////////////////////////////////
//
// Function combining detection and reflection functions, returns new velocity
//
////////////////////////////////////////////////////////////////////////////
    void detectAndReflect(int ff, Polygon3 poly, Point3D pStart, Point3D pDest, Point3D vel, float e) {
        Plane3 plane = null;
        PointLocation pStartLoc = null, pDestLoc = null;
        plane = findPlaneAttributes(poly);
        plane.setNormal(plane.getNormal().multiply(1));
//        System.out.println("Find plane attribues " + plane.getNormal());
//        determinePointLocation(plane, pStart, pDest, pStartLoc, pDestLoc);
//        System.out.println(pStart + " " + pDest);
        pStartLoc = determinePointLocationStart(plane, pStart, pDest);
        pDestLoc = determinePointLocationEnd(plane, pStart, pDest);
        if(ff == 2) {
        System.out.println(pDestLoc);
        }
        /*if (pDestLoc == (PointLocation.PLANEBACK)) {
        } */else if (!pStartLoc.equals(pDestLoc) && isPointBounded(poly, pStart, pDest, plane)) {
            // Compute the new velocity after object collides with polygon
            System.out.println("NEW VELOSITY!!!!!!!");
            System.out.println(bola.getVelocity());
            Point3D finalVelocity = collisionVelocity(plane.getNormal(), vel, e);
            System.out.println(poly);
            bola.setVelocity(finalVelocity);
            System.out.println(bola.getVelocity());
        }
    }

    void drawFloor(GL gl) {
        for (int i = 0; i < floorCounter; i++) {
            //drawTri(myfloor[i].topleft, myfloor[i].topright, myfloor[i].botright);
            gl.glPushMatrix();
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glColor4f(0.2f, 0.2f, 0.7f, 1.0f);
            gl.glVertex3f(myfloor[i].getTopLeft().x, myfloor[i].getTopLeft().y - 4, myfloor[i].getTopLeft().z);
            gl.glVertex3f(myfloor[i].getTopRight().x, myfloor[i].getTopRight().y - 4, myfloor[i].getTopRight().z);
            gl.glVertex3f(myfloor[i].getBotRight().x, myfloor[i].getBotRight().y - 4, myfloor[i].getBotRight().z);
            gl.glEnd();
            gl.glPopMatrix();
        }
    }

    void buildTriangle(int count, Polygon3[] tri, Point3D tl, Point3D tr, Point3D br) {
        tri[count - 1].setTopLeft(tl);
        tri[count - 1].setTopRight(tr);
        tri[count - 1].setBotRight(br);
    }

    void buildFloorDimensions(int num, Polygon3[] tri, float vert1[][][]) {
        Point3D vrtx[] = new Point3D[3];
        for (int z = 0; z < tri.length; z++) {
            tri[z] = new Polygon3();
        }
        for (int i = 0; i < num; i++) {
            floorCounter++;
            for (int j = 0; j < 3; j++) {
                vrtx[j] = new Point3D(0, 0, 0);
                vrtx[j].x = (vert1[i][j][0]);
                vrtx[j].y = (vert1[i][j][1]);
                vrtx[j].z = (vert1[i][j][2]);
            }

            buildTriangle(floorCounter, tri, vrtx[0], vrtx[1], vrtx[2]);
//            System.out.println("Building new floor " + tri[i]);
        }
    }

    GLUT glut = new GLUT();

    void drawBall(GL gl) {
        gl.glPushMatrix();
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glTranslatef(bola.getPosition().x, bola.getPosition().y, bola.getPosition().z);
        glut.glutSolidSphere(BALLRADIUS, 50, 50);
        //glutSolidCube(6);
        gl.glPopMatrix();

    }

    void setAcceleration(Object3 bola, Point3D gravity, Point3D windvelocity, float k) {
        bola.setAcceleration(gravity);

        float X = bola.getAcceleration().x - (k / bola.getMass() * bola.getVelocity().x + k / bola.getMass() * windvelocity.x);
        float Y = bola.getAcceleration().y - (k / bola.getMass() * bola.getVelocity().y + k / bola.getMass() * windvelocity.y);
        float Z = bola.getAcceleration().z - (k / bola.getMass() * bola.getVelocity().z + k / bola.getMass() * windvelocity.z);

        Point3D newAcceleration = new Point3D(X, Y, Z);
        bola.setAcceleration(newAcceleration);

    }

    void eulerIntegrate(Object3 bola, float dt) {
        Point3D Pos;
        Point3D Vel;

        Pos = bola.getPosition().add(bola.getVelocity().multiply(dt));
        Vel = bola.getVelocity().add(bola.getAcceleration().multiply(dt));

        bola.setVelocity(Vel); // Update object's velocity
        bola.setPosition(Pos); // Update object's position
    }

    void ballDisplacement() {

        Point3D Ygravity = new Point3D(0, gravity, 0);
        Point3D pStart = bola.getPosition(); // Save old position for use in collision detection later

        setAcceleration(bola, Ygravity, windvelocity, k); // Compute total acceleration and then integrate
        eulerIntegrate(bola, dt);
        Point3D pDest = bola.getPosition(); // Save new position

        Point3D velocity = bola.getVelocity(); // Save old velocity
        for (int pp = 0; pp < floorCounter; pp++) {
//            System.out.println(bola.getVelocity());
            detectAndReflect(pp, myfloor[pp], pStart, pDest, velocity, e); // Detect collision with the floor
//        System.out.println("After" + bola.getVelocity());
        }

//        Polygon3 tiltingFloor = null;
//        detectAndReflect(tiltingFloor, pStart, pDest, velocity, e); // Detect collision with other surfaces (if any)
//        bola.setVelocity(velocity); // Save new velocity
    }

    static void addBall() {
        Point3D initAccel = new Point3D(0.0f, 00.0f, 0.0f);
        bola.setAcceleration(initAccel);
        bola.setMass(0.5f);

        Point3D initPosition = new Point3D(-10.0f, 10.0f, 20.0f);
        bola.setPosition(initPosition);

        Point3D initVelocity = new Point3D(10.0f, 0.0f, 0.0f);
        bola.setVelocity(initVelocity);
    }

}
