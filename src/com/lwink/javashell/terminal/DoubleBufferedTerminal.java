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
package com.lwink.javashell.terminal;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.lwink.javashell.terminal.api.CursorPosition;
import com.lwink.javashell.terminal.api.KeyPressReceiver;
import com.lwink.javashell.terminal.api.ResizeObserver;
import com.lwink.javashell.terminal.api.TermColor;
import com.lwink.javashell.terminal.api.TermSize;
import com.lwink.javashell.terminal.api.Terminal;

/**
 * This class creates a buffer on top of an existing Terminal.  It adds efficiency to the
 * terminal because it will only write to areas of the screen that require it.
 */
public class DoubleBufferedTerminal implements Terminal, ResizeObserver
{
  private Terminal terminal;
  private TermSize termSize;
  private int[][] buffer;
  private int[][] current;
  private int cursorRow;
  private int cursorCol;
  
  public DoubleBufferedTerminal(Terminal underlyingTerminal)
  {
    this.terminal = underlyingTerminal;
    this.termSize = terminal.getTerminalSize();
    this.buffer = new int[termSize.getRows()][termSize.getColumns()];
    this.current = new int[termSize.getRows()][termSize.getColumns()];
    terminal.registerResizeObserver(this);
  }
  
  @Override
  public void stop()
  {
    terminal.stop();
  }

  @Override
  public TermSize getTerminalSize()
  {
    return terminal.getTerminalSize();
  }

  @Override
  public void registerResizeObserver(ResizeObserver observer)
  {
    terminal.registerResizeObserver(observer);
  }

  @Override
  public void moveCursor(int col, int row)
  {
    cursorRow = row;
    cursorCol = col;
    terminal.moveCursor(col, row);
  }

  @Override
  public void setCursorVisible(boolean visible)
  {
    terminal.setCursorVisible(visible);
  }

  @Override
  public void putCharacter(char c)
  {
    buffer[cursorRow][cursorCol++] = c;
  }

  @Override
  public void clearScreen()
  {
    IntStream.range(0, termSize.getRows())
      .forEach(r -> Arrays.fill(buffer[r], 0));
  }

  @Override
  public void enterPrivateMode()
  {
    terminal.enterPrivateMode();
  }

  @Override
  public void exitPrivateMode()
  {
    terminal.exitPrivateMode();
  }

  @Override
  public void flush()
  {
    terminal.flush();
  }

  @Override
  public void bell()
  {
    terminal.bell();
  }

  @Override
  public void eraseLineWithCursor(CursorPosition where)
  {
    switch (where)
    {
    case BEFORE_CURSOR:
      IntStream.range(0, cursorCol + 1).forEach(c -> buffer[cursorRow][c] = 0);
      break;
    case AFTER_CURSOR:
      IntStream.range(cursorCol, termSize.getColumns()).forEach(c -> buffer[cursorRow][c] = 0);
      break;
    case BEFORE_AND_AFTER:
      IntStream.range(0, termSize.getColumns()).forEach(c -> buffer[cursorRow][c] = 0);
      break;
    }
  }

  @Override
  public void setForegroundColor(TermColor color)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void setBackgroundColor(TermColor color)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void resetAttributes()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void resetColorToDefaults()
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onResize(TermSize terminalSize)
  {
    int minRows = Math.min(terminalSize.getRows(), termSize.getRows());
    
    int newBuffer[][] = new int[terminalSize.getRows()][];
    int newCurrent[][] = new int[terminalSize.getRows()][];
    
    IntStream.range(0, minRows)
        .forEach(r -> Arrays.copyOf(buffer[r], terminalSize.getColumns()));
    
    IntStream.range(0, minRows)
        .forEach(r -> Arrays.copyOf(current[r], terminalSize.getColumns()));
    
    this.buffer = newBuffer;
    this.current = newCurrent;
    
    this.termSize = terminalSize;
  }

	@Override
	public void registerKeyPressReceiver(KeyPressReceiver keyPressReceiver)
	{
		terminal.registerKeyPressReceiver(keyPressReceiver);
	}

	@Override
	public void eraseCharacters(int numChars)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCharacters(int numChars)
	{
		// TODO Auto-generated method stub
		
	}
}
