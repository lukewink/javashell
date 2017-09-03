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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.SignalListener;

import com.lwink.javashell.terminal.api.CursorPosition;
import com.lwink.javashell.terminal.api.KeyPress;
import com.lwink.javashell.terminal.api.KeyPressReceiver;
import com.lwink.javashell.terminal.api.ResizeObserver;
import com.lwink.javashell.terminal.api.TermColor;
import com.lwink.javashell.terminal.api.TermSize;
import com.lwink.javashell.terminal.api.Terminal;
import com.lwink.javashell.util.Preconditions;

public class SshAnsiTerminal implements Terminal, SignalListener
{
  private OutputStream outputStream;
  private Environment sshEnv;
  private Set<ResizeObserver> resizeObservers = new HashSet<>();
  private static final Charset charset = Charset.forName("utf8");
  private WritableByteChannel writeChannel;
  private final TerminalInputReader inputReader;
  private Optional<KeyPressReceiver> keyPressReceiver = Optional.empty();
  private Runnable runOnExit = () -> {};
  private boolean running;
  private TermColor fgColor = TermColor.DEFAULT;
  private TermColor bgColor = TermColor.DEFAULT;
  
  public SshAnsiTerminal(InputStream inputStream, OutputStream outputStream, Environment sshEnv)
  {
    this.outputStream = Preconditions.checkNotNull(outputStream);
    this.sshEnv = Preconditions.checkNotNull(sshEnv);
    this.writeChannel = Channels.newChannel(this.outputStream);
    this.inputReader = new TerminalInputReader(inputStream, charset, (kp) -> onKeyPress(kp));
    this.sshEnv.addSignalListener(this, Signal.WINCH);
  }
  
  public SshAnsiTerminal(InputStream inputStream, OutputStream outputStream, Environment sshEnv, Runnable runOnExit)
  {
  	this(inputStream, outputStream, sshEnv);
  	this.runOnExit = runOnExit;
  }
  
  public void start()
  {
  	this.running = true;
    inputReader.start();
  }
  
