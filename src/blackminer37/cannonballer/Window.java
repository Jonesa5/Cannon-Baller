package blackminer37.cannonballer;

import blackminer37.cannonballer.util.Skills;
import blackminer37.cannonballer.util.Timer;
import org.powbot.walking.model.Skill;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.ClientAccessor;

import javax.swing.*;
import java.awt.*;

public class Window extends ClientAccessor {
    private JFrame frame;
    private boolean updateButtonWaiting = false;

    private final Main main;
    private final Timer timer;
    private final Skills skill;
    private int xpPer = 1;
    private int xpHr = 1, expTil = 1;

    public Window(ClientContext ctx, Main main) {
        super(ctx);
        this.main = main;
        this.skill = new Skills(ctx, Skill.Smithing.getIndex());
        this.timer = new Timer();
    }

    /**
     * This updates the information in the JFrame window.
     */
    public void updateWindow() {
        timeLabel.setText("" + timer.getFormattedTimeFromAdjustedStart());

        smithedLabel.setText("Cannonballs: " + main.smithed());

        int level = ctx.skills.realLevel(Skill.Smithing.getIndex());
        prevLevelLabel.setText("" + level);
        nextLevLabel.setText("" + (level + 1));

        expTil = ctx.skills.experienceAt(level + 1) - ctx.skills.experience(Skill.Smithing.getIndex());
        progressBar.setValue(map(ctx.skills.experience(Skill.Smithing.getIndex()), ctx.skills.experienceAt(level), ctx.skills.experienceAt(level + 1), 0, 100));
        xpTillLabel.setText("" + expTil);

        try {
            int sellPer = Integer.parseInt(gpSellTextField.getText());
            int gpPer = Integer.parseInt(gpPerTextField.getText());
            int profit = (sellPer * 4) - gpPer;
            profitLabel.setText("Profit: " + profit + " (" + (profit * (main.smithed() / 4)) + ")");
        } catch(Exception e) { System.out.println("|Cannon-Baller| Window.updateWindow() TryCatch ERROR!"); }

        xpHr = skill.getExperiencePerHour();
        xpHrLabel.setText("xp/hr " + xpHr);
    }

    /**
     * This puts a number (num) on a scale of min and max.
     * Example: (num=6, nMin=0, nMax=10, min=0, max=100)
     * This will return 60 as the number 6 is scaled up to be between 0 and 100;
     * <p>
     * This also works the other way around:
     * Example: (num=60, nMin=0, nMax=100, min=0, max=10)
     * This will return 6 as the number 60 is scaled down to be between 0 and 10;
     * <p>
     * As well as backwards:
     * Example: (num=6, nMin=0, nMax=10, min=100, max=0)
     * This will return 40 as the number 6 is scaled up to be between 0 and 100 and then the remainder is used;
     * <p>
     * As well as with a complicated scale:
     * Example: (num=6, nMin=2, nMax=7, min=12, max=43)
     * This will return 37 (36.8 rounded up) as the number 6 scaled up to be between 12 and 43;
     * <p>
     * ******
     * NOTE: This can easily be modified to return double values by changing all the Integers as well as the return type to double.
     * ******
     *
     * @param num  The number that is being scaled
     * @param nMin The minimum value that num could be
     * @param nMax The maximum value that num could be
     * @param min  The minimum value that num can be scaled to
     * @param max  The maximum value that num can be scaled to
     * @return The num scaled to be between the min and max values
     */
    private int map(int num, int nMin, int nMax, int min, int max) {
        return (num - nMin) * (max - min) / (nMax - nMin) + min;
    }

