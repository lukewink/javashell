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

import java.util.LinkedList;
import java.util.List;

public class CommandHistory
{
  private List<String> history = new LinkedList<>();
  private int capacity;
  private int index = -1;
  
  public CommandHistory(int capacity)
  {
    this.capacity = capacity;
  }
  
  public void add(String cmd)
  {
  	index = -1;
  	if (history.size() > 0 && history.get(0).equals(cmd))
  	{
  		// Don't add a command if it the same as the previous one
  		return;
  	}
    history.add(0, cmd); // Add the new command to the beginning of the history
    if (history.size() > capacity)
    {
      history.remove(history.size() - 1); // Remove the last item
    }
  }
  
  public String back()
  { 
    if (index < history.size() - 1)
    {
      index++;
    }
    return history.get(index);
  }
  
  public String forward()
  {
    if (index > 0)
    {
      index--;
      return history.get(index);
    }
    else if (index == 0)
    {
      index--;
    }
    return "";
  }
}
