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

public interface DisplayBuffer
{
  public void addText(String text);
  public void addTextLine(String text);
  public void resizeWidth(int newWidth);
  public void draw(Terminal terminal, int bufferRow);
  
  /**
   * Returns the number of lines in the buffer with valid content.
   * 
   * @return The number of lines in the buffer with content.
   */
  public int getNumberOfRowsWithContent();
}
