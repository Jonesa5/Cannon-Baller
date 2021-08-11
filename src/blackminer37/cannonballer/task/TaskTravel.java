package blackminer37.cannonballer.task;

import blackminer37.cannonballer.Finals;
import blackminer37.cannonballer.Window;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Movement;

import java.awt.*;

public class TaskTravel extends Task {
    private final String NAME = "TaskTravel";
    private final String VERSION = "0.0.0";

    private final Tile BANK = new Tile(3096, 3494, 0);
    private final Tile SMELTER = new Tile(3109, 3499, 0);

    private final Window window;
    private final boolean toSmelter;

    public TaskTravel(ClientContext ctx, Window window, boolean toSmelter) {
        super(ctx);
        this.toSmelter = toSmelter;
        this.window = window;
    }

    @Override
    public boolean activate() {
        if (!toSmelter && ctx.bank.opened()) {
            completed = true;
            return false;
        }
        return !completed
                && !ctx.players.local().inMotion();
    }

    /**
     * This is when the character is traveling to or from the furnace.
     */
    @Override
    public void execute() {
        Tile dest = BANK;
        if (toSmelter) {
            dest = SMELTER;
            System.out.println("|Cannon-Baller| Traveling to the furnace");
            window.runningLabel().setText("To Furnace");
        } else {
            System.out.println("|Cannon-Baller| Traveling to the Bank");
            window.runningLabel().setText("To Bank");
        }

        checkEnergy();

        double dist = ctx.players.local().tile().distanceTo(dest);
        if (dist <= Finals.INTERACTION_RANGE) {
            completed = true;
            return;
        }

        if (dist > Finals.MINIMAP_RANGE) // the furnace is too far away for step, possible walked off for some reason?
            ctx.movement.walkTo(dest.derive(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
        else
            ctx.movement.step(dest.derive(Random.nextInt(-1, 1), Random.nextInt(-1, 1))); // the furnace is within range so step to it

        Tile finalDest = dest;
        Condition.wait(() -> !ctx.players.local().inMotion() && ctx.players.local().tile().distanceTo(finalDest) < Finals.INTERACTION_RANGE, Random.nextInt(648, 1132), 10);

        if (ctx.players.local().tile().distanceTo(dest) > Finals.INTERACTION_RANGE)
            return; // If we are not in range then try to walk to the furnace again
        completed = true; // We are now in range and it's time to start smithing
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

    /**
     * This checks if the character has enough energy to start running if not running already.
     */
    private void checkEnergy() {
        Movement move = ctx.movement;
        if (!move.running() && move.energyLevel() > Random.nextInt(18, 24)) // If your not running and you have energy
            Condition.wait(() -> move.running(true), Random.nextInt(648, 1132), 3); // Then set running to true
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

    public boolean toSmelter() { return toSmelter; }
}
