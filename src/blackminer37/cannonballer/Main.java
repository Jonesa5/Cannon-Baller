package blackminer37.cannonballer;

import org.powbot.walking.model.Skill;
import org.powerbot.script.*;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Component;

import javax.swing.*;
import java.awt.*;

/**
 * @author BlackMiner37
 * @date July 17, 2021
 * @version 1.0.0
 * This script uses PowBot to control an Old School Runescape character
 * and automatically creates cannonballs for a profit as long as the character
 * has the required materials and skill level.
 */

@Script.Manifest(
        name = "Cannon-Baller",
        description = "Smiths cannonballs in Edgeville. " +
                "Start in Edgeville, and it will detect whether to start at the bank or go straight to the furnace. " +
                "As soon as the start button is pressed it will start running the bot. " +
                "A small amount of load time (usually 1-5 seconds) will happen and this is just for the window to pop up and for the bot to determine what needs to happen first.",
        version = "1.0.0",
        trialDays = 1L,
        priv = true,
        mobileReady = true
)

public class Main extends PollingScript<ClientContext> implements PaintListener {

    private JFrame frame;
    private final Tile BANK = new Tile(3096, 3494, 0);
    private final Tile SMELTER = new Tile(3109, 3499, 0);
    private final int INTERACTION_RANGE = 3;
    private final int MINIMAP_RANGE = 15;

    private final int AMMO_MOULD = 4;
    private final int STEEL_BAR = 2353;
    private final int CANNONBALL = 2;

    private final int WIDGET_CANNONBALL_ID = 270;
    private final int COMPONENT_CANNONBALL_ID = 14;
    private final int COMPONENT_CANNONBALL_SUB_ID = 29;
    private final int WIDGET_ALL_ID = 270;
    private final int COMPONENT_ALL_ID = 12;
    private final int COMPONENT_ALL_SUB_ID = 0;

    private boolean updateButtonWaiting = false, showTTL = true;
    private int smithed = 0, xpPer = 1;
    private int xpBefore;
    private Timer timer;
    private Skills skill;
    private int timerBefore = 0, idleTimer = 0;
    private int xpHr = 1, expTil = 1;

    /**
     * This is where the states are initialized and held.
     * The states are: BANK, SMITH, TRAVEL_TO, TRAVEL_FROM, WAIT
     * The default state is TRAVEL_FROM, but this is reset in the startSetUp method.
     */
    private enum STATE {
        BANK, SMITH, TRAVEL_TO, TRAVEL_FROM, WAIT
    }
    private STATE state = STATE.TRAVEL_FROM;

    /**
     * This is first method to be called when the bot is started.
     * This is where the set up method for the JFrame is called
     * as well as where the timer and skill tracker are initialized
     */
    @Override
    public void start() {
        System.out.println("|Cannon-Baller| Starting");
        initComponents();
        ctx.input.blocking(true);
        ctx.setForceSingleTap(false);
        ctx.setDismissRandoms(false);
        xpBefore = ctx.skills.experience(Skill.Smithing.getIndex());
        skill = new Skills(ctx, Skill.Smithing.getIndex());
        timer = new Timer();
        startSetUp();
        updateWindow();
    }

    /**
     * This is where the bot is stopped and the JFrame is closed.
     */
    @Override
    public void stop() {
        System.out.println("|Cannon-Baller| Stopping");
        runningLabel.setText("Stopped");
        ctx.input.blocking(false);
        ctx.controller.stop();
        frame.dispose();
        super.stop();
    }

    /**
     * This is called when the bot needs to be logged out and suspended.
     * This will not close the JFrame and it will keep the bot running in the background.
     * By suspending the bot rather then stopping it the data
     * that is being kept track of will still be there when the bot is resumed.
     */
    private void logOutAndSuspend() {
        System.out.println("|Cannon-Baller| Suspending");
        ctx.game.logout();
        ctx.controller.suspend();
        ctx.input.blocking(false);
    }

