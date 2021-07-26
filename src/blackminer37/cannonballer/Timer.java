package blackminer37.cannonballer;

/**
 * @author BlackMiner37
 * @date July 17, 2021
 * @version 1.0.0
 * This keeps track of the information of a Timer. The timer can be used to keep track of the time since it was started,
 * or to keep track of a duration of time.
 */

public class Timer {

    private long startTime, pauseTime, adjustTime;
    private int durationInSeconds, endTime;
    private boolean paused = false;

    /**
     * This is the main constructor for the Timer class. This will start a timer at the current time.
     */
    public Timer() {
        this.startTime = System.currentTimeMillis();
        this.durationInSeconds = 0;
        this.endTime = getCurrentTimeSeconds();
    }

    /**
     * This is the Timer constructor that sets the duration time. The given parameter will set the duration of the timer. This will start a timer at the current time.
     * @param durationInSeconds The amount of time in seconds that the duration will be set to.
     */
    public Timer(int durationInSeconds) {
        this.startTime = System.currentTimeMillis();
        this.durationInSeconds = durationInSeconds;
        this.endTime = getCurrentTimeSeconds() + durationInSeconds;
    }

    /**
     * This resets the timer without having to create a new instance.
     */
    public void reset() {
        startTime = System.currentTimeMillis();
        resetDuration();
        endTime = getCurrentTimeSeconds() + durationInSeconds;
    }

    /**
     * This pauses the timer at the current time.
     */
    public void pause() {
        if (paused) return;
        System.out.println("|Timer| paused");
        paused = true;
        pauseTime = System.currentTimeMillis();
    }

    /**
     * This returns if the timer is currently paused or not.
     * @return isPaused Boolean
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * This resumes the timer from the point when it was paused
     */
    public void resume() {
        if (!paused) return;
        System.out.println("|Timer| resumed");
        adjustTime = (System.currentTimeMillis() - pauseTime);
        paused = false;
    }

    /**
     * This resets the duration of the timer with the duration that was used when the timer was initiated.
     */
    public void resetDuration() {
        endTime = getCurrentTimeSeconds() + durationInSeconds;
    }

    /**
     * This resets the duration of the timer with the given parameter.
     * @param durationInSeconds The time in seconds that the duration will be reset to.
     */
    public void resetDuration(int durationInSeconds) {
        endTime = getCurrentTimeSeconds() + durationInSeconds;
    }

    /**
     * This returns the duration of the timer in the standard American clock format (HH:MM:SS).
     * @return formattedDuration String
     */
    public String getFormattedDurationTime() {
        long timeLeft = endTime - getTimeFromStart();
        long seconds = (timeLeft / 1000) % 60;
        long minutes = (timeLeft / 60000) % 60;
        long hours = (timeLeft / 3600000) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * This returns the time in milliseconds that the timer was started.
     * @return startTimeInMilliseconds Integer
     */
    public int getStartTime() {
        return (int)startTime;
    }

    /**
     * This returns the time that the timer was started in the standard American clock format (HH:MM:SS).
     * @return formattedStartTime String
     */
    public String getFormattedStartTime() {
        long seconds = (startTime / 1000) % 60;
        long minutes = (startTime / 60000) % 60;
        long hours = (startTime / 3600000) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * This returns the amount of time that the timer has been running in milliseconds.
     * @return timeFromStartInMilliseconds Integer
     */
    public int getTimeFromStart() {
        return (int)(System.currentTimeMillis() - startTime);
    }

    /**
     * This returns the amount of time that the timer has been running in seconds.
     * @return timeFromStartInSeconds Integer
     */
    public int getTimeFromStartInSeconds() {
        return (int)(System.currentTimeMillis() - startTime) / 1000;
    }

    /**
     * This returns the amount of time that the timer has been active in the standard American clock format (HH:MM:SS).
     * @return formattedTimeFromStart String
     */
    public String getFormattedTimeFromStart() {
        long runTimeMili = System.currentTimeMillis() - startTime;
        long seconds = (runTimeMili / 1000) % 60;
        long minutes = (runTimeMili / 60000) % 60;
        long hours = (runTimeMili / 3600000) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * This returns the difference in time between the given parameter and the current time in the standard American clock format (HH:MM:SS).
     * @param miliSeconds The time to be compared with in milliseconds.
     * @return formattedTimeFromGivenTime String
     */
    public String getFormattedTimeFromGivenTime(int miliSeconds) {
        long seconds = (miliSeconds / 1000) % 60;
        long minutes = (miliSeconds / 60000) % 60;
        long hours = (miliSeconds / 3600000) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * This returns the amount of time that the timer has been active, minus the time paused, in milliseconds.
     * @return adjustedTimeFromStart Integer
     */
    public int getTimeFromAdjustedStart() {
        return (int)(System.currentTimeMillis() - (startTime + adjustTime));
    }

    /**
     * This returns the amount of time that the timer has been active, minus the time paused, in the standard American clock format (HH:MM:SS).
     * @return formattedAdjustedTimeFromStart String
     */
    public String getFormattedTimeFromAdjustedStart() {
        long runTimeMili = System.currentTimeMillis() - (startTime + adjustTime);
        long seconds = (runTimeMili / 1000) % 60;
        long minutes = (runTimeMili / 60000) % 60;
        long hours = (runTimeMili / 3600000) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * This returns the difference between the current time and midnight, January 1, 1970 UTC in milliseconds.
     * @return currentTimeSeconds Integer
     */
    public int getCurrentTimeMillis() {
        return (int)System.currentTimeMillis();
    }

    /**
     * This returns the difference between the current time and midnight, January 1, 1970 UTC in seconds.
     * @return currentTimeSeconds Integer
     */
    public int getCurrentTimeSeconds() {
        return (int)System.currentTimeMillis() / 1000;
    }

    /**
     * This returns the amount of milliseconds that the timer has been paused.
     * This only updates after the timer has been resumed.
     * @return pauseTimeInMilliseconds Integer
     */
    public int getPauseTimeMillis() {
        return (int)adjustTime;
    }

    /**
     * This returns the amount of seconds that the timer has been paused.
     * This only updates after the timer has been resumed.
     * @return pauseTimeInSeconds Integer
     */
    public int getPauseTimeInSeconds() {
        return (int)adjustTime / 1000;
    }

    /**
     * This checks the current time with the timer duration and returns if the current time is past or not.
     * @return pastDuration Boolean
     */
    public boolean isPastDuration() {
        return (getCurrentTimeSeconds() >= endTime);
    }

}
