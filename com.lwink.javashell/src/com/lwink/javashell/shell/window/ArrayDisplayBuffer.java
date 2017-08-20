/**
 * Copyright 2016 Luke Winkenbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 **/
package com.lwink.javashell.shell.window;

import com.lwink.javashell.terminal.api.Terminal;

/**
 * The display buffer implementation.  Internally, everything is stored as an
 * array of integers serving as terminal cells.  The reason for 
 * using integers instead of a complex object is so that everything is backed
 * by a true array of values instead of an array of references.  This speeds
 * things up a lot.
 * 
 * Cell format is as follows:
 * [xxxxxxxxxCCCCCCCCTTTTTTTTTTTTTTTT]
 * x = Not used.
 * C = Color bits
 * T = Character bits
 */
public class ArrayDisplayBuffer implements DisplayBuffer
{
  private int[] buffer;
  private int cellCount;
  private int[] lineIndexes;
  private int lineInsertIndex;
  private int width;
  
  public ArrayDisplayBuffer(int width, int numberOfLines)
  {
    this.buffer = new int[100000];
    this.cellCount = 0;
    this.lineIndexes = new int[2];
    this.lineInsertIndex = 0;
    this.width = width;
  }
  
  @Override
  public void addText(String text)
  {
    if (cellCount + text.length() > buffer.length)
    {
      rollBuffer();
      lineInsertIndex = 0; // This forces us to recalculate all the line indexes instead of just
                           // the current line
      
      // If we still don't have enough room for the text, just truncate it.
      if (cellCount + text.length() > buffer.length)
      {
        text = text.substring(0, buffer.length - cellCount);
      }
    }
    // Add the characters to the buffer
    text.chars().forEach(c -> buffer[cellCount++] = c);
    
    // Recalulate the line indexes starting at the current line
    recalcualteLineIndexes(width, lineInsertIndex);
  }
  
  @Override
  public void addTextLine(String text)
  {
    addText(text + '\n');
  }
  
  @Override
  public void drawLine(Terminal terminal, int bufferRow)
  {
    int count = getVisibleCharsOnLine(bufferRow);
    int index = lineIndexes[bufferRow];

    for (int i = 0; i < count; i++)
    {
      int cell = buffer[index++];
      char c = (char)(cell & 0xFF);
      if (c == (char)0)
        c = ' ';
      terminal.putCharacter(c);
    }
    for (int i = count; i < width; i++)
    {
      terminal.putCharacter(' ');
    }
  }
  
  @Override
  public void resizeWidth(int newWidth)
  { 
    recalcualteLineIndexes(newWidth, 0);
    
    this.width = newWidth;
  }
  
  /**
   * Rolls the buffer by cutting off the oldest 10% of the characters and moving the rest of
   * the characters to the beginning of the buffer.  This makes room for new characters in the
   * buffer.
   */
  protected void rollBuffer()
  {
    int rollPoint = (int)(buffer.length * 0.1); // Roll at 10%
    int cellIndex = 0;
    for (int i = rollPoint; i < cellCount; i++)
    {
      buffer[cellIndex++] = buffer[i];
    }
    cellCount = cellIndex;
    recalcualteLineIndexes(width, 0);
  }
  
  /**
   * Recalculate the indexes of the line markers.  
   * 
   * This function will modify the lineIndexes member to fill out the proper indexes into
   * the buffer member.
   * @param width The width of the terminal
   * @param startLineIndex The line at which to start the calculation.
   */
  protected void recalcualteLineIndexes(int width, int startLineIndex)
  {
    if (startLineIndex > lineInsertIndex)
    {
      // We can't start the recalculation at a line that doesn't exist
      throw new RuntimeException("Attempting to start recalculating lines at line " + startLineIndex + 
          " when there are only " + lineInsertIndex + " total lines!");
    }
    lineInsertIndex = startLineIndex;
    int cellIndex = lineIndexes[startLineIndex];
    int col = 0;
    boolean newLineOnNextChar = false;
    while (cellIndex < cellCount)
    {
      if (newLineOnNextChar)
      {
        newLineOnNextChar = false;
        newLine(cellIndex);
        col = 0;
      }
      int cell = buffer[cellIndex];
      char c = (char)(cell & 0xFF);
      
      if (c == '\n' || ++col >= width)
      {
        newLineOnNextChar = true;
      }
      cellIndex++;
    }
  }

  @Override
  public int getNumberOfRowsWithContent()
  {
    // If the current line is empty, then we don't want to count it.
    if (lineIndexes[lineInsertIndex] == cellCount)
    {
      return lineInsertIndex;
    }
    else
    {
      return lineInsertIndex + 1;
    }
  }
  
  protected int getVisibleCharsOnLine(int lineIndex)
  {
    if (lineIndex > lineInsertIndex)
    {
      throw new RuntimeException("Invalid line number: " + lineIndex);
    }
    
    int count;
    int lineBeginIndex;
    int lineEndIndex;
    
    lineBeginIndex = lineIndexes[lineIndex];
    
    if (lineIndex == lineInsertIndex)
    {
      // This is the last line
      lineEndIndex = cellCount;
    }
    else
    {
      lineEndIndex = lineIndexes[lineIndex + 1];
    }
    
    count = lineEndIndex - lineBeginIndex;
    
    // If the last character in the line is a newline character, then we don't 
    // want to count it.
    if (count > 0 && buffer[lineEndIndex - 1] == '\n')
    {
      count--;
    }
    
    if (count < 0)
    {
      throw new RuntimeException("Got a negative cell count on line: " + lineIndex);
    }
    
    return count;
  }
  
  protected void newLine(int bufferIndex)
  {
    if (++lineInsertIndex >= lineIndexes.length)
    {
      growLineIndexeBuffer();
    }
    lineIndexes[lineInsertIndex] = bufferIndex;
  }
  
  protected void growLineIndexeBuffer()
  {
    int[] newLineIndexes = new int[lineIndexes.length + 100];
    for (int i = 0; i < lineIndexes.length; i++)
    {
      newLineIndexes[i] = lineIndexes[i];
    }
    lineIndexes = newLineIndexes;
  }
}
