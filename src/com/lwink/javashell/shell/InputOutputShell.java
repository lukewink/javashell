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
package com.lwink.javashell.shell;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.javashell.shell.api.InputCallback;
import com.lwink.javashell.shell.api.Shell;
import com.lwink.javashell.shell.window.InputWindow;
import com.lwink.javashell.shell.window.Window;
import com.lwink.javashell.terminal.api.KeyPress;
import com.lwink.javashell.terminal.api.TermSize;
import com.lwink.javashell.terminal.api.Terminal;

/**
 * Implementation of a simple shell.  This shell has 2 windows, a 1 line input window at the bottom
 * and a multi-line output window on the top.
 */
public class InputOutputShell implements Shell
{
  public static final Logger LOG = LoggerFactory.getLogger(InputOutputShell.class);
  
  /** The underlying Terminal that this shell sits on top of */
  private Terminal terminal;
  
  /** The main display window */
  private Window mainWindow;
  
  /** The number of visible columns in the terminal */
  int columns;
  
  /** The number of visible rows in the terminal */
  int rows;
  
  /** The terminal input window */
  private InputWindow inputWindow;
  
  /** Holds a history of previously run commands */
  private CommandHistory commandHistory = new CommandHistory(100);
  
  /** A callback to receive terminal input */
  private Optional<InputCallback> inputCallback = Optional.empty();
  
  public InputOutputShell(Terminal terminal)
  {
    this.terminal = terminal;
    
    TermSize size = terminal.getTerminalSize();
    this.mainWindow = new Window(terminal, size.getColumns(), size.getRows() - 1, 0, 0);
    this.inputWindow = new InputWindow(terminal, size.getColumns(), size.getRows() - 1);
    onResize(size);
    terminal.registerResizeObserver(this::onResize);
    terminal.registerKeyPressReceiver(this::onKeyPress);


    terminal.enterPrivateMode();
    terminal.clearScreen();
    inputWindow.refresh();
    terminal.flush();
  }
  
  @Override
  public void close()
  {
    terminal.exitPrivateMode();
    terminal.stop();
  }
  
  @Override
  public void addOutput(String string)
  {
    addOutput(string, true);
  }
  
  @Override
  public void addOutput(String string, boolean refresh)
  {
    mainWindow.addTextWithNewLine(string, false);
    inputWindow.resetCursorPosition();
    if (refresh)
    {
      refresh();
    }
  }
  
  @Override
  public void refresh()
  {
    mainWindow.refresh();
    inputWindow.resetCursorPosition();
    terminal.flush();
  }
  
  @Override
  public void registerInputCallback(InputCallback inputCallback)
  {
    this.inputCallback = Optional.ofNullable(inputCallback);
  }
  
  /**
   * Called when there is input ready to be processed
   */
  protected void inputReady()
  { 
    String input = inputWindow.getWindowContents();
    commandHistory.add(input);
    if (inputCallback.isPresent())
    {
      inputCallback.get().inputReady(input, this);
    }
    inputWindow.clearWindowContents();
    inputWindow.refresh();
  }
  
  /**
   * Called by the Terminal when it detects the terminal size has changed.
   * 
   * @param newSize The new terminal size.
   */
  protected void onResize(TermSize newSize)
  {
    LOG.debug("Terminal has been resized {}x{}", newSize.getColumns(), newSize.getRows());
    columns = newSize.getColumns();
    rows = newSize.getRows();
    mainWindow.resize(columns, rows - 1);
    inputWindow.resize(columns, rows-1);
  }

  /**
   * Called by the Terminal when a key press has been received.
   */
  protected void onKeyPress(KeyPress keyPress)
  {
    switch (keyPress.getType())
    {
    case NORMAL:
      char c = keyPress.getChar();
      if (c == '\n' || c == '\r')
      {
        inputReady();
      }
      else
      {
        inputWindow.addChar(c).refresh();
      }
      break;
    case ARROW_LEFT:
      inputWindow.cursorLeft();
      break;
    case ARROW_RIGHT:
      inputWindow.cursorRight();
      break;
    case ARROW_UP:
      {
        String s = commandHistory.back();
        inputWindow.setText(s).refresh();;
      }
      break;
    case ARROW_DOWN:
      {
        String s = commandHistory.forward();
        inputWindow.setText(s).refresh();;
      }
      break;
    case BACKSPACE:
      inputWindow.deleteCharBehindCursorPos().refresh();;
      break;
    case EOF:
      close();
      break;
    case READ_ERROR:
      break;
    case INVALID:
      terminal.bell();
      break;
    default:
      break;
    }   
  }
}