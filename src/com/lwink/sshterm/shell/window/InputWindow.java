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

import java.util.stream.IntStream;

import com.lwink.sshterm.terminal.api.CursorPosition;
import com.lwink.sshterm.terminal.api.Terminal;

public class InputWindow
{
  private StringBuilder buffer;
  private int cursorPos;
  private int visiblePos;
  private int width;
  private int row;
  private Terminal terminal;
  
  public InputWindow(Terminal terminal, int width, int row)
  {
    buffer = new StringBuilder(100);
    cursorPos = 0;
    visiblePos = 0;
    this.terminal = terminal;
    this.width = width;
    this.row = row;
  }
  
  /**
   * Move the cursor position to the left if possible.
   */
  public void cursorLeft()
  {
    if (cursorPos > 0)
    {
      terminal.moveCursor(--cursorPos, row);
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
    if (cursorPos + visiblePos >= buffer.length())
    {
      // The cursor is already at the end of the input.  We could sound a bell or flash the 
      // screen here if needed.
      return;
    }
    
    if (cursorPos + 1 == width)
    {
      // The cursor is already in the last cell, so we need to "scroll" to the right
      visiblePos++;
      refresh();
    }
    else
    {
      terminal.moveCursor(++cursorPos, row);
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
    buffer.insert(cursorPos + visiblePos, c);
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
    cursorPos = 0;
    return this;
  }
  
  public InputWindow deleteCharBehindCursorPos()
  {
    int deletePos = cursorPos + visiblePos - 1; // -1 because we delete the character behind the cursor
    if (buffer.length() > deletePos && deletePos >= 0)
    {
      buffer.deleteCharAt(deletePos);
      if (cursorPos > 0)
      {
        cursorPos--;
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
      cursorPos = Math.min(buffer.length(), width - 1);
      refresh();
    }
  }
  
  /**
   * Redraw the input window's characters.
   */
  public void refresh()
  {
    terminal.setCursorVisible(false);
    terminal.moveCursor(0, row);

    int count = Math.min(buffer.length(), width - 1);

    IntStream.range(visiblePos, count + visiblePos)
        .map(buffer::charAt)
        .forEach(c -> terminal.putCharacter((char)c));
    terminal.eraseLineWithCursor(CursorPosition.AFTER_CURSOR);
    terminal.moveCursor(cursorPos, row);
    terminal.setCursorVisible(true);
    terminal.flush();
  }
  
  /**
   * Moves the cursor position to where the input window thinks it should be.
   */
  public void resetCursorPosition()
  {
    terminal.moveCursor(cursorPos, row);
  }

}
