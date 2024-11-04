package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;
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
	private ImageIcon chaserIcon;
	private boolean fogOfWar;	// ly：战争迷雾
	private boolean chasing;
	private int chaserDist;

	public final int ROWS=20;//行
	public final int COLS=20;//列
	public final int H=20;//每一块的宽高
	Block[][] blocks = null;

	Rect start ;//开始方形
	Rect end ;//终点方形
	Rect chaser;//追逐方形

	private String gameFlag="pause";//游戏状态
	private Timer timer; // 计时器
	private int elapsedSeconds; // 经过的秒数

	private JLabel modeDescriptionLabel = new JLabel();

	//构造方法
	public GamePanel(GameFrame mainFrame){
		this.setLayout(null);
		this.setOpaque(false);
		this.mainFrame=mainFrame;
		this.panel =this;
		this.elapsedSeconds=0;
		this.modeDescriptionLabel=new JLabel(mainFrame.modeDesc);

		// 根据模式判断是否有fog 更新fogOfWar
		hasFog();
		// 根据模式判断是否有chaser 更新chasing
		hasChaser();

		// 加载图标
		playerIcon = new ImageIcon("imgs/playerIcon.jpg");
		endIcon = new ImageIcon("imgs/endIcon.png");

		//创建菜单
		createMenu();
		//创建数组内容
		createBlocks();
		//计算处理线路
		computed();
		//创建开始结束的方形
		createRects();
		//创建追逐者
		hasChaser();
		System.out.println(chasing);
		if (chasing) {
			chaserIcon = new ImageIcon("imgs/chaserIcon.jpg");
			createChaser();
		}
		//ly:寻找正确路径
		findPath();
		//添加键盘事件监听
		createKeyListener();
	}

	//ly创建追逐者
	private void createChaser() {
		chaser = new Rect(0, 0, H, "chaser");
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

	// 判断是否有追逐者
	private void hasChaser() {
		if (Objects.equals(mainFrame.mode, "『迷雾追逐模式』")) {
			chasing = true;
			createChaser();
		} else {
			chasing = false;
			chaserDist = -1;
		}
	}

	// ly在游戏胜利或求助时显示路径
	private void showCorrectPath() {
		repaint(); // 重新绘制面板以显示路径
	}

	// 重置fogOfWar状态
	private void hasFog() {
		fogOfWar =(!Objects.equals(mainFrame.mode, "『普通模式』"));
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

	// 基于从 start 到 end 的路径计算从 chaser 到 start 的路径
	private List<Block> calculateChaserToStartPath() {
		if (chaser == null) {
			return Collections.emptyList(); // 如果 chaser 为 null，返回一个空列表
		}
		Block chaserBlock = blocks[chaser.getI()][chaser.getJ()];
		Block startBlock = blocks[start.getI()][start.getJ()];

		if (correctPath.isEmpty()) {
			findPath(); // 先寻找从 start 到 end 的路径
		}

		List<Block> chaserPath = new ArrayList<>();
		Set<Block> visited = new HashSet<>(); // 防止重复访问
		Queue<Block> queue = new LinkedList<>();
		Map<Block, Block> parentMap = new HashMap<>(); // 存储路径回溯信息

		queue.add(chaserBlock);
		visited.add(chaserBlock);
		parentMap.put(chaserBlock, null);

		// 使用 BFS 进行最短路径查找
		while (!queue.isEmpty()) {
			Block current = queue.poll();

			// 如果找到 startBlock，就构造路径
			if (current == startBlock) {
				Block step = current;
				while (step != null) {
					chaserPath.add(0, step); // 从末尾到起点反向加入
					step = parentMap.get(step);
				}
				break;
			}

			// 访问 correctPath 中的邻居，确保路径方向符合找到的路径
			int index = correctPath.indexOf(current);
			if (index != -1 && index + 1 < correctPath.size()) {
				Block nextStep = correctPath.get(index + 1);
				if (!visited.contains(nextStep)) {
					queue.add(nextStep);
					visited.add(nextStep);
					parentMap.put(nextStep, current);
				}
			}
		}

		if (chaserPath.isEmpty()) {
			System.out.println("No path to Start found from Chaser.");
		}

		return chaserPath;
	}


	// 获取当前时间格式化的方法
	private String getCurrentTimeFormatted() {
		int hours = elapsedSeconds / 3600;
		int minutes = (elapsedSeconds % 3600) / 60;
		int seconds = elapsedSeconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	// 游戏开始
	void startGame() {
		gameFlag = "start"; // 设置游戏状态为 "start"
		System.out.println("Game started!"); // 确认进入了该方法

		// 初始化计时器
		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				elapsedSeconds++; // 每秒递增
				drawStatus(getGraphics()); // 更新状态面板
			}
		});
		timer.start(); // 启动计时器

		drawStatus(getGraphics()); // 更新状态面板
		mainFrame.requestFocus(); // 确保主窗口获得焦点，能够接收键盘输入
	}

	//绘制开始结束方块
	private void drawRect(Graphics g) {
		// 绘制终点图标
		if (endIcon != null) {
			g.drawImage(endIcon.getImage(), end.getJ() * H + 7, end.getI() * H + 7, H-2, H-2, this);
		}

		// 绘制玩家图标
		if (playerIcon != null) {
			g.drawImage(playerIcon.getImage(), start.getJ() * H + 7, start.getI() * H + 7, H-2, H-2, this);
		}
	}

	private void drawChaser(Graphics g) {
		// 绘制追逐图标
		if (chasing && mainFrame.mode == "『迷雾追逐模式』" && elapsedSeconds > 5) {
			g.drawImage(chaserIcon.getImage(), chaser.getJ() * H + 7, chaser.getI() * H + 7, H - 2, H - 2, this);
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

	// ly绘制路径
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

	// ly绘制迷雾
	private void drawFog(Graphics g) {
		int playerX = start.getJ(); // 玩家在网格中的列
		int playerY = start.getI(); // 玩家在网格中的行
		int chaserX = 0;
		int chaserY = 0;
		if (chaser != null) {
			chaserX = chaser.getJ();
			chaserY = chaser.getI();
		}
		int endX = end.getJ(); // 终点在网格中的列
		int endY = end.getI(); // 终点在网格中的行


		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				// 判断当前块是否在可见区域内
				boolean isVisible = (Math.abs(i - playerY) < 2 && Math.abs(j - playerX) < 2) || // 玩家周围的 4x4 块
						(Math.abs(i - chaserY) < 2 && Math.abs(j - chaserX) < 2) ||
						(Math.abs(i - endY) < 2 && Math.abs(j - endX) < 2); // 终点周围的 4x4 块

				if (!isVisible) {
					g.setColor(new Color(0, 0, 0, 250)); // 不透明黑色
					g.fillRect(j * H + 8, i * H + 8, H, H); // 绘制迷雾覆盖
				}
			}
		}
	}

	// ly右侧状态栏
	// 绘制状态面板的函数，显示模式及其他状态信息
	public void drawStatus(Graphics g) {
		// 更新状态面板的内容，而不是清除它
		if (mainFrame.statusPane.getComponentCount() == 0) {
			// 创建开始按钮
			JButton startButton = new JButton("开始");
			startButton.setBounds(450, 230, 100, 30);
			startButton.addActionListener(e -> {
				System.out.println("Start button clicked!");
				startGame();
			});
			mainFrame.statusPane.add(startButton);

			// 模式标签
			JLabel modeLabel = new JLabel(mainFrame.mode);
			modeLabel.setFont(new Font("幼圆", Font.PLAIN, 15));
			modeLabel.setBounds(450, 15, 130, 40);
			mainFrame.statusPane.add(modeLabel);

			// 模式描述
			modeDescriptionLabel.setFont(new Font("幼圆", Font.PLAIN, 12));
			modeDescriptionLabel.setBounds(450, 60, 300, 40);
			mainFrame.statusPane.add(modeDescriptionLabel);

			// 当前步数标签
			JLabel currentStepsLabel = new JLabel("当前步数: " + start.curSteps);
			currentStepsLabel.setFont(new Font("幼圆", Font.PLAIN, 12));
			currentStepsLabel.setBounds(450, 105, 200, 40);
			mainFrame.statusPane.add(currentStepsLabel);

			// 理想步数标签
			String expSteps = (gameFlag.equals("end") ? String.valueOf(correctPath.size()) : "?");
			JLabel expectedStepsLabel = new JLabel("理想步数: " + expSteps);
			expectedStepsLabel.setFont(new Font("幼圆", Font.PLAIN, 12));
			expectedStepsLabel.setBounds(450, 150, 200, 40);
			mainFrame.statusPane.add(expectedStepsLabel);

			// 当前用时标签
			JLabel timeLabel = new JLabel("当前用时: " + getCurrentTimeFormatted());
			timeLabel.setFont(new Font("幼圆", Font.PLAIN, 12));
			timeLabel.setBounds(450, 260, 200, 40);
			mainFrame.statusPane.add(timeLabel);

			// 步数距离标签
			JLabel distanceLabel = new JLabel();
			if (chasing && chaser != null && chaserDist >= 0 && mainFrame.mode == "『迷雾追逐模式』") {
				chaserDist = calculateChaserToStartPath().size() - 1;
				distanceLabel = new JLabel("距离追逐者: " + chaserDist + " 步");
				distanceLabel.setFont(new Font("幼圆", Font.PLAIN, 12));
				distanceLabel.setBounds(450, 190, 200, 40);
				mainFrame.statusPane.add(distanceLabel);
			} else if (distanceLabel != null) {
				mainFrame.statusPane.remove(distanceLabel);
			}
		} else {
			// 更新已有组件的文本
			((JLabel) mainFrame.statusPane.getComponent(1)).setText(mainFrame.mode);
			((JLabel) mainFrame.statusPane.getComponent(3)).setText("当前步数: " + start.curSteps);
			String expSteps = (gameFlag.equals("end") ? String.valueOf(correctPath.size()) : "?");
			((JLabel) mainFrame.statusPane.getComponent(4)).setText("理想步数: " + expSteps);

			// 更新当前用时标签
			((JLabel) mainFrame.statusPane.getComponent(5)).setText("当前用时: " + getCurrentTimeFormatted());

			// 更新步数距离标签
			if (chasing && chaser != null && mainFrame.mode == "『迷雾追逐模式』") {
				int chaserDist = calculateChaserToStartPath().size() - 1;
				((JLabel) mainFrame.statusPane.getComponent(6)).setText("距离追逐者: " + chaserDist + " 步");
			}
		}

		// 刷新状态面板
		mainFrame.statusPane.setLayout(null);
		mainFrame.statusPane.revalidate();
		mainFrame.statusPane.repaint();
	}


	public void updateModeDescriptionLabel() {
		modeDescriptionLabel.setText(mainFrame.modeDesc);
		repaint(); // 可能需要重新绘制面板以更新显示
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// 绘制网格、起点和终点
		drawBlock(g);
		drawRect(g);
		drawChaser(g);
		drawStatus(g);

		if (fogOfWar) drawFog(g);
		if (showPath) drawPath(g);
	}

	//添加键盘监听
	private void createKeyListener() {
		KeyAdapter l = new KeyAdapter() {
			//按下
			@Override
			public void keyPressed(KeyEvent e) {
				if(!"start".equals(gameFlag))
					return;
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
			mainFrame.restart();
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

	// 重置追逐者状态
	private void resetChaser() {
		chasing = false; // 禁用追逐者状态
		chaser = null;   // 清除追逐者位置
		chaserIcon = null; // 清除追逐者图标
		chaserDist = 0;   // 重置步数距离
	}

	//重新开始
	public void resetGame() {
		gameFlag = "pause";
		showPath = false;
		resetChaser();
		start.curSteps = 0;
		elapsedSeconds = 0;
		if (timer != null) timer.stop();
		hasFog();
		System.out.println(mainFrame.mode);
		hasChaser();

		// 重置每个块的访问状态和墙
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				blocks[i][j].setVisited(false);
				blocks[i][j].walls[0] = true;
				blocks[i][j].walls[1] = true;
				blocks[i][j].walls[2] = true;
				blocks[i][j].walls[3] = true;
			}
		}

		// 重新生成迷宫和路径
		computed();
		start.setI(0);
		start.setJ(0);
		findPath();
		repaint();

		// 更新状态面板
		drawStatus(getGraphics());
	}

	//游戏胜利
	public void gameWin() {
		gameFlag = "end";
		showPath = true;
		showCorrectPath(); // 显示路径	//ly 新游戏不显示路径
		timer.stop();
		//弹出结束提示
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("思源宋体", Font.PLAIN, 18)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("思源宋体", Font.PLAIN, 18)));

		Object[] options = {"是", "否"};
		int result = JOptionPane.showOptionDialog(
				mainFrame,
				"你胜利了,太棒了!\n是否要挑战其他模式？",
				"胜利",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				options,
				options[0]
		);
		if (result == JOptionPane.YES_OPTION) {
			System.out.println("用户选择了挑战其他模式！");
			mainFrame.restart();
		} else if (result == JOptionPane.NO_OPTION) {
			System.out.println("用户选择了不挑战其他模式。");
		}

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
