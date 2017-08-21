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

public class Window
{
  /** The terminal that this window is associated with */
  private Terminal terminal;
  
  /** Width of the terminal */
  private int width;
  /** Height of the terminal */
  private int height;
  
  /** The leftmost position of the window */
  private int leftPosition;
  /** The top location of the window */
  private int topPosition;
  
  /** The buffer to store the text data */
  private DisplayBuffer displayBuffer;
  
  /** The number of rows that is scrolled back.  A value of zero means no scrolling */
  private int scrollPosition;
  
  /**
   * Create a new window on a terminal.
   * 
   * @param terminal The owning terminal.
   * @param width The width of the new window.  This must be less than the width of the
   *        terminal plus the column parameter.
   * @param height The height of the new window.  This must be less than the height of
   *        the terminal plus the row parameter.
   * @param leftPosition The terminal column to create the window in.  A value of 0 will create the
   *        window in the first column of the terminal.
   * @param topPosition The terminal row to create the window in.  A value of 0 will create the
   *        window at the very top (first row) of the terminal.
   */
  public Window(Terminal terminal, int width, int height, int leftPosition, int topPosition)
  {
    this.terminal = terminal;
    this.width = width;
    this.height = height;
    this.leftPosition = leftPosition;
    this.topPosition = topPosition;
    this.scrollPosition = 0;
    this.displayBuffer = new ArrayDisplayBuffer(width, 10);
  }
  
  /**
   * Add text to the window.
   * 
   * @param text The next to add.
   * @param refresh If true, the window will refresh.
   */
  public void addText(String text, boolean refresh)
  {
    displayBuffer.addText(text);
    if (scrollPosition == 0 && refresh)
    {
      // Redraw the window only if we are not scrolled.
      refresh();
    }
  }
  
  /**
   * Add text to the window followed by a newline.
   * 
   * @param text The next to add.  A new line will be appended to the end.
   * @param refresh If true, the window will refresh.
   */
  public void addTextWithNewLine(String text, boolean refresh)
  {
    addText(text + '\n', refresh);
  }
  
  /**
   * Scroll the window up a specified number of rows.
   * 
   * @param numRows The number of rows to scroll.
   */
  public void scrollUp(int numRows)
  {
  	int maxScroll = getMaxScrollPosition();
  	int scrollAmount = Math.min(numRows, maxScroll - scrollPosition);
  	if (scrollAmount > 0)
  	{
  		scrollPosition += scrollAmount;
  		refresh();
  	}
  }
  
  /**
   * Scroll the window down a specified number of rows.
   * 
   * @param numRows The number of rows to scroll.
   */
  public void scrollDown(int numRows)
  {
  	scrollPosition = Math.max(0, scrollPosition - numRows);
  	refresh();
  }
  
  /**
   * Sets the window scroll position to a specified value.
   * 
   * @param newScrollPosition The position to scroll to.  Valid range is [0, getNumberOfRowsWithContent() - height].
   *        If the parameter is outside the valid range, the scroll position will be set to the closest
   *        valid value.
   */
  public void setScrollPosition(int newScrollPosition)
  {
  	newScrollPosition = Math.max(0, newScrollPosition);
  	newScrollPosition = Math.min(getMaxScrollPosition(), newScrollPosition);
  	if (this.scrollPosition != newScrollPosition)
  	{
  		this.scrollPosition = newScrollPosition;
  		refresh();
  	}
  }
  
  /**
   * Scroll the window up the height of the window.
   */
  public void pageUp()
  {
  	scrollUp(height);
  }
  
  /**
   * Scroll the window down the height of the window.
   */
  public void pageDown()
  {
  	scrollDown(height);
  }
  
  /**
   * Resize the window.
   * 
   * @param width The new width of the window.
   * @param height The new height of the window.
   */
  public void resize(int width, int height)
  {
    if (this.width != width)
    {
      displayBuffer.resizeWidth(width);
      this.width = width;
    }
    this.height = height;
    refresh();
  }
  
  /**
   * Completely redraws the window.
   */
  public void refresh()
  {
  	// First make the cursor invisible so that it's not seen while the screen is drawn
    terminal.setCursorVisible(false);
    
    // bufferStartRow is the row into the display buffer that is the first to be displayed
    // at the top of the window.  Note that this can be a negative number if the window is
    // larger than the number of rows in the buffer.
    int bufferStartRow = displayBuffer.getNumberOfRowsWithContent() - height - scrollPosition;
    
    // Go through the window line by line from the top of the window to the bottom and
    // draw each line from the buffer.
    for (int i = 0; i < height; i++)
    {
      terminal.moveCursor(leftPosition, i + topPosition);
      int bufferRow = bufferStartRow + i;
      if (bufferRow >= 0)
      {
        displayBuffer.drawLine(terminal, bufferRow);
      }
      else
      {
        // No data to fill this row, so make it blank
      	terminal.eraseCharacters(width);
      }
    }
    terminal.setCursorVisible(true);
    terminal.flush();
  }
  
  /**
   * Returns the max scroll position.
   * 
   * @return The maximum scroll position
   */
  protected int getMaxScrollPosition()
  {
  	return Math.max(0, displayBuffer.getNumberOfRowsWithContent() - height);
  }
}
