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
package com.lwink.sshterm.shell.api;

/**
 * Interface that gets passed to a Shell in order to receive input from the terminal.
 */
public interface InputCallback
{
  /**
   * Called when a line of input has been entered by a terminal followed by a new line.
   * 
   * @param input Input from the terminal.
   * @param shell The shell that the input came from.
   */
  void inputReady(String input, Shell shell);
}
