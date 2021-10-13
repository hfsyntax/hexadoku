package gamePlay;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Dictionary--Hold the Boggle dictionary used by the computer player to search the board. The
 * dictionary is read from a file, perhaps modified during game play, then optionally save back to
 * the original file.
 *
 * Class invariant: the dictionary is sorted the dictionary words are all lower case the dictionary
 * words are never null the dictionary words are never empty
 *
 * @author C. Fox and Noah Kaiser
 * @version 4/25
 */
class Dictionary
{
  private static final String DICTIONARY = "dictionary.txt"; // all words
  private ArrayList<String> list; // the words in the dictionary
  private int sizeWhenSaved; // how many words when last saved
  private String fileName; // for reading and saving

  /**
   * Create a new dictionary and fill it from the dictionary file. If the file cannot be read, the
   * dictionary will be empty.
   */
  public Dictionary()
  {
    this(DICTIONARY);
  }

  /**
   * Create a new dictionary and fill it from the specified file. This constructor is used for
   * testing purposes.
   *
   * @param fName
   *          file used to fill the dictionary
   */
  public Dictionary(String fName)
  {
    fileName = fName;
    list = new ArrayList<String>();
    try (BufferedReader file = new BufferedReader(new FileReader(fileName)))
    {
      String word = file.readLine();
      while (word != null)
      {
        if (0 < word.length())
        {
          if (!list.contains(word.toLowerCase()))
          {
            list.add(word.toLowerCase());
          }
        }
        word = file.readLine();
      }
    }
    catch (IOException ignored) // if input fails, use what we've got
    {
    }

    // make sure the words are sorted
    Collections.sort(list);
    sizeWhenSaved = list.size();
  }

  /**
   * save the boggle board state by writing to the dictionary file.
   */
  public void save()
  {
    try
    {
      PrintWriter writer = new PrintWriter(fileName);
      ArrayList<String> temp = new ArrayList<String>();
      if (list.size() >= 1000)
      {
        for (int i = 0; i < 1000; i++)
        {
          temp.add(list.get(new Random().nextInt()));
          sizeWhenSaved++;
        }
      }
      else
      {
        for (int i = 0; i < list.size(); i++)
        {
          temp.add(list.get(new Random().nextInt()));
          sizeWhenSaved++;
        }
      }
      Collections.sort(temp);
      writer.write(temp.toString());
      writer.close();
    }
    catch (IOException ignored)
    {
    }

  }

  /**
   * Add word to the dictionary based on the probability.
   * 
   * @param word
   *          the word
   * @param prob
   *          the probability
   */
  public void learn(String word, int prob)
  {
    double generator = new Random().nextDouble();
    if (prob / 10 >= generator && !list.contains(word))
      list.add(word);
    if (list.size() - sizeWhenSaved >= 20)
      save();
    Collections.sort(list);
  }

  /**
   * Removes the word from the list.
   * 
   * @param word
   *          the word to remove.
   */
  public void forget(String word)
  {
    if (list.contains(word))
    {
      list.remove(word);
    }
    Collections.sort(list);
  }

  /**
   * Check if string exists in the list.
   * 
   * @param s
   *          the string to search.
   * @return true if the string exists in the list.
   */
  public boolean isWord(String s)
  {

    if (s == null || s.isEmpty())
      return false;

    int initial = 0;
    int last = list.size() - 1;
    while (initial <= last)
    {
      int index = initial + (last - initial) / 2;
      int result = s.compareTo(list.get(index));
      if (result == 0)
        return true;
      if (result > 0)
        initial = index + 1;
      else
        last = index - 1;
    }
    return false;
  }

  /**
   * Check if the word in list contains the specified prefix.
   * 
   * @param s
   *          the string to check.
   * @return true only if the string begins with the prefix.
   */
  public boolean isPrefix(String s)
  {
    for (int i = 0; i < list.size(); i++)
    {
      if (list.get(i).startsWith(s))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if the list is sorted correctly.
   * 
   * @return true if the list is sorted.
   */
  boolean isSorted()
  {
    if (list.size() == 1)
      return true;

    for (int i = 1; i < list.size(); i++)
      if (list.get(i).compareTo(list.get(i - 1)) < 0)
        return false;
    return true;
  }

  /**
   * Get word from list.
   * 
   * @param index
   *          the index to search.
   * @return the list element.
   */
  String getWord(int index)
  {
    return list.get(index);
  }

  /**
   * return the size of the list.
   * 
   * @return size of list.
   */
  int size()
  {
    return list.size();
  }

}
