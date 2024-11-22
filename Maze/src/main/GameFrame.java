package main;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

public class GameFrame extends JFrame {
	String mode;
	String modeDesc;	// 模式描述
	HashMap<String, Integer> map = new HashMap<>();
	BackgroundPanel backgroundPanel;
	GamePanel gamePanel;
	JLayeredPane layeredPane;
	JLayeredPane statusPane;
	int width;
	int height;
	int mazeSide;

	public GameFrame(String mode, int mazeSize) {
		this.mode = mode;
		this.mazeSide = mazeSize;
		setTitle(mode + "迷宫");

		ImageIcon titleIcon = new ImageIcon("imgs/titleIcon.png");
		setIconImage(titleIcon.getImage());

		// 初始化模式和图片映射
		map.put("『普通模式』", 0);
		map.put("『迷雾模式』", 1);
		map.put("『迷雾追逐模式』", 2);

		// 初始化背景面板并加载对应背景图片
		backgroundPanel = new BackgroundPanel("imgs/mazeBg" + map.getOrDefault(mode, 0) + ".jpg");

		// 初始化 GamePanel
		gamePanel = new GamePanel(this, mazeSide);

		describeMode();

		// 获取合适窗口大小
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = (int)(screenSize.width * 0.8);
		System.out.println(width);
		height = (int)(screenSize.height * 0.8);
		System.out.println(height);

		// 初始化 JLayeredPane
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension((int) (width * 0.7), height));
		statusPane = new JLayeredPane();
		statusPane.setPreferredSize(new Dimension((int) (width * 0.3), height));

		// 设置面板的位置和大小
		layeredPane.setBounds(0, 0, (int) (width * 0.7), height);
		statusPane.setBounds((int) (width * 0.7), 0, (int) (width * 0.3), height);
		backgroundPanel.setBounds(0, 0, (int) (width * 0.7), height);
		gamePanel.setBounds(0, 0, (int) (width * 0.7), height);

		// 将面板添加到 JLayeredPane
		layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(gamePanel, JLayeredPane.PALETTE_LAYER); // GamePanel 显示在上层

		// 设置 JFrame 的内容面板为 JLayeredPane
		this.add(layeredPane);
		this.add(statusPane);


		setSize(width, height);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		// 650, 480
	}

	// 添加一个 restart newGame 方法，避免每次重启都重新创建实例
	public void newGame() {
		setFont(new Font("幼圆", Font.PLAIN, 18));

		// 1. 弹出选择游戏模式的对话框
		Object[] options = {"『普通模式』", "『迷雾模式』", "『迷雾追逐模式』", "取消"};
		int modeChoice = JOptionPane.showOptionDialog(
				this,
				"请选择游戏模式：",
				"新游戏",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]
		);

		// 检查用户是否点击了取消
		if (modeChoice == -1 || modeChoice == 3) {
			return; // 用户取消操作，直接返回
		}

		// 设置当前模式
		this.mode = options[modeChoice].toString();
		setTitle(this.mode + "迷宫");

		// 2. 弹出输入框获取迷宫边长
		int defaultSize = gamePanel.getMazeSide(); // 获取当前迷宫大小作为默认值
		String input = (String) JOptionPane.showInputDialog(
				this,
				"请输入迷宫大小（边长4-100，默认 " + defaultSize + "）：",
				"新游戏",
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				defaultSize
		);

		// 检查用户是否点击取消或输入为空
		if (input == null) {
			return;
		}

		if (input.trim().isEmpty()) {
			input = String.valueOf(defaultSize); // 使用默认值
		}

		int mazeSide;
		try {
			mazeSide = Integer.parseInt(input.trim());

			if (mazeSide < 4 || mazeSide > 100) {
				// 如果输入超出合理范围，提示并使用默认值
				JOptionPane.showMessageDialog(
						this,
						"输入值不在合理范围内（4-100），将使用默认值 " + defaultSize,
						"提示",
						JOptionPane.WARNING_MESSAGE
				);
				mazeSide = defaultSize;
			}
			if (mazeSide <= 0) throw new NumberFormatException(); // 确保输入为正数
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(
					this,
					"输入无效，请输入一个正整数！",
					"错误",
					JOptionPane.ERROR_MESSAGE
			);
			return; // 输入无效，直接返回
		}

		// 3. 更新 `GamePanel` 的迷宫大小并重新生成迷宫
		gamePanel.setMazeSide(mazeSide);

		// 4. 更新背景面板
		layeredPane.remove(backgroundPanel); // 移除旧的背景面板
		backgroundPanel = new BackgroundPanel("imgs/mazeBg" + map.getOrDefault(this.mode, 0) + ".jpg");
		backgroundPanel.setBounds(0, 0, (int) (width * 0.7), height);
		layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER); // 添加新的背景面板

		// 5. 重置 `GamePanel` 状态并开始新游戏
		gamePanel.resetGame();
		describeMode();

		// 重新布局并重绘窗口
		layeredPane.revalidate();
		layeredPane.repaint();
	}


	private void describeMode() {
		if (Objects.equals(this.mode, "『普通模式』")) {
			modeDesc = "白白净净的迷宫等着你探索！";
		} else if (Objects.equals(this.mode, "『迷雾模式』")) {
			modeDesc = "在有限视野中摸索着完成迷宫!";
		} else {
			modeDesc = "穿越迷雾的同时还要避开追逐!";
		}
		// 更新 GamePanel 中的模式描述标签
		gamePanel.updateModeDescriptionLabel();
	}


	// 内部类，用于加载和绘制背景图片
	private class BackgroundPanel extends JPanel {
		Image backgroundImage;

		public BackgroundPanel(String imagePath) {
			backgroundImage = new ImageIcon(imagePath).getImage();
		}


		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (backgroundImage != null) {
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

				// 绘制半透明黑色覆盖
				g.setColor(new Color(0, 0, 0, 120)); // 最后一个参数是透明度，范围0-255
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}
}
