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

public interface Shell
{
	/**
	 * Close the shell freeing up any resources it is using.
	 */
  void close();
  
  /**
   * Adds text to the output window of the shell.  This function is equivalent to calling
   * {@link #addOutput(String, boolean)} passing true as the refresh parameter.
   * 
   * @param string The text to add to the window.
   */
  void addOutput(String string);
  
  /**
   * Adds text to the output window of the shell.
   * 
   * @param string The text to add to the window.
   * @param refresh true to refresh the output window.  If false, the text will be added to the 
   *        internal buffer, but the window contents will not be updated.
   */
  void addOutput(String string, boolean refresh);
  
  /**
   * Redraw the contents of the main output window.  In general, it's probably not necessary to call
   * this function since the {@link #addOutput(String, boolean)} functions allows the caller to specify
   * whether to refresh there.
   */
  void refresh();
  
  /**
   * Register a callback to receive input from the user.
   * 
   * @param inputCallback callback to receive input from the user.
   */
  void registerInputCallback(InputCallback inputCallback);
}
