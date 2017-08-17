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
 * Serves as the data model for a terminal window.  The display buffer
 * holds text as well as metadata such as color or style.  The display
 * buffer also calculates which row text would be drawn on, therefore 
 * the display buffer also has a width element.
 */
public interface DisplayBuffer
{
	/**
	 * Adds text to the end of the display buffer.
	 * 
	 * @param text The text to add to the buffer.
	 */
  public void addText(String text);
  
  /**
   * Adds text to the end of the display buffer followed by a new line
   * 
   * @param text The text to add to the buffer
   */
  public void addTextLine(String text);
  
  /**
   * Changes the width of the display buffer.  This will cause the rows
   * that text is drawn on to change.
   * 
   * @param newWidth The new width in columns
   */
  public void resizeWidth(int newWidth);
  
  /**
   * Draw a line of text to the terminal.
   * 
   * @param terminal The terminal to draw to.  This function assumes that 
   *        the cursor is already at the correct position.
   * @param bufferRow The row of the display buffer to draw to the terminal
   */
  public void drawLine(Terminal terminal, int bufferRow);
  
  /**
   * Returns the number of lines in the buffer with valid content.
   * 
   * @return The number of lines in the buffer with content.
   */
  public int getNumberOfRowsWithContent();
}
