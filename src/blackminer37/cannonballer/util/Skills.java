package blackminer37.cannonballer.util;

import org.powerbot.script.rt4.ClientContext;

/**
 * @author BlackMiner37
 * @date July 17, 2021
 * @version 1.0.0
 * This keeps track of the information of a specific Old School RuneScape skill for use with PowBot scripts.
 */

public class Skills {

    private ClientContext ctx;
    private int skillIndex, startLevel, startExperience, experiencePerHour = 0;
    private final Timer timer = new Timer();

    /**
     * This is the main constructor for the Skills class.
     * @param ctx ClientContext
     * @param skillIndex Integer
     */
    public Skills(ClientContext ctx, int skillIndex) {
        this.ctx = ctx;
        this.skillIndex = skillIndex;
        this.startLevel = getLevel();
        this.startExperience = getExperience() - 1;
    }

    /**
     * This returns the level of the skill that is being tracked.
     * @return level Integer
     */
    public int getLevel() {
        return ctx.skills.level(skillIndex);
    }

    /**
     * This returns the real level of the skill that is being tracked.
     * @return realLevel Integer
     */
    public int getRealLevel() {
        return ctx.skills.realLevel(skillIndex);
    }

    /**
     * This returns the amount of levels that have been gained since the Skills instance was created.
     * @return levelsGained Integer
     */
    public int getGainedLevels() {
        return getLevel() - startLevel;
    }

    /**
     * This returns the current experience of the skill.
     * @return currentExperience Integer
     */
    public int getExperience() {
        return ctx.skills.experience(skillIndex);
    }

    /**
     * This returns the amount of experience that has been gained since the Skills instance was initiated.
     * @return gainedExperience Integer
     */
    public int getGainedExperience() {
        return getExperience() - startExperience;
    }

    /**
     * This returns an unformatted Integer of how much experience can be gained over the next hour.
     * @return experiencePerHour Integer
     */
    public int getExperiencePerHour() {
        int currentExp = ctx.skills.experience(skillIndex);
        int expGain = currentExp - getStartExperience();
        experiencePerHour = (int)(expGain / (timer.getTimeFromStartInSeconds() / 3600.0));
        return experiencePerHour;
    }

    /**
     * This returns the level of the skill that was saved when the Skills instance was created.
     * @return startLevel Integer
     */
    public int getStartLevel() {
        return startLevel;
    }

    /**
     * This returns the experience amount that was saved when the Skills instance was created.
     * @return startExperience Integer
     */
    public int getStartExperience() {
        return startExperience;
    }

    /**
     * This returns the experience that would be required for a specific level.
     * @param level Integer
     * @return Experience Integer
     */
    public int getExperienceAtLevel(int level) {
        return ctx.skills.experienceAt(level);
    }

    /**
     * This returns the Timer associated with this class.
     * @return timer Timer
     */
    public Timer getTimer() {
        return timer;
    }

}
