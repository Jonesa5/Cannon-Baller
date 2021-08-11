package blackminer37.cannonballer.task;

import blackminer37.cannonballer.Finals;
import blackminer37.cannonballer.Window;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Component;
import org.powerbot.script.rt4.GameObject;
import org.powerbot.script.rt4.Widget;

import java.awt.*;

public class TaskSmith extends Task {
    private final String NAME = "TaskSmith";
    private final String VERSION = "0.0.0";

    private final int AMMO_MOULD = 4;
    private final int STEEL_BAR = 2353;

    private final int[] ITEM_IDs = {AMMO_MOULD, STEEL_BAR};

    private final int WIDGET_CANNONBALL_ID = 270;
    private final int COMPONENT_CANNONBALL_ID = 14;
    private final int COMPONENT_CANNONBALL_SUB_ID = 29;
    private final int COMPONENT_ALL_ID = 12;
    private final int COMPONENT_ALL_SUB_ID = 0;

    private GameObject smelter;

    private final Window window;

    public TaskSmith(ClientContext ctx, Window window) {
        super(ctx);
        this.window = window;
    }

    @Override
    public boolean activate() {
        smelter = ctx.objects.toStream().within(Finals.INTERACTION_RANGE).name("Furnace").first();
        return !completed
                && smelter != GameObject.NIL
                && smelter.inViewport()
                && smelter.tile().distanceTo(ctx.players.local().tile()) <= Finals.INTERACTION_RANGE
                && !ctx.players.local().interacting().valid()
                && !ctx.players.local().inMotion();
    }

    /**
     * This is when the character is selecting the options to start smithing cannonballs.
     * This goes through interacting with the furnace and selecting the option to smith cannonballs.
     */
    @Override
    public void execute() {
        System.out.println("|Cannon-Baller| Interacting with Furnace");
        window.runningLabel().setText("Smithing");

        for (Integer i : ITEM_IDs) { // Check if we have all the items we need
            if (ctx.inventory.toStream().id(i).isEmpty()) {
                System.out.println("|Cannon-Baller| Required items are missing! " + i);
                completed = true; // We don't have them so go back to bank
                return;
            }
        }

        smelter.interact("Smelt");

        Widget cannonballWidget = ctx.widgets.widget(WIDGET_CANNONBALL_ID); // The cannonball widget menu
        Component cannonballButton = cannonballWidget.component(COMPONENT_CANNONBALL_ID).component(COMPONENT_CANNONBALL_SUB_ID);  // The cannonball button on the widget

        Condition.wait(cannonballWidget::valid, Random.nextInt(648, 1132), 2);
        if (cannonballButton.visible()) {

            Component all = cannonballWidget.component(COMPONENT_ALL_ID).component(COMPONENT_ALL_SUB_ID); // The ALL button on the widget
            if (all.textureId() == 1545) { // -1 is selected, 1545 is not selected
                System.out.println("|Cannon-Baller| Selecting All");
                Condition.wait(all::click, Random.nextInt(648, 873), 2); // The ALL button has been pressed
            }

            System.out.println("|Cannon-Baller| Smithing cannonballs");
            Condition.wait(cannonballButton::click, 250, 8); // Cannonball button was pressed

            Condition.wait(() -> ctx.players.local().animation() != -1, 750, 3); // Wait until you are actually making cannonballs
            if (ctx.players.local().animation() == -1) return; // If not smithing then try again
            completed = true;
            System.out.println("|Cannon-Baller| Waiting to finish Smithing");
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