package com.mycompany.dsa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.JPanel;

public class AllocationPanel extends JPanel {
    private List<Node> nodes;
    private List<Edge> edges;
    private List<Integer> shortestPath;
    private int allocatedNode;
    private Color allocatedColor;

    public AllocationPanel(List<Node> nodes, List<Edge> edges, List<Integer> shortestPath, int allocatedNode, Color allocatedColor) {
        this.nodes = nodes;
        this.edges = edges;
        this.shortestPath = shortestPath;
        this.allocatedNode = allocatedNode;
        this.allocatedColor = allocatedColor;
    }

    public void updateAllocation(List<Node> nodes, List<Edge> edges, List<Integer> shortestPath, int allocatedNode, Color allocatedColor) {
        this.nodes = nodes;
        this.edges = edges;
        this.shortestPath = shortestPath;
        this.allocatedNode = allocatedNode;
        this.allocatedColor = allocatedColor;
        repaint(); // Repaint the panel to reflect the changes
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw edges
        for (Edge edge : edges) {
            if (shortestPath != null && shortestPath.contains(edge.u) && shortestPath.contains(edge.v)
                    && (shortestPath.indexOf(edge.u) == shortestPath.indexOf(edge.v) - 1
                    || shortestPath.indexOf(edge.v) == shortestPath.indexOf(edge.u) - 1)) {
                g2d.setColor(Color.RED); // Highlight shortest path edges
            } else {
                g2d.setColor(Color.BLACK); // Default edge color
            }
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(nodes.get(edge.u).x, nodes.get(edge.u).y, nodes.get(edge.v).x, nodes.get(edge.v).y);
        }

        // Draw nodes
        for (Node node : nodes) {
            int size = (node.id >= 50 && node.id <= 99) ? 35 : 50; // Two-wheeler spots are smaller than four-wheeler spots
            Color color = Color.LIGHT_GRAY; // Default color for parking spots

            // Set colors and sizes for specific node types
            if (node.id == 100 || node.id == 101) { // Entrances
                size = 40;
                color = Color.BLUE;
            } else if (node.id == 102 || node.id == 103) { // Exits
                size = 40;
                color = Color.RED;
            }

            // Determine color based on allocation state and type
            if (node.isStay) {
                color = Color.YELLOW; // Stay state
            } else if (node.isLeave) {
                color = Color.GRAY; // Leave state
            } else if (node.isLongTerm) {
                color = Color.PINK; // Long-term parking
            } else if (node.isAllocated && node.id == allocatedNode) {
                color = allocatedColor; // Highlight the specifically allocated node
            } else if (node.isAllocated) {
                color = Color.GREEN; // Allocated short-term spots
            }

            // Draw the parking spot
            g2d.setColor(color);
            g2d.fillRect(node.x - size / 2, node.y - size / 2, size, size);

            // Add borders for nodes
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.BLACK);
            g2d.drawRect(node.x - size / 2, node.y - size / 2, size, size);

            // Add labels for nodes
            g2d.setColor(Color.BLACK);
            g2d.drawString(Integer.toString(node.id), node.x - 10, node.y + 5);

            // Add labels for entrances and exits
            if (node.id == 100) {
                g2d.drawString("Entry 1", node.x - 35, node.y + size / 2 + 10);
            } else if (node.id == 101) {
                g2d.drawString("Entry 2", node.x + size / 2 - 10, node.y + size / 2 + 10);
            } else if (node.id == 102) {
                g2d.drawString("Exit 1", node.x - 35, node.y - size / 2 - 5);
            } else if (node.id == 103) {
                g2d.drawString("Exit 2", node.x + size / 2 - 10, node.y - size / 2 - 5);
            }
        }
    }
}
