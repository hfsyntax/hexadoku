package gamePlay;

import java.util.Random;

/**
 * Board--Keeps the 4x4 grid of letters, mixing it, making it accessible, and checking words against
 * it to make sure that they are really present. As an aid to algorithms, asking for a letter in an
 * illegal board position returns a special Board.NON_LETTER character. Also, letters can be marked
 * as "in use"; requesting an in use letter return the special NON_LETTER character. This facility
 * also aids in searching the board.
 *
 * @author C. Fox and Noah Kaiser
 * @version 4/25
 */
public class Board
{

  public static final char NON_LETTER = '\0'; // grid edge character
  public static final int DIMENSION = 4; // grid size

  private static final int RANDOM_RANGE = 96; // for mixing the grid
  private static final char[] LETTER // Boggle frequencies of occurrence
      = {'j', 'k', 'q', 'y', 'z', // frequency 1 in 96
          'b', 'c', 'f', 'g', 'm', 'p', 'v', // frequency 2 in 96
          'b', 'c', 'f', 'g', 'm', 'p', 'v', 'd', 'u', 'w', 'x', // frequency 3 in 96
          'd', 'u', 'w', 'x', 'd', 'u', 'w', 'x', 'h', 'l', 'r', // frequency 5 in 96
          'h', 'l', 'r', 'h', 'l', 'r', 'h', 'l', 'r', 'h', 'l', 'r', 'a', 'i', 'n', 's', 'o', // frequency
                                                                                               // 6
                                                                                               // in
                                                                                               // 96
          'a', 'i', 'n', 's', 'o', 'a', 'i', 'n', 's', 'o', 'a', 'i', 'n', 's', 'o', 'a', 'i', 'n',
          's', 'o', 'a', 'i', 'n', 's', 'o', 'e', 't', // frequency 10 in 96
          'e', 't', 'e', 't', 'e', 't', 'e', 't', 'e', 't', 'e', 't', 'e', 't', 'e', 't', 'e', 't'};

  private static final Random URN = new Random(); // random LETTER indices

  private char[][] grid; // all letters on the board
  private boolean[][] inUse; // true iff that letter used in a word

  /**
   * Create a new Boggle board.
   */
  public Board()
  {
    grid = new char[DIMENSION][DIMENSION];
    inUse = new boolean[DIMENSION][DIMENSION];
    for (int r = 0; r < DIMENSION; r++)
      for (int c = 0; c < DIMENSION; c++)
      {
        grid[r][c] = NON_LETTER;
        inUse[r][c] = false;
      }
  }

  /**
   * Create a boggle board with a fixed grid for testing.
   * 
   * @param newGrid
   *          the known grid
   */
  Board(char[][] newGrid)
  {
    grid = newGrid;

    // make sure the grid is ok
    if (grid.length != DIMENSION)
      throw new IllegalArgumentException();
    for (int row = 0; row < DIMENSION; row++)
      if (grid[row].length != DIMENSION)
        throw new IllegalArgumentException();

    // set up the inUse array
    inUse = new boolean[DIMENSION][DIMENSION];
    for (int r = 0; r < DIMENSION; r++)
      for (int c = 0; c < DIMENSION; c++)
        inUse[r][c] = false;
  }

  /**
   * Mix up the letters on the board according to the letter distribution in a real (physical)
   * Boggle game. The static final char array LETTER contains the letters in the correct proportions
   * to satisfy this requirement. The static URN object generates random indices into LETTER.
   *
   * As a side-effect, this method sets all inUse flags to false.
   */
  public void mix()
  {
    for (int row = 0; row < grid.length; row++)
    {
      for (int col = 0; col < grid[row].length; col++)
      {
        grid[row][col] = LETTER[URN.nextInt(RANDOM_RANGE)];
      }
    }
  }

