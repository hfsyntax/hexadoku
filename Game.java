package gamePlay;

import java.util.HashSet;
import java.util.Set;

/**
 *  Game--This class is the Boggle play supervisor. It knows the players
 *  and the board, starts rounds of play, scores each round, determines
 *  if the game is over, and manages the dictionary.
 *
 *  The protocol for playing games from the UI class is as follows:
 *    1) Create a game object with a designated target score and difficulty.
 *    2) Call reset() to initialize the game. This can be done to restart
 *       a game at any time.
 *    3) Call startRound() to begin a round. This will increment the round
 *       counter but will not change other scoring varables. The board is
 *       mixed and the the computer and human players are told to find words.
 *    4) Tells the human player about its words for this round. This is
 *       done by querying the game object using getHumanPlayer(), and
 *       then telling this proxy about the words with setWordSet().
 *    5) Score the round with scoreRound().
 *    6) Register any rejected words with rejectWords(). This will cause the
 *       round to be rescored automatically.
 *    7) Query and display the results of the round using various query 
 *       methods. All scoring variables are recomputed when scoreRound() is
 *       called and recomputed whenever rejectWords() is called.
 *    8) See if the game is over, and if not, continue the game at step 3.
 *    9) The difficulty level can be changed at any time. This will affect
 *       the likelihood that the dictionary will learn a word.
 *   10) The target game value can be changed at any time, but doing so will
 *       automatically cause the game to restart, interrupting the old
 *       game and starting a new one.
 *
 *   @author C. Fox
 *  @version 03/03
 */
public class Game
{
  public static final int MIN_TARGET = 1;     // lowest allowed
  public static final int MAX_TARGET = 10000; // highest allowed
  public static final int DFLT_TARGET = 100;  // unless changed

  public static final int MIN_DIFFICULTY = 1;  // learn 10% of new words
  public static final int MAX_DIFFICULTY = 10; // learn all new words
  public static final int DFLT_DIFFICULTY = 5; // learn 50% of new words

  private int targetScore;              // game over when this score reached
  private int difficulty;               // rate at which program learns words
  private int roundNumber;              // count the rounds in the game
  private Board board;                  // where the letters are arrayed
  private Dictionary dictionary;        // used by the computer player
  private Player human;                 // proxy for the human program user
  private Player machine;               // computerized gamePlay wizard
  private Set<String> commonWords;      // found by both players in a round
  private Set<String> invalidWords;     // human's words not on the board
  private Set<String> humanOnlyWords;   // found by human alone in a round
  private Set<String> machineOnlyWords; // found by computer alone in a round

  /**
   * Create a new game with the specified target score and difficulty.
   *
   * @param endScore stop when this score is reached
   * @param hardness on a scale of 1 to 10, set the probability that
   *        the computer will learn a given new word (10 means always)
   */
  public Game(int endScore, int hardness)
  {
    targetScore = (endScore < MIN_TARGET || MAX_TARGET < endScore)
                  ? DFLT_TARGET : endScore;
    difficulty = (hardness < MIN_DIFFICULTY || MAX_DIFFICULTY < hardness)
                 ? DFLT_DIFFICULTY : hardness;
    roundNumber      = 0;
    board            = new Board();
    dictionary       = new Dictionary();
    human            = new HumanPlayer();
    machine          = new ComputerPlayer(dictionary);
    commonWords      = new HashSet<>();
    invalidWords     = new HashSet<>();
    humanOnlyWords   = new HashSet<>();
    machineOnlyWords = new HashSet<>();
  }

  /**
   * Initialize a new game.
   *
   * @post roundNumber == 0
   * @post (Player p)(p.roundScore == p.totalScore == 0)
   */
  public void reset()
  {
    roundNumber = 0;
    human.resetScores();
    machine.resetScores();
  }

  /**
   * Initialize one round of the game.
   */
  public void startRound()
  {
    roundNumber++;
    board.mix();
    machine.findWords(board);
    human.findWords(board);
  }

  /**
   * Score the latest round of the game, thus ending the round (and possibly the game).
   */
  public void scoreRound()
  {
    // sort words to prepare for assigning scores
    partitionWordSets();

    // learn new words from the human's word set
    for (String word : humanOnlyWords)
      if (2 < word.length()) dictionary.learn(word, difficulty);

    // update the round scores
    human.assignRoundScore(scoreWordSet(humanOnlyWords));
    machine.assignRoundScore(scoreWordSet(machineOnlyWords));
  }

  /**
   * Disallow words rejected by the user. These words must be removed
   * from the dictionary and each player's word sets, plus the word sets
   * used for scoring. Also, the round scores may have to be adjusted.
   *
   * @param rejectedWords words disallowed.
   */
  public void rejectWords(Set<String> rejectedWords)
  {
    if (rejectedWords != null)
    {
      Set<String> humanWordSet = human.getWordSet();
      Set<String> machineWordSet = machine.getWordSet();

      // remove words from dictionary and word sets, and adjust scores
      for (String word : rejectedWords)
      {
        int wordScore = scoreWord(word);

        // delete from the dictionary and players' word sets
        dictionary.forget(word);
        humanWordSet.remove(word);
        machineWordSet.remove(word);

        // delete from scoring word sets and adjust scores
        commonWords.remove(word);
        if (humanOnlyWords.contains(word))
        {
          humanOnlyWords.remove(word);
          human.decrementRoundScore(wordScore);
        }
        if (machineOnlyWords.contains(word))
        {
          machineOnlyWords.remove(word);
          machine.decrementRoundScore(wordScore);
        }
      }

      // reset the players' word sets
      human.setWordSet(humanWordSet);
      machine.setWordSet(machineWordSet);
    }
  }

