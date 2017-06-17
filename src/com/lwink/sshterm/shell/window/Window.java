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
package com.lwink.sshterm.shell.window;

import com.lwink.sshterm.terminal.api.Terminal;

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
  
  public void addText(String text, boolean refresh)
  {
    displayBuffer.addText(text);
    if (scrollPosition == 0 && refresh)
    {
      // Redraw the window only if we are not scrolled.
      refresh();
    }
  }
  
  public void addTextWithNewLine(String text, boolean refresh)
  {
    addText(text + '\n', refresh);
  }
  
  public void resize(int width, int height)
  {
    System.out.println("Width: " + this.width + " -> " + width);
    if (this.width != width)
    {
      displayBuffer.resizeWidth(width);
      this.width = width;
    }
    this.height = height;
    refresh();
  }
  
  public void refresh()
  {
  	// First make the cursor invisible so that it's not seen while the screen is drawn
    terminal.setCursorVisible(false);
    
    // bufferStartRow is the row into the display buffer that is the first to be displayed
    // at the top of the window.  Note that this can be a negative number if the window is
    // larger than the number of rows in the buffer.
    int bufferStartRow = displayBuffer.getNumberOfRowsWithContent() - height;
    
    // Go through the window line by line and draw what needs to be drawn.
    for (int i = 0; i < height; i++)
    {
      terminal.moveCursor(leftPosition, i + topPosition);
      int bufferRow = bufferStartRow + i;
      if (bufferRow >= 0)
      {
        displayBuffer.draw(terminal, bufferRow);
      }
      else
      {
        // No data to fill this row, so make it blank
        for (int j = 0; j < width; j++)
        {
          terminal.putCharacter(' ');
        }
      }
    }
    terminal.setCursorVisible(true);
    terminal.flush();
  }
}
