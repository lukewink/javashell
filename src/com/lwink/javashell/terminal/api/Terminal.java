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
package com.lwink.javashell.terminal.api;

public interface Terminal
{
  
  /**
   * Stop the terminal closing its input and output streams.
   */
  public void stop();
  
  /**
   * Get the size of the terminal.
   * 
   * @return The size of the terminal.
   */
  public TermSize getTerminalSize();
  
  /**
   * Register a callback to receive notifications when the terminal has been resized.
   * 
   * @param observer Callback to be notified.
   */
  public void registerResizeObserver(ResizeObserver observer);
  
  /**
   * Register an interface to receive key presses from the terminal's input stream.
   * 
   * @param keyPressReceiver Interface to receive keypresses.
   */
  void registerKeyPressReceiver(KeyPressReceiver keyPressReceiver);
  
  /**
   * Moves the terminal's cursor.
   * 
   * @param col The column to move the cursor to.
   * @param row The row to move the cursor to.
   */
  public void moveCursor(int col, int row);
  
  /**
   * Sets whether the terminal cursor is visible or not.
   * 
   * @param visible If true, then the cursor will be made visible, otherwise it will not
   *        be visible.
   */
  public void setCursorVisible(boolean visible);
  
  /**
   * Puts a character on the terminal at the current cursor position.
   * 
   * @param c The character to be written.
   */
  public void putCharacter(char c);
  
  /**
   * Clears the terminal screen of any visible characters.
   */
  public void clearScreen();
  
  /**
   * Will attempt to enter the terminal into private mode.  This (if supported by the terminal) will
   * give the application a clean screen to write on, and it may save the original state of the terminal
   * so that when private mode is exited, the terminal will show the previous screen.
   */
  public void enterPrivateMode();
  
  /**
   * Exit out of private mode.  This will revert the terminal to its previous state if possible.
   */
  public void exitPrivateMode();
  
  /**
   * Flushes the underlying output stream.
   */
  public void flush();
  
  /**
   * Send a bell notification to the terminal.
   */
  public void bell();
  
  /**
   * Deletes all or part of the row that the cursor is on.
   * 
   * @param where Specifies what part of the line is deleted.  Either before the cursor, after the cursor,
   *        or the entire line.
   */
  public void eraseLineWithCursor(CursorPosition where);
  
  /**
   * Sets the foreground color that will be used for characters drawn in the future.
   * 
   * @param color The foreground color to be used.
   */
  public void setForegroundColor(TermColor color);
  
  /**
   * Sets the background color that will be used for characters drawn in the future.
   * 
   * @param color The background color to be used.
   */
  public void setBackgroundColor(TermColor color);
  
  /**
   * Reset all text attributes.
   */
  public void resetAttributes();
  
  /**
   * Resets only the colors to their default foreground and background colors.
   */
  public void resetColorToDefaults();
}