  /**
   * Determine whether this game is finished.
   *
   * @return True iff target score is reached by at least one Player.
   */
  public boolean isOver()
  {
    return (targetScore <= human.getTotalScore())
        || (targetScore <= machine.getTotalScore());
  }

  /**
   * Determine the game ending score.
   *
   * @return The game ending score in range 1..10000
   */
  public int getTargetScore()
  {
    return targetScore;
  }

  /**
   * Set the game ending score. This will force the game to restart.
   *
   * @param newTarget new game-ending score in range 1..10000. Out of
   *                  range values are ignored.
   */
  public void setTargetScore(int newTarget)
  {
    if ((MIN_TARGET <= newTarget) && (newTarget <= MAX_TARGET))
    {
      targetScore = newTarget;
      reset();
    }
  }

  /**
   * Determine the game difficulty level.
   *
   * @return The difficulty level in range 1..10. This value,
   *         when divided by 10, is the probability that the computer
   *         will learn a given new word.
   */
  public int getDifficulty()
  {
    return difficulty;
  }

  /**
   * Set the game difficulty level.
   *
   * @param newLevel new difficulty level, which must be in 1..10.
   *                 Out of range values are ignored.
   */

  public void setDifficulty(int newLevel)
  {
    if ((MIN_DIFFICULTY <= newLevel) && (newLevel <= MAX_DIFFICULTY))
      difficulty = newLevel;
  }

  /**
   * Determine the current round number.
   *
   * @return The round number in range 1..*
   */
  public int getRoundNumber()
  {
    return roundNumber;
  }

  /**
   * Return the current board.
   *
   * @return The current board.
   */

  public Board getBoard()
  {
    return board;
  }

  /**
   * Return the human Player object.
   *
   * @return The human Player object.
   */

  public Player getHumanPlayer()
  {
    return human;
  }

  /**
   * Return the computer Player object.
   *
   * @return The computer Player object.
   */

  public Player getComputerPlayer()
  {
    return machine;
  }

  /**
   * Return the set of valid words found by both players.
   *
   * @return The set of common words.
   */

  public Set<String> getCommonWords()
  {
    return commonWords;
  }

  /**
   * Return the set of words typed by the human but not on the board.
   *
   * @return The set of invalid words.
   */

  public Set<String> getInvalidWords()
  {
    return invalidWords;
  }

  /**
   * Return the set of valid words found by the human player only.
   *
   * @return The set of human only words.
   */

  public Set<String> getHumanOnlyWords()
  {
    return humanOnlyWords;
  }

  /**
   * Return the set of valid words found by the computer player only.
   *
   * @return The set of computer only words.
   */

  public Set<String> getComputerOnlyWords()
  {
    return machineOnlyWords;
  }

  /////////////////////////////////////////////////////////////////////////
  ///   Private Methods   /////////////////////////////////////////////////

  /**
   * Partitions all the words found by the human and computer players
   * into four classes: those found by both, those found by the human but
   * not on the board, those found on the board by the human alone, and
   * those found by the computer alone.
   */
  private void partitionWordSets()
  {
    Set<String> humanWordSet; // the human player's word set
    Set<String> machineWordSet; // the computer player's word set
    Set<String> validWords;   // human words that are on the board

    // fetch the players' word sets
    humanWordSet = human.getWordSet();
    machineWordSet = machine.getWordSet();

    // create the invalid and valid word set
    invalidWords = new HashSet<String>();
    validWords   = new HashSet<String>();
    for (String word : humanWordSet)
      if (!board.isOnBoard(word))
        invalidWords.add(word);
      else
        validWords.add(word);

    // create the common words set
    commonWords = new HashSet<String>(validWords);
    commonWords.retainAll(machineWordSet);

    // remove common words from the players' valid word sets
    humanOnlyWords = new HashSet<String>(validWords);
    humanOnlyWords.removeAll(commonWords);
    machineOnlyWords = new HashSet<String>(machineWordSet);
    machineOnlyWords.removeAll(commonWords);
  }

  /**
   * Compute the score for a set of words.
   *
   * @param wordSet words whose scores are computed and summed
   * @return The sum of the scores for each word in the wordSet
   */
  private int scoreWordSet(Set<String> wordSet)
  {
    int score = 0;
    for (String word : wordSet)
      score += scoreWord(word);
    return score;
  }

  /**
   * Figure out the score for a word given the rules of Boggle.
   *
   * @param word scored
   * @return The word's score in range 0..11
   */
  private int scoreWord(String word)
  {
    int result;

    switch (word.length())
    {
      case 0:
      case 1:
      case 2:
        result = 0;
        break;
      case 3:
      case 4:
        result = 1;
        break;
      case 5:
        result = 2;
        break;
      case 6:
        result = 3;
        break;
      case 7:
        result = 5;
        break;
      default:
        result = 11;
        break;
    }
    return result;
  }

} // Game
