package main;
import java.awt.*;
import java.util.*;
import java.util.List;


public class KruskalMazeGenerator {
    private int rows;
    private int cols;
    private Block[][] blocks;
    private List<Edge> edges = new ArrayList<>();
    private UnionFind unionFind;
    private GamePanel gamePanel;

    public KruskalMazeGenerator(int rows, int cols, GamePanel panel) {
        this.rows = rows;
        this.cols = cols;
        this.gamePanel = panel;
        this.blocks = new Block[rows][cols];
        this.unionFind = new UnionFind(rows * cols);
        initializeBlocksAndEdges();
    }

    // Initialize blocks with walls and edges with weights
    private void initializeBlocksAndEdges() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                blocks[i][j] = new Block(i, j, 20, gamePanel);
                // 增加上下左右墙的边缘
                if (i > 0) edges.add(new Edge(i, j, i - 1, j)); // top
                if (j > 0) edges.add(new Edge(i, j, i, j - 1)); // left
            }
        }
        // Shuffle edges to add randomness
        Collections.shuffle(edges);
    }

    public Block[][] generateMaze() {
        // Step 1: First, create a path from (0,0) to (ROWS-1, COLS-1)
        createUniquePath();

        // Step 2: Then run Kruskal's algorithm to remove walls and create a maze with higher complexity
        int wallsBroken = 0;
        int targetWalls = (int) (rows * cols * 2.0 / 3); // Increase wall density

        for (Edge edge : edges) {
            int cell1 = edge.cell1.getI() * cols + edge.cell1.getJ();
            int cell2 = edge.cell2.getI() * cols + edge.cell2.getJ();

            if (unionFind.find(cell1) != unionFind.find(cell2)) {
                // Connect cells by removing walls
                removeWalls(edge.cell1, edge.cell2);
                unionFind.union(cell1, cell2);
                wallsBroken++;
                if (wallsBroken >= targetWalls) break; // Stop when sufficient walls are broken
            }
        }
        return blocks;
    }

    private void createUniquePath() {
        // Use DFS or BFS to create a unique path from (0, 0) to (rows - 1, cols - 1)
        Stack<Block> stack = new Stack<>();
        Set<Block> visited = new HashSet<>();

        Block start = blocks[0][0];
        Block end = blocks[rows - 1][cols - 1];
        stack.push(start);

        while (!stack.isEmpty()) {
            Block current = stack.pop();
            if (visited.contains(current)) continue;
            visited.add(current);

            if (current == end) break;

            List<Block> neighbors = current.findNeighbors();
            Collections.shuffle(neighbors);

            for (Block neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                    removeWalls(current, neighbor); // Ensure these blocks are connected
                    break;
                }
            }
        }
    }

    private void removeWalls(Block cell1, Block cell2) {
        // 根据位置移除上下左右的墙
        int dx = cell1.getI() - cell2.getI();
        int dy = cell1.getJ() - cell2.getJ();

        if (dx == 1) { // cell1 is below cell2
            cell1.walls[0] = false; // remove top wall of cell1
            cell2.walls[2] = false; // remove bottom wall of cell2
        } else if (dx == -1) { // cell1 is above cell2
            cell1.walls[2] = false;
            cell2.walls[0] = false;
        } else if (dy == 1) { // cell1 is right of cell2
            cell1.walls[3] = false;
            cell2.walls[1] = false;
        } else if (dy == -1) { // cell1 is left of cell2
            cell1.walls[1] = false;
            cell2.walls[3] = false;
        }
    }

    // Edge class to represent edges between cells
    class Edge {
        Block cell1;
        Block cell2;

        public Edge(int i1, int j1, int i2, int j2) {
            this.cell1 = blocks[i1][j1];
            this.cell2 = blocks[i2][j2];
        }
    }
}


