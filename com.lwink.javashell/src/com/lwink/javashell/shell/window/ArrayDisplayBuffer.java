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
import com.lwink.javashell.terminal.api.TermColor;
import com.lwink.javashell.terminal.api.Terminal;

/**
 * The display buffer implementation.  Internally, everything is stored as an
 * array of integers serving as terminal cells.  The reason for 
 * using integers instead of a complex object is so that everything is backed
 * by a true array of values instead of an array of references.  This speeds
 * things up a lot.
 * 
 * Cell format is as follows:
 * [xxxxxxBBBBBFFFFFTTTTTTTTTTTTTTTT]
 * x = Not used.
 * F = Foreground color bits
 * B = Background color bits
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
    this.lineIndexes = new int[100];
    this.lineInsertIndex = 0;
    this.width = width;
  }
  
  @Override
  public void addText(String text, TextAttributes attributes)
  {
  	// This is a pretty crude way of handling tab characters.  A better approach would be to
  	// align tab stops on a particular column.
  	text = text.replaceAll("\t", "  ");
  	
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
    
    int cellAttributes = attributes != null  ? getCellAttributes(attributes) : 0;
    
    // Add the characters to the buffer
    text.chars().forEach(c -> buffer[cellCount++] = c | cellAttributes);
    
    // Recalulate the line indexes starting at the current line
    recalcualteLineIndexes(width, lineInsertIndex);
  }
  
  @Override
  public void addTextLine(String text, TextAttributes attributes)
  {
    addText(text + '\n', attributes);
  }
  
  @Override
  public void drawLine(Terminal terminal, int bufferRow)
  {
    int count = getVisibleCharsOnLine(bufferRow);
    int index = lineIndexes[bufferRow];
    int fgColor = -1;  // Default to something impossible
    int bgColor = -1;
    
    for (int i = 0; i < count; i++)
    {
      int cell = buffer[index++];
      char c = getCharFromCell(cell);
      if (c == (char)0)
        c = ' ';
      int newFgColor = (cell & 0x001F0000) >> 16;
    	int newBgColor = (cell & 0x03E00000) >> 21;
      if (fgColor != newFgColor)
      {
      	terminal.setForegroundColor(toTermColor(newFgColor));
      	fgColor = newFgColor;
      }
      if (bgColor != newBgColor)
      {
      	terminal.setBackgroundColor(toTermColor(newBgColor));
      	bgColor = newBgColor;
      }
      terminal.putCharacter(c);
    }
    terminal.eraseCharacters(width - count);
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
      char c = getCharFromCell(cell);
      
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
    if (lineIndexes[lineInsertIndex] == cellCount - 1)
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
    if (count > 0 && getCharFromCell(buffer[lineEndIndex - 1]) == '\n')
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
  
  /**
   * Convert color codes in the display buffer cells to TermColor enumeration values.
   * 
   * @param color Integer value of color codes in display buffer.
   * @return Corresponding TermColor.
   */
  private TermColor toTermColor(int color)
  {
  	// The color values do not match the ANSI escape code values.  This is because I wanted
  	// 0 to be the default color.
  	switch (color)
  	{
  	case 1:
  		return TermColor.BLACK;
  	case 2:
  		return TermColor.RED;
  	case 3:
  		return TermColor.GREEN;
  	case 4:
  		return TermColor.YELLOW;
  	case 5:
  		return TermColor.BLUE;
  	case 6:
  		return TermColor.MAGENTA;
  	case 7:
  	  return TermColor.CYAN;
  	case 8:
  		return TermColor.WHITE;
  	}
  	return TermColor.DEFAULT;
  }
  
  /**
   * Converts a TermColor to the value used to represent that color in the buffer.  This is the opposite function as
   * {@link #toTermColor(int)}.
   * 
   * @param color Color to convert.
   * @return Integer value to store in array buffer.
   */
  private int toBufferColor(TermColor color)
  {
  	switch (color)
  	{
  	case BLACK:
  		return 1;
  	case RED:
  		return 2;
  	case GREEN:
  		return 3;
  	case YELLOW:
  		return 4;
  	case BLUE:
  		return 5;
  	case MAGENTA:
  		return 6;
  	case CYAN:
  		return 7;
  	case WHITE:
  		return 8;
  	default:
  		return 0;
  	}
  }
  
  private int getCellAttributes(TextAttributes attributes)
  {
  	int cellAttributes = 0;
  	int fgPart = toBufferColor(attributes.getFgColor());
  	int bgPart = toBufferColor(attributes.getBgColor());
  	
  	// See the comments for this class to understand why the following shifting
  	// takes place.
  	cellAttributes |= (fgPart << 16);
  	cellAttributes |= (bgPart << 21);
  	
  	return cellAttributes;
  }
  
  private char getCharFromCell(int cell)
  {
  	return (char)(0x0000FFFF & cell);
  }
}