  /**
   * Fetch a character at a board location if it is not in use, but do not mark it as in use.
   *
   * @param row
   *          on the board
   * @param col
   *          on the board
   * @return Board.NON_LETTER if (row,col) is out-of-bounds or the letter is in use, otherwise, the
   *         letter at that spot
   */
  public char charAt(int row, int col)
  {
    char result = NON_LETTER;
    if ((0 <= row) && (row < DIMENSION) && (0 <= col) && (col < DIMENSION))
      if (!inUse[row][col])
        result = grid[row][col];
    return result;
  }

  /**
   * Fetch a character at a board location, and also mark it as in use. This method is intended for
   * finding and checking words on the gamePlay board.
   *
   * @param row
   *          on the board
   * @param col
   *          on the board
   * @return Board.NON_LETTER if (row,col) is out-of-bounds or the letter is in use, otherwise, the
   *         letter at that spot
   */
  public char useCharAt(int row, int col)
  {
    char result = NON_LETTER;
    if ((0 <= row) && (row < DIMENSION) && (0 <= col) && (col < DIMENSION))
      if (!inUse[row][col])
      {
        result = grid[row][col];
        inUse[row][col] = true;
      }
    return result;
  }

  /**
   * Mark a letter on the board as no longer in use. This method is intended for finding and
   * checking words on the gamePlay board. Do nothing if the location is out of range.
   * 
   * @param row
   *          on the board
   * @param col
   *          on the board
   */
  public void unUseCharAt(int row, int col)
  {
    if ((0 <= row) && (row < DIMENSION) && (0 <= col) && (col < DIMENSION))
      inUse[row][col] = false;
  }

  /**
   * Mark all letters on the board as no longer in use.
   */
  public void unUseAll()
  {
    for (int row = 0; row < grid.length; row++)
    {
      for (int col = 0; col < grid[row].length; col++)
      {
        inUse[row][col] = false;
      }
    }
  }

  /**
   * See if a word is on the board. The null String is never on the board and the empty String is
   * always on the board.
   *
   * @param word
   *          to check the board for
   * @return true iff word is found on the board
   */
  public boolean isOnBoard(String word)
  {
    if (word == null)
      return false;
    if (0 == word.length())
      return true;
    unUseAll();
    for (int row = 0; row < DIMENSION; row++)
      for (int col = 0; col < DIMENSION; col++)
        if (checkWord(row, col, word))
          return true;
    return false;
  }

  ///////////////////////////////////////////////////////////////////
  /// Package Methods ///////////////////////////////////////////

  /**
   * Say whether a grid location is marked as in use; for testing.
   * 
   * @param row
   *          which row in the grid
   * @param col
   *          which col in the grid
   * @return true iff the designated row and col are in use
   */
  boolean isInUse(int row, int col)
  {
    return inUse[row][col];
  }

  ///////////////////////////////////////////////////////////////////
  /// Private Methods ///////////////////////////////////////////

  /**
   * Check whether a word can be completed starting at a given location on the board. This method
   * attempts to match the first character of the word with the letter at the given board position.
   * If the match works, then it calls itself to match the remainder of the word at surrounding
   * board positions.
   *
   * @pre word is not null and not empty
   * @param row
   *          where the word matching starts
   * @param col
   *          where the word matching starts
   * @param word
   *          to be matched from the given spot
   * @return true Iff the word is matched from the given spot
   */
  private boolean checkWord(int row, int col, String word)
  {
    char letter = charAt(row, col);
    if (letter != word.charAt(0))
      return false;
    if (1 == word.length())
      return true;

    // the first letter matches and there is a suffix to check
    boolean isFound = false;
    String suffix = word.substring(1);

    // mark the board position as in use and try to match suffix
    useCharAt(row, col);
    for (int r = row - 1; (r <= row + 1) && !isFound; r++)
      for (int c = col - 1; (c <= col + 1) && !isFound; c++)
        isFound = checkWord(r, c, suffix);
    unUseCharAt(row, col);

    return isFound;
  }

} // Board
