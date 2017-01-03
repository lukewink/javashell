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
package com.lwink.sshterm.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.sshterm.server.api.TerminalClosedListener;
import com.lwink.sshterm.server.api.TerminalCreatedListener;
import com.lwink.sshterm.server.api.TerminalServer;
import com.lwink.sshterm.terminal.SshAnsiTerminal;

public class SshTerminalServer implements TerminalServer
{
  public static final Logger LOG = LoggerFactory.getLogger(SshTerminalServer.class);
  private SshServer sshd;
  private Optional<TerminalCreatedListener> terminalCreatedListener = Optional.empty();
  private Optional<TerminalClosedListener> terminalClosedListener = Optional.empty();
  private int port;
  private File keyFile;
  
  public SshTerminalServer(int port, File keyFile)
  {
  	this.port = port;
  	this.keyFile = keyFile;
  }
  
  @Override
  public void start(TerminalCreatedListener terminalCreatedListener, TerminalClosedListener terminalClosedListener)
  {
  	this.terminalCreatedListener = Optional.ofNullable(terminalCreatedListener);
  	this.terminalClosedListener = Optional.ofNullable(terminalClosedListener);
    try
    {
      LOG.info("Starting ShellServer");
      sshd = SshServer.setUpDefaultServer();
      sshd.setPort(port);
      sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyFile));
      sshd.setPasswordAuthenticator(getPasswordAuthenticator());
      sshd.setShellFactory(new ShellFactory());
      sshd.start();
    }
    catch (IOException e)
    {
      LOG.error("Failed to start", e);
      throw new RuntimeException(e);
    }
    LOG.info("ShellServer has been started");
  }
  
  @Override
  public void stop()
  {
    LOG.info("Stopping ShellServer");
    try
    {
      sshd.stop();
    }
    catch (IOException e)
    {
      LOG.error("Failed to stop ShellServer", e);
    }
  }
  
  protected PasswordAuthenticator getPasswordAuthenticator()
  {
  	return (user, password, server) -> true;
  }
  
  /**
   * Factory class to create SSH Command objects by Apache MINA.
   * 
   * 
   */
  class ShellFactory implements Factory<Command>
  {

    @Override
    public Command create()
    {
      System.out.println("Creating new command");
      return new SshShell();
    }
  }
  
  /**
   * Represents bare SSH Shell. 
   *
   * A SshShell contains the SSH shell's input and output streams, as well as the
   * terminal representation.
   */
  class SshShell implements Command
  {   
    private InputStream inputStream;
    private OutputStream outputStream;
    private SshAnsiTerminal terminal;

    /**
     * Called by MINA when the shell has been terminated.
     */
    @Override
    public void destroy()
    {
      terminalClosedListener.ifPresent(l -> l.onTerminalClosed(terminal));
      terminal.stop();
    }

    /**
     * Used to set the error OutputStream.
     */
    @Override
    public void setErrorStream(OutputStream outputStream)
    {
    }

    /**
     * Sets the ExitCallback that the shell must use to notify the MINA server
     * that the shell has exited.
     */
    @Override
    public void setExitCallback(ExitCallback exitCallback)
    {
      System.out.println("setExitCallback"); 
    }

    /**
     * The shell's InputStream.
     */
    @Override
    public void setInputStream(InputStream inputStream)
    {
      this.inputStream = inputStream;
    }

    /**
     * The shell's OutputStream
     */
    @Override
    public void setOutputStream(OutputStream outputStream)
    {
      this.outputStream = outputStream;
    }

    /**
     * Called by the server when the shell has been started.
     * 
     * This function is called after the streams have been set.
     */
    @Override
    public void start(Environment env) throws IOException
    { 
      terminal = new SshAnsiTerminal(inputStream, outputStream, env);
      terminal.start();
      
      terminalCreatedListener.ifPresent(l -> l.onTerminalCreated(terminal));
    }
    
  }

}
