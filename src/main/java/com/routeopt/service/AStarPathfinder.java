package com.routeopt.service;

import com.routeopt.model.TrafficZone;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AStarPathfinder {
    private static final int GRID_SIZE = 50; // 50x50 grid for routing resolution

    public static class Node implements Comparable<Node> {
        public int x, y;
        public double gCost; // Cost from start
        public double hCost; // Heuristic cost to end
        public Node parent;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public double getFCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.getFCost(), other.getFCost());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Calculates the shortest path between start (x1, y1) and end (x2, y2)
     * factoring in dynamic traffic zones and roadblocks.
     * Coordinate system is assumed to be 0 to 100.
     */
    public List<double[]> findPath(double x1, double y1, double x2, double y2, List<TrafficZone> trafficZones) {
        // Map 0-100 coordinates to grid indices
        int startX = clamp((int) Math.round(x1 * (GRID_SIZE - 1) / 100.0), 0, GRID_SIZE - 1);
        int startY = clamp((int) Math.round(y1 * (GRID_SIZE - 1) / 100.0), 0, GRID_SIZE - 1);
        int endX = clamp((int) Math.round(x2 * (GRID_SIZE - 1) / 100.0), 0, GRID_SIZE - 1);
        int endY = clamp((int) Math.round(y2 * (GRID_SIZE - 1) / 100.0), 0, GRID_SIZE - 1);

        // Grid weights
        double[][] gridWeights = new double[GRID_SIZE][GRID_SIZE];
        for (double[] row : gridWeights) {
            Arrays.fill(row, 1.0); // Default cell weight
        }

        // Apply traffic zones
        for (TrafficZone zone : trafficZones) {
            int zoneGridX = clamp((int) Math.round(zone.getX() * (GRID_SIZE - 1) / 100.0), 0, GRID_SIZE - 1);
            int zoneGridY = clamp((int) Math.round(zone.getY() * (GRID_SIZE - 1) / 100.0), 0, GRID_SIZE - 1);
            int gridRadius = clamp((int) Math.round(zone.getRadius() * (GRID_SIZE - 1) / 100.0), 1, GRID_SIZE);

            // Populate circular cost zone
            for (int dx = -gridRadius; dx <= gridRadius; dx++) {
                for (int dy = -gridRadius; dy <= gridRadius; dy++) {
                    int nx = zoneGridX + dx;
                    int ny = zoneGridY + dy;
                    if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        if (distance <= gridRadius) {
                            // Scale down influence toward edges, keep central severity
                            double influence = (gridRadius - distance) / gridRadius;
                            double currentSeverity = 1.0 + (zone.getSeverity() - 1.0) * influence;
                            gridWeights[nx][ny] = Math.max(gridWeights[nx][ny], currentSeverity);
                        }
                    }
                }
            }
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();

        Node startNode = new Node(startX, startY);
        Node endNode = new Node(endX, endY);

        startNode.gCost = 0;
        startNode.hCost = getHeuristic(startNode, endNode);
        openSet.add(startNode);

        Map<String, Node> allNodes = new HashMap<>();
        allNodes.put(startX + "," + startY, startNode);

        Node destinationNode = null;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            closedSet.add(current);

            if (current.x == endNode.x && current.y == endNode.y) {
                destinationNode = current;
                break;
            }

            // Check neighbors (8-way movement)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;

                    int nx = current.x + dx;
                    int ny = current.y + dy;

                    if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                        double stepCost = gridWeights[nx][ny];

                        // If it's a roadblock, skip node entirely
                        if (stepCost >= 900.0) {
                            continue;
                        }

                        // Diagonal movement cost is slightly higher
                        double moveDistance = (dx == 0 || dy == 0) ? 1.0 : Math.sqrt(2);
                        double newCost = current.gCost + (moveDistance * stepCost);

                        String key = nx + "," + ny;
                        Node neighbor = allNodes.get(key);
                        if (neighbor == null) {
                            neighbor = new Node(nx, ny);
                            allNodes.put(key, neighbor);
                        }

                        if (closedSet.contains(neighbor)) continue;

                        if (newCost < neighbor.gCost || !openSet.contains(neighbor)) {
                            neighbor.gCost = newCost;
                            neighbor.hCost = getHeuristic(neighbor, endNode);
                            neighbor.parent = current;

                            if (!openSet.contains(neighbor)) {
                                openSet.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        List<double[]> pathPoints = new ArrayList<>();
        if (destinationNode != null) {
            // Reconstruct path
            Node temp = destinationNode;
            while (temp != null) {
                // Convert back from grid coordinates to 0-100 system
                double wx = temp.x * 100.0 / (GRID_SIZE - 1);
                double wy = temp.y * 100.0 / (GRID_SIZE - 1);
                pathPoints.add(new double[]{wx, wy});
                temp = temp.parent;
            }
            Collections.reverse(pathPoints);
        } else {
            // Fallback: simple line if no grid path is found due to blockage
            pathPoints.add(new double[]{x1, y1});
            pathPoints.add(new double[]{x2, y2});
        }

        return pathPoints;
    }

    /**
     * Calculates distance between A and B on the coordinate system for travel statistics
     */
    public double getPathCost(List<double[]> path, List<TrafficZone> trafficZones) {
        double cost = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = path.get(i);
            double[] p2 = path.get(i + 1);
            double baseDist = Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
            
            // Adjust distance weight by any traffic zone covers
            double trafficMultiplier = getTrafficMultiplier((p1[0] + p2[0]) / 2.0, (p1[1] + p2[1]) / 2.0, trafficZones);
            cost += baseDist * trafficMultiplier;
        }
        return cost;
    }

    private double getTrafficMultiplier(double x, double y, List<TrafficZone> trafficZones) {
        double multiplier = 1.0;
        for (TrafficZone zone : trafficZones) {
            double dist = Math.sqrt(Math.pow(x - zone.getX(), 2) + Math.pow(y - zone.getY(), 2));
            if (dist <= zone.getRadius()) {
                double influence = (zone.getRadius() - dist) / zone.getRadius();
                double severity = 1.0 + (zone.getSeverity() - 1.0) * influence;
                multiplier = Math.max(multiplier, severity);
            }
        }
        return multiplier;
    }

    private double getHeuristic(Node a, Node b) {
        // Euclidean distance heuristic
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
