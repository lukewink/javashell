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

public class KeyPress
{
  private Type type;
  private char ch;
  private boolean shift;
  private boolean ctrl;
  
  public KeyPress(char c)
  {
    this.ch = c;
    this.type = Type.NORMAL;
    this.shift = false;
    this.ctrl = false;
  }
  
  public KeyPress(Type type)
  {
    this.type = type;
  }
  
  public KeyPress(Type type, char c, boolean shift, boolean ctrl)
  {
    this.ch = c;
    this.type = type;
    this.shift = shift;
    this.ctrl = ctrl;
  }
  
  public char getChar()
  {
    return ch;
  }
  
  public Type getType()
  {
    return type;
  }
  
  public boolean shift()
  {
  	return shift;
  }
  
  public boolean ctrl()
  {
  	return ctrl;
  }
  
  public static Builder builder()
  {
    return new Builder();
  }
  
  public String toString()
  {
  	StringBuilder sb = new StringBuilder();
  	sb.append("Type: ").append(type.toString());
  	sb.append(" Shift: ").append(shift);
  	sb.append(" Ctrl: ").append(ctrl);
  	return sb.toString();
  }
  
  public enum Type
  {
    NORMAL,
    ARROW_UP,
    ARROW_DOWN,
    ARROW_LEFT,
    ARROW_RIGHT,
    BACKSPACE,
    INVALID, //Used for when an invalid escape sequence has been issued
    PAGE_UP,
    PAGE_DOWN,
    READ_ERROR,
    EOF
  }
  
  public static class Builder
  {
    private char ch;
    private Type type;
    private boolean shift = false;
    private boolean ctrl = false;
    
    public Builder type(Type type)
    {
      this.type = type;
      return this;
    }
    
    public Builder ch(char ch)
    {
      this.ch = ch;
      return this;
    }
    
    public Builder shift(boolean shift)
    {
    	this.shift = shift;
    	return this;
    }
    
    public Builder ctrl(boolean ctrl)
    {
    	this.ctrl = ctrl;
    	return this;
    }
    
    public KeyPress build()
    {
      return new KeyPress(type, ch, shift, ctrl);
    }
  }
}
