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
package com.lwink.sshterm.terminal.api;

public class KeyPress
{
  private Type type;
  private char ch;
  
  public KeyPress(char c)
  {
    this.ch = c;
    this.type = Type.NORMAL;
  }
  
  public KeyPress(Type type)
  {
    this.type = type;
  }
  
  public KeyPress(Type type, char c)
  {
    this.ch = c;
    this.type = type;
  }
  
  public char getChar()
  {
    return ch;
  }
  
  public Type getType()
  {
    return type;
  }
  
  public static Builder builder()
  {
    return new Builder();
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
    READ_ERROR,
    EOF
  }
  
  public static class Builder
  {
    private char ch;
    private Type type;
    
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
    
    public KeyPress build()
    {
      return new KeyPress(type, ch);
    }
  }
}
