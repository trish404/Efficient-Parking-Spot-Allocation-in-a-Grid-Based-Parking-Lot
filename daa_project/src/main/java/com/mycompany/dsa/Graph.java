package com.mycompany.dsa;

import java.util.*;

class Graph {
    private final int V; // Number of nodes
    private final Map<Integer, List<Edge>> adj; // Adjacency list
    private List<Node> nodes;

    // Constructors
    Graph(int V) {
        this.V = V;
        this.nodes = null;
        adj = new HashMap<>();
        for (int i = 0; i < V; ++i) {
            adj.put(i, new LinkedList<>());
        }
    }

    Graph(int V, List<Node> nodes) {
        this.V = V;
        this.nodes = nodes;
        adj = new HashMap<>();
        for (int i = 0; i < V; ++i) {
            adj.put(i, new LinkedList<>());
        }
    }

    // Add edge
    void addEdge(int u, int v, int weight) {
        adj.computeIfAbsent(u, k -> new LinkedList<>());
        adj.computeIfAbsent(v, k -> new LinkedList<>());

        adj.get(u).add(new Edge(u, v, weight));
        adj.get(v).add(new Edge(v, u, weight)); // Undirected graph
    }

    // Dijkstra's Algorithm
    List<Integer> dijkstraShortestPath(int src, int dest) {
        int[] dist = new int[V];
        int[] prev = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(node -> dist[node]));
        pq.add(src);

        while (!pq.isEmpty()) {
            int u = pq.poll();

            for (Edge e : adj.get(u)) {
                int v = e.v;
                int weight = e.weight;

                if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                    pq.add(v);
                }
            }
        }

        return reconstructPath(src, dest, prev);
    }

    // Bellman-Ford Algorithm
    List<Integer> bellmanFordShortestPath(int src, int dest) {
        int[] dist = new int[V];
        int[] prev = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        for (int i = 0; i < V - 1; i++) {
            for (int u = 0; u < V; u++) {
                for (Edge e : adj.get(u)) {
                    int v = e.v;
                    int weight = e.weight;

                    if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                        dist[v] = dist[u] + weight;
                        prev[v] = u;
                    }
                }
            }
        }

        return reconstructPath(src, dest, prev);
    }
    
 // Content-Based Search (CBS)
    List<Integer> contentBasedSearch(int src, int dest) {
        int[] dist = new int[V];
        int[] prev = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(node -> dist[node]));
        pq.add(src);

        while (!pq.isEmpty()) {
            int u = pq.poll();

            for (Edge e : adj.get(u)) {
                int v = e.v;
                int adjustedWeight = e.weight + getContextWeight(v); // Dynamic edge weight adjustment

                if (dist[u] != Integer.MAX_VALUE && dist[u] + adjustedWeight < dist[v]) {
                    dist[v] = dist[u] + adjustedWeight;
                    prev[v] = u;
                    pq.add(v);
                }
            }
        }

        return reconstructPath(src, dest, prev);
    }

    // Jump Point Search (JPS)
    List<Integer> jumpPointSearch(int src, int dest) {
        int[] dist = new int[V];
        int[] prev = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        PriorityQueue<Integer> openSet = new PriorityQueue<>(Comparator.comparingInt(node -> dist[node] + heuristic(node, dest)));
        openSet.add(src);

        while (!openSet.isEmpty()) {
            int current = openSet.poll();

            if (current == dest) {
                return reconstructPath(src, dest, prev);
            }

            for (Edge edge : adj.get(current)) {
                int neighbor = edge.v;

                if (isJumpPoint(current, neighbor)) {
                    int jumpNode = jumpToNext(current, neighbor);
                    if (jumpNode != -1) {
                        neighbor = jumpNode;
                    } else {
                        continue;
                    }
                }

                if (dist[current] + edge.weight < dist[neighbor]) {
                    dist[neighbor] = dist[current] + edge.weight;
                    prev[neighbor] = current;
                    openSet.add(neighbor);
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    // Combined Search Algorithm (JPS + CBS)
    List<Integer> combinedSearch(int src, int dest) {
        int[] dist = new int[V];
        int[] prev = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        PriorityQueue<Integer> openSet = new PriorityQueue<>(Comparator.comparingInt(node -> dist[node] + heuristic(node, dest)));
        openSet.add(src);

        while (!openSet.isEmpty()) {
            int current = openSet.poll();

            if (current == dest) {
                return reconstructPath(src, dest, prev);
            }

            for (Edge edge : adj.get(current)) {
                int neighbor = edge.v;

                if (isJumpPoint(current, neighbor)) {
                    int jumpNode = jumpToNext(current, neighbor);
                    if (jumpNode != -1) {
                        neighbor = jumpNode;
                    } else {
                        continue;
                    }
                }

                int adjustedWeight = edge.weight + getContextWeight(neighbor);

                if (dist[current] + adjustedWeight < dist[neighbor]) {
                    dist[neighbor] = dist[current] + adjustedWeight;
                    prev[neighbor] = current;
                    openSet.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

    // Find Closest Exit
    int[] findClosestExit(int src, int[] exits) {
        int closestExit = -1;
        int shortestDistance = Integer.MAX_VALUE;

        for (int exit : exits) {
            List<Integer> path = combinedSearch(src, exit);
            if (!path.isEmpty()) {
                int distance = path.size() - 1; // Path length is (nodes - 1)
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closestExit = exit;
                }
            }
        }

        return new int[]{closestExit, shortestDistance};
    }

    // Heuristic for JPS
    private int heuristic(int node, int dest) {
        Node n = nodes.get(node);
        Node d = nodes.get(dest);
        return Math.abs(n.x - d.x) + Math.abs(n.y - d.y); // Manhattan distance
    }

    private boolean isJumpPoint(int current, int neighbor) {
        Node currNode = nodes.get(current);
        Node neighNode = nodes.get(neighbor);
        return Math.abs(currNode.x - neighNode.x) > 1 || Math.abs(currNode.y - neighNode.y) > 1;
    }

    private int jumpToNext(int current, int neighbor) {
        Node currNode = nodes.get(current);
        Node neighNode = nodes.get(neighbor);

        int stepX = Integer.compare(neighNode.x, currNode.x);
        int stepY = Integer.compare(neighNode.y, currNode.y);

        int nextX = currNode.x + stepX;
        int nextY = currNode.y + stepY;

        for (Node node : nodes) {
            if (node.x == nextX && node.y == nextY && !node.isAllocated) {
                return node.id;
            }
        }

        return -1; // No valid jump point
    }

    private int getContextWeight(int node) {
        return nodes.get(node).isAllocated ? 10 : 0;
    }

    private List<Integer> reconstructPath(int src, int dest, int[] prev) {
        List<Integer> path = new ArrayList<>();
        for (int at = dest; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}