    /**
     * This is called at start up.
     * If the character does not have the required skill level then the script will end.
     * If the inventory is not open, then this will open it.
     * If the viewing angle isn't high enough then it will change it.
     * This also decides which state the bot should start with by checking the inventory for steel bars
     */
    private void startSetUp() {
        Condition.wait(() -> ctx.game.clientState() == Constants.GAME_LOADED, 500, 20);
        if (!ctx.players.local().inViewport()) {
            Component playButton = ctx.widgets.widget(378).component(80);
            Condition.wait(() -> playButton.visible() && playButton.click(), 250, 8);
            Condition.wait(() -> ctx.players.local().inViewport(), 500, 20);
        }
        if (ctx.skills.level(Skill.Smithing.getIndex()) < 35) {
            System.out.println("|Cannon-Baller| Smithing level is too low!");
            runningLabel.setText("Level too low!!");
            ctx.controller.suspend();
            ctx.input.blocking(false);
            return;
        }
        if (ctx.camera.pitch() < 99) ctx.camera.pitch(true);
        Condition.wait(() -> ctx.camera.pitch() == 99, 250, 16);
        if (ctx.game.tab() != Game.Tab.INVENTORY)
            Condition.wait(() -> ctx.game.tab(Game.Tab.INVENTORY), Random.nextInt(256, 394), 8);
        if (ctx.inventory.toStream().id(STEEL_BAR).isEmpty()) state = STATE.TRAVEL_FROM;
        else state = STATE.TRAVEL_TO;
    }

    /**
     * This is called once every second.
     * This is where the idle timer is being kept track of as well as where
     * the actions left and time-to-level is switched back and forth to be displayed.
     * If the bot is resumed from a suspended state this is where the bot is told to start running again.
     */
    private void tick() {
        timerBefore = timer.getTimeFromStartInSeconds();
        if (ctx.controller.isSuspended() && ctx.game.clientState() == Constants.GAME_LOADED) {
            runningLabel.setText("Running");
            ctx.controller.resume();
            timer.resume();
        }
        if (idleTimer >= Random.nextInt(6, 10)) {
            if (ctx.inventory.toStream().id(STEEL_BAR).isEmpty()) state = STATE.TRAVEL_FROM;
            else state = STATE.TRAVEL_TO;
            idleTimer = 0;
            return;
        }
        if (ctx.players.local().animation() == -1) idleTimer++;
            else idleTimer = 0;
        if (idleTimer > 0) runningLabel.setText("Idling: " + idleTimer);
        if (showTTL) ttlLabel.setText("" + timer.getFormattedTimeFromGivenTime((int) ((int) (expTil / (xpHr / 3600.0)) * 1000.0)));
            else ttlLabel.setText("Actions: " + (expTil / xpPer));
        showTTL = !showTTL;
        updateWindow();
    }

    /**
     * This is when the "Update Max" button is pressed.
     * This waits until the bank is open and then it detects how many bars
     * are left and adds it to the amount that has already been smithed.
     */
    private void updateMax() {
        if (updateButtonWaiting && ctx.bank.opened()) {
            barsTextField.setText(((smithed / 4) + ctx.bank.toStream().id(STEEL_BAR).first().stackSize()) + "");
            updateButton.setText("Update Max");
            updateButton.setEnabled(true);
            updateButtonWaiting = false;
        }
    }

    /**
     * This is the heart of the bot.
     * This is where the bot decides what to do based on it's current state.
     * This is also where the bot detects and closes a "Press to continue" message like when a level is gained.
     */
    @Override
    public void poll() {
        updateWindow();
        if (timer.getTimeFromStartInSeconds() > timerBefore) tick();
        if (ctx.game.clientState() == Constants.GAME_LOADED) { // The client is running and the player is logged in
            if (ctx.chat.canContinue()) { // If we have leveled up or started a dialog
                Condition.wait(() -> true, Random.nextInt(732, 3930), 1);
                updateWindow();
                Component component = ctx.widgets.widget(233).component(3);
                if (component.visible() && component.click()) // Then click continue
                    System.out.println("|Cannon-Baller| Clicked on continue");
                state = STATE.SMITH; // and go back to smithing (assumed that it will only happen during a level up)
            }
            switch (state) {
                case BANK: bankGoods();
                break;
                case TRAVEL_TO: travelTo();
                break;
                case SMITH: smithCannonballs();
                break;
                case TRAVEL_FROM: travelFrom();
                break;
                case WAIT: waitForSmithing();
                break;
            }
        } else {
            runningLabel.setText("Suspended");
            timer.pause();
            //logOutAndSuspend(); // The client is not running or the player is not logged in.
        }
    }

