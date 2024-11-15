package main;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/*
 * 迷宫单元类——迷宫的结构：路径和墙
 */
public class Block {
	private GamePanel panel = null;
	private int i=0;  //二维数组的下标i
	private int j=0;  //二维数组的下标j
	private int h=0;  //宽高
	private int start=6; //偏移像素
	private int x1=0;  //x1坐标
	private int y1=0;  //y1坐标
	private int x2=0;  //x2坐标
	private int y2=0;  //y2坐标
	private int x3=0;  //x3坐标
	private int y3=0;  //y3坐标
	private int x4=0;  //x4坐标
	private int y4=0;  //y4坐标
	boolean[] walls = new boolean[4]; // 0: top, 1: right, 2: bottom, 3: left
	private boolean visited = false;

	// 构造方法
	public Block(int i, int j, int h, GamePanel panel) {
		this.i = i;
		this.j = j;
		this.h = h;
		this.panel = panel;
		init();
	}

	// 计算四个顶点的坐标
	private void init() {
		this.x1 = start + j * h;
		this.y1 = start + i * h;
		this.x2 = start + (j + 1) * h;
		this.y2 = start + i * h;
		this.x3 = start + (j + 1) * h;
		this.y3 = start + (i + 1) * h;
		this.x4 = start + j * h;
		this.y4 = start + (i + 1) * h;

		// 默认四个墙都存在
		walls[0] = true; // 上
		walls[1] = true; // 右
		walls[2] = true; // 下
		walls[3] = true; // 左
	}

	// 绘制迷宫块
	public void draw(Graphics g) {
		drawBlock(g);
	}

	private void drawBlock(Graphics g) {
		g.setColor(Color.ORANGE);

		if (walls[0]) g.drawLine(x1, y1, x2, y2); // 上
		if (walls[1]) g.drawLine(x2, y2, x3, y3); // 右
		if (walls[2]) g.drawLine(x3, y3, x4, y4); // 下
		if (walls[3]) g.drawLine(x4, y4, x1, y1); // 左
	}

	// 移除墙壁
	public void setRightWall(boolean isVisible) {
		walls[1] = isVisible;
	}

	public void setBottomWall(boolean isVisible) {
		walls[2] = isVisible;
	}

	public void setLeftWall(boolean isVisible) {
		walls[3] = isVisible;
	}

	public void setTopWall(boolean isVisible) {
		walls[0] = isVisible;
	}

	// 获取当前块的索引
	public int getIndex() {
		return i * panel.COLS + j; // 将(i, j)转化为唯一的索引
	}

	// 查找邻居块
	public List<Block> findNeighbors() {
		List<Block> res = new ArrayList<Block>();

		Block top = getNeighbor(0, false);
		Block right = getNeighbor(1, false);
		Block bottom = getNeighbor(2, false);
		Block left = getNeighbor(3, false);

		if (top != null) res.add(top);
		if (right != null) res.add(right);
		if (bottom != null) res.add(bottom);
		if (left != null) res.add(left);

		return res;
	}

	public Block getNeighbor(int type, boolean lose_visited) {
		Block neighbor;
		int ti = 0, tj = 0;
		if (type == 0) { ti = i - 1; tj = j; } // 上
		else if (type == 1) { ti = i; tj = j + 1; } // 右
		else if (type == 2) { ti = i + 1; tj = j; } // 下
		else if (type == 3) { ti = i; tj = j - 1; } // 左

		Block[][] blocks = panel.blocks;

		if (ti < 0 || tj < 0 || ti >= panel.ROWS || tj >= panel.COLS) {
			neighbor = null;
		} else {
			neighbor = blocks[ti][tj];
			if (neighbor.visited && !lose_visited) {
				neighbor = null;
			}
		}
		return neighbor;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int getJ() {
		return j;
	}

	public void setJ(int j) {
		this.j = j;
	}
}