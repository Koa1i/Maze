package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class InitialFrame extends JFrame {
    private boolean buttonsCreated = false; // 标识按钮是否已创建
    private JLabel hintLabel; // 用于显示提示文字
    private JLabel blinkingTextLabel;   // 用于显示闪烁文字的标签
    private JLabel textLabel; // 用于显示模式描述
    private Timer blinkTimer; // 控制文字闪烁

    public InitialFrame() {
        setTitle("『迷宫游戏』");
        ImageIcon titleIcon = new ImageIcon("imgs/endIcon.png");
        setIconImage(titleIcon.getImage());
        setSize(430, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setLayout(null); // 使用绝对布局，以便更灵活地放置组件

        // 创建并设置背景面板
        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setBounds(0, 0, 430, 480);
        bgPanel.setLayout(null); // 背景面板使用绝对布局
        add(bgPanel);

        // 创建用于显示模式描述的标签
        textLabel = new JLabel("", SwingConstants.CENTER);
        textLabel.setFont(new Font("幼圆", Font.ITALIC | Font.BOLD, 14));
        textLabel.setForeground(Color.BLACK);
        textLabel.setBounds(0, 420, 430, 30); // 放置在窗口底部
        bgPanel.add(textLabel);

        // 设置 hintLabel 背景为图片，并创建文字标签
        hintLabel = new JLabel(new ImageIcon("imgs/initialBg.jpeg"));
        hintLabel.setBounds(0, 420, 430, 30); // 放置在窗口底部
        hintLabel.setLayout(new BorderLayout()); // 设置布局，以便放置文字标签
        bgPanel.add(hintLabel);

        // 创建用于显示闪烁文字的标签
        blinkingTextLabel = new JLabel("点击按钮或按任意键开始", SwingConstants.CENTER);
        blinkingTextLabel.setFont(new Font("幼圆", Font.ITALIC | Font.BOLD, 14));
        blinkingTextLabel.setOpaque(false); // 设置为透明，仅显示文字
        hintLabel.add(blinkingTextLabel, BorderLayout.CENTER); // 将文字标签放在 hintLabel 中心

        // 设置文字闪烁效果
        blinkTimer = new Timer(500, e -> blinkingTextLabel.setVisible(!blinkingTextLabel.isVisible()));
        blinkTimer.start();

        // 添加键盘和鼠标监听器
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { // 按任意键继续
                startGame();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { // 鼠标任意键点击
                startGame();
            }
        });

        setFocusable(true); // 让窗口可以接收键盘事件
        requestFocusInWindow();
    }

    private void startGame() {
        blinkingTextLabel.setVisible(false);
        blinkTimer.stop();
        ((BackgroundPanel) getContentPane().getComponent(0)).setShowStartText(false); // 设置标志位
        showModeButtons();
    }

    // 显示模式选择按钮
    private void showModeButtons() {
        if (!buttonsCreated) { // 避免重复创建按钮
            createModeButtons();
            buttonsCreated = true;
            repaint(); // 刷新窗口
            revalidate(); // 重绘组件布局
        }
    }

    // 创建并添加模式选择按钮
    private void createModeButtons() {
        JButton mode1Button = new JButton("『普通模式』");
        JButton mode2Button = new JButton("『迷雾模式』");
        JButton mode3Button = new JButton("『迷雾追逐模式』");

        // 设置按钮大小和位置
        mode1Button.setBounds(145, 160, 135, 40);
        mode2Button.setBounds(145, 210, 135, 40);
        mode3Button.setBounds(145, 260, 135, 40);

        // 添加鼠标监听器来显示模式描述
        mode1Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                textLabel.setText("白白净净的迷宫等着你探索！");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                textLabel.setText("");
            }
        });

        mode2Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                textLabel.setText("在有限视野中摸索着完成迷宫!");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                textLabel.setText("");
            }
        });

        mode3Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                textLabel.setText("穿越迷雾的同时还要避开追逐!");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                textLabel.setText("");
            }
        });

        // 添加按钮点击事件，点击后进入对应模式的 GameFrame
        mode1Button.addActionListener(e -> openGameFrame("『普通模式』"));
        mode2Button.addActionListener(e -> openGameFrame("『迷雾模式』"));
        mode3Button.addActionListener(e -> openGameFrame("『迷雾追逐模式』"));

        // 将按钮添加到背景面板
        JPanel bgPanel = (JPanel) getContentPane().getComponent(0); // 获取背景面板
        bgPanel.add(mode1Button);
        bgPanel.add(mode2Button);
        bgPanel.add(mode3Button);

        // 刷新窗口以显示新按钮
        repaint();
        revalidate();
    }

    // 打开对应的 GameFrame 窗口
    private void openGameFrame(String mode) {
        GameFrame gameFrame = new GameFrame(mode);
        //GamePanel gamePanel = new GamePanel(gameFrame);
        //gameFrame.add(gamePanel);
        gameFrame.setVisible(true);
        this.setVisible(false); // 隐藏当前 InitialFrame
    }

    // 自定义 JPanel 用于绘制背景图片
    private class BackgroundPanel extends JPanel {
        private Image bgImg = Toolkit.getDefaultToolkit().getImage("imgs/initialBg.jpeg");
        private boolean showStartText = true; // 新增标志位，控制是否显示“开始”

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);

            if (showStartText) { // 仅当标志位为true时显示“开始”
                drawWord(g, 33, Color.GREEN, "开始", 180, 220);
            }
        }

        // 设置标志位的方法
        public void setShowStartText(boolean showStartText) {
            this.showStartText = showStartText;
            repaint(); // 重新绘制背景面板
        }
    }

    // 绘制字符串
    public static void drawWord(Graphics g, int size, Color cl, String str, int x, int y) {
        g.setColor(cl);
        g.setFont(new Font("楷体", Font.PLAIN, 32));
        g.drawString(str, x, y);
    }
}
