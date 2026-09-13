/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.alemdoequilibrio.game;

public class Hero {
    private double x;
    private double y;
    private double speed;
    
    public Hero(double x, double y, double speed){
        this.x = x;
        this.y = y;
        this.speed = speed;
    }
    
    public void updateMovement(double directionX, double directionY, 
            double deltaTime, double maxX, double maxY){
        
        double length = Math.sqrt(directionX * directionX + directionY * directionY);
        
        if (length > 0){
            directionX /= length;
            directionY /= length;
        }
        
        double newX = x + directionX * speed * deltaTime;
        double newY = y + directionY * speed * deltaTime;
        
        x = Math.max(0, Math.min(newX, maxX));
        y = Math.max(0, Math.min(newY, maxY));
    }
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
}
