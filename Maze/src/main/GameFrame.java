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

	public GameFrame(String mode) {
		this.mode = mode;
		setTitle(mode + "迷宫");

		ImageIcon titleIcon = new ImageIcon("imgs/endIcon.png");
		setIconImage(titleIcon.getImage());

		// 初始化模式和图片映射
		map.put("『普通模式』", 0);
		map.put("『迷雾模式』", 1);
		map.put("『迷雾追逐模式』", 2);

		// 初始化背景面板并加载对应背景图片
		backgroundPanel = new BackgroundPanel("imgs/mazeBg" + map.getOrDefault(mode, 0) + ".jpg");

		// 初始化 GamePanel
		gamePanel = new GamePanel(this);

		describeMode();

		// 初始化 JLayeredPane
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(430, 480));
		statusPane = new JLayeredPane();
		statusPane.setPreferredSize(new Dimension(170, 480));

		// 设置面板的位置和大小
		layeredPane.setBounds(0, 0, 430, 480);
		statusPane.setBounds(430, 0, 220, 480);
		backgroundPanel.setBounds(0, 0, 430, 480);
		gamePanel.setBounds(0, 0, 430, 480);

		// 将面板添加到 JLayeredPane
		layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(gamePanel, JLayeredPane.PALETTE_LAYER); // GamePanel 显示在上层

		// 设置 JFrame 的内容面板为 JLayeredPane
		this.add(layeredPane);
		this.add(statusPane);

		setSize(650, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	// 添加一个 restart 方法，避免每次重启都重新创建实例
	public void restart() {
		// 1. 获取选择的模式
		Object[] options = {"『普通模式』", "『迷雾模式』", "『迷雾追逐模式』"};
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
		// 2. 设置当前模式
		this.mode = options[modeChoice].toString();
		setTitle(this.mode + "迷宫");

		// 更新 `BackgroundPanel` 的背景图片路径
		layeredPane.remove(backgroundPanel); // 移除旧的背景面板
		backgroundPanel = new BackgroundPanel("imgs/mazeBg" + map.getOrDefault(this.mode, 0) + ".jpg");
		backgroundPanel.setBounds(0, 0, 430, 480);
		layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER); // 添加新的背景面板

		// 3. 重置 `GamePanel` 中的状态
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