    /**
     * This is when the character is banking the cannonballs or other items in the inventory.
     * At this time there should be no steel bars left in the inventory but lots of cannonballs.
     */
    private void bankGoods() {
        System.out.println("|Cannon-Baller| Banking goods");
        runningLabel.setText("Banking");
        Bank bank = ctx.bank;
        Condition.wait(() -> bank.open() && bank.opened(), Random.nextInt(648, 1132), 5);
        updateMax();
        updateWindow();
        if (bank.currentTab() != 0) bank.currentTab(0); // Open the general tab
        if (ctx.inventory.toStream().isNotEmpty()) { // There is stuff in the inventory
            System.out.println("|Cannon-Baller| Banking all");
            int[] ids = {AMMO_MOULD, STEEL_BAR};
            bank.depositAllExcept(ids); // Bank everything expect the mould and steel bars
            //Condition.wait(() -> ctx.inventory.toStream().id(CANNONBALL).isEmpty(), Random.nextInt(648, 1132), 5);
            updateWindow();
        }
        Bank freshBank = ctx.bank; // Refresh the bank at this point to prevent bugs from depositing bars
        if (freshBank.toStream().id(STEEL_BAR).isEmpty()) { // There are no more steel bars to use
            System.out.println("|Cannon-Baller| Bank is out of steel bars!!");
            runningLabel.setText("No Bars!");
            freshBank.close();
            logOutAndSuspend();
            return; // So close the bank and log out
        }
        if (ctx.inventory.toStream().id(AMMO_MOULD).isEmpty()) freshBank.withdraw(AMMO_MOULD, 1); // There is no mould in the inventory
        System.out.println("|Cannon-Baller| Withdraw steel bars");
        if (!freshBank.withdrawModeQuantity(Bank.Amount.ALL)) freshBank.withdrawModeQuantity(Bank.Amount.ALL); // Change the bank mode to ALL to save time
            freshBank.withdraw(STEEL_BAR, Bank.Amount.ALL); // and withdraw the bars
        if (ctx.inventory.toStream().id(STEEL_BAR).isEmpty()) return; // If there are no bars in the inventory then try again
        Condition.wait(freshBank::close, Random.nextInt(648, 1132), 3);
        updateWindow();
        state = STATE.TRAVEL_TO; // Bars were withdrawn and its time to go to the smelter
    }