  @Override
  public void stop()
  {
    try
    {
    	this.running = false;
    	runOnExit.run();
      inputReader.stop();
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

  @Override
  public TermSize getTerminalSize()
  {
    Map<String, String> envMap = sshEnv.getEnv();
    
    int columns = Integer.parseInt(envMap.get(Environment.ENV_COLUMNS));
    int rows = Integer.parseInt(envMap.get(Environment.ENV_LINES));
    
    return new TermSize(columns, rows);
  }

  @Override
  public void registerResizeObserver(ResizeObserver observer)
  {
    if (resizeObservers.contains(observer) == false)
    {
      resizeObservers.add(observer);
    }
  }

  @Override
  public void moveCursor(int col, int row)
  {
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add(String.valueOf(row + 1))
        .add(';')
        .add(String.valueOf(col + 1))
        .add('H')
        .build();
    writeBytes(bb);
  }

  @Override
  public void setCursorVisible(boolean visible)
  {
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add('?')
        .add('2')
        .add('5')
        .add((visible) ? 'h' : 'l')
        .build();
    writeBytes(bb);
  }

  @Override
  public void putCharacter(char c)
  {
    CharBuffer buf = CharBuffer.allocate(1).put(c);
    writeBytes(charset.encode((CharBuffer)buf.flip()));
  }

  /**
   * Clears the screen.
   */
  @Override
  public void clearScreen()
  {
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add('2')
        .add('J')
        .build();
    writeBytes(bb);
  }

  @Override
  public void enterPrivateMode()
  {
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add('?')
        .add('1')
        .add('0')
        .add('4')
        .add('9')
        .add('h')
        .build();
    writeBytes(bb);
    
  }

  @Override
  public void exitPrivateMode()
  {
    // TODO: This is not complete
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add('?')
        .add('1')
        .add('0')
        .add('4')
        .add('9')
        .add('l')
        .build();
    writeBytes(bb);
    
  }

  /**
   * Flushes the underlying output stream.
   */
  @Override
  public synchronized void flush()
  {
    try
    {
    	if (running)
    	{
    		outputStream.flush();
    	}
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void bell()
  {
    ByteBuffer bb = ByteBufferBuilder.create().add((char)7).build();
    writeBytes(bb);
  }
  
  @Override
  public void setForegroundColor(TermColor color)
  {
  	if (fgColor != color)
  	{
	    ByteBuffer bb = ByteBufferBuilder.create()
	        .csi()
	        .add(String.valueOf(color.getColor() + 30))
	        .add('m')
	        .build();
	    writeBytes(bb);
	    fgColor = color;
  	}
  }

  @Override
  public void setBackgroundColor(TermColor color)
  {
  	if (bgColor != color)
  	{
	    ByteBuffer bb = ByteBufferBuilder.create()
	        .csi()
	        .add(String.valueOf(color.getColor() + 40))
	        .add('m')
	        .build();
	    writeBytes(bb);
	    bgColor = color;
  	}
  }

  @Override
  public void resetAttributes()
  {
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add('0')
        .add('m')
        .build();
    writeBytes(bb);
  }

  @Override
  public void resetColorToDefaults()
  {
  	fgColor = TermColor.DEFAULT;
  	bgColor = TermColor.DEFAULT;
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add("39;49m")
        .build();
    writeBytes(bb);
  }

  /**
   * Called by MINA when the terminal changes size.
   */
  @Override
  public void signal(Signal signal)
  {
    TermSize ts = getTerminalSize();
    
    for (ResizeObserver obs : resizeObservers)
    {
      obs.onResize(ts);
    }
  }
  
  @Override
  public void registerKeyPressReceiver(KeyPressReceiver keyPressReceiver)
  {
    this.keyPressReceiver = Optional.ofNullable(keyPressReceiver);
  }
  

  @Override
  public void eraseLineWithCursor(CursorPosition where)
  {
    ByteBuffer bb = ByteBufferBuilder.create()
        .csi()
        .add(where.equals(CursorPosition.BEFORE_CURSOR) ? '1' : where.equals(CursorPosition.AFTER_CURSOR) ? '0' : '2')
        .add('K')
        .build();
    writeBytes(bb);
  }
  
  @Override
  public void eraseCharacters(int numChars)
  {
  	if (numChars > 0)
  	{
	  	ByteBuffer bb = ByteBufferBuilder.create()
	        .csi()
	        .add(String.valueOf(numChars))
	        .add('X')
	        .build();
	    writeBytes(bb);
  	}
  }
  
  @Override
  public void deleteCharacters(int numChars)
  {
  	if (numChars > 0)
  	{
	  	ByteBuffer bb = ByteBufferBuilder.create()
	        .csi()
	        .add(String.valueOf(numChars))
	        .add('P')
	        .build();
	    writeBytes(bb);
  	}
  }
  
  /**
   * Write bytes to the output stream.  All writes to the output stream should happen 
   * through this function as it is the only synchronized function.
   * 
   * @param bytes An array of bytes to write to the terminal output stream.
   */
  private synchronized void writeBytes(ByteBuffer buf)
  {
    try
    {
    	if (running)
    	{
    		writeChannel.write(buf);
    	}
    } 
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Called by the TerminalInputReceiver when a new key press is available.  This function is
   * called by a separate thread.
   * 
   * @param keyPress Key press received by the terminal server.
   */
  protected void onKeyPress(KeyPress keyPress)
  {
    if (keyPressReceiver.isPresent())
    {
      keyPressReceiver.get().onKeyPress(keyPress);
    }
  }
  
  /**
   * A class to build ByteBuffers used to write to the terminal.
   */
  static class ByteBufferBuilder
  {
    ByteBuffer buffer = ByteBuffer.allocate(32);
    
    public ByteBufferBuilder()
    {
    }
    
    public static ByteBufferBuilder create()
    {
      return new ByteBufferBuilder();
    }
    
    /**
     * Add the Control Sequence Initiator byte sequence to the buffer.
     * 
     * @return This ByteBufferBuilder for chaining.
     */
    public ByteBufferBuilder csi()
    {
      add(0x1b);
      add('[');
      return this;
    }
    
    /**
     * @param b Byte to add to the buffer
     * @return This ByteBufferBuilder for chaining.
     */
    public ByteBufferBuilder add(byte b)
    {
      buffer.put(b);
      return this;
    }
    
    /**
     * Adds a byte (in integer form) to the byte buffer.
     * 
     * @param i Integer to add to the byte buffer.
     * @return This ByteBufferBuilder for chaining.
     */
    public ByteBufferBuilder add(int i)
    {
      Preconditions.checkArgument(i < Byte.MAX_VALUE && i > Byte.MIN_VALUE);

      return add((byte)i);
    }
    
    /**
     * Adds a ASCII character to the byte buffer.  Terminal commands use ASCII characters
     * so it is useful to have a fast function to convert ASCII characters to bytes.  This
     * function should not be used for user input since that may contains characters that 
     * are not ASCII.
     * 
     * @param c A ASCII character to add.
     * @return This ByteBufferBuilder for chaining.
     */
    public ByteBufferBuilder add(char c)
    {
      Preconditions.checkArgument(c < Byte.MAX_VALUE && c > Byte.MIN_VALUE);
      return add((byte)c);
    }
    
    /**
     * Adds a encoded String to the buffer.  The encoding that is used is the same
     * as what is used in the SshAnsiTerminal class.
     * 
     * @param s String to add to the buffer.
     * @return This ByteBufferBuilder for chaining.
     */
    public ByteBufferBuilder add(String s)
    {
      buffer.put(s.getBytes(charset));
      return this;
    }
    
    /**
     * @return The built ByteBuffer.
     */
    public ByteBuffer build()
    {
      return (ByteBuffer)buffer.flip();
    }
  }
}