    /**
     * This initiates the JFrame window and it's components.
     * The components are:
     *         nameLabel - JLabel();
     *         smithedLabel - JLabel();
     *         timeLabel - JLabel();
     *         profitLabel - JLabel();
     *         gpPerTextField - JTextField();
     *         gpPerLabel - JLabel();
     *         gpSellTextField - JTextField();
     *         gpSellLabel - JLabel();
     *         barsTextField - JTextField();
     *         barsLabel - JLabel();
     *         progressBar - JProgressBar();
     *         prevLevelLabel - JLabel();
     *         nextLevLabel - JLabel();
     *         xpTillLabel - JLabel();
     *         xpHrLabel - JLabel();
     *         ttlLabel - JLabel();
     *         updateButton - JButton();
     *         separator - JSeparator();
     *         runningLabel - JLabel();
     */
    public void initComponents() {
        JLabel nameLabel = new JLabel();
        smithedLabel = new JLabel();
        timeLabel = new JLabel();
        profitLabel = new JLabel();
        gpPerTextField = new JTextField();
        JLabel gpPerLabel = new JLabel();
        barsTextField = new JTextField();
        JLabel barsLabel = new JLabel();
        progressBar = new JProgressBar();
        prevLevelLabel = new JLabel();
        nextLevLabel = new JLabel();
        xpTillLabel = new JLabel();
        xpHrLabel = new JLabel();
        ttlLabel = new JLabel();
        gpSellTextField = new JTextField();
        JLabel gpSellLabel = new JLabel();
        updateButton = new JButton();
        JSeparator separator = new JSeparator();
        runningLabel = new JLabel();

        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setTitle("Cannon-Baller");
        frame.setAlwaysOnTop(true);
        frame.setBackground(new Color(50, 50, 100));
        frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        frame.setMaximumSize(new Dimension(190, 180));
        frame.setMinimumSize(new Dimension(190, 180));
        frame.setName("frame"); // NOI18N
        frame.setResizable(false);

        nameLabel.setFont(new Font("AR Julian", Font.PLAIN, 28)); // NOI18N
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setText("Cannon-Baller");

        smithedLabel.setText("Smithed: 0");

        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setText("00:00:00");
        timeLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        profitLabel.setText("Profit: 240 (0)");

        gpPerTextField.setText("400");

        gpPerLabel.setText("gp Buy");

        gpSellTextField.setText("160");

        gpSellLabel.setText("gp Sell ea");

        barsTextField.setText("100000");

        barsLabel.setText("Max Bars");

        progressBar.setValue(0);

        prevLevelLabel.setText("98");

        nextLevLabel.setText("99");

        xpTillLabel.setHorizontalAlignment(SwingConstants.CENTER);
        xpTillLabel.setText("0");

        xpHrLabel.setText("0 XP/hr");

        ttlLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ttlLabel.setText("TTL: 00:00:00");

        updateButton.setText("Update Max");
        updateButton.addActionListener(e -> {
            updateButton.setText("Waiting...");
            updateButton.setEnabled(false);
            updateButtonWaiting = true;
        });

        runningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        runningLabel.setText("Running");

        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(separator)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(gpSellTextField, GroupLayout.Alignment.LEADING)
                                                        .addComponent(gpPerTextField, GroupLayout.Alignment.LEADING))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(gpPerLabel)
                                                        .addComponent(gpSellLabel))
                                                .addGap(19, 19, 19)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(barsTextField, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(barsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                        .addComponent(updateButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(smithedLabel)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addComponent(profitLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(timeLabel)
                                                                .addGap(22, 22, 22))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(18, 18, 18)
                                                                .addComponent(runningLabel, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(xpHrLabel, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(ttlLabel))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(prevLevelLabel)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(xpTillLabel, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(nextLevLabel))
                                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(nameLabel, GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE))
                                                        .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 185, GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(nameLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(smithedLabel)
                                        .addComponent(timeLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(profitLabel)
                                        .addComponent(runningLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gpPerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gpPerLabel)
                                        .addComponent(barsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(barsLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gpSellTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gpSellLabel)
                                        .addComponent(updateButton, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(nextLevLabel)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(prevLevelLabel)
                                                .addComponent(xpTillLabel)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(xpHrLabel)
                                        .addComponent(ttlLabel))
                                .addGap(16, 16, 16))
        );
        frame.pack();
        frame.setVisible(true);
    }

    private JLabel smithedLabel;
    private JTextField gpPerTextField;
    private JTextField gpSellTextField;
    private JTextField barsTextField;
    private JLabel nextLevLabel;
    private JLabel prevLevelLabel;
    private JLabel profitLabel;
    private JProgressBar progressBar;
    private JLabel runningLabel;
    private JLabel timeLabel;
    private JLabel ttlLabel;
    private JButton updateButton;
    private JLabel xpHrLabel;
    private JLabel xpTillLabel;

    public JFrame frame() { return frame; }

    public JLabel runningLabel() { return runningLabel; }

    public JTextField barsTextField() { return barsTextField; }

    public JButton updateButton() { return updateButton; }
    public boolean updateButtonWaiting() { return updateButtonWaiting; }
    public void updateButtonWaiting(boolean wait) { updateButtonWaiting = wait; }

    public int xpPer() { return xpPer; }

    public void setXpPer(int xpPer) { this.xpPer = xpPer; }

    public int xpHr() { return xpHr; }

    public int expTil() { return expTil; }

    public JLabel ttlLabel() { return ttlLabel; }

    public void gpPerTextField(String s) { gpPerTextField.setText(s); }
    public void gpSellPerTextField(String s) { gpSellTextField.setText(s); }

    public Timer timer() { return timer; }
}
