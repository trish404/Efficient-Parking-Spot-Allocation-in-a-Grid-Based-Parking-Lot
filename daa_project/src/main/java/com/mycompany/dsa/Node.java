package com.mycompany.dsa;

import java.awt.Color;

public class Node {
    public int id; // Unique identifier for the node
    public int x; // X-coordinate of the node
    public int y; // Y-coordinate of the node
    public boolean isTwoWheeler; // Indicates if the node is for two-wheeler parking
    public boolean isAllocated; // Indicates if the node is allocated
    public boolean isLongTerm; // Indicates if the node is for long-term parking
    public boolean isStay; // Indicates if the node is in a "stay" state
    public boolean isEntry; // Indicates if the node is an entry point
    public boolean isExit; // Indicates if the node is an exit point
    public Color color; // Node's color
    public boolean isLeave; // Indicates if the node is in a "leave" state
    public int overstayedTime; // Tracks overstayed time in seconds

    // Updated Constructor
    public Node(int id, int x, int y, boolean isTwoWheeler, boolean isEntry, boolean isExit) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isTwoWheeler = isTwoWheeler;
        this.isEntry = isEntry;
        this.isExit = isExit;
        this.color = Color.GRAY; // Default color for unallocated nodes
        reset(); // Initialize other attributes to default values
    }

    // Reset the node's state to its default values
    public void reset() {
        this.isAllocated = false;
        this.isLongTerm = false;
        this.isStay = false;
        this.isLeave = false;
        this.overstayedTime = 0;
        this.color = Color.GRAY; // Reset to default color
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", isTwoWheeler=" + isTwoWheeler +
                ", isAllocated=" + isAllocated +
                ", isLongTerm=" + isLongTerm +
                ", isStay=" + isStay +
                ", isEntry=" + isEntry +
                ", isExit=" + isExit +
                ", isLeave=" + isLeave +
                ", overstayedTime=" + overstayedTime +
                ", color=" + color +
                '}';
    }
    
    public void deallocate() {
        this.isAllocated = false; // Mark as not allocated
        this.isLongTerm = false; // Clear long-term state
        this.isStay = false; // Clear stay state
        this.color = Color.GRAY; // Reset color to default
    }

}
