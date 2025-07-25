package com.mycompany.dsa;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VehicleSelection extends JFrame {
    JLabel label, termLabel, entranceLabel;
    JRadioButton fourWheeler, twoWheeler, longTerm, shortTerm, entrance1, entrance2;
    JButton submit, showTableButton;
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final DefaultTableModel tableModel;
    private int selectedEntrance;

    public VehicleSelection(List<Node> nodes, List<Edge> edges) {
        setLayout(new GridLayout(12, 1));

        // Vehicle type selection
        label = new JLabel("Select Vehicle Type:");
        fourWheeler = new JRadioButton("Four-Wheeler");
        twoWheeler = new JRadioButton("Two-Wheeler");
        ButtonGroup vehicleGroup = new ButtonGroup();
        vehicleGroup.add(fourWheeler);
        vehicleGroup.add(twoWheeler);

        // Parking term selection
        termLabel = new JLabel("Select Term of Parking:");
        longTerm = new JRadioButton("Long-Term Parking");
        shortTerm = new JRadioButton("Short-Term Parking");
        ButtonGroup parkingGroup = new ButtonGroup();
        parkingGroup.add(longTerm);
        parkingGroup.add(shortTerm);

        // Entrance selection
        entranceLabel = new JLabel("Select Entrance:");
        entrance1 = new JRadioButton("Entrance 1");
        entrance2 = new JRadioButton("Entrance 2");
        ButtonGroup entranceGroup = new ButtonGroup();
        entranceGroup.add(entrance1);
        entranceGroup.add(entrance2);

        // Submit and table buttons
        submit = new JButton("Submit");
        showTableButton = new JButton("Show Table");

        submit.addActionListener(e -> handleSubmission());
        showTableButton.addActionListener(e -> displayTable());

        // Add components to the panel
        add(label);
        add(fourWheeler);
        add(twoWheeler);
        add(termLabel);
        add(longTerm);
        add(shortTerm);
        add(entranceLabel);
        add(entrance1);
        add(entrance2);
        add(submit);
        add(showTableButton);

        this.nodes = nodes;
        this.edges = edges;

        // Initialize the timing table
        tableModel = new DefaultTableModel(
                new String[]{"Node Allocated", "Dijkstra Time (ns)", "Bellman-Ford Time (ns)", "CBS Time (ns)", "JPS Time (ns)", "Combined Time (ns)"}, 0
        );

        setSize(300, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("Vehicle Selection");
        setVisible(true);
    }

    private void handleSubmission() {
        // Validate inputs
        if (!fourWheeler.isSelected() && !twoWheeler.isSelected()) {
            JOptionPane.showMessageDialog(null, "Please select a vehicle type.");
            return;
        }
        if (!longTerm.isSelected() && !shortTerm.isSelected()) {
            JOptionPane.showMessageDialog(null, "Please select the term of parking.");
            return;
        }
        if (!entrance1.isSelected() && !entrance2.isSelected()) {
            JOptionPane.showMessageDialog(null, "Please select an entrance.");
            return;
        }

        // Determine the selected entrance
        selectedEntrance = entrance1.isSelected() ? 100 : 101;

        // Allocate nearest parking spot
        int start = fourWheeler.isSelected() ? 0 : 50; // Four-wheelers: 0-49, Two-wheelers: 50-99
        int end = fourWheeler.isSelected() ? 49 : 99;

        int nearestSpot = allocateNearestNode(nodes, start, end, selectedEntrance);
        if (nearestSpot == -1) {
            JOptionPane.showMessageDialog(null, "No available parking spots.");
            return;
        }

        Node spotNode = nodes.get(nearestSpot);

        // Handle already allocated spot
        if (spotNode.isAllocated) {
            int response = JOptionPane.showConfirmDialog(
                null,
                "Spot " + nearestSpot + " is already allocated. Would you like to deallocate it?",
                "Spot Already Allocated",
                JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                // Deallocate the spot and show overstayed time
                deallocateNode(spotNode, nearestSpot);
                return;
            } else {
                // Cancel operation
                JOptionPane.showMessageDialog(null, "Spot allocation remains unchanged.");
                return;
            }
        }

        // Allocate the spot
        spotNode.isAllocated = true;
        spotNode.isLongTerm = longTerm.isSelected();
        String term = longTerm.isSelected() ? "Long-Term" : "Short-Term";
        Color allocatedColor = longTerm.isSelected() ? Color.PINK : Color.GREEN;

        // Run algorithms and measure times
        Graph graph = new Graph(nodes.size(), nodes);
        for (Edge edge : edges) {
            graph.addEdge(edge.u, edge.v, edge.weight);
        }

        long dijkstraTime = measureTime(() -> graph.dijkstraShortestPath(selectedEntrance, nearestSpot));
        long bellmanFordTime = measureTime(() -> graph.bellmanFordShortestPath(selectedEntrance, nearestSpot));
        long cbsTime = measureTime(() -> graph.combinedSearch(selectedEntrance, nearestSpot));
        long jpsTime = measureTime(() -> graph.jumpPointSearch(selectedEntrance, nearestSpot));
        long combinedTime = measureTime(() -> graph.combinedSearch(selectedEntrance, nearestSpot));

        // Add timing results to the table
        tableModel.addRow(new Object[]{nearestSpot, dijkstraTime, bellmanFordTime, cbsTime, jpsTime, combinedTime});

        // Notify user
        JOptionPane.showMessageDialog(null, "Spot " + nearestSpot + " allocated near Entrance " + selectedEntrance + " (" + term + ").");

        // Display parking lot visualization
        List<Integer> shortestPath = graph.dijkstraShortestPath(selectedEntrance, nearestSpot); // Example using Dijkstra
        AllocationDisplay allocationDisplay = AllocationDisplay.getInstance(
                nodes, edges, shortestPath, nearestSpot, allocatedColor, longTerm.isSelected()
        );
        allocationDisplay.setVisible(true);
    }

    private void deallocateNode(Node spotNode, int nearestSpot) {
        int overstayedTime = spotNode.overstayedTime; // Retrieve overstayed time before reset
        spotNode.reset(); // Reset the node's properties

        // Determine closest exit
        Graph graph = new Graph(nodes.size(), nodes);
        for (Edge edge : edges) {
            graph.addEdge(edge.u, edge.v, edge.weight);
        }
        List<Integer> exitNodes = Arrays.asList(102, 103); // IDs for exits
        int closestExit = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int exit : exitNodes) {
            int distance = graph.combinedSearch(nearestSpot, exit).size();
            if (distance > 0 && distance < minDistance) {
                minDistance = distance;
                closestExit = exit;
            }
        }

        String exitMessage = (closestExit != -1) ? "Closest exit is Exit " + (closestExit == 102 ? "1" : "2") : "No valid exit found.";

        // Repaint the allocation display
        AllocationDisplay.getInstance(nodes, edges, null, -1, Color.GRAY, false).repaint();

        // Show a message dialog with overstayed time and closest exit
        JOptionPane.showMessageDialog(
                null,
                "Parking spot " + nearestSpot + " deallocated successfully.\n" +
                "Overstayed time: " + (overstayedTime > 0 ? overstayedTime + " seconds" : "None.") + "\n" +
                exitMessage,
                "Deallocation Successful",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void displayTable() {
        JFrame tableFrame = new JFrame("Algorithm Timing Results");
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        tableFrame.setLayout(new BorderLayout());
        tableFrame.add(scrollPane, BorderLayout.CENTER);
        tableFrame.setSize(600, 300);
        tableFrame.setLocationRelativeTo(null);
        tableFrame.setVisible(true);
    }

    public static int allocateNearestNode(List<Node> nodes, int start, int end, int entranceId) {
        int minDistance = Integer.MAX_VALUE;
        int nearestNode = -1;

        for (int i = start; i <= end; i++) {
            if (!nodes.get(i).isAllocated) {
                int distance = Math.abs(nodes.get(i).x - nodes.get(entranceId).x) +
                        Math.abs(nodes.get(i).y - nodes.get(entranceId).y);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestNode = i;
                }
            }
        }

        return nearestNode;
    }

    private long measureTime(Runnable task) {
        long startTime = System.nanoTime();
        task.run();
        return System.nanoTime() - startTime;
    }
}