    /**
     * This is when the character is traveling to the furnace.
     * At this time the inventory should have steel bars, the ammo mould, and no cannonballs.
     */
    private void travelTo() {
        System.out.println("|Cannon-Baller| Traveling to the furnace");
        runningLabel.setText("To Furnace");
        double dist = ctx.players.local().tile().distanceTo(SMELTER);
        checkEnergy();
        if (dist < INTERACTION_RANGE) {
            state = STATE.SMITH;
            return;
        }
        if (dist > MINIMAP_RANGE) // the furnace is too far away for step, possible walked off for some reason?
            ctx.movement.walkTo(SMELTER.derive(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
        else ctx.movement.step(SMELTER.derive(Random.nextInt(-1, 1), Random.nextInt(-1, 1))); // the furnace is within range so step to it
        Condition.wait(() -> !ctx.players.local().inMotion() && ctx.players.local().tile().distanceTo(SMELTER) < INTERACTION_RANGE, Random.nextInt(648, 1132), 10);
        updateWindow();
        if (ctx.players.local().tile().distanceTo(SMELTER) > INTERACTION_RANGE) return; // If we are not in range then try to walk to the furnace again
        state = STATE.SMITH; // We are now in range and it's time to start smithing
    }

    /**
     * This is when the character is selecting the options to start smithing cannonballs.
     * This goes through interacting with the furnace and selecting the option to smith cannonballs.
     */
    private void smithCannonballs() {
        System.out.println("|Cannon-Baller| Interacting with Furnace");
        runningLabel.setText("Smithing");
        int[] ids = {AMMO_MOULD, STEEL_BAR};
        for (Integer i : ids) { // Check if we have all the items we need
            if (ctx.inventory.toStream().id(i).isEmpty()) {
                System.out.println("|Cannon-Baller| Required items are missing! " + i);
                state = STATE.TRAVEL_FROM; // We don't have them so go back to bank
                return;
            }
        }
        GameObject smelter = ctx.objects.toStream().within(INTERACTION_RANGE).name("Furnace").first();
        smelter.interact("Smelt");
        Widget cannonballWidget = ctx.widgets.widget(WIDGET_CANNONBALL_ID); // The cannonball widget menu
        Component cannonballButton = cannonballWidget.component(COMPONENT_CANNONBALL_ID).component(COMPONENT_CANNONBALL_SUB_ID);  // The cannonball button on the widget
        Condition.wait(cannonballButton::visible, Random.nextInt(648, 1132), 5);
        updateWindow();
        Component all = cannonballWidget.component(COMPONENT_ALL_ID).component(COMPONENT_ALL_SUB_ID); // The ALL button on the widget
        if (cannonballButton.visible()) {
            if (all.textureId() == 1545) { // -1 is selected, 1545 is not selected
                System.out.println("|Cannon-Baller| Selecting All");
                all.click();
                Condition.wait(() -> ctx.widgets.widget(WIDGET_ALL_ID).component(COMPONENT_ALL_ID).component(COMPONENT_ALL_SUB_ID).textureId() == 1545, Random.nextInt(648, 1132), 5);
                updateWindow(); // The ALL button has been pressed
            }
            System.out.println("|Cannon-Baller| Smithing cannonballs");
            cannonballButton.click();
            Condition.wait(() -> !ctx.widgets.widget(WIDGET_CANNONBALL_ID).component(COMPONENT_CANNONBALL_ID).visible(), 250, 8); // Cannonball button was pressed
            updateWindow();
            Condition.wait(() -> ctx.players.local().animation() != -1, Random.nextInt(948, 1304), 3); // Wait until you are actually making cannonballs
            if (ctx.players.local().animation() == -1) return; // If not smithing then try again
            state = STATE.WAIT;
            System.out.println("|Cannon-Baller| Waiting to finish Smithing");
        }
    }

    /**
     * This is when the character is traveling to the bank from the furnace.
     * At this time the inventory should have no steel bars left, but there may or may not be any cannonballs.
     */
    private void travelFrom() {
        System.out.println("|Cannon-Baller| Traveling to the bank");
        runningLabel.setText("To Bank");
        checkEnergy();
        if (ctx.players.local().tile().distanceTo(BANK) > MINIMAP_RANGE) // the bank is too far away for step, possible walked off for some reason?
            ctx.movement.walkTo(BANK.derive(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
        else ctx.movement.step(BANK.derive(Random.nextInt(-1, 1), Random.nextInt(-1, 1))); // the bank is within range so step to it
        Condition.wait(() -> ctx.players.local().inMotion() && ctx.players.local().tile().distanceTo(BANK) <= INTERACTION_RANGE, Random.nextInt(648, 1132), 10);
        updateWindow();
        if (ctx.players.local().tile().distanceTo(BANK) > INTERACTION_RANGE) return;  // If we are not in range then try to walk to the bank again
        state = STATE.BANK; // We are now in range and it's time to bank
    }

    /**
     * This is for when the bot is smithing and waiting to be done smithing.
     * This is between when the smithing window closes and the character starts smithing
     * and when there are no more steel bars left and the character is not doing anything.
     * This is also where the amount of bars that have been smithed is being kept track of,
     * and if the amount of smithed bars is equal to or above the max number then the bot
     * will call the logOutAndSuspend method.
     */
    private void waitForSmithing() {
        runningLabel.setText("Smithing");
        int xp = ctx.skills.experience(Skill.Smithing.getIndex());
        if (xp > xpBefore) { // This compares previous xp value to current
            xpPer = xp - xpBefore; // If the current xp is more then we smithed a cannonball and it needs to be counted
            xpBefore = xp;
            smithed += 4;
        }
        if (smithed / 4 >= Integer.parseInt(barsTextField.getText())) {
            System.out.println("|Cannon-Baller| Max bars have been smithed!");
            runningLabel.setText("Max Reached!");
            logOutAndSuspend();
        }
        if (ctx.inventory.toStream().id(STEEL_BAR).isEmpty()) { // We are out of steel bars
            System.out.println("|Cannon-Baller| Finished Smithing");
            Condition.wait(() -> ctx.players.local().animation() < 0, Random.nextInt(648, 1132), 2); // And we are not doing anything
            updateWindow();
            state = STATE.TRAVEL_FROM; // So it's time to go back to the bank
        }
    }

    /**
     * This checks if the character has enough energy to start running if not running already.
     */
    private void checkEnergy() {
        Movement move = ctx.movement;
        if (!move.running() && move.energyLevel() > Random.nextInt(18, 24)) // If your not running and you have energy
            Condition.wait(() -> move.running(true), Random.nextInt(648, 1132), 3); // Then set running to true
    }

    /**
     * This puts a number (num) on a scale of min and max.
     * Example: (num=6, nMin=0, nMax=10, min=0, max=100)
     * This will return 60 as the number 6 is scaled up to be between 0 and 100;
     *
     * This also works the other way around:
     * Example: (num=60, nMin=0, nMax=100, min=0, max=10)
     * This will return 6 as the number 60 is scaled down to be between 0 and 10;
     *
     * As well as backwards:
     * Example: (num=6, nMin=0, nMax=10, min=100, max=0)
     * This will return 40 as the number 6 is scaled up to be between 0 and 100 and then the remainder is used;
     *
     * As well as with a complicated scale:
     * Example: (num=6, nMin=2, nMax=7, min=12, max=43)
     * This will return 37 (36.8 rounded up) as the number 6 scaled up to be between 12 and 43;
     *
     * ******
     * NOTE: This can easily be modified to return double values by changing all the Integers as well as the return type to double.
     * ******
     *
     * @param num The number that is being scaled
     * @param nMin The minimum value that num could be
     * @param nMax The maximum value that num could be
     * @param min The minimum value that num can be scaled to
     * @param max The maximum value that num can be scaled to
     * @return The num scaled to be between the min and max values
     */
    private int map(int num, int nMin, int nMax, int min, int max) {
        return (num - nMin) * (max - min) / (nMax - nMin) + min;
    }

    /**
     * This updates the information in the JFrame window.
     */
    private void updateWindow() {
        timeLabel.setText("" + timer.getFormattedTimeFromAdjustedStart());
        smithedLabel.setText("Cannonballs: " + smithed);
        int level = ctx.skills.realLevel(Skill.Smithing.getIndex());
        prevLevelLabel.setText("" + level);
        nextLevLabel.setText("" + (level + 1));
        expTil = ctx.skills.experienceAt(level + 1) - ctx.skills.experience(Skill.Smithing.getIndex());
        progressBar.setValue(map(ctx.skills.experience(Skill.Smithing.getIndex()), ctx.skills.experienceAt(level), ctx.skills.experienceAt(level + 1), 0, 100));
        xpTillLabel.setText("" + expTil);
        int sellPer = Integer.parseInt(gpSellTextField.getText());
        int gpPer = Integer.parseInt(gpPerTextField.getText());
        int profit = (sellPer * 4) - gpPer;
        profitLabel.setText("Profit: " + profit + " (" + (profit * (smithed / 4)) + ")");
        xpHr = skill.getExperiencePerHour();
        xpHrLabel.setText("xp/hr " + xpHr);
    }

    /**
     * This currently does nothing.
     * @param graphics The Java Graphics library
     */
    @Override
    public void repaint(Graphics graphics) {
        // Window painting stuff here
        // Currently this is used for mobile only so I didn't bother doing this
        // Once PowBot Mobile v2 comes out I will create the code for graphics painting
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
    private void initComponents() {
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

        nameLabel.setFont(new Font("Steelfish Outline", Font.PLAIN, 36)); // NOI18N
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
}
