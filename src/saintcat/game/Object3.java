/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saintcat.game;

import java.awt.Color;

/**
 *
 * @author Admin
 */
public class Object3 {

    Point3D position;
    Point3D velocity;
    Point3D acceleration;
    Point3D axis;
    Color color;
    float alpha;
    float mass;
    float bodyrot;

    Point3D getPosition() {
        return position;
    }

    Point3D getVelocity() {
        return velocity;
    }

    Point3D getAcceleration() {
        return acceleration;
    }

    Point3D getAxis() {
        return axis;
    }

    void setPosition(Point3D a) {
        position = a;
    }

    void setVelocity(Point3D a) {
        velocity = a;
    }

    void setAcceleration(Point3D a) {
        acceleration = a;
    }

    void setAxis(Point3D a) {
        axis = a;
    }

    float getMass() {
        return mass;
    }

    void setMass(float a) {
        mass = a;
    }
}
