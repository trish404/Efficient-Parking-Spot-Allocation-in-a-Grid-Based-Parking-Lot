package com.mycompany.dsa;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class AllocationDisplay extends JFrame {

    private final Map<Integer, Timer> timers = new HashMap<>();
    private final Map<Integer, Timer> overstayTimers = new HashMap<>();
    private int timeLimit;
    private JButton submitButton, exitButton;
    private ButtonGroup buttonGroup;
    private JRadioButton stayButton;
    private JRadioButton leaveButton;
    private AllocationPanel allocationPanel;
    private Color allocatedColor;
    private boolean isLongTermParking;
    private int allocatedNode;
    private List<Node> nodes;
    private List<Edge> edges;
    private static AllocationDisplay instance = null;

    private AllocationDisplay(List<Node> nodes, List<Edge> edges, List<Integer> shortestPath, int allocatedNode, Color allocatedColor, boolean isLongTermParking) {
        this.nodes = nodes;
        this.edges = edges;
        this.allocatedColor = allocatedColor;
        this.isLongTermParking = isLongTermParking;
        this.allocatedNode = allocatedNode;

        setTitle("Allocation Display");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel leftMarginPanel = new JPanel();
        leftMarginPanel.setPreferredSize(new Dimension(425, getHeight()));

        JPanel topMarginPanel = new JPanel();
        topMarginPanel.setPreferredSize(new Dimension(getWidth(), 50));

        allocationPanel = new AllocationPanel(nodes, edges, shortestPath, allocatedNode, allocatedColor);
        add(allocationPanel);

        add(leftMarginPanel, BorderLayout.WEST);
        add(topMarginPanel, BorderLayout.NORTH);
        add(allocationPanel, BorderLayout.CENTER);

        createExitButton();
        updateTimeLimit();
        createTimer(allocatedNode);
    }

    public static AllocationDisplay getInstance(List<Node> nodes, List<Edge> edges, List<Integer> shortestPath, int allocatedNode, Color allocatedColor, boolean isLongTermParking) {
        if (instance == null) {
            instance = new AllocationDisplay(nodes, edges, shortestPath, allocatedNode, allocatedColor, isLongTermParking);
        } else {
            instance.updateParameters(nodes, edges, shortestPath, allocatedNode, allocatedColor, isLongTermParking);
        }
        return instance;
    }

    private void updateParameters(List<Node> nodes, List<Edge> edges, List<Integer> shortestPath, int allocatedNode, Color allocatedColor, boolean isLongTermParking) {
        this.nodes = nodes;
        this.edges = edges;
        this.allocatedColor = allocatedColor;
        this.isLongTermParking = isLongTermParking;
        this.allocatedNode = allocatedNode;

        allocationPanel.updateAllocation(nodes, edges, shortestPath, allocatedNode, allocatedColor);

        updateTimeLimit();
        createTimer(allocatedNode);
    }

    private void updateTimeLimit() {
        timeLimit = isLongTermParking ? 20 : 10;
    }

    private void createTimer(int allocatedNode) {
        if (timers.containsKey(allocatedNode)) {
            timers.get(allocatedNode).stop();
        }

        Timer timer = new Timer(1000, e -> {
            timeLimit--;
            if (timeLimit == 0) {
                Timer currentTimer = timers.get(allocatedNode);
                if (currentTimer != null) {
                    currentTimer.stop();
                    showTimesUpDialog(allocatedNode);
                }
            }
        });

        timers.put(allocatedNode, timer);
        timer.start();
    }

    private void createExitButton() {
        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> showExitDialog());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showExitDialog() {
        JTextField deallocationField = new JTextField(5);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Enter parking spot number to deallocate:"));
        panel.add(deallocationField);

        int result = JOptionPane.showConfirmDialog(
                null, panel, "Exit Parking Spot", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int deallocatedNode = Integer.parseInt(deallocationField.getText());
                deallocateParkingSpace(deallocatedNode);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.");
            }
        }
    }

    private void deallocateParkingSpace(int deallocatedNode) {
        Node node = nodes.get(deallocatedNode);

        if (overstayTimers.containsKey(deallocatedNode)) {
            overstayTimers.get(deallocatedNode).stop();
            overstayTimers.remove(deallocatedNode);
        }

        if (timers.containsKey(deallocatedNode)) {
            timers.get(deallocatedNode).stop();
            timers.remove(deallocatedNode);
        }

        // Find the closest exit
        Graph graph = new Graph(nodes.size(), nodes);
        for (Edge edge : edges) {
            graph.addEdge(edge.u, edge.v, edge.weight);
        }

        int[] exits = {102, 103}; // Example exit IDs
        int closestExit = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int exit : exits) {
            List<Integer> path = graph.dijkstraShortestPath(deallocatedNode, exit);
            if (!path.isEmpty() && path.size() < minDistance) {
                minDistance = path.size();
                closestExit = exit;
            }
        }

        String exitMessage = closestExit != -1
                ? "Closest exit is " + closestExit + " with distance " + minDistance + "."
                : "No exit found.";

        JOptionPane.showMessageDialog(
                this,
                "Parking spot " + deallocatedNode + " deallocated successfully.\n" +
                        "Overstayed time: " + (node.overstayedTime > 0 ? node.overstayedTime + " seconds." : "None.") + "\n" +
                        exitMessage,
                "Deallocation Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        node.reset(); // Reset node properties
        SwingUtilities.invokeLater(() -> allocationPanel.repaint());
    }

    private void showTimesUpDialog(int allocatedNode) {
        JDialog dialog = new JDialog(this, "Time's Up", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel("Time's up for parking spot " + allocatedNode), BorderLayout.CENTER);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JPanel dialogPanel = new JPanel();
        stayButton = new JRadioButton("Stay");
        leaveButton = new JRadioButton("Leave");
        submitButton = new JButton("Submit");

        buttonGroup = new ButtonGroup();
        buttonGroup.add(stayButton);
        buttonGroup.add(leaveButton);

        dialogPanel.add(stayButton);
        dialogPanel.add(leaveButton);
        dialogPanel.add(submitButton);

        stayButton.addActionListener(e -> handleStay(allocatedNode));
        leaveButton.addActionListener(e -> handleLeave(allocatedNode));

        submitButton.addActionListener(e -> dialog.dispose());

        dialog.add(dialogPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleStay(int allocatedNode) {
        Node node = nodes.get(allocatedNode);
        node.isStay = true;
        node.isAllocated = true;
        node.color = Color.YELLOW;

        if (overstayTimers.containsKey(allocatedNode)) {
            overstayTimers.get(allocatedNode).stop();
        }

        Timer overstayTimer = new Timer(1000, evt -> node.overstayedTime++);
        overstayTimers.put(allocatedNode, overstayTimer);
        overstayTimer.start();

        SwingUtilities.invokeLater(() -> allocationPanel.repaint());
    }

    private void handleLeave(int allocatedNode) {
        deallocateParkingSpace(allocatedNode);
    }
}
