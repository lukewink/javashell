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

import java.util.stream.IntStream;

import com.lwink.javashell.terminal.api.CursorPosition;
import com.lwink.javashell.terminal.api.Terminal;

public class InputWindow
{
	/**
	 * The buffer which holds the user's input.
	 */
  private StringBuilder buffer;
  
  /**
   * This holds the cursor position in the user input area (not including the prompt area).  So if
   * the total window with is 10 and the prompt length is 3, this variable can hold values from 
   * 0 to 6.
   */
  private int bufferCursorPos;
  
  /**
   * This contains how many characters the view is scrolled to the right.  So if the total window
   * width is 6 (and there is no prompt), and the user inputs 6 characters, this value would be 1
   * since the window would need to be scrolled one to the right for the cursor to be on the spot
   * where the next character would go.
   */
  private int visiblePos;
  private int width; /** Window width */
  private int row; /** The terminal row that the window resides on */
  private int col; /** How many columns from the left the window is positioned */
  private Terminal terminal;
  private String prompt;
  
  public InputWindow(Terminal terminal, int width, int row)
  {
    buffer = new StringBuilder(100);
    bufferCursorPos = 0;
    visiblePos = 0;
    this.terminal = terminal;
    this.width = width;
    this.row = row;
    this.col = 0;
    prompt = "";
  }
  
  /**
   * Sets the prompt.  The window must be refreshed for the change to take effect.
   * 
   * @param prompt The new prompt string
   */
  public void setPrompt(String prompt)
  {
  	this.prompt = prompt;
  }
  
  /**
   * Move the cursor position to the left if possible.
   */
  public void cursorLeft()
  {
    if (bufferCursorPos > 0)
    {
    	--bufferCursorPos;
      resetCursorPosition();
      terminal.flush();
    }
    else
    {
      if (visiblePos > 0)
      {
        visiblePos--;
        refresh();
      }
    }
  }
  
  /**
   * Move the cursor position to the right if possible.
   */
  public void cursorRight()
  {
    if (bufferCursorPos + visiblePos >= buffer.length())
    {
      // The cursor is already at the end of the input.  We could sound a bell or flash the 
      // screen here if needed.
      return;
    }
    
    if (prompt.length() + bufferCursorPos + 1 == width)
    {
      // The cursor is already in the last cell, so we need to "scroll" to the right
      visiblePos++;
      refresh();
    }
    else
    {
    	++bufferCursorPos;
      resetCursorPosition();
      terminal.flush();
    }
  }
  
  /**
   * Add a new character to the input window buffer.  The character will be placed at the
   * cursor position.  {@link #refresh()} needs to be called for the updated state to
   * be redrawn.
   * 
   * @param c The character to add.
   * @return This InputWindow.
   */
  public InputWindow addChar(char c)
  {
    buffer.insert(bufferCursorPos + visiblePos, c);
    cursorRight();
    return this;
  }
  
  /**
   * Sets the contents of the window.  Any previous characters will be removed and replaced
   * with the passed string.  {@link #refresh()} needs to be called for the updated state to
   * be redrawn.  The cursor position will be moved to the end of the new string.
   * 
   * @param s A string that will be the new contents of the window.
   * @return This InputWindow.
   */
  public InputWindow setText(String s)
  {
    clearWindowContents();
    s.chars().forEach(c -> addChar((char)c));
    return this;
  }
  
  /**
   * Get the current window contents.
   * 
   * @return The current window contents.
   */
  public String getWindowContents()
  {
    return buffer.toString();
  }
  
  /**
   * Clear's the input window's character buffer. {@link #refresh()} needs to be called before
   * the update is drawn.
   * 
   * @return This InputWindow.
   */
  public InputWindow clearWindowContents()
  {
    buffer.delete(0, buffer.length());
    bufferCursorPos = 0;
    visiblePos = 0;
    return this;
  }
  
  /**
   * Delete the character behind the current cursor position and shift all characters after
   * the deleted character to the left.
   * 
   * @return This InputWindow
   */
  public InputWindow deleteCharBehindCursorPos()
  {
    int deletePos = bufferCursorPos + visiblePos - 1; // -1 because we delete the character behind the cursor
    if (buffer.length() > deletePos && deletePos >= 0)
    {
      buffer.deleteCharAt(deletePos);
      if (bufferCursorPos > 0)
      {
        bufferCursorPos--;
      }
      else if (visiblePos > 0)
      {
        visiblePos--;
      }
    }
    return this;
  }
  
  /**
   * Resize the input window or change the row it is drawn on.
   * 
   * @param width The new width of the input window
   * @param row The row that the input window is drawn on.
   */
  public void resize(int width, int row)
  {
    if (width != this.width || row != this.row)
    {
      this.width = width;
      this.row = row;
      
      visiblePos = Math.max(buffer.length() - width + 1, 0);
      bufferCursorPos = Math.min(buffer.length(), width - 1);
      refresh();
    }
  }
  
  /**
   * Redraw the input window's characters.
   */
  public void refresh()
  {
    terminal.setCursorVisible(false);
    terminal.moveCursor(col, row);
    
    // First draw the prompt
    int promptVisibleChars = Math.min(prompt.length(), width - 1);
		IntStream.range(0, promptVisibleChars)
					   .map(prompt::charAt)
		         .forEach(c -> terminal.putCharacter((char) c));

    // This variable holds the number of visible characters.  The minus 1 at the end is
    // so that there is room for the cursor at the end of the buffer
    int bufferVisibleChars = Math.min(buffer.length(), width - promptVisibleChars - 1);

    if (bufferVisibleChars > 0)
		{
			IntStream.range(visiblePos, bufferVisibleChars + visiblePos)
			         .map(buffer::charAt)
					     .forEach(c -> terminal.putCharacter((char) c));
		}
    // Erase everything after the cursor's current position
    terminal.eraseLineWithCursor(CursorPosition.AFTER_CURSOR);
    
    resetCursorPosition();
    terminal.setCursorVisible(true);
    terminal.flush();
  }
  
  /**
   * Moves the cursor position to where the input window thinks it should be.
   */
  public void resetCursorPosition()
  {
    terminal.moveCursor(getTerminalCursorCol(), row);
  }
  
  /**
   * Moves the cursor to the beginning of the line.
   */
  public void moveCursorToBeginningOfLine()
  {
  	bufferCursorPos = 0;
  	visiblePos = 0;
    refresh();
  }
  
  /**
   * Moves cursor to the end of the line
   */
  public void moveCursorToEndOfLine()
  {
  	int maxBufferCursorPos = width - prompt.length() - 1;
  	bufferCursorPos = Math.min(buffer.length(), maxBufferCursorPos);
  	visiblePos = Math.max(0, buffer.length() - bufferCursorPos);
    refresh();
  }
  
  /**
   * Gets column that the Terminal cursor should be in.
   * 
   * @return The column that the Terminal cursor should be in.
   */
  private int getTerminalCursorCol()
  {
  	return col + bufferCursorPos + prompt.length();
  }

}
