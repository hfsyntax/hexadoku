package gamePlay;

/**
 *  TickListener--Interface for a class that listens for EggTimer ticks.
 *  See the EggTimer class for more details.
 *
 *   @author C. Fox
 *  @version 3/19
 */
public interface TickListener
{
  /**
   * Respond to an EggTimer tick.
   * @param timer which timer provided the tick
   */
  void tick(EggTimer timer);
}
