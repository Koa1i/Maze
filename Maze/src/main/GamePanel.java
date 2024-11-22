package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.FontUIResource;

import static java.lang.Math.abs;

/*
 * 画布类
 */
public class GamePanel extends JPanel implements ActionListener{
	private JMenuBar jmb = null;
	private int jmbHeight;
	private GameFrame mainFrame = null;
	private GamePanel panel = null;
	private List<Block> correctPath = new ArrayList<>(); // 用于存储正确路径
	private boolean showPath = false; // 控制路径显示的变量
	private ImageIcon playerIcon;
	private ImageIcon endIcon;
	private ImageIcon chaserIcon;
	private ImageIcon revivalIcon;
	private boolean fogOfWar;	// ly：战争迷雾
	private boolean chasing;
	private int chaserDist;

	public int width;
	public int height;
	public int xBias;
	public int yBias;
	public int ROWS=20;//行	// ly自定义大小
	public int COLS=20;//列
	public int H=30;//每一块的宽高
	Block[][] blocks = null;

	Rect start ;//开始方形
	Rect end ;//终点方形
	Rect chaser;//追逐方形

	private String gameFlag="pause";//游戏状态
	private boolean isPaused = false; // 记录游戏是否暂停
	private Timer timer; // 计时器
	private int elapsedSeconds; // 经过的秒数

	private Timer chaseTimer;	//chase计时器

	private boolean hasRevival = true; // 控制复活是否可用
	private boolean revivalActive = false;  //
	private Timer revivalBlinkTimer; // 控制复活图标闪烁的计时器
	private boolean isRevivalMessageShown = false;	// 复活提示消息

	// 定义类成员变量来引用各个状态标签
	private JButton startButton;
	private JLabel modeLabel;
	private JLabel currentStepsLabel;
	private JLabel expectedStepsLabel;
	private JLabel timeLabel;
	private JLabel distanceLabel;
	private JLabel revivalLabel;
	private JLabel modeDescriptionLabel = new JLabel();

	JCheckBoxMenuItem jmi5 = new JCheckBoxMenuItem("小猿搜题");	// ly: 在右边的钩钩

	boolean flippedFlag = false;	// 调试flag

	private ImageIcon victoryIcon, medalIcon, overIcon, fingerIcon, cheeseIcon;  // 胜利和失败图标
	private int iconWidth, iconHeight;   // 每个图标的当前宽高
	private int targetWidth, targetHeight;  // 每个图标的目标宽高
	private int[] xPositions;  // 每个图标的 x 坐标
	private int yPosition;     // 所有图标的 y 坐标
	private Timer enlargeTimer = null;
	private double efficiency;

	//构造方法
	public GamePanel(GameFrame mainFrame, int mazeSide){
		this.setLayout(null);
		this.setOpaque(false);
		this.mainFrame=mainFrame;
		this.panel =this;
		this.elapsedSeconds=0;
		this.modeDescriptionLabel=new JLabel(mainFrame.modeDesc);
		this.flippedFlag = false;
		this.ROWS = mazeSide;
		this.COLS = mazeSide;

		// 根据模式判断是否有fog 更新fogOfWar
		hasFog();
		// 根据模式判断是否有chaser 更新chasing
		hasChaser();

		// 加载图标
		playerIcon = new ImageIcon("imgs/playerIcon.png");
		endIcon = new ImageIcon("imgs/endIcon.png");

		JCheckBoxMenuItem jmi5 = new JCheckBoxMenuItem("小猿搜题");	// ly: 在右边的钩钩

		//创建菜单
		createMenu();

		// 获取合适窗口大小
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = (int)(screenSize.width * 0.8);
		System.out.println(width);
		height = (int)(screenSize.height * 0.8);
		System.out.println(height);
		xBias = (int) (width * 0.08);
		yBias = (int) (height * 0.1);

		// 设置合适方格块大小
		jmbHeight = jmb.getHeight();
		setBlockSide();

		//创建方块
		createBlocks();
		//ly 使用Kruskal算法生成迷宫
		KruskalMazeGenerator generator = new KruskalMazeGenerator(ROWS, COLS, panel);
		blocks = generator.generateMaze();
		createBlocks();
		//计算处理线路
		computed();
		//创建开始结束的方形
		createRects();
		//创建追逐者
		hasChaser();
		System.out.println("chasing is " + chasing);
		if (chasing) {
			chaserIcon = new ImageIcon("imgs/chaserIcon.png");
			createRevival();
			createChaser();
		}
		//ly:寻找正确路径
		findPath();
		//添加键盘事件监听
		createKeyListener();
	}

	// 获取合适方格块大小
	private void setBlockSide() {
		int menuBarHeight = jmbHeight; // 假设菜单栏高度为 30 像素
		int extraHeight = 100 - COLS;   // 其他额外高度占用
		// 根据窗口宽度和有效高度（总高度减去菜单栏等占用）计算单元格大小
		int cellWidth = width / COLS;                          // 单元格宽度
		int cellHeight = (height - menuBarHeight - extraHeight) / ROWS; // 单元格高度
		// 为了确保单元格为正方形，取较小的值
		H = Math.min(cellWidth, cellHeight);
		System.out.println("单元格大小 (H): " + H);
		// 适配后的迷宫总宽度和高度
		int mazeWidth = H * COLS;
		int mazeHeight = H * ROWS;
		// 打印调试信息
		System.out.println("迷宫宽度: " + mazeWidth + ", 迷宫高度: " + mazeHeight);
	}

