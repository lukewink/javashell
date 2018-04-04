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
package com.lwink.javashell.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.sshd.common.Factory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.kex.KeyExchange;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.javashell.server.api.Authenticator;
import com.lwink.javashell.server.api.TerminalClosedListener;
import com.lwink.javashell.server.api.TerminalCreatedListener;
import com.lwink.javashell.server.api.TerminalServer;
import com.lwink.javashell.terminal.SshAnsiTerminal;
import com.lwink.javashell.util.Preconditions;

public class SshTerminalServer implements TerminalServer
{
  public static final Logger LOG = LoggerFactory.getLogger(SshTerminalServer.class);
  private SshServer sshd;
  private Optional<TerminalCreatedListener> terminalCreatedListener = Optional.empty();
  private Optional<TerminalClosedListener> terminalClosedListener = Optional.empty();
  private int port;
  private File keyFile;
  private Authenticator authenticator;
  private Collection<String> supportedCiphers;
  private Collection<NamedFactory<KeyExchange>> supportedKeyExchangeAlgorithms;
  private Collection<String> supportedMacs;
  private boolean started;
  
  public SshTerminalServer(int port, File keyFile, Authenticator authenticator)
  {
  	this.port = port;
  	this.keyFile = keyFile;
  	this.authenticator = authenticator;
  	this.sshd = SshServer.setUpDefaultServer();
  	this.supportedCiphers = sshd.getCipherFactoriesNames();
  	this.supportedKeyExchangeAlgorithms = sshd.getKeyExchangeFactories();
  	this.supportedMacs = sshd.getMacFactoriesNames();
  	this.started = false;
  }
  
  @Override
  public void start(TerminalCreatedListener terminalCreatedListener, TerminalClosedListener terminalClosedListener)
  {
  	this.terminalCreatedListener = Optional.ofNullable(terminalCreatedListener);
  	this.terminalClosedListener = Optional.ofNullable(terminalClosedListener);
    try
    {
      LOG.info("Starting ShellServer");
      
      AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider(keyFile);
      hostKeyProvider.setAlgorithm(KeyUtils.RSA_ALGORITHM);
      
      sshd.setPort(port);
      sshd.setKeyPairProvider(hostKeyProvider);
      sshd.setPasswordAuthenticator(this::authenticate);
      sshd.setShellFactory(new ShellFactory());
      
      LOG.info("Cipher factories: {}", sshd.getCipherFactoriesNameList());
      
      LOG.info("Key exchange factories: {}", sshd.getKeyExchangeFactories());
      LOG.info("Compression factories: {}", sshd.getCompressionFactoriesNameList());
      LOG.info("MAC factories: {}", sshd.getMacFactoriesNameList());

      sshd.start();
    }
    catch (IOException e)
    {
      LOG.error("Failed to start", e);
      throw new RuntimeException(e);
    }
    started = true;
    LOG.info("ShellServer has been started");
  }
  
  @Override
	public Collection<String> getSupportedKeyExchangeAlgorithms()
  {
  	return supportedKeyExchangeAlgorithms
  			.stream()
  			.map(nf -> nf.getName())
  			.collect(Collectors.toList());
  }
  
  @Override
	public void setKeyExchangeAlgorithms(Collection<String> algorithms)
  {
  	Preconditions.checkState(!started);
  	sshd.setKeyExchangeFactories(supportedKeyExchangeAlgorithms
  			.stream()
  			.filter(nf -> algorithms.contains(nf.getName()))
  			.collect(Collectors.toList()));
  }
  
  @Override
	public Collection<String> getSupportedCiphers()
  {
  	return supportedCiphers;
  }
  
  @Override
	public void setCiphers(Collection<String> ciphers)
  {
  	Preconditions.checkState(!started);
  	sshd.setCipherFactoriesNames(ciphers);
  }
  
  @Override
  public void setMacs(Collection<String> macs)
  {
  	sshd.setMacFactoriesNames(macs);
  }
	
  @Override
	public Collection<String> getSupportedMacs()
	{
  	return supportedMacs;
	}
  
  @Override
  public synchronized void waitForStop() throws InterruptedException
  {
  	this.wait();
  }
  
  @Override
  public synchronized void stop()
  {
    LOG.info("Stopping ShellServer");
    try
    {
      sshd.stop();
      this.notifyAll();
    }
    catch (IOException e)
    {
      LOG.error("Failed to stop ShellServer", e);
    }
    started = false;
  }
  
  /**
   * Authenticates a MINA SSH connection.
   * 
   * @param username The user attempting to connect
   * @param password The password provided by the user
   * @param session Information about the underlying session
   * @return true if the connection can be authenticated.
   * @throws PasswordChangeRequiredException To tell the user that the password must be changed.
   */
  protected boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException
	{
		return authenticator.authenticate(username, password);
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
		private ExitCallback exitCallback;

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
      this.exitCallback = exitCallback;
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
    	Runnable runOnExit = () -> {
    		exitCallback.onExit(0, "The terminal has quit");
    	};
      terminal = new SshAnsiTerminal(inputStream, outputStream, env, runOnExit);
      terminal.start();
      
      terminalCreatedListener.ifPresent(l -> l.onTerminalCreated(terminal));
    }
    
  }
}
