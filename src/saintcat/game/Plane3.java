/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saintcat.game;


/**
 *
 * @author Admin
 */
public class Plane3 {

    Point3D normal;
    float distfromOrigin;

    Point3D getNormal() {
        return normal;
    }

    void setNormal(Point3D a) {
        normal = a;
    }

    float getDistfromOrigin() {
        return distfromOrigin;
    }

    void setDistfromOrigin(float a) {
        distfromOrigin = a;
    }
}
