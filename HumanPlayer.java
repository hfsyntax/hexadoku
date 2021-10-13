package gamePlay;

import java.util.HashSet;

/**
 * Human Player Class for Boggle.
 * 
 * @author C. Fox and Noah Kaiser
 * @version 4/25
 */
public class HumanPlayer extends Player
{
  /**
   * Default constructor for HumanPlayer.
   */
  public HumanPlayer()
  {
    super();
    wordSet = new HashSet<String>();
  }

  /**
   * Empty method to find words from the board.
   * 
   * @param b
   *          the board to search.
   */
  public void findWords(Board b)
  {

  }
}
