package gamePlay;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 *  EggTimer--Counts down in seconds to 0. The EggTimer is given the time
 *  period it must count when created, and it immediately starts counting.
 *  Once it gets to 0 the EggTimer is defunct and a new one must be created 
 *  to count once more. The EggTimer can be stopped at any time, but it is 
 *  then defunct and cannot be restarted.
 *
 *  Clients implementing the TickListener interface can register themselves
 *  as tick listeners, which means that their tick() operations will be
 *  called every time the EggTimer ticks (every second).
 *
 *  Clients can interrogate the EggTimer to find out how many seconds are 
 *  left at any time. They can also get a String version of the time left
 *  suitable for display.
 *
 *  Note that this timer is very accurate because it uses the 
 *  java.util.Timer to provide clock ticks; using javax.swing.Timer
 *  is not very accurate at all.
 *
 *  Class invariant: (timer != null) iff (0 < secondsLeft)
 *                   listeners != null
 *
 *   @author C. Fox
 *  @version 3/19
 */
public class EggTimer extends TimerTask
{
  private Timer timer;                  // provides one pulse every second
  private int secondsLeft;              // for counting down from whatever
  private Set<TickListener> listeners;  // tick listeners notified every tick

  /**
   * Creates a one-time EggTimer that starts right away and counts down
   * from the parameter, in seconds.
   *
   * @param secondsToCount value from which it counts down
   */
  public EggTimer(int secondsToCount)
  {
    secondsLeft = secondsToCount;
    timer = (0 < secondsLeft) ? new Timer() : null;
    if (timer != null)
      timer.scheduleAtFixedRate(this, 1000, 1000);
    listeners = new HashSet<TickListener>();
  }

  /**
   * Respond to an alert from the java.util.Timer object. This is a method
   * required in extending the java.TimerUser class.
   *
   * The seconds left counter is decremented. If the EggTimer runs out the
   * Timer is stopped and forgotten.
   */
  public void run()
  {
    secondsLeft--;
    if (secondsLeft <= 0) stop();
    notifyListeners();
  }

  /**
   * Add a tick listener to the set of tick listeners.
   *
   * @param l tick listener added to the set
   */
  public void addListener(TickListener l)
  {
    listeners.add(l);
  }

  /**
   * Remove a tick listener from the set of tick listeners.
   *
   * @param l tick listener removed from the set
   */
  public void removeListener(TickListener l)
  {
    listeners.remove(l);
  }

  /**
   * Fetch the seconds left.
   * 
   * @return How many seconds are left on this EggTimer
   */
  public int getSecondsLeft()
  {
    return secondsLeft;
  }

  /**
   * Fetch the time left as a String in the format m:ss.
   * 
   * @return The time left as a String.
   */
  public String getTimeLeft()
  {
    return String.format("%d:%02d", secondsLeft/60, secondsLeft%60);
  }

  /**
   * Stop this EggTimer. It may not be restarted.
   */
  public void stop()
  {
    if (timer != null)
    {
      timer.cancel();
      timer = null;
    }
  } // stop

   //////////////////////////////////////////////////////////////////////////
   ///   Private Methods   //////////////////////////////////////////////////

  /**
   * Iterate through the list of tick listeners and notify each one that
   * the timer has ticked.
   */
  private void notifyListeners()
  {

    for (TickListener l : listeners)
      l.tick(this);

  }

} // EggTimer
