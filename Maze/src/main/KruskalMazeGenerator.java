package main;

import java.util.*;

public class KruskalMazeGenerator {
    private int rows;
    private int cols;
    private Block[][] blocks;
    private PriorityQueue<Edge> edgeQueue;
    private UnionFind unionFind;
    private GamePanel gamePanel;

    public KruskalMazeGenerator(int rows, int cols, GamePanel panel) {
        this.rows = rows;
        this.cols = cols;
        this.gamePanel = panel;
        this.blocks = new Block[rows][cols];
        this.unionFind = new UnionFind(rows * cols);
        this.edgeQueue = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight)); // 优先队列按权重升序
        initializeBlocksAndEdges();
    }

    // 使用墙和带权重的边初始化块
    private void initializeBlocksAndEdges() {
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                blocks[i][j] = new Block(i, j, 20, gamePanel);
                // 增加上下左右墙的边缘
                if (i > 0) edgeQueue.add(new Edge(i, j, i - 1, j, random.nextInt(100))); // top
                if (j > 0) edgeQueue.add(new Edge(i, j, i, j - 1, random.nextInt(100))); // left
            }
        }
    }

    public Block[][] generateMaze() {
        // 步骤1：创建从 (0,0) 到 (rows-1, cols-1) 的唯一路径
        createUniquePath();

        // 步骤2：运行基于堆的 Kruskal 算法移除墙壁
        int wallsBroken = 0;
        int targetWalls = (int) (rows * cols * 2.0 / 2);

        while (!edgeQueue.isEmpty() && wallsBroken < targetWalls) {
            Edge edge = edgeQueue.poll();
            int cell1 = edge.cell1.getI() * cols + edge.cell1.getJ();
            int cell2 = edge.cell2.getI() * cols + edge.cell2.getJ();

            if (unionFind.find(cell1) != unionFind.find(cell2)) {
                // 通过移除墙壁来连接单元
                removeWalls(edge.cell1, edge.cell2);
                unionFind.union(cell1, cell2);
                wallsBroken++;
            }
        }
        return blocks;
    }

    private void createUniquePath() {
        // 使用 DFS 创建从 (0,0) 到 (rows-1, cols-1) 的唯一路径
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
                    removeWalls(current, neighbor); // 确保连接
                    break;
                }
            }
        }
    }

    private void removeWalls(Block cell1, Block cell2) {
        // 根据位置移除上下左右的墙
        int dx = cell1.getI() - cell2.getI();
        int dy = cell1.getJ() - cell2.getJ();

        if (dx == 1) {
            cell1.walls[0] = false;
            cell2.walls[2] = false;
        } else if (dx == -1) {
            cell1.walls[2] = false;
            cell2.walls[0] = false;
        } else if (dy == 1) {
            cell1.walls[3] = false;
            cell2.walls[1] = false;
        } else if (dy == -1) {
            cell1.walls[1] = false;
            cell2.walls[3] = false;
        }
    }

    // 单元格之间的边
    class Edge {
        Block cell1;
        Block cell2;
        int weight;

        public Edge(int i1, int j1, int i2, int j2, int weight) {
            this.cell1 = blocks[i1][j1];
            this.cell2 = blocks[i2][j2];
            this.weight = weight;
        }
    }
}
