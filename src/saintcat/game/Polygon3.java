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
public class Polygon3 {

    Point3D topleft;
    Point3D topright;
    Point3D botright;

    Polygon3() {
        topleft = new Point3D(0.0f, 0.0f, 0.0f);
        topright = new Point3D(0.0f, 0.0f, 0.0f);
        botright = new Point3D(0.0f, 0.0f, 0.0f);
    }

    Polygon3(Point3D _topleft, Point3D _topright, Point3D _botright) {
        topleft = _topleft;
        topright = _topright;
        botright = _botright;
    }

    Point3D getTopLeft() {
        return topleft;
    }

    Point3D getTopRight() {
        return topright;
    }

    Point3D getBotRight() {
        return botright;
    }

    void setTopLeft(Point3D _topleft) {
        topleft = _topleft;
    }

    void setTopRight(Point3D _topright) {
        topright = _topright;
    }

    void setBotRight(Point3D _botright) {
        botright = _botright;
    }

    @Override
    public String toString() {
        return topleft + " " + topright + " " + botright;
    }
    
    
}
