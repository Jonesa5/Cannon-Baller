package blackminer37.cannonballer;

import blackminer37.cannonballer.task.TaskBank;
import blackminer37.cannonballer.task.TaskSmith;
import blackminer37.cannonballer.task.TaskTravel;
import blackminer37.cannonballer.task.TaskWait;
import org.powbot.walking.model.Skill;
import org.powerbot.script.*;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.GeItem;
import java.awt.*;

/**
 * @author BlackMiner37
 * @date August 11, 2021
 * @version 2.0.13
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
        version = "2.0.13",
        trialDays = 1L,
        mobileReady = true
)

public class Main extends PollingScript<ClientContext> implements PaintListener {

    private Window window;
    private final int STEEL_BAR = 2353;
    private final int CANNONBALL = 2;

    private boolean showTTL = true;
    private int smithed = 0;
    private int timerBefore = 0, idleTimer = 0;

    private int xpBefore;
    private int xp = ctx.skills.experience(Skill.Smithing.getIndex());

    private TaskBank taskBank;
    private TaskTravel taskTravel;
    private TaskSmith taskSmith;
    private TaskWait taskWait;

    /**
     * This is where the states are initialized and held.
     * The states are: BANK, SMITH, TRAVEL, WAIT
     * The default state is TRAVEL_FROM, but this is reset in the startSetUp method.
     */
    public enum STATE {
        BANK, SMITH, TRAVEL, WAIT
    }

    private STATE state = STATE.TRAVEL;

    /**
     * This is first method to be called when the bot is started.
     * This is where the set up method for the JFrame is called
     * as well as where the timer and skill tracker are initialized
     */
    @Override
    public void start() {
        System.out.println("|Cannon-Baller| Starting");
        window = new Window(ctx, this);
        window.initComponents();
        taskBank = new TaskBank(ctx, window, this);
        taskTravel = new TaskTravel(ctx, window, false);
        taskSmith = new TaskSmith(ctx, window);
        taskWait = new TaskWait(ctx, window, this);
        ctx.input.blocking(true);
        ctx.setForceSingleTap(false);
        ctx.setDismissRandoms(false);
        startSetUp();
        window.updateWindow();
    }

    /**
     * This is where the bot is stopped and the JFrame is closed.
     */
    @Override
    public void stop() {
        System.out.println("|Cannon-Baller| Stopping");
        window.runningLabel().setText("Stopped");
        ctx.input.blocking(false);
        ctx.controller.stop();
        window.frame().dispose();
        super.stop();
    }

    /**
     * This is called when the bot needs to be logged out and suspended.
     * This will not close the JFrame and it will keep the bot running in the background.
     * By suspending the bot rather then stopping it the data
     * that is being kept track of will still be there when the bot is resumed.
     */
    public void logOutAndSuspend() {
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
        if (ctx.skills.level(Skill.Smithing.getIndex()) < 35) {
            System.out.println("|Cannon-Baller| Smithing level is too low!");
            window.runningLabel().setText("Level too low!!");
            ctx.controller.suspend();
            ctx.input.blocking(false);
            return;
        }

        if (ctx.camera.pitch() < 99) ctx.camera.pitch(99);
        if (ctx.game.tab() != Game.Tab.INVENTORY) ctx.game.tab(Game.Tab.INVENTORY);

        window.gpPerTextField(GeItem.getPrice(STEEL_BAR) + "");
        window.gpSellPerTextField(GeItem.getPrice(CANNONBALL) + "");

        if (ctx.inventory.toStream().id(STEEL_BAR).isEmpty()) taskTravel = new TaskTravel(ctx, window, false);
        else taskTravel = new TaskTravel(ctx, window, true);
        state = STATE.TRAVEL;
    }

    /**
     * This is called once every second.
     * This is where the idle timer is being kept track of as well as where
     * the actions left and time-to-level is switched back and forth to be displayed.
     * If the bot is resumed from a suspended state this is where the bot is told to start running again.
     */
    private void tick() {
        timerBefore = window.timer().getTimeFromStartInSeconds();

        if (ctx.controller.isSuspended() && ctx.game.clientState() == Constants.GAME_LOADED) {
            window.runningLabel().setText("Running");
            ctx.controller.resume();
            window.timer().resume();
        }

        if (idleTimer >= Random.nextInt(6, 10)) {
            if (ctx.inventory.toStream().id(STEEL_BAR).isEmpty()) taskTravel = new TaskTravel(ctx, window, false);
            else taskTravel = new TaskTravel(ctx, window, true);
            state = STATE.TRAVEL;
            idleTimer = 0;
            return;
        }
        if (ctx.players.local().animation() == -1) idleTimer++;
            else idleTimer = 0;
        if (idleTimer > 0) window.runningLabel().setText("Idling: " + idleTimer);

        if (showTTL)
            window.ttlLabel().setText("" + window.timer().getFormattedTimeFromGivenTime((int) ((int) (window.expTil() / (window.xpHr() / 3600.0)) * 1000.0)));
            else window.ttlLabel().setText("Actions: " + (window.expTil() / window.xpPer()));
        showTTL = !showTTL;
        window.updateWindow();
    }

    /**
     * This is the heart of the bot.
     * This is where the bot decides what to do based on it's current state.
     * This is also where the bot detects and closes a "Press to continue" message like when a level is gained.
     */
    @Override
    public void poll() {
        if (window.timer().getTimeFromStartInSeconds() > timerBefore) tick();

        if (ctx.game.clientState() == Constants.GAME_LOADED) { // The client is running and the player is logged in

            if (ctx.chat.canContinue() && ctx.chat.clickContinue()) {
                state = STATE.SMITH; // and go back to smithing (assumed that it will only happen during a level up)
            }
            xp = ctx.skills.experience(Skill.Smithing.getIndex());

            switch (state) {
                case BANK:
                    if (taskBank.activate()) taskBank.execute();
                    if (taskBank.getCompleted()) {
                        taskTravel = new TaskTravel(ctx, window, true);
                        state = STATE.TRAVEL;
                    }
                    break;
                case TRAVEL:
                    if (taskTravel.activate()) taskTravel.execute();
                    if (taskTravel.getCompleted()) {
                        if (taskTravel.toSmelter()) {
                            taskSmith = new TaskSmith(ctx, window);
                            state = STATE.SMITH;
                        } else {
                            taskBank = new TaskBank(ctx, window, this);
                            state = STATE.BANK;
                        }
                    }
                    break;
                case SMITH:
                    if (taskSmith.activate()) taskSmith.execute();
                    if (taskSmith.getCompleted()) {
                        taskWait = new TaskWait(ctx, window, this);
                        state = STATE.WAIT;
                    }
                    break;
                case WAIT:
                    if (taskWait.activate()) taskWait.execute();
                    if (taskWait.getCompleted()) {
                        taskTravel = new TaskTravel(ctx, window, false);
                        state = STATE.TRAVEL;
                    }
                    break;
            }
        } else {
            window.runningLabel().setText("Suspended");
            window.timer().pause();
        }
    }

    /**
     * This currently does nothing.
     *
     * @param graphics The Java Graphics library
     */
    @Override
    public void repaint(Graphics graphics) {
        // Window painting stuff here
        // Currently this is used for mobile only so I didn't bother doing this
        // Once PowBot Mobile v2 comes out I will create the code for graphics painting
    }

    public int smithed() { return smithed; }
    public void smithedAdd(int smith) { smithed += smith; }

    public STATE state() { return state; }

    public int xp() { return xp; }
    public int xpBefore() { return xpBefore; }
    public void xpBefore(int xp) { xpBefore = xp; }
}
