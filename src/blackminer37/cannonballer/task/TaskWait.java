package blackminer37.cannonballer.task;

import blackminer37.cannonballer.Main;
import blackminer37.cannonballer.Window;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.rt4.ClientContext;

import java.awt.*;

public class TaskWait extends Task {
    private final String NAME = "TaskWait";
    private final String VERSION = "0.0.0";

    private final int STEEL_BAR = 2353;

    private final Window window;
    private final Main main;



    public TaskWait(ClientContext ctx, Window window, Main main) {
        super(ctx);
        this.window = window;
        this.main = main;
    }

    @Override
    public boolean activate() {
        return !completed && !ctx.players.local().inMotion();
    }

    /**
     * This is for when the bot is smithing and waiting to be done smithing.
     * This is between when the smithing window closes and the character starts smithing
     * and when there are no more steel bars left and the character is not doing anything.
     * This is also where the amount of bars that have been smithed is being kept track of,
     * and if the amount of smithed bars is equal to or above the max number then the bot
     * will call the logOutAndSuspend method.
     */
    @Override
    public void execute() {
        window.runningLabel().setText("Smithing");
        if (main.xp() > main.xpBefore()) { // This compares previous xp value to current
            window.setXpPer(main.xp() - main.xpBefore()); // If the current xp is more then we smithed a cannonball and it needs to be counted
            main.xpBefore(main.xp());
            main.smithedAdd(4);
        }

        try {
            if (main.smithed() / 4 >= Integer.parseInt(window.barsTextField().getText())) {
                System.out.println("|Cannon-Baller| Max bars have been smithed!");
                window.runningLabel().setText("Max Reached!");
                main.logOutAndSuspend();
            }
        } catch (Exception e) { System.out.println("|Cannon-Baller| TastWait.execute() TryCatch ERROR"); }

        if (ctx.inventory.toStream().id(STEEL_BAR).isEmpty()) { // We are out of steel bars
            System.out.println("|Cannon-Baller| Finished Smithing");
            Condition.wait(() -> ctx.players.local().animation() < 0, Random.nextInt(648, 1132), 2); // And we are not doing anything
            completed = true; // So it's time to go back to the bank
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