package com.mycompany.dsa;

import java.util.ArrayList;
import java.util.List;

public class DSA {

    public static void main(String[] args) {

        // Initialize nodes and edges
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        int V = 103; // Total nodes in a 10x10 grid plus entrances and exits
        int gridSize = 45; // Grid size for compact layout
        int padding = 15; // Padding between nodes

        // Screen dimensions
        int screenWidth = 800; // Example fixed width
        int screenHeight = 800; // Example fixed height

        // Calculate total grid dimensions
        int totalWidth = gridSize * 10 + padding * 9;
        int totalHeight = gridSize * 10 + padding * 9;

        // Center the grid on screen
        int startX = (screenWidth - totalWidth) / 2;
        int startY = (screenHeight - totalHeight) / 2;

        // Create nodes and edges for the grid
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int x = startX + j * (gridSize + padding);
                int y = startY + i * (gridSize + padding);
                boolean isTwoWheeler = (i >= 5); // Assume rows 5-9 are two-wheeler spots
                nodes.add(new Node(i * 10 + j, x, y, false, false, isTwoWheeler));

                // Add horizontal edges
                if (j < 9) {
                    edges.add(new Edge(i * 10 + j, i * 10 + j + 1, 1));
                    edges.add(new Edge(i * 10 + j + 1, i * 10 + j, 1));
                }

                // Add vertical edges
                if (i < 9) {
                    edges.add(new Edge(i * 10 + j, (i + 1) * 10 + j, 1));
                    edges.add(new Edge((i + 1) * 10 + j, i * 10 + j, 1));
                }
            }
        }

        // Add entrances and exits
     // Add entrances and exits
        nodes.add(new Node(100, startX - (gridSize + padding), startY + 2 * (gridSize + padding), true, false, false)); // Entrance 1
        nodes.add(new Node(101, startX + 10 * (gridSize + padding), startY + 7 * (gridSize + padding), true, false, false)); // Entrance 2
        nodes.add(new Node(102, startX + 5 * (gridSize + padding), startY - (gridSize + padding), false, true, false)); // Exit
        nodes.add(new Node(103, startX + 7 * (gridSize + padding), startY - (gridSize + padding), false, false, true)); // Exit 2

        // Connect entrances to the grid
        edges.add(new Edge(100, 20, 1)); // Entrance 1 connects to node 20
        edges.add(new Edge(101, 79, 1)); // Entrance 2 connects to node 79

        // Connect exits to the grid
        edges.add(new Edge(102, 4, 1)); // Exit connects to node 4
        edges.add(new Edge(103, 6, 1)); // Exit 2 connects to node 6

        // Pass initialized nodes and edges to VehicleSelection
        VehicleSelection vehicleSelection = new VehicleSelection(nodes, edges);
        vehicleSelection.setVisible(true);
    }
}
