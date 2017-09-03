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

import com.lwink.javashell.shell.api.TextAttributes;
import com.lwink.javashell.terminal.api.TermSize;
import com.lwink.javashell.terminal.api.Terminal;

/**
 * The display buffer implementation.  Internally, everything is stored as a 
 * 2 dimensional array of integers serving as terminal cells.  The reason for 
 * using integers instead of a complex object is so that everything is backed
 * by a true array of values instead of an array of references.  This speeds
 * things up a lot.
 * 
 * Cell format is as follows:
 * [NxxxxxxxxCCCCCCCCTTTTTTTTTTTTTTTT]
 * N = Newline bit.  If set, then there is a explicit newline starting with this char.
 * x = Not used.
 * C = Color bits
 * T = Character bits
 */
public class DoubleArrayDisplayBuffer implements DisplayBuffer
{
  /** The width of the display buffer */
  int width;
  
  /** The height of the display buffer */
  int height;
  
  /** The display buffer memory.  The total size is width * height * sizeof(cell) */
  int[][] buffer;
  
  /** The column to insert the next character */
  int insertX;
  
  /** The row to insert the next character */
  int insertY;
  
  public DoubleArrayDisplayBuffer(int width, int height)
  {
    this.width = width;
    this.height = height;
    this.insertX = 0;
    this.insertY = 0;
    this.buffer = new int[height][width];
  }
  
  @SuppressWarnings("unused")
  public void resizeWidth(int newWidth)
  {
    int[][] newBuffer = new int[height][newWidth];
    int maxWidth = newWidth > width ? newWidth : width;
    
    for (int i = 0; i < height; i++)
    {
      int[] oldRow = buffer[i];
    }
    
    buffer = newBuffer;
    width = newWidth;
  }
  
  
  public void addText(String string, TextAttributes attributes)
  {
    for (int i = 0; i < string.length(); i++)
    {
      char c = string.charAt(i);
      if (c == '\n')
      {
        newLine(true);
      }
      else if(c == '\r')
      {
        // Ignore
      }
      else
      {
        buffer[insertY][insertX++] = c;
        if (insertX >= width)
        {
          newLine(false);
        }
      }
    }
  }
  
  public void addTextLine(String string, TextAttributes attributes)
  {
    addText(string + '\n', attributes);
  }
  
  public void drawLine(Terminal terminal, int bufferRow)
  {
    TermSize terminalSize = terminal.getTerminalSize();
    if (terminalSize.getColumns() != width)
    {
      throw new IllegalStateException("Terminal size does not match buffer size");
    }
    
    for (int i = 0; i < width; i++)
    {
      int cell = buffer[bufferRow][i];
      char c = (char)(cell & 0xFF);
      if (c == (char)0)
        c = ' ';
      terminal.putCharacter(c);
    }
  }
  
  /**
   * Returns the number of lines in the buffer with valid content.
   * 
   * @return The number of lines in the buffer with content.
   */
  public int getNumberOfRowsWithContent()
  {
    int amount = insertY;
    
    // If there is content in the current line, then we need to count it.
    if (insertX > 0)
      amount++;
    
    return amount;
  }
  
  /**
   * Go to a new terminal line.  If necessary, the terminal buffer will be modified
   * to make room for the new line.
   * 
   * @param explicit If true, then this is an explicit new line (requested externally).  Otherwise
   *        this is a newline due to running out of columns for the current line.
   */
  protected void newLine(boolean explicit)
  {
    insertX = 0;
    insertY++;
    if (insertY >= height)
    {
      bufferFull();
    }
    if (explicit == true)
    {
      buffer[insertY][insertX] |= 0x80000000;
    }
  }
  
  protected void bufferFull()
  {
    System.out.println("Reallocating buffer");
    // trimAmount is the number of rows that we are deleting from the top
    int trimAmount = (int)(height * 0.2);
    if (trimAmount == 0)
      trimAmount = 1;
    
    int[][] newBuffer = new int[height][width];
    for (int i = trimAmount, j = 0; i < height; i++)
    {
      newBuffer[j++] = buffer[i];
    }
    buffer = newBuffer;
    insertY -= trimAmount;
    if (insertY < 0)
    {
      throw new IllegalStateException("Invalid insertY value: " + insertY);
    }
  }
}
