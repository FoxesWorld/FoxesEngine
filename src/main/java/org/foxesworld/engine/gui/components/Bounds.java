package org.foxesworld.engine.gui.components;

import java.awt.*;

public class Bounds {
    private int x, y;
    private Size size;

    public Bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.size.width = width;
        this.size.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public static class Size {
        private int width, height;

        public Size() {}

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public Rectangle getBounds(){
        return new Rectangle(this.x, this.y, this.getSize().width, this.getSize().height);
    }
}