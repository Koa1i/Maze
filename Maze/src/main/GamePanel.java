package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;



// ly引入unionfind prim？ kruskal？

/*
 * 画布类
 */
public class GamePanel extends JPanel implements ActionListener{
	private JMenuBar jmb = null;
	private GameFrame mainFrame = null;
	private GamePanel panel = null;
	private List<Block> correctPath = new ArrayList<>(); // 用于存储正确路径
	private boolean showPath = false; // 控制路径显示的变量
	private ImageIcon playerIcon;
	private ImageIcon endIcon;

	public final int ROWS=20;//行
	public final int COLS=20;//列
	public final int H=20;//每一块的宽高
	Block[][] blocks = null;
	
	Rect start ;//开始方形
	Rect end ;//终点方形
	
	private String gameFlag="start";//游戏状态
	
	//构造方法
	public GamePanel(GameFrame mainFrame){
		this.setLayout(null);
		this.setOpaque(false);
		this.mainFrame=mainFrame;
		this.panel =this;

		// 加载图标
		playerIcon = new ImageIcon("imgs/playerIcon.jpg"); // 替换为奶龙图标的路径
		endIcon = new ImageIcon("imgs/endIcon.png"); // 替换为旗帜图标的路径
		// chaserIcon = new ImageIcon("imgs/chaserIcon.jpg);

		//创建菜单
		createMenu();
		//创建数组内容
		createBlocks();
		//计算处理线路
		computed();
		//创建开始结束的方形
		createRects();
		//ly:寻找正确路径
		findPath();
		//添加键盘事件监听
		createKeyListener();
	}
	//创建开始结束的方形
	private void createRects() {
		start = new Rect(0, 0, H, "start") ;
		end = new Rect(ROWS-1, COLS-1, H, "end") ;
	}
	//创建数组内容
	private void createBlocks() {
		blocks = new Block[ROWS][COLS];
		Block block ;
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				block = new Block(i, j,H,this);
				blocks[i][j]=block;
			}
		}
	}
	
	//线路的计算处理 DFS
	private void computed(){
		/*
		1.将起点作为当前迷宫单元并标记为已访问
		2.当还存在未标记的迷宫单元，进行循环
			1).如果当前迷宫单元有未被访问过的相邻的迷宫单元
				(1).随机选择一个未访问的相邻迷宫单元
				(2).将当前迷宫单元入栈
				(3).移除当前迷宫单元与相邻迷宫单元的墙
				(4).标记相邻迷宫单元并用它作为当前迷宫单元
			2).如果当前迷宫单元不存在未访问的相邻迷宫单元，并且栈不空
				(1).栈顶的迷宫单元出栈
				(2).令其成为当前迷宫单元
		 */
		Random random = new Random();
		Stack<Block> stack = new Stack<Block>();//栈
		Block current = blocks[0][0];//取第一个为当前单元
		current.setVisited(true);//标记为已访问
		
		int unVisitedCount=ROWS*COLS-1;//因为第一个已经设置为访问了，所以要减去1
		List<Block> neighbors ;//定义邻居
		Block next;
		while(unVisitedCount>0){
			neighbors = current.findNeighbors();//查找邻居集合(未被访问的)
			if(neighbors.size()>0){//如果当前迷宫单元有未被访问过的的相邻的迷宫单元
				//随机选择一个未访问的相邻迷宫单元
				int index = random.nextInt(neighbors.size()); 
				next = neighbors.get(index);
				//将当前迷宫单元入栈
				stack.push(current);
				//移除当前迷宫单元与相邻迷宫单元的墙
				this.removeWall(current,next);
				//标记相邻迷宫单元并用它作为当前迷宫单元
				next.setVisited(true);
				//标记一个为访问，则计数器递减1
				unVisitedCount--;//递减
				current = next;
			}else if(!stack.isEmpty()){//如果当前迷宫单元不存在未访问的相邻迷宫单元，并且栈不空
				/*
					1.栈顶的迷宫单元出栈
					2.令其成为当前迷宫单元
				*/
				Block cell = stack.pop();
				current = cell;
			}
		}
	}
	
	//移除当前迷宫单元与相邻迷宫单元的墙
	private void removeWall(Block current, Block next) {
		if(current.getI()==next.getI()){//横向邻居
			if(current.getJ()>next.getJ()){//匹配到的是左边邻居
				//左边邻居的话，要移除自己的左墙和邻居的右墙
				current.walls[3]=false;
				next.walls[1]=false;
			}else{//匹配到的是右边邻居
				//右边邻居的话，要移除自己的右墙和邻居的左墙
				current.walls[1]=false;
				next.walls[3]=false;
			}
		}else if(current.getJ()==next.getJ()){//纵向邻居
			if(current.getI()>next.getI()){//匹配到的是上边邻居
				//上边邻居的话，要移除自己的上墙和邻居的下墙
				current.walls[0]=false;
				next.walls[2]=false;
			}else{//匹配到的是下边邻居
				//下边邻居的话，要移除自己的下墙和邻居的上墙
				current.walls[2]=false;
				next.walls[0]=false;
			}
		}
	}

	// ly在游戏胜利或求助时显示路径
	private void showCorrectPath() {
		repaint(); // 重新绘制面板以显示路径
	}

	// 重置所有块的访问状态
	private void resetVisited() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				blocks[i][j].setVisited(false);
			}
		}
	}

	// 添加寻找正确路径的方法
	private void findPath() {
		resetVisited();	//重置访问状态
		correctPath.clear(); // 清空上一次路径
		Stack<Block> pathStack = new Stack<>();
		Block startBlock = blocks[0][0]; // 起点
		Block endBlock = blocks[ROWS - 1][COLS - 1]; // 终点

		// 递归DFS
		boolean pathFound = dfsPath(startBlock, endBlock, pathStack);
		if (pathFound) {
			correctPath.addAll(pathStack); // 将找到的路径存入列表
		}
	}

	// 使用DFS查找路径，并考虑墙壁
	private boolean dfsPath(Block current, Block end, Stack<Block> path) {
		if (current == end) {
			path.push(current); // 将终点加入路径
			return true; // 找到路径
		}

		current.setVisited(true);
		path.push(current);

		// 按顺序获取上下左右的邻居
		Block[] neighbors = {current.getNeighbor(0, true), current.getNeighbor(1, true),
				current.getNeighbor(2, true), current.getNeighbor(3, true)};

		for (int i = 0; i < neighbors.length; i++) {
			Block neighbor = neighbors[i];
			if (neighbor != null && !neighbor.isVisited() && !current.walls[i]) {
				if (dfsPath(neighbor, end, path)) {
					return true;
				}
			}
		}

		path.pop(); // 回溯
		return false;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// 绘制网格、起点和终点
		drawBlock(g);
		drawRect(g);

		// ly 绘制路径，只有当 showPath 为 true 时才绘制
		if (showPath) {
			drawPath(g);
		}
	}

	//绘制开始结束方块
	private void drawRect(Graphics g) {
		// 绘制终点图标，稍微向右下偏移
		if (endIcon != null) {
			g.drawImage(endIcon.getImage(), end.getJ() * H + 5, end.getI() * H + 5, H, H, this);
		}

		// 绘制玩家图标，稍微向右下偏移
		if (playerIcon != null) {
			g.drawImage(playerIcon.getImage(), start.getJ() * H + 5, start.getI() * H + 5, H, H, this);
		}
	}

	//绘制迷宫块
	private void drawBlock(Graphics g) {
		Block block ;
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				block = blocks[i][j];
				if(block!=null){
					block.draw(g);
				}
			}
		}
	}
	// ly绘制路径的方法
	private void drawPath(Graphics g) {
		g.setColor(Color.GREEN); // 设置路径线的颜色
		int offset = 5; // 右移的偏移量

		// 遍历路径中的每个块，连接相邻块的中心
		for (int i = 0; i < correctPath.size() - 1; i++) {
			Block currentBlock = correctPath.get(i);
			Block nextBlock = correctPath.get(i + 1);

			// 当前块和下一个块的中心点坐标
			int x1 = currentBlock.getJ() * H + H / 2 + offset;
			int y1 = currentBlock.getI() * H + H / 2 + offset;
			int x2 = nextBlock.getJ() * H + H / 2 + offset;
			int y2 = nextBlock.getI() * H + H / 2 + offset;

			// 绘制从当前块中心到下一个块中心的线
			g.drawLine(x1, y1, x2, y2);
		}
	}
	
	//添加键盘监听
	private void createKeyListener() {
		KeyAdapter l = new KeyAdapter() {
			//按下
			@Override
			public void keyPressed(KeyEvent e) {
				if(!"start".equals(gameFlag)) return ;
				int key = e.getKeyCode();
				switch (key) {
					//向上
					case KeyEvent.VK_UP:
					case KeyEvent.VK_W:
						if(start!=null) start.move(0,blocks,panel);
						break;
						
					//向右	
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_D:
						if(start!=null) start.move(1,blocks,panel);
						break;
						
					//向下
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_S:
						if(start!=null) start.move(2,blocks,panel);
						break;
						
					//向左
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_A:
						if(start!=null) start.move(3,blocks,panel);
						break;
				}
			
			}
			//松开
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
		};
		//给主frame添加键盘监听
		mainFrame.addKeyListener(l);
	}

	private Font createFont(){
		return new Font("思源宋体",Font.BOLD,18);
	}
	
	//创建菜单
	private void createMenu() {
		//创建JMenuBar
		jmb = new JMenuBar();			// ly: 路径显示
		//取得字体
		Font tFont = createFont(); 
		//创建游戏选项
		JMenu jMenu1 = new JMenu("游戏");
		jMenu1.setFont(tFont);
		//创建帮助选项
		JMenu jMenu2 = new JMenu("帮助");
		jMenu2.setFont(tFont);
		
		JMenuItem jmi1 = new JMenuItem("新游戏");
		jmi1.setFont(tFont);
		JMenuItem jmi2 = new JMenuItem("退出");
		jmi2.setFont(tFont);
		//jmi1 jmi2添加到菜单项“游戏”中
		jMenu1.add(jmi1);
		jMenu1.add(jmi2);
		
		JMenuItem jmi3 = new JMenuItem("操作帮助");
		jmi3.setFont(tFont);
		JMenuItem jmi4 = new JMenuItem("胜利条件");
		jmi4.setFont(tFont);
		JCheckBoxMenuItem jmi5 = new JCheckBoxMenuItem("小猿搜题");	// ly: 在右边的钩钩
		jmi5.setFont(tFont);
		//jmi3 jmi4 jmi5添加到菜单项“帮助”中
		jMenu2.add(jmi3);
		jMenu2.add(jmi4);
		jMenu2.add(jmi5);
		
		jmb.add(jMenu1);
		jmb.add(jMenu2);
		
		mainFrame.setJMenuBar(jmb);

		//设置指令
		jmi1.setActionCommand("restart");
		jmi2.setActionCommand("exit");
		jmi3.setActionCommand("help");
		jmi4.setActionCommand("win");
		jmi5.setActionCommand("answer");

		//添加监听
		jmi1.addActionListener(e -> {
			jmi5.setSelected(false);
			actionPerformed(e);
		});
		jmi2.addActionListener(this);
		jmi3.addActionListener(this);
		jmi4.addActionListener(this);
		jmi5.addActionListener(e -> {
			if (jmi5.isSelected()) {
				System.out.println("选中");
				showPath = true;
				showCorrectPath(); // 选中时调用 showCorrectPath 方法
			} else {
				System.out.println("取消选中");
				showPath = false; // 取消选择时不再显示路径
				repaint(); // 重新绘制面板
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		System.out.println(command);
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
		if ("exit".equals(command)) {
			Object[] options = { "确定", "取消" };
			int response = JOptionPane.showOptionDialog(this, "您确认要退出吗", "",
					JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					options, options[0]);
			if (response == 0) {
				System.exit(0);
			} 
		}else if("restart".equals(command)){
			restart();
		}else if("help".equals(command)){
			JOptionPane.showMessageDialog(null, "通过键盘的上下左右(↑↓←→或WSAD)来移动",
					"提示！", JOptionPane.INFORMATION_MESSAGE);
		}else if("win".equals(command)){
			JOptionPane.showMessageDialog(null, "移动到终点获得胜利",
					"提示！", JOptionPane.INFORMATION_MESSAGE);
		}else if("answer".equals(command)){	//ly显示路径
			//findPath();
			showCorrectPath();
		}
	}
	
	//重新开始
	void restart() {	// ly按需模式重开
		//1.游戏状态
		gameFlag="start";
		showPath=false;
		//2.迷宫单元重置
		Block block ;
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				block = blocks[i][j];
				if(block!=null){
					block.setVisited(false);
					block.walls[0]=true;
					block.walls[1]=true;
					block.walls[2]=true;
					block.walls[3]=true;
				}
			}
		}
		//3.计算处理线路
		computed();
		//开始方块归零
		start.setI(0);
		start.setJ(0);
		//ly
		findPath();
		//重绘
		repaint();
	}
	//游戏胜利
	public void gameWin() {
		gameFlag = "end";
		showCorrectPath(); // 显示路径	//ly 新游戏不显示路径
		//弹出结束提示
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("思源宋体", Font.PLAIN, 18)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("思源宋体", Font.PLAIN, 18)));
	    JOptionPane.showMessageDialog(mainFrame, "你胜利了,太棒了!");		// ly: 如果难度低问用户是否挑战更高难度
	}
	//游戏结束
	public void gameOver() {
		gameFlag = "end";
		//弹出结束提示
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("思源宋体", Font.PLAIN, 18)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("思源宋体", Font.PLAIN, 18)));
	    JOptionPane.showMessageDialog(mainFrame, "你失败了,请再接再厉!");
	}
}