	//ly创建复活甲
	private void createRevival() {
		revivalIcon = new ImageIcon("imgs/revivalIcon.png");
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

	//BFS并取消追逐者路径对 correctPath 的依赖
	private List<Block> calculateChaserToStartPath() {
		if (chaser == null) {
			return Collections.emptyList();
		}
		Block chaserBlock = blocks[chaser.getI()][chaser.getJ()];
		Block startBlock = blocks[start.getI()][start.getJ()];

		List<Block> chaserPath = new ArrayList<>();
		Set<Block> visited = new HashSet<>();
		Queue<Block> queue = new LinkedList<>();
		Map<Block, Block> parentMap = new HashMap<>();

		queue.add(chaserBlock);
		visited.add(chaserBlock);
		parentMap.put(chaserBlock, null);

		// 使用 BFS 计算最短路径
		while (!queue.isEmpty()) {
			Block current = queue.poll();

			if (current == startBlock) {
				Block step = current;
				while (step != null) {
					chaserPath.add(0, step);
					step = parentMap.get(step);
				}
				break;
			}

			// 遍历所有邻居
			for (int i = 0; i < 4; i++) {
				Block neighbor = current.getNeighbor(i, true);
				if (neighbor != null && !visited.contains(neighbor) && !current.walls[i]) {
					queue.add(neighbor);
					visited.add(neighbor);
					parentMap.put(neighbor, current);
				}
			}
		}

		// 未找到路径的情况下返回空路径
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


	// 追逐逻辑
	private void startChasing() {
		// 如果已有计时器在运行，先停止该计时器
		if (chaseTimer != null && chaseTimer.isRunning()) {
			chaseTimer.stop();
		}

		chaseTimer = new Timer(200, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!"start".equals(gameFlag) || revivalActive) {
					stopChasing();
					return;
				}

				List<Block> pathToPlayer = calculateChaserToStartPath();
				if (!pathToPlayer.isEmpty()) {
					if (pathToPlayer.size() >= 2) {
						Block nextStep = pathToPlayer.get(1); // 获取追逐者下一步位置
						chaser.setPosition(nextStep.getI(), nextStep.getJ());
					}

					// 检查是否被抓住
					chaserCaughtPlayer();

					repaint(); // 重绘以更新追逐者位置
				}
			}
		});
		chaseTimer.start(); // 启动追逐计时器
	}


	// 停止追逐的计时器
	private void stopChasing() {
		if (chaseTimer != null) {
			chaseTimer.stop(); // 停止追逐计时器
		}
	}

	// 复活逻辑
	private void activateRevival() {
		revivalActive = true;

		// 获取或创建 revivalLabel，如果不存在则添加到 statusPane 中
		if (revivalLabel == null) {
			revivalLabel = new JLabel(revivalIcon);
			revivalLabel.setBounds((int) (width * 0.8) - xBias, 6 * yBias, height / 5, height / 5);
			mainFrame.statusPane.add(revivalLabel);
			mainFrame.statusPane.revalidate();
			mainFrame.statusPane.repaint();
		}

		// 创建并启动复活闪烁定时器
		revivalBlinkTimer = new Timer(300, e -> {
			// 切换 revivalLabel 的可见性
			revivalLabel.setVisible(!revivalLabel.isVisible());
		});
		revivalBlinkTimer.start();  // 启动闪烁定时器

		// 显示复活提示窗口
		showRevivalMessage();

		// 创建并启动一个定时器，在 3 秒后停止复活闪烁并移除 revivalLabel
		Timer revivalStopTimer = new Timer(3000, e -> {
			// 停止闪烁定时器
			revivalBlinkTimer.stop();

			// 确保 revivalLabel 显示状态为可见
			revivalLabel.setVisible(true);

			// 移除 revivalLabel
			mainFrame.statusPane.remove(revivalLabel);
			mainFrame.statusPane.revalidate();
			mainFrame.statusPane.repaint();

			// 关闭复活功能并恢复追逐
			hasRevival = false;
			revivalActive = false;

			// 5秒后恢复追逐逻辑
			startChasing(); // 恢复追逐行为
		});
		revivalStopTimer.setRepeats(false); // 确保定时器只执行一次
		revivalStopTimer.start();  // 启动停止定时器
	}


	// 显示追逐出发信息
	private void showChaserMessage() {
		JWindow chaserWindow = new JWindow(mainFrame);
		chaserWindow.setSize((int) (width * 0.18), (int) (height * 0.1));
		chaserWindow.setLocation(width / 2 + (int)(1.85 * xBias), height / 4);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0, 180)); // 半透明背景
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel messageLabel0 = new JLabel("\uD83D\uDE08", SwingConstants.CENTER); // 😈
		JLabel messageLabel1 = new JLabel("贝利亚还有", SwingConstants.CENTER);
		JLabel messageLabel2 = new JLabel((5 - elapsedSeconds) + "秒到达战场", SwingConstants.CENTER);

		// 设置字体和颜色
		messageLabel0.setFont(new Font("Microsoft", Font.PLAIN, 20));
		messageLabel1.setFont(new Font("幼圆", Font.BOLD, 18));
		messageLabel2.setFont(new Font("幼圆", Font.BOLD, 18));

		messageLabel0.setForeground(Color.MAGENTA);
		messageLabel1.setForeground(Color.WHITE);
		messageLabel2.setForeground(Color.WHITE);

		panel.add(messageLabel0);
		panel.add(messageLabel1);
		panel.add(messageLabel2);

		chaserWindow.setContentPane(panel);
		chaserWindow.setVisible(true);

		// 倒计时更新
		Timer countDownTimer = new Timer(1000, e -> {
			if (elapsedSeconds < 5) {
				elapsedSeconds++;
				messageLabel2.setText((5 - elapsedSeconds) + "秒到达战场");
			}
		});
		countDownTimer.setRepeats(true);
		countDownTimer.start();

		// 渐变效果
		Timer fadeOutTimer = new Timer(50, null);
		AtomicInteger alpha = new AtomicInteger(255);
		fadeOutTimer.addActionListener(e -> {
			if (elapsedSeconds >= 5) { // 倒计时结束后开始透明渐变
				if (alpha.get() > 0) {
					alpha.addAndGet(-5);
					chaserWindow.setOpacity(alpha.get() / 255f);
				} else {
					fadeOutTimer.stop();
					chaserWindow.dispose();
					countDownTimer.stop();
				}
			}
		});
		fadeOutTimer.start();
	}

	// 显示复活信息
	private void showRevivalMessage() {
		JWindow revivalWindow = new JWindow(mainFrame);
		revivalWindow.setSize((int) (width * 0.18), (int) (height * 0.1));
		revivalWindow.setLocation(width / 2 + (int)(1.85 * xBias), height / 4);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0, 180)); // 半透明背景
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel messageLabel0 = new JLabel("\uD83D\uDC94", SwingConstants.CENTER); // 💔
		JLabel messageLabel1 = new JLabel("奶龙的复活甲掉了", SwingConstants.CENTER);
		JLabel messageLabel2 = new JLabel("你有3秒无敌时间来摆脱贝利亚！", SwingConstants.CENTER);

		// 设置字体和颜色
		messageLabel0.setFont(new Font("Microsoft", Font.PLAIN, 20));
		messageLabel1.setFont(new Font("幼圆", Font.BOLD, 18));
		messageLabel2.setFont(new Font("幼圆", Font.BOLD, 18));

		messageLabel0.setForeground(Color.RED);
		messageLabel1.setForeground(Color.WHITE);
		messageLabel2.setForeground(Color.WHITE);

		panel.add(messageLabel0);
		panel.add(messageLabel1);
		panel.add(messageLabel2);

		revivalWindow.setContentPane(panel);
		revivalWindow.setVisible(true);

		// 渐变效果
		Timer fadeOutTimer = new Timer(50, null);
		AtomicInteger alpha = new AtomicInteger(255);
		fadeOutTimer.addActionListener(e -> {
			if (alpha.get() > 0) {
				alpha.addAndGet(-5);
				revivalWindow.setOpacity(alpha.get() / 255f);
			} else {
				fadeOutTimer.stop();
				revivalWindow.dispose();
			}
		});

		// 3秒后开始透明渐变
		Timer delayTimer = new Timer(3000, e -> fadeOutTimer.start());
		delayTimer.setRepeats(false);
		delayTimer.start();
	}

	// 显示结算信息
	private void showVictoryMessage(int medalCount) {
		JWindow victoryWindow = new JWindow(mainFrame);
		victoryWindow.setSize((int) (width * 0.18), (int) (height * 0.11));
		victoryWindow.setLocation(width / 2 + (int)(1.85 * xBias), height / 4);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0, 180)); // 半透明背景
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// 修复负数问题
		int optimalSteps = correctPath.size() - 1;
		int excessSteps = start.curSteps - optimalSteps;
		double efficiency = (double) Math.max(0, excessSteps) / optimalSteps;

		// 格式化效率百分比
		String formattedEfficiency = String.format("%.2f%%", (1 - efficiency) * 100);

		JLabel messageLabel0 = new JLabel("\uD83D\uDE0D \uD83D\uDE0D \uD83D\uDE0D", SwingConstants.CENTER); //😍
		JLabel messageLabel1 = new JLabel("奶龙的效率为 " + formattedEfficiency, SwingConstants.CENTER);
		JLabel messageLabel2 = new JLabel("你因此获得了 " + medalCount + " 块奖牌 !", SwingConstants.CENTER);	// 🏅

		// 设置字体和颜色
		messageLabel0.setFont(new Font("Microsoft", Font.PLAIN, 20));
		messageLabel1.setFont(new Font("Microsoft", Font.BOLD, 18));
		messageLabel2.setFont(new Font("Microsoft", Font.BOLD, 18));

		messageLabel0.setForeground(Color.RED);
		messageLabel1.setForeground(Color.WHITE);
		messageLabel2.setForeground(Color.WHITE);

		panel.add(messageLabel0);
		panel.add(messageLabel1);
		panel.add(messageLabel2);

		victoryWindow.setContentPane(panel);
		victoryWindow.setVisible(true);
	}

	// 贝利亚抓住奶龙
	private void chaserCaughtPlayer() {
		if (chaser.getI() == start.getI() && chaser.getJ() == start.getJ()) {
			if (hasRevival) {
				stopChasing();  // 停止追逐
				activateRevival(); // 激活复活机制
			} else {
				gameOver();  // 没有复活则结束游戏
			}
			return;
		}
		return;
	}

	//绘制开始结束方块
	private void drawRect(Graphics g) {
		// 绘制终点图标
		if (endIcon != null) {
			g.drawImage(endIcon.getImage(), end.getJ() * H + 7, end.getI() * H + 7, H-2, H-2, this);
		}

		// 绘制玩家图标
		if (playerIcon != null) {
			if (!flippedFlag) {
				playerIcon = flipImageHorizontally(playerIcon);
				flippedFlag = true;
			}
			g.drawImage(playerIcon.getImage(), start.getJ() * H + 7, start.getI() * H + 7, H-2, H-2, this);
		}
	}

	private ImageIcon flipImageHorizontally(ImageIcon icon) {
		// 获取原始图片
		Image originalImage = icon.getImage();
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();

		// 创建翻转后的空白图像
		BufferedImage flippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// 使用 Graphics2D 进行翻转
		Graphics2D g2d = flippedImage.createGraphics();
		AffineTransform transform = AffineTransform.getScaleInstance(-1, 1); // 水平翻转
		transform.translate(-width, 0); // 移动图像位置
		g2d.drawImage(originalImage, transform, null);
		g2d.dispose();

		return new ImageIcon(flippedImage);
	}

	private void drawChaser(Graphics g) {
		// 绘制追逐图标
		if (chasing && Objects.equals(mainFrame.mode, "『迷雾追逐模式』")) {
			chaserIcon = new ImageIcon("imgs/chaserIcon.png");
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
		if (!fogOfWar) {
			return;
		}

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
				boolean isVisible = (abs(i - playerY) < 2 && abs(j - playerX) < 2) || // 玩家周围的 4x4 块
						(abs(i - chaserY) < 2 && abs(j - chaserX) < 2) ||
						(abs(i - endY) < 2 && abs(j - endX) < 2); // 终点周围的 4x4 块

				if (!isVisible) {
					g.setColor(new Color(0, 0, 0, 250)); // 不透明黑色
					g.fillRect(j * H + 6, i * H + 8, H, H); // 绘制迷雾覆盖
				}
			}
		}
	}

	private void drawRevivalIcon(Graphics g) {
		if (revivalIcon == null) {
			createRevival();
		}

		// 如果复活激活并且追逐模式启动，绘制复活图标
		if (isRevivalActive() && chasing) {
			// 如果复活闪烁正在进行中，且当前复活甲不可见，则不绘制图标
			if (revivalBlinkTimer != null && revivalBlinkTimer.isRunning() && !revivalLabel.isVisible()) {
				return; // 不绘制图标
			}
			g.drawImage(revivalIcon.getImage(), (int) (width * 0.8) - xBias, 6 * yBias, height / 5, height / 5, this);
		}
	}



	// 初始化状态面板的固定组件，仅调用一次
	private void initializeStatusPane() {
		if (mainFrame.statusPane.getComponentCount() > 0) return;

		// 背景
		ImageIcon statusBg = new ImageIcon("imgs/statusBg1.png");
		JLabel statusBgLabel = new JLabel(statusBg);
		JLabel statusBgLabel2 = new JLabel(new ImageIcon("imgs/statusBg2.png"));
		statusBgLabel.setBounds((int) (width * 0.7), -(int) (1.8*yBias), (int) (width * 0.3), height);
		statusBgLabel2.setBounds((int) (width * 0.7 - 0.1 * xBias), (int) (5.5*yBias), (int) (width * 0.3), height);

		mainFrame.statusPane.add(statusBgLabel, JLayeredPane.DEFAULT_LAYER);
		mainFrame.statusPane.add(statusBgLabel2, JLayeredPane.DEFAULT_LAYER);

		// 创建“开始”按钮
		startButton = new JButton("开始");
		startButton.setFont(new Font("幼圆", Font.PLAIN, 18));
		ImageIcon buttonIcon = new ImageIcon("imgs/fingerIcon.png");
		startButton.setIcon(buttonIcon);

		// 设置图标和文字的布局
		startButton.setHorizontalTextPosition(SwingConstants.RIGHT); // 文字在图标右边
		startButton.setVerticalTextPosition(SwingConstants.CENTER);  // 垂直居中
		startButton.setHorizontalAlignment(SwingConstants.LEFT);     // 图标靠左对齐
		startButton.setIconTextGap((int) (width * 0.04));                              // 图标和文字之间的间距

		// 设置按钮位置和大小
		startButton.setBounds((int) (width * 0.8) - xBias, 5 * yBias, (int) (width * 0.2), height / 11);


		startButton.addActionListener(e -> {
			if ("开始".equals(startButton.getText())) {
				// 点击“开始”或“继续”
				startGame();
				updateStartButton("暂停");
			} else if ("暂停".equals(startButton.getText())) {
				// 点击“暂停”
				gameFlag = "pause"; // 暂停游戏
				if (timer != null) {
					timer.stop(); // 停止计时器
				}
				updateStartButton("继续");
			} else if ("继续".equals(startButton.getText())) {
				// 点击“继续”
				startGame();
				updateStartButton("暂停");
			}
		});
		mainFrame.statusPane.add(startButton, JLayeredPane.MODAL_LAYER);

		// 模式标签 1
		modeLabel = new JLabel(mainFrame.mode);
		switch (mainFrame.mode) {
			case "『普通模式』": modeLabel.setForeground(Color.GREEN); break;
			case "『迷雾模式』": modeLabel.setForeground(Color.BLUE); break;
			case "『迷雾追逐模式』": modeLabel.setForeground(Color.RED); break;
			default: modeLabel.setForeground(Color.BLACK); break;
		}

		modeLabel.setFont(new Font("幼圆", Font.BOLD, 20));
		modeLabel.setBounds((int) (width * 0.8) - xBias, (int) (yBias * 0.25), (int) (width * 0.2), height / 10);
		mainFrame.statusPane.add(modeLabel, JLayeredPane.MODAL_LAYER);

		// 模式描述 2
		modeDescriptionLabel = new JLabel();
		updateModeDescriptionLabel();
		modeDescriptionLabel.setFont(new Font("幼圆", Font.PLAIN, 18));
		modeDescriptionLabel.setBounds((int) (width * 0.8) - xBias, (int) (yBias * 0.8), (int) (width * 0.2), height / 10);
		mainFrame.statusPane.add(modeDescriptionLabel, JLayeredPane.MODAL_LAYER);

		// 当前步数标签
		currentStepsLabel = new JLabel("当前步数: " + start.curSteps);
		currentStepsLabel.setFont(new Font("幼圆", Font.PLAIN, 18));
		currentStepsLabel.setBounds((int) (width * 0.8) - xBias, (int) (2 * yBias), (int) (width * 0.2), height / 10);
		mainFrame.statusPane.add(currentStepsLabel, JLayeredPane.MODAL_LAYER);

		// 理想步数标签
		expectedStepsLabel = new JLabel("理想步数: ?");
		expectedStepsLabel.setFont(new Font("幼圆", Font.PLAIN, 18));
		expectedStepsLabel.setBounds((int) (width * 0.8) - xBias, (int) (2.8 * yBias), (int) (width * 0.2), height / 10);
		mainFrame.statusPane.add(expectedStepsLabel, JLayeredPane.MODAL_LAYER);

		// 当前用时标签
		timeLabel = new JLabel("当前用时: " + getCurrentTimeFormatted());
		timeLabel.setFont(new Font("幼圆", Font.PLAIN, 18));
		timeLabel.setBounds((int) (width * 0.8) - xBias, (int) (4.3 * yBias), (int) (width * 0.2), height / 10);
		mainFrame.statusPane.add(timeLabel, JLayeredPane.MODAL_LAYER);

		mainFrame.statusPane.setLayout(null);
		mainFrame.statusPane.revalidate();
		mainFrame.statusPane.repaint();
	}

	// 绘制状态面板的内容，显示模式及其他状态信息
	public void drawStatus(Graphics g) {
		// 确保面板组件已初始化
		initializeStatusPane();

		// 更新模式、步数、理想步数和时间
		modeLabel.setText(mainFrame.mode);
		currentStepsLabel.setText("当前步数: " + start.curSteps);
		String expSteps = (gameFlag.equals("over") || gameFlag.equals("win") ? String.valueOf(correctPath.size() - 1) : "?");
		expectedStepsLabel.setText("理想步数: " + expSteps);
		timeLabel.setText("当前用时: " + getCurrentTimeFormatted());

		// 动态更新距离追逐者标签
		if ("『迷雾追逐模式』".equals(mainFrame.mode) && chasing && chaser != null) {
			if (distanceLabel == null) {
				distanceLabel = new JLabel();
				distanceLabel.setFont(new Font("幼圆", Font.BOLD, 18));
				distanceLabel.setBounds((int) (width * 0.8) - xBias, (int) (3.55 * yBias), (int) (width * 0.2), height / 10);
				mainFrame.statusPane.add(distanceLabel, JLayeredPane.MODAL_LAYER);
			}
			int chaserDist = calculateChaserToStartPath().size() - 1;
			if (chaserDist <= 10) {
				distanceLabel.setForeground(Color.RED);
			} else if (chaserDist > 10 && chaserDist <= 20) {
				distanceLabel.setForeground(Color.MAGENTA);
			} else {
				distanceLabel.setForeground(Color.GREEN);
			}
			distanceLabel.setText("距离追逐者: " + chaserDist + " 步");
		} else if (distanceLabel != null) {
			mainFrame.statusPane.remove(distanceLabel);
			distanceLabel = null;
		}

		// 动态更新复活图标
		if (hasRevival && chasing) {
			if (revivalLabel == null) {
				revivalLabel = new JLabel(revivalIcon);
				revivalLabel.setBounds((int) (width * 0.8) - xBias, 6 * yBias, height / 5, height / 5);
				mainFrame.statusPane.add(revivalLabel, JLayeredPane.MODAL_LAYER);
			}
		} else if (revivalLabel != null) {
			mainFrame.statusPane.remove(revivalLabel);
			revivalLabel = null;
		}

		mainFrame.statusPane.revalidate();
		mainFrame.statusPane.repaint();
	}



	private boolean isRevivalActive() {
		return revivalActive;
	}


	public void updateModeDescriptionLabel() {
		if (modeLabel != null) {
			switch (mainFrame.mode) {
				case "『普通模式』": modeLabel.setForeground(Color.GREEN); break;
				case "『迷雾模式』": modeLabel.setForeground(Color.BLUE); break;
				case "『迷雾追逐模式』": modeLabel.setForeground(Color.RED); break;
				default: modeLabel.setForeground(Color.BLACK); break;
			}
		}

		modeDescriptionLabel.setText(mainFrame.modeDesc);

		repaint(); // 可能需要重新绘制面板以更新显示
	}

	public void drawMedals(Graphics g, int medalCount) {
		showVictoryMessage(medalCount);

		int cnt = medalCount;
		while (cnt > 0) {
			int xBiasCnt = 2 - cnt;
			int yBiasCnt = abs(2 - cnt);
			g.drawImage(medalIcon.getImage(), (getWidth() - iconWidth) / 2 - xBiasCnt * xBias, (getHeight() - iconHeight) / 6 - (yBiasCnt * yBias), width / 10, width / 10, this);
			cnt--;
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// 绘制网格、起点和终点
		drawBlock(g);

		if (chaserDist <= 1) {
			drawRect(g);
			drawChaser(g);
		}
		drawChaser(g);
		drawRect(g);

		drawStatus(g);

		if (fogOfWar) drawFog(g);
		if (showPath) drawPath(g);
		if (hasRevival) drawRevivalIcon(g);

		if (gameFlag.equals("win")) {
			// 确定奖牌数量
			if (efficiency <= 0.1) {
				drawMedals(g, 3);
			} else if (efficiency <= 0.2) {
				drawMedals(g, 2);
			} else {
				drawMedals(g, 1);
			}

			g.drawImage(victoryIcon.getImage(), (getWidth() - iconWidth) / 2, (getHeight() - iconHeight) / 2, width / 6, width / 6, this);
		} else if (gameFlag.equals("over")) {
			g.drawImage(overIcon.getImage(), xPositions[0] + 2*xBias, yPosition - yBias * 3, iconWidth, iconHeight, this);
			g.drawImage(fingerIcon.getImage(), xPositions[1] + 2*xBias, yPosition - yBias * 3, iconWidth, iconHeight, this);
			g.drawImage(cheeseIcon.getImage(), xPositions[2] + 2*xBias, yPosition - yBias * 3, iconWidth, iconHeight, this);
		}
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
		return new Font("幼圆",Font.BOLD,18);
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

		JMenuItem jmi0 = new JMenuItem("重新开始");
		jmi0.setFont(tFont);
		JMenuItem jmi1 = new JMenuItem("新游戏");
		jmi1.setFont(tFont);
		JMenuItem jmi2 = new JMenuItem("退出");
		jmi2.setFont(tFont);
		//jmi0-2添加到菜单项“游戏”中
		jMenu1.add(jmi0);
		jMenu1.add(jmi1);
		jMenu1.add(jmi2);

		JMenuItem jmi3 = new JMenuItem("操作帮助");
		jmi3.setFont(tFont);
		JMenuItem jmi4 = new JMenuItem("胜利条件");
		jmi4.setFont(tFont);
		jmi5 = new JCheckBoxMenuItem("小猿搜题");	// ly: 在右边的钩钩
		jmi5.setFont(tFont);
		//jmi3-5添加到菜单项“帮助”中
		jMenu2.add(jmi3);
		jMenu2.add(jmi4);
		jMenu2.add(jmi5);

		jmb.add(jMenu1);
		jmb.add(jMenu2);

		mainFrame.setJMenuBar(jmb);

		//设置指令
		jmi0.setActionCommand("restart");
		jmi1.setActionCommand("new");
		jmi2.setActionCommand("exit");
		jmi3.setActionCommand("help");
		jmi4.setActionCommand("win");
		jmi5.setActionCommand("answer");

		//添加监听
		jmi0.addActionListener(e -> {
			jmi5.setSelected(false);
			actionPerformed(e); // 调用 actionPerformed 方法
		});
		jmi1.addActionListener(e -> {
			jmi5.setSelected(false);
			actionPerformed(e);
		});
		jmi2.addActionListener(this);
		jmi3.addActionListener(this);
		jmi4.addActionListener(this);
		jmi5.addActionListener(e -> {
			if (jmi5.isSelected()) {
				System.out.println("选中小猿搜题");
				showPath = true;
				showCorrectPath(); // 选中时调用 showCorrectPath 方法
			} else {
				System.out.println("取消选中小猿");
				showPath = false; // 取消选择时不再显示路径
				repaint(); // 重新绘制面板
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		System.out.println(command);
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("幼圆", Font.ITALIC, 18)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("幼圆", Font.ITALIC, 18)));
		if ("exit".equals(command)) {
			Object[] options = { "确定", "取消" };
			int response = JOptionPane.showOptionDialog(this, "您确认要退出吗", "",
					JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					options, options[0]);
			if (response == 0) {
				System.exit(0);
			}
		}else if("restart".equals(command)) {
			restartGame();
		}else if("new".equals(command)){
			mainFrame.newGame();
		}else if("help".equals(command)){
			showHelpMessage(); // 调用新方法显示帮助信息
		}else if("win".equals(command)){
			showVictoryConditions();
		}else if("answer".equals(command)){	//ly显示路径
			//findPath();
			showCorrectPath();
		}
	}

	// 打印操作提示
	private void showHelpMessage() {
		// 创建帮助窗口
		JWindow helpWindow = new JWindow(mainFrame);
		helpWindow.setSize((int) (width * 0.4), (int) (height * 0.1));
		helpWindow.setLocation(width / 2 - helpWindow.getWidth() / 2, height / 2 - helpWindow.getHeight() / 2);

		// 添加内容到面板
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0, 180)); // 半透明背景
		panel.setLayout(new BorderLayout());

		JLabel helpLabel = new JLabel("通过键盘的上下左右(↑↓←→或WSAD)来移动", SwingConstants.CENTER);
		helpLabel.setFont(new Font("思源宋体", Font.ITALIC, 18));
		helpLabel.setForeground(Color.WHITE); // 设置文字颜色
		panel.add(helpLabel, BorderLayout.CENTER);

		helpWindow.setContentPane(panel);
		helpWindow.setVisible(true);

		// 渐变效果
		Timer helpDeadTimer;
		AtomicInteger alpha = new AtomicInteger(255); // 起始透明度

		helpDeadTimer = new Timer(50, e -> {
			if (alpha.get() > 0) {
				alpha.addAndGet(-5); // 每次减少透明度
				helpWindow.setOpacity(alpha.get() / 255f); // 调整窗口整体透明度
			} else {
				((Timer) e.getSource()).stop(); // 停止定时器
				helpWindow.dispose(); // 销毁窗口
			}
		});
		helpDeadTimer.start();
	}

	// 打印胜利条件
	private void showVictoryConditions() {
		String msg = "";
		if (Objects.equals(mainFrame.mode, "『普通模式』")) msg = "找寻路径走到迷宫出口！";
		else if (Objects.equals(mainFrame.mode, "『迷雾模式』")) msg = "在重重迷雾中摸索迷宫出口！";
		else msg = "找出迷宫出口，且不要被贝利亚逮捕两次！";

		// 创建窗口
		JWindow victoryConditionsWindow = new JWindow(mainFrame);
		victoryConditionsWindow.setSize((int) (width * 0.4), (int) (height * 0.1));
		victoryConditionsWindow.setLocation(width / 2 - victoryConditionsWindow.getWidth() / 2, height / 2 - victoryConditionsWindow.getHeight() / 2);

		// 添加不透明的 JLabel 到 JPanel 中
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0, 180)); // 半透明背景
		panel.setLayout(new BorderLayout());

		JLabel victoryConditionsLabel = new JLabel(msg, SwingConstants.CENTER);
		victoryConditionsLabel.setFont(new Font("幼圆", Font.PLAIN, 18));
		victoryConditionsLabel.setForeground(Color.WHITE); // 确保文字可见
		panel.add(victoryConditionsLabel, BorderLayout.CENTER);

		victoryConditionsWindow.setContentPane(panel);
		victoryConditionsWindow.setVisible(true);

		// 透明度渐变效果
		Timer vcDeadTimer;
		AtomicInteger alpha = new AtomicInteger(255); // 起始透明度

		vcDeadTimer = new Timer(50, e -> {
			if (alpha.get() > 0) {
				alpha.addAndGet(-5); // 每次减少透明度
				victoryConditionsWindow.setOpacity(alpha.get() / 255f); // 调整窗口整体透明度
			} else {
				((Timer) e.getSource()).stop(); // 停止定时器
				victoryConditionsWindow.dispose(); // 销毁窗口
			}
		});
		vcDeadTimer.start();
	}

	// 游戏开始
	public void startGame() {
		if (!"start".equals(gameFlag)) {
			// 显示提示信息（仅首次启动）
			if ("pause".equals(gameFlag)) {
				showVictoryConditions(); // 显示胜利条件
				if ("『迷雾追逐模式』".equals(mainFrame.mode)) {
					showChaserMessage(); // 显示追逐者信息
				}
			}

			// 设置游戏状态为启动
			gameFlag = "start";

			// 初始化计时器并启动
			if (timer == null) {
				timer = new Timer(1000, e -> {
					elapsedSeconds++; // 每秒递增
					drawStatus(getGraphics()); // 更新状态面板

					// 追逐逻辑启动条件
					if ((elapsedSeconds >= 5) && chasing) {
						startChasing();
					}
				});
			}

			// 启动计时器
			timer.start();
			System.out.println("Game started!");
		} else {
			System.out.println("Game is already running!");
		}

		// 确保主窗口获得焦点
		mainFrame.requestFocus();
	}



	// 重置迷宫——新游戏
	public void resetGame() {
		gameFlag = "pause"; // 暂停状态
		showPath = false;
		jmi5.setSelected(false);
		resetChaser();
		setBlockSide();
		start.curSteps = 0;

		updateStartButton("开始");

		hasFog();
		hasChaser();
		if ("『迷雾追逐模式』".equals(mainFrame.mode)) {
			chasing = true; // 确保追逐逻辑标志位被设置
			hasRevival = true; // 开启复活机制
		}

		elapsedSeconds = 0; // 重置计时变量
		if (timer != null) {
			timer.stop(); // 重新启动计时器
		} else {
			timer = new Timer(1000, e -> {
				elapsedSeconds++;
				drawStatus(getGraphics()); // 每秒更新状态面板
			});
			timer.start(); // 初始化并启动计时器
		}

		// 重置迷宫
		createBlocks();
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				blocks[i][j].setVisited(false);
				blocks[i][j].walls[0] = true;
				blocks[i][j].walls[1] = true;
				blocks[i][j].walls[2] = true;
				blocks[i][j].walls[3] = true;
			}
		}

		computed();
		start.setI(0);
		start.setJ(0);
		end.setI(ROWS - 1);
		end.setJ(COLS - 1);
		findPath();
		repaint();

		drawStatus(getGraphics()); // 更新状态面板
	}

	// 重置迷宫——重新开始本局
	public void restartGame() {
		gameFlag = "pause"; // 暂停状态
		showPath = false;
		jmi5.setSelected(false);
		resetChaser();
		start.curSteps = 0;

		updateStartButton("开始");

		hasFog();
		hasChaser();
		if ("『迷雾追逐模式』".equals(mainFrame.mode)) {
			chasing = true; // 确保追逐逻辑标志位被设置
			hasRevival = true; // 开启复活机制
		}

		elapsedSeconds = 0; // 重置计时变量
		if (timer != null) {
			timer.stop(); // 重新启动计时器
		} else {
			timer = new Timer(1000, e -> {
				elapsedSeconds++;
				drawStatus(getGraphics()); // 每秒更新状态面板
			});
			timer.start(); // 初始化并启动计时器
		}


		// 仅重置访问状态，不改变墙的布局
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				blocks[i][j].setVisited(false);
			}
		}

		start.setI(0);
		start.setJ(0);
		findPath();
		repaint();

		drawStatus(getGraphics()); // 更新状态面板
	}

	private void updateStartButton(String text) {
		if (startButton != null) {
			startButton.setFont(new Font("幼圆", Font.PLAIN, 20));
			startButton.setText(text);
		}
	}


	// 重置追逐者状态
	private void resetChaser() {
		chasing = false; // 禁用追逐者状态
		chaser = null;   // 清除追逐者位置
		chaserIcon = null; // 清除追逐者图标
		chaserDist = 0;   // 重置步数距离
	}


	// 初始化图标
	private void initializeGameIcons() {
		if (victoryIcon == null) victoryIcon = new ImageIcon("imgs/victoryIcon.png");
		if (medalIcon == null) medalIcon = new ImageIcon("imgs/medalIcon.png");
		if (overIcon == null) overIcon = new ImageIcon("imgs/overIcon.png");
		if (fingerIcon == null) fingerIcon = new ImageIcon("imgs/fingerIcon.png");
		if (cheeseIcon == null) cheeseIcon = new ImageIcon("imgs/cheeseIcon.png");

		iconWidth = 50;
		iconHeight = 50;

		targetWidth = (int) (getWidth() / 6);
		targetHeight = (int) (getHeight() / 6);

		int spacing = 20;
		xPositions = new int[]{
				(getWidth() - 3 * targetWidth - 2 * spacing) / 2,
				(getWidth() - 3 * targetWidth - 2 * spacing) / 2 + targetWidth + spacing,
				(getWidth() - 3 * targetWidth - 2 * spacing) / 2 + 2 * (targetWidth + spacing)
		};
		yPosition = (getHeight() - targetHeight) / 2;
	}


	private void enlargeIcons() {
		if (enlargeTimer != null && enlargeTimer.isRunning()) {
			enlargeTimer.stop();
			enlargeTimer = null; // 释放引用，方便垃圾回收
		}

		enlargeTimer = new Timer(30, e -> {
			if (iconWidth < targetWidth && iconHeight < targetHeight) {
				iconWidth += 5;
				iconHeight += 5;
				repaint();
			} else {
				enlargeTimer.stop();
				enlargeTimer = null; // 定时器完成后释放
				newGameOptions();
			}
		});
		enlargeTimer.start();

	}

	//游戏胜利
	// 游戏胜利
	public void gameWin() {
		gameFlag = "win";
		showPath = true;
		fogOfWar = false;
		showCorrectPath(); // 显示路径（根据需求可以去除）
		timer.stop();
		stopChasing(); // 停止追逐

		// 加载并放大图标
		initializeGameIcons();
		enlargeIcons(); // 启动图标放大逻辑

		// 弹出结束提示
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("幼圆", Font.PLAIN, 18)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("幼圆", Font.PLAIN, 18)));
	}


	// 游戏失败
	public void gameOver() {
		gameFlag = "over";
		showPath = true;
		fogOfWar = false;
		showCorrectPath(); // 显示路径
		timer.stop();
		stopChasing(); // 停止追逐

		// 加载并放大图标
		initializeGameIcons();
		enlargeIcons(); // 启动图标放大逻辑

		// 弹出结束提示
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("幼圆", Font.PLAIN, 18)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("幼圆", Font.PLAIN, 18)));
	}


	private void newGameOptions() {
		String message = gameFlag == "win" ? "你胜利了,太棒了!\n是否要挑战其他模式？"
				: "你害奶龙被贝利亚撕成奶酪了！\n试试别的模式吧?";
		Object[] options = {"是", "重新挑战"};
		int result = JOptionPane.showOptionDialog(
				mainFrame,
				message,
				"游戏结束",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				options,
				options[0]
		);
		if (result == JOptionPane.YES_OPTION) {
			System.out.println("用户选择了挑战其他模式！");
			mainFrame.newGame();
		} else if (result == JOptionPane.NO_OPTION) {
			restartGame();
			System.out.println("用户重新挑战当前迷宫！");
		}
	}

	private void stopTimer() {
		if (timer != null)
			timer.stop();
		if (chaseTimer != null)
			chaseTimer.stop();
		if (revivalBlinkTimer != null)
			revivalBlinkTimer.stop();
		if (enlargeTimer != null)
			enlargeTimer.stop();
	}

	public void setMazeSide(int mazeSide) {
		this.ROWS = mazeSide;
		this.COLS = mazeSide;
	}

	public int getMazeSide() {
		return ROWS;
	}
}
