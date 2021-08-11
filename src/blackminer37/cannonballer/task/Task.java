package blackminer37.cannonballer.task;

import org.powerbot.script.rt4.ClientAccessor;
import org.powerbot.script.rt4.ClientContext;

import java.awt.*;

public abstract class Task extends ClientAccessor {

    protected boolean completed = false;
    public Task(ClientContext ctx) {
        super(ctx);
    }
    public abstract boolean activate();
    public abstract void execute();
    public abstract void repaint(Graphics g, int x, int y);
    public abstract boolean getCompleted();
    public abstract void setCompleted(boolean completed);
    public abstract String getName();
}