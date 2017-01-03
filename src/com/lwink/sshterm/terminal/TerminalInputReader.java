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
package com.lwink.sshterm.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.sshterm.terminal.api.KeyPress;
import com.lwink.sshterm.terminal.api.KeyPress.Type;
import com.lwink.sshterm.terminal.api.KeyPressReceiver;
import com.lwink.util.Preconditions;

/**
 * Class that will read characters asynchronously from a terminal's input stream and
 * push them to a callback interface.
 */
public class TerminalInputReader
{
  public static final Logger LOG = LoggerFactory.getLogger(TerminalInputReader.class);
  private final InputStreamReader reader;
  private final Executor executor;
  private final KeyPressReceiver receiver;
  private boolean run = false;
  
  /**
   * Create a new TerminalInputReader. 
   * 
   * @param inputStream The input stream of the terminal.
   * @param charset The charset of the data that is coming from the terminal.
   * @param receiver An interface that will receive characters that are sent to the terminal.
   */
  public TerminalInputReader(InputStream inputStream, Charset charset, KeyPressReceiver receiver)
  {
    reader = new InputStreamReader(inputStream, charset);
    executor = Executors.newSingleThreadExecutor();
    this.receiver = Preconditions.checkNotNull(receiver);
  }
  
  /**
   * Start the thread that reads terminal input.
   */
  public void start()
  {
    run = true;
    executor.execute(() -> getKeyPresses());
  }
  
  /**
   * Stops the thread that reads terminal input.
   */
  public void stop()
  {
    run = false;
  }
  
  /**
   * Gets key presses in a loop and pushes them to the KeyPressReceiver interface.  This
   * function will not return until stop() is called.
   */
  protected void getKeyPresses()
  {
    try
    {
      Vector<Integer> buf = new Vector<>();
      while (run)
      {
        int input = reader.read();
        LOG.debug("Received terminal input: {}", input);
        if (input == -1 || input == 3 || input == 4)
        {
          receiver.onKeyPress(KeyPress.builder().type(input == -1 ? Type.READ_ERROR : Type.EOF).build());
          reader.close();
          break;  // Input stream is closed, we can exit the read thread
        }
        if (buf.size() > 0)
        {
          buf.add(input);
          checkForSequence(buf);
        }
        else if (input == 27) // ESCAPE
        {
          buf.clear();
          buf.add(input);
        }
        else if (input == 127 || input == 8)
        {
          receiver.onKeyPress(KeyPress.builder().type(Type.BACKSPACE).build());
        }
        else
        {
          receiver.onKeyPress(KeyPress.builder().type(Type.NORMAL).ch((char)input).build());
        }
      }
    }
    catch (IOException e)
    {
      LOG.warn("Got an exception reading from input stream", e);
      throw new RuntimeException(e);
    }
    LOG.info("Input thread is stopping");
  }
  
  protected void checkForSequence(Vector<Integer> seq)
  {
    Preconditions.checkArgument(seq.size() > 1 && seq.get(0) == 27);
    switch(seq.get(1))
    {
    case 91: // ASCII '['.  This means we have received a CSI.
      if (seq.size() == 2)
      {
        // We don't have the full sequence yet
        return;
      }
      switch(seq.get(2))
      {
      case (int)'A':
        receiver.onKeyPress(KeyPress.builder().type(Type.ARROW_UP).build());
        break;
      case (int)'B':
        receiver.onKeyPress(KeyPress.builder().type(Type.ARROW_DOWN).build());
        break;
      case (int)'C':
        receiver.onKeyPress(KeyPress.builder().type(Type.ARROW_RIGHT).build());
        break;
      case (int)'D': // Left Arrow
        receiver.onKeyPress(KeyPress.builder().type(Type.ARROW_LEFT).build());
        break;
      
      default:
        receiver.onKeyPress(KeyPress.builder().type(Type.INVALID).build());
      }
      break;
    default:
      receiver.onKeyPress(KeyPress.builder().type(Type.INVALID).build());
    }

    seq.clear();
  }
}
