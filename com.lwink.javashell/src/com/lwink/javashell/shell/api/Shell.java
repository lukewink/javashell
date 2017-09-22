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
package com.lwink.javashell.shell.api;

import com.lwink.javashell.terminal.api.TermSize;

public interface Shell
{
	/**
	 * Close the shell freeing up any resources it is using.
	 */
  void close();
  
  /**
   * Adds text to the output window of the shell.  This function is equivalent to calling
   * {@link #addOutput(String, TextAttributes, boolean, boolean)} passing true as the newline and refresh parameters.
   * 
   * @param string The text to add to the window.
   */
  default void addOutput(String string)
  {
  	addOutput(string, null);
  }
  
  /**
   * Adds text to the output window of the shell.  This function is equivalent to calling
   * {@link #addOutput(String, TextAttributes, boolean, boolean)} passing true as the newline and refresh parameters.
   * 
   * @param string The text to add to the window.
   * @param attributes The attributes to associate with the added text.  If null, then default attributes
   *        will be used.
   */
  void addOutput(String string, TextAttributes attributes);
  
  /**
   * Adds text to the output window of the shell.
   * 
   * @param string The text to add to the window.
   * @param addNewLine Whether to add a new line to the end of the input
   * @param refresh true to refresh the output window.  If false, the text will be added to the 
   *        internal buffer, but the window contents will not be updated.
   */
  default void addOutput(String string, boolean addNewLine, boolean refresh)
  {
  	addOutput(string, null, addNewLine, refresh);
  }
  
  /**
   * Adds text to the output window of the shell.
   * 
   * @param string The text to add to the window.
   * @param attributes The attributes to associate with the added text. If null, then default attributes
   *        will be used.
   * @param addNewLine Whether to add a new line to the end of the input
   * @param refresh true to refresh the output window.  If false, the text will be added to the 
   *        internal buffer, but the window contents will not be updated.
   */
  void addOutput(String string, TextAttributes attributes, boolean addNewLine, boolean refresh);
  
  /**
   * Redraw the contents of the main output window.  In general, it's probably not necessary to call
   * this function since the {@link #addOutput(String, boolean, boolean)} functions allows the caller
   * to specify whether to refresh there.
   */
  void refresh();
  
  /**
   * Register a callback to receive input from the user.
   * 
   * @param inputCallback callback to receive input from the user.
   */
  void registerInputCallback(InputCallback inputCallback);
  
  /**
   * Set a new value to act as the input prompt.
   * 
   * @param newPrompt A String to set as the new input prompt.
   */
  void setPrompt(String newPrompt);
  
  /**
   * Get the size of the display window.
   * 
   * @return The size of the display window
   */
  TermSize getOutputWindowSize();
}
