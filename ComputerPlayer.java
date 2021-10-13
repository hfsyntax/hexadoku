package gamePlay;

import java.util.HashSet;
import java.util.Set;

/**
 * Computer Player Class for Boggle.
 * 
 * @author C. Fox and Noah Kaiser
 * @version 4/25
 */
public class ComputerPlayer extends Player
{
  private Dictionary dictionary;

  /**
   * ComputerPlayer Constructor. Calls parent, initializes dictionary.
   * 
   * @param d
   *          the dictionary
   */
  public ComputerPlayer(Dictionary d)
  {
    super();
    dictionary = d;
  }

  /**
   * Find words from board, copy to a hashset.
   * 
   * @param b
   *          the board to search.
   */
  public void findWords(Board b)
  {
    wordSet = new HashSet<String>();
    copy(b, wordSet, dictionary.size() - 1);
  }

  /**
   * Recursively add words from the board to the dictionary in a hashset.
   * 
   * @param b
   *          the board
   * @param h
   *          the hashset
   * @param n
   *          the index
   */
  void copy(Board b, Set<String> h, int n)
  {
    if (n >= 0 && b.isOnBoard(dictionary.getWord(n)))
    {
      h.add(dictionary.getWord(n));
      copy(b, h, n - 1);
    }
  }

}
