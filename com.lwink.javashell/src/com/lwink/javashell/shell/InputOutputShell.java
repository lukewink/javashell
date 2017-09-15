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
import com.lwink.javashell.shell.api.TextAttributes;
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
  
  private boolean closed = false;
  
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
  	closed = true;
    terminal.exitPrivateMode();
    terminal.stop();
  }
  
  @Override
  public void addOutput(String string, TextAttributes attributes)
  {
    addOutput(string, attributes, true, true);
  }
  
  @Override
  public void addOutput(String string, TextAttributes attributes, boolean addNewLine, boolean refresh)
  {
  	checkShell();
  	if (addNewLine)
  	{
  		string += '\n';
  	}
    mainWindow.addText(string, attributes, false); // Don't refresh here.  We will do so below if needed.
    inputWindow.resetCursorPosition();
    if (refresh)
    {
      refresh();
    }
  }
  
  @Override
  public void refresh()
  {
  	checkShell();
    mainWindow.refresh();
    inputWindow.resetCursorPosition();
    terminal.flush();
  }
  
  @Override
  public void registerInputCallback(InputCallback inputCallback)
  {
    this.inputCallback = Optional.ofNullable(inputCallback);
  }
  
  @Override
	public void setPrompt(String newPrompt)
	{
  	checkShell();
		inputWindow.setPrompt(newPrompt);
		inputWindow.refresh();
		terminal.flush();
	}
  
  /**
   * Checks to ensure the shell has not been closed.  If the shell has been closed,
   * a runtime exception is thrown.
   */
  protected void checkShell()
  {
  	if (closed)
  	{
  		throw new RuntimeException("Shell has been closed");
  	}
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
    
    // Scroll all the way back down.
    mainWindow.setScrollPosition(0);
    
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
   * 
   * @param keyPress Information on the key that was pressed
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
    	if (!keyPress.shift() && !keyPress.ctrl())
    	{
    		inputWindow.cursorLeft();
    	}
      break;
    case ARROW_RIGHT:
    	if (!keyPress.shift() && !keyPress.ctrl())
    	{
    		inputWindow.cursorRight();
    	}
      break;
    case ARROW_UP:
    	if (!keyPress.shift() && !keyPress.ctrl())
      {
        String s = commandHistory.back();
        inputWindow.setText(s).refresh();
      }
    	else if (keyPress.shift() && keyPress.ctrl())
    	{
    		// Up one line
    		mainWindow.scrollUp(1);
    	}
      break;
    case ARROW_DOWN:
    	if (!keyPress.shift() && !keyPress.ctrl())
      {
        String s = commandHistory.forward();
        inputWindow.setText(s).refresh();
      }
    	else if (keyPress.shift() && keyPress.ctrl())
    	{
    		mainWindow.scrollDown(1);
    	}
      break;
    case BACKSPACE:
      inputWindow.deleteCharBehindCursorPos().refresh();;
      break;
    case CONTROL:
    	handleControl(keyPress);
    	break;
    case EOF:
      close();
      break;
    case PAGE_DOWN:
      mainWindow.pageDown();
    	break;
    case PAGE_UP:
      mainWindow.pageUp();
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
  
  protected void handleControl(KeyPress keyPress)
  {
  	if (!keyPress.shift() && keyPress.ctrl())
  	{
  		switch (keyPress.getChar())
  		{
  		case 'a':
  			inputWindow.moveCursorToBeginningOfLine();
  			break;
  		case 'e':
  			inputWindow.moveCursorToEndOfLine();
  			break;
  		}
  	}
  }
}
