package blackminer37.cannonballer.task;

import blackminer37.cannonballer.Finals;
import blackminer37.cannonballer.Window;
import blackminer37.cannonballer.Main;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.rt4.Bank;
import org.powerbot.script.rt4.ClientContext;

import java.awt.*;

public class TaskBank extends Task {
    private final String NAME = "TaskBank";
    private final String VERSION = "0.0.0";

    private final int AMMO_MOULD = 4;
    private final int STEEL_BAR = 2353;

    private final int[] ITEM_IDs = {AMMO_MOULD, STEEL_BAR};
    private final Window window;
    private final Main main;

    public TaskBank(ClientContext ctx, Window window, Main main) {
        super(ctx);
        this.window = window;
        this.main = main;
    }

    @Override
    public boolean activate() {
        return !completed
                && ctx.bank.nearest().tile().distanceTo(ctx.players.local()) < Finals.INTERACTION_RANGE
                && !ctx.players.local().interacting().valid()
                && !ctx.players.local().inMotion();
    }

    /**
     * This is when the character is banking the cannonballs or other items in the inventory.
     * At this time there should be no steel bars left in the inventory but lots of cannonballs.
     */
    @Override
    public void execute() {
        System.out.println("|Cannon-Baller| Banking goods");
        window.runningLabel().setText("Banking");

        if (!ctx.bank.opened() && !ctx.bank.open()) return;
        Condition.wait(ctx.bank::opened, Random.nextInt(632, 948), 2);

        if (ctx.bank.currentTab() != 0) ctx.bank.currentTab(0); // Open the general tab

        if (window.updateButtonWaiting()) {
            window.barsTextField().setText(((main.smithed() / 4) + ctx.bank.toStream().id(STEEL_BAR).first().stackSize()) + "");
            window.updateButton().setText("Update Max");
            window.updateButton().setEnabled(true);
            window.updateButtonWaiting(false);
        }

        System.out.println("|Cannon-Baller| Banking all");
        if (ctx.bank.depositAllExcept(ITEM_IDs)) {
            if (ctx.bank.toStream().id(STEEL_BAR).isEmpty()) { // There are no more steel bars to use
                System.out.println("|Cannon-Baller| Bank is out of steel bars!!");
                window.runningLabel().setText("No Bars!");
                ctx.bank.close();
                main.logOutAndSuspend();
                return; // So close the bank and log out
            }

            System.out.println("|Cannon-Baller| Withdraw steel bars");
            if (ctx.inventory.toStream().id(AMMO_MOULD).isEmpty()) ctx.bank.withdraw(AMMO_MOULD, 1);
            ctx.bank.withdraw(STEEL_BAR, Bank.Amount.ALL);
            Condition.wait(() -> ctx.inventory.toStream().id(STEEL_BAR).isNotEmpty(), Random.nextInt(324, 648), 4);
            if (ctx.inventory.toStream().id(STEEL_BAR).isNotEmpty()) { // Successfully has bar count
                ctx.bank.close();
                completed = true;
            }
        }
    }

    public void repaint(Graphics g, int x, int y) {
        /*
        g.setColor(Finals.GUI_COLOR_BACKGROUND);
        g.fillRect(x, y, Finals.GUI_WIDTH, Finals.SUB_GUI_HEIGHT);
        g.setColor(Finals.GUI_COLOR_TRIM);
        g.drawRect(x, y, Finals.GUI_WIDTH, Finals.SUB_GUI_HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString(("    " + NAME + " " + VERSION), x + Finals.GUI_TEXT_X, y + Finals.GUI_TEXT_Y);
         */
    }

    @Override
    public boolean getCompleted() {
        return this.completed;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getName() {
        return NAME;
    }
}