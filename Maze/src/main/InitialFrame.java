package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class InitialFrame extends JFrame {
    private boolean buttonsCreated = false; // 标识按钮是否已创建
    private JLabel hintLabel; // 用于显示提示文字
    private JLabel blinkingTextLabel;   // 用于显示闪烁文字的标签
    private JLabel textLabel; // 用于显示模式描述

    // 开头动画
    private JLabel playerLabel;
    private JLabel chaserLabel;
    private ImageIcon chaserIcon;
    private Timer playerTimer;
    private Timer chaserTimer;
    private int[] playerX;
    private int[] chaserX;
    private boolean isAnimated = false;

    private Timer blinkTimer; // 控制文字闪烁
    public int width;
    public int height;

    public InitialFrame() {
        setTitle("『迷宫游戏』");
        ImageIcon titleIcon = new ImageIcon("imgs/titleIcon.png");
        setIconImage(titleIcon.getImage());

        // 获取合适窗口大小
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int)(screenSize.width * 0.8);
        System.out.println(width);
        height = (int)(screenSize.height * 0.8);
        System.out.println(height);
        setSize(width, height);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setLayout(null); // 使用绝对布局，以便更灵活地放置组件

        // 创建并设置背景面板
        BackgroundPanel bgPanel = new BackgroundPanel();
        bgPanel.setBounds(0, 0, width, height);
        bgPanel.setLayout(null); // 背景面板使用绝对布局
        add(bgPanel);

        // 动画预备
        showPlayerIcon();

        // 创建用于显示模式描述的标签
        textLabel = new JLabel("", SwingConstants.CENTER);
        textLabel.setFont(new Font("幼圆", Font.ITALIC | Font.BOLD, 20));
        textLabel.setForeground(Color.BLACK);
        textLabel.setBounds(0, height - 80, width, 50); // 放置在窗口底部
        bgPanel.add(textLabel);

        // 设置 hintLabel 背景为图片，并创建文字标签
        hintLabel = new JLabel(new ImageIcon("imgs/mazeBg0.jpg"));
        hintLabel.setBounds(0, height - 80, width, 50); // 放置在窗口底部
        hintLabel.setLayout(new BorderLayout()); // 设置布局，以便放置文字标签
        bgPanel.add(hintLabel);

        // 创建用于显示闪烁文字的标签
        blinkingTextLabel = new JLabel("点 击 按 钮 或 按 任 意 键 开 始", SwingConstants.CENTER);
        blinkingTextLabel.setFont(new Font("幼圆", Font.ITALIC | Font.BOLD, 20));
        blinkingTextLabel.setForeground(Color.BLACK);
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

    private void showPlayerIcon() {
        JLayeredPane layeredPane = getLayeredPane(); // 获取主窗体的分层面板

        // 创建玩家图标
        ImageIcon playerIcon = new ImageIcon("imgs/playerIcon.png");

        // 获取原始图片
        Image playerImage = playerIcon.getImage();

        // 按照比例缩小至25%
        int newWidth = playerIcon.getIconWidth() / 4;
        int newHeight = playerIcon.getIconHeight() / 4;

        // 缩放图片
        Image scaledPlayerImage = playerImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // 创建新的缩放图标
        playerIcon = new ImageIcon(scaledPlayerImage);

        // 创建玩家标签
        playerLabel = new JLabel(playerIcon);
        playerLabel.setBounds(width / 2 - newWidth / 2, height / 2 - height / 10, newWidth, newHeight);
        layeredPane.add(playerLabel, JLayeredPane.PALETTE_LAYER); // 添加到调色板层


    }


    private void startGame() {
        blinkingTextLabel.setVisible(false); // 隐藏闪烁文字
        blinkTimer.stop();

        JLayeredPane layeredPane = getLayeredPane(); // 获取主窗体的分层面板
        // 创建追逐者图标
        if (chaserIcon == null) {
            chaserIcon = new ImageIcon("imgs/chaserIcon.png");
            chaserLabel = new JLabel(chaserIcon);
            chaserLabel.setBounds(-100, height / 2 - height / 10, chaserIcon.getIconWidth(), chaserIcon.getIconHeight());
            layeredPane.add(chaserLabel, JLayeredPane.PALETTE_LAYER);
        }

        // 创建水平翻转后的图标
        ImageIcon playerIcon = new ImageIcon("imgs/playerIcon.png");
        ImageIcon flippedPlayerIcon = flipImageHorizontally(playerIcon);

        // 获取原始图片
        Image playerImage = flippedPlayerIcon.getImage();

        // 按照比例缩小至25%
        int newWidth = flippedPlayerIcon.getIconWidth() / 4;
        int newHeight = flippedPlayerIcon.getIconHeight() / 4;

        // 缩放图片
        Image scaledPlayerImage = playerImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // 创建新的缩放图标
        playerIcon = new ImageIcon(scaledPlayerImage);

        // 更新 player 图标为翻转后的图标
        playerLabel.setIcon(playerIcon);

        // 添加一个停顿计时器
        new Timer(1000, new ActionListener() { // 停顿 1 秒
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop(); // 停止计时器

                // 开始移动玩家
                playerTimer.start();
            }
        }).start();


        repaint();

        // 启动移动动画
        if (!isAnimated)
            startAnimation();
    }

    private void startAnimation() {
        isAnimated = true;

        // 定义初始位置
        playerX = new int[]{width / 2 - 50}; // 玩家初始X位置
        chaserX = new int[]{-100};          // 追逐者初始X位置

        // 玩家向右移动的定时器
        playerTimer = new Timer(10, e -> {
            if (playerX[0] < width + 100) { // 玩家移动到右边界
                playerX[0] += 15; // 玩家移动速度
                playerLabel.setLocation(playerX[0], height / 2 - height / 10);
            } else {
                playerTimer.stop();
            }
        });

        // 追逐者向玩家移动的定时器
        chaserTimer = new Timer(10, new ActionListener() {
            private boolean stoppedForTwoSeconds = false; // 标记是否已经停留过两秒

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!stoppedForTwoSeconds) {
                    // 阶段1：追逐者移动到指定位置
                    if (chaserX[0] < width / 20) { // targetX 为窗口指定的停留位置
                        chaserX[0] += 5; // 调整初始移动速度
                        chaserLabel.setLocation(chaserX[0], height / 2 - 50);
                    } else {
                        // 到达目标位置后停1秒
                        stoppedForTwoSeconds = true;
                        ((Timer) e.getSource()).stop(); // 暂停当前计时器

                        // 两秒后恢复追逐
                        new Timer(1000, evt -> {
                            ((Timer) evt.getSource()).stop();
                            resumeChase();
                        }).start();
                    }
                }
            }
        });

        // 玩家初始停顿0.5秒的定时器
        new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                playerTimer.start(); // 两秒后启动玩家移动
            }
        }).start();

        // 确保在事件调度线程中启动追逐者动画
        SwingUtilities.invokeLater(chaserTimer::start);
    }


    // 定义恢复追逐逻辑
    private void resumeChase() {
        chaserTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 阶段2：追逐玩家
                if (chaserX[0] < playerX[0]) { // 继续追逐玩家
                    chaserX[0] += 18; // 追逐速度
                    chaserLabel.setLocation(chaserX[0], height / 2 - 50);
                } else {
                    // 玩家被追上，停止所有计时器
                    chaserTimer.stop();
                    playerTimer.stop();
                    removeIconsAndStartEnlarge(); // 移除图标并显示放大动画
                }
            }
        });
        chaserTimer.start();
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


    private void removeIconsAndStartEnlarge() {
        // 移除图标
        getContentPane().remove(playerLabel);
        getContentPane().remove(chaserLabel);
        repaint();

        // 启动放大动画
        BackgroundPanel bgPanel = (BackgroundPanel) getContentPane().getComponent(0);
        bgPanel.setShowStartText(false); // 隐藏“开始”文本

        // 启动放大动画
        bgPanel.startEnlargeAnimation();
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

        JButton mode1Button = new JButton("\uD83E\uDD13     普通模式    \uD83E\uDD13");  // 🤓
        JButton mode2Button = new JButton("\uD83D\uDE28     迷雾模式    \uD83D\uDE28");  // 😨
        JButton mode3Button = new JButton("\uD83D\uDE08    迷雾追逐模式  \uD83D\uDE08");    // 😈

        ImageIcon mode1Icon = new ImageIcon("imgs/mazeBg0.jpg");
        ImageIcon mode2Icon = new ImageIcon("imgs/mazeBg1.jpg");
        ImageIcon mode3Icon = new ImageIcon("imgs/mazeBg2.jpg");
        mode1Button.setIcon(mode1Icon);
        mode2Button.setIcon(mode2Icon);
        mode3Button.setIcon(mode3Icon);

        // 调整文字与图标位置
        mode1Button.setHorizontalTextPosition(SwingConstants.CENTER); // 文字水平居中
        mode1Button.setVerticalTextPosition(SwingConstants.CENTER);   // 文字垂直居中
        mode2Button.setHorizontalTextPosition(SwingConstants.CENTER);
        mode2Button.setVerticalTextPosition(SwingConstants.CENTER);
        mode3Button.setHorizontalTextPosition(SwingConstants.CENTER);
        mode3Button.setVerticalTextPosition(SwingConstants.CENTER);

        int xBias = width / 8;
        int yBias = height / 10;

        mode1Button.setFont(new Font("Microsoft", Font.BOLD, 20));
        mode2Button.setFont(new Font("Microsoft", Font.BOLD, 20));
        mode3Button.setFont(new Font("Microsoft", Font.BOLD, 20));

        mode1Button.setForeground(Color.MAGENTA);
        mode2Button.setForeground(Color.ORANGE);
        mode3Button.setForeground(Color.RED);

        // 设置按钮大小和位置
        mode1Button.setBounds(width / 2 - xBias, height / 2 - yBias, width / 4, height / 11);
        mode2Button.setBounds(width / 2 - xBias, height / 2, width / 4, height / 11);
        mode3Button.setBounds(width / 2 - xBias, height / 2 + yBias, width / 4, height / 11);

        // 添加鼠标监听器来显示模式描述
        mode1Button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                textLabel.setFont(new Font("幼圆", Font.ITALIC | Font.BOLD, 20));
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
                textLabel.setFont(new Font("幼圆", Font.ITALIC | Font.BOLD, 20));
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
                textLabel.setFont(new Font("幼圆", Font.ITALIC | Font.BOLD, 20));
                textLabel.setText("穿越迷雾的同时还要避开追逐!");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                textLabel.setText("");
            }
        });

        // 添加按钮点击事件，点击后进入对应模式的 GameFrame
        mode1Button.addActionListener(e -> getMazeSide("『普通模式』"));
        mode2Button.addActionListener(e -> getMazeSide("『迷雾模式』"));
        mode3Button.addActionListener(e -> getMazeSide("『迷雾追逐模式』"));



        // 将按钮添加到背景面板
        JPanel bgPanel = (JPanel) getContentPane().getComponent(0); // 获取背景面板
        bgPanel.add(mode1Button);
        bgPanel.add(mode2Button);
        bgPanel.add(mode3Button);

        // 刷新窗口以显示新按钮
        repaint();
        revalidate();
    }

    private void getMazeSide(String mode) {

        // 设置全局字体（影响所有 JOptionPane）
        UIManager.put("OptionPane.messageFont", new Font("幼圆", Font.PLAIN, 18));
        UIManager.put("OptionPane.buttonFont", new Font("幼圆", Font.PLAIN, 18));

        // 提供默认值
        int defaultSize = 20;
        int mazeSize;

        // 弹出输入框，提示用户输入迷宫大小
        String input = (String) JOptionPane.showInputDialog(
                this,
                "请输入迷宫大小（边长，建议 4-100，默认为 20）：",  // 提示信息
                "新游戏", // 设置标题
                JOptionPane.QUESTION_MESSAGE, // 提示框类型
                null, // 没有图标
                null, // 选择值
                defaultSize // 默认值（输入框的初始值）
        );

        // 验证用户输入是否为合法数字
        try {
            if (input == null) {
                // 用户取消
                return;
            } else if (input.trim().isEmpty()) {
                mazeSize = defaultSize;
            } else {
                mazeSize = Integer.parseInt(input.trim());
                if (mazeSize < 4 || mazeSize > 100) {
                    // 如果输入超出合理范围，提示并使用默认值
                    JOptionPane.showMessageDialog(
                            this,
                            "输入值不在合理范围内（4-100），将使用默认值 " + defaultSize,
                            "提示",
                            JOptionPane.WARNING_MESSAGE
                    );
                    mazeSize = defaultSize;
                }
            }
        } catch (NumberFormatException ex) {
            // 如果输入的不是数字，提示并使用默认值
            JOptionPane.showMessageDialog(
                    this,
                    "输入无效，必须是数字，将使用默认值 " + defaultSize,
                    "提示",
                    JOptionPane.WARNING_MESSAGE
            );
            mazeSize = defaultSize;
        }

        // 打开对应模式的游戏窗口
        openGameFrame(mode, mazeSize);
    }



    // 打开对应的 GameFrame 窗口
    private void openGameFrame(String mode, int mazeSize) {
        // 初始化游戏窗口，传入迷宫大小
        GameFrame gameFrame = new GameFrame(mode, mazeSize);
        gameFrame.setVisible(true);
        this.dispose(); // 关闭当前窗口
    }

    // 自定义 JPanel 用于绘制背景图片
    private class BackgroundPanel extends JPanel {
        private Image bgImg = Toolkit.getDefaultToolkit().getImage("imgs/mazeBg0.jpg");
        private Image startImg = Toolkit.getDefaultToolkit().getImage("imgs/initialStart.png");
        private boolean showStartText = true; // 是否显示“开始”
        private boolean enlargeStart = false; // 是否正在放大图片
        private double scaleFactor = 1.0; // 图片缩放因子

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);

            if (showStartText || enlargeStart) {
                // 计算缩放后的宽度和高度
                int newWidth = (int) (startImg.getWidth(this) * scaleFactor);
                int newHeight = (int) (startImg.getHeight(this) * scaleFactor);

                // 计算居中位置
                int x = (getWidth() - newWidth) / 2;
                int y = (getHeight() - newHeight) / 2;

                // 绘制缩放后的图片
                g.drawImage(startImg, x, y, newWidth, newHeight, this);
            }
        }

        public void startEnlargeAnimation() {
            enlargeStart = true;
            Timer enlargeTimer = new Timer(30, null); // 每30毫秒更新一次
            enlargeTimer.addActionListener(e -> {
                if (scaleFactor < 3) { // 缩放到窗口的1/3后停止
                    scaleFactor += 0.05; // 控制缩放速度
                    repaint();
                } else {
                    enlargeTimer.stop();
                    enlargeStart = false;
                    SwingUtilities.invokeLater(InitialFrame.this::showModeButtons); // 放大完成后显示模式按钮
                }
            });
            enlargeTimer.start();
        }



        public void setShowStartText(boolean showStartText) {
            this.showStartText = showStartText;
            repaint();
        }
    }
}
