package main;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class GameFrame extends JFrame {
	String mode;
	HashMap<String, Integer> map = new HashMap<>();
	BackgroundPanel backgroundPanel;

	public GameFrame(String mode) {
		this.mode = mode;
		setTitle(mode + "迷宫");

		ImageIcon titleIcon = new ImageIcon("imgs/endIcon.png");
		setIconImage(titleIcon.getImage());

		// 初始化模式和图片映射
		map.put("『普通模式』", 0);
		map.put("『迷雾模式』", 1);
		map.put("『迷雾追逐模式』", 2);

		// 创建背景面板并加载对应背景图片
		backgroundPanel = new BackgroundPanel("imgs/mazeBg" + map.getOrDefault(mode, 0) + ".jpg");

		// 创建 GamePanel
		GamePanel gamePanel = new GamePanel(this);

		// 创建 JLayeredPane
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(430, 480));

		// 设置面板的位置和大小
		backgroundPanel.setBounds(0, 0, 430, 480);
		gamePanel.setBounds(0, 0, 430, 480);

		// 将面板添加到 JLayeredPane
		layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(gamePanel, JLayeredPane.PALETTE_LAYER); // GamePanel 显示在上层

		// 设置 JFrame 的内容面板为 JLayeredPane
		setContentPane(layeredPane);

		setSize(430, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
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
