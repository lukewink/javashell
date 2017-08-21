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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.javashell.terminal.api.KeyPress;
import com.lwink.javashell.terminal.api.KeyPress.Type;
import com.lwink.javashell.terminal.api.KeyPressReceiver;
import com.lwink.javashell.util.Preconditions;

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
  private TermInfo termInfo;
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
    this.termInfo = new TermInfo(receiver);
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
        termInfo.addCh((char)input);
      }
    }
    catch (IOException e)
    {
      LOG.warn("Got an exception reading from input stream", e);
      throw new RuntimeException(e);
    }
    LOG.info("Input thread is stopping");
  }
}
