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
package com.lwink.javashell.server.api;

import java.util.Collection;

public interface TerminalServer
{
	void start(TerminalCreatedListener terminalCreatedListener, TerminalClosedListener terminalClosedListener);
  
	/**
	 * Causes the calling thread to block until either the thread has been interrupted, or the 
	 * {@link #stop()} function has been called.
	 * 
	 * @throws InterruptedException If the current thread is interrupted.
	 */
	void waitForStop() throws InterruptedException;
	
	/**
	 * Stops the server.  Any threads waiting on the {@link #waitForStop()} function will become
	 * unblocked.
	 */
  void stop();
  
  /**
   * Sets the ciphers for the SSH server to use.  This cannot be called after calling 
   * {@link #start(TerminalCreatedListener, TerminalClosedListener)} otherwise an exception will be thrown.
   * If this function is not called, this all supported ciphers can be used.
   * 
   * @param ciphers The ciphers to support.  Must be a subset of the list returned by {@link #getSupportedCiphers()}.
   */
  void setCiphers(Collection<String> ciphers);

  /**
   * Gets a collection of encryption ciphers that are supported by the SSH server.
   * 
   * @return A list of implemented ciphers.
   */
	Collection<String> getSupportedCiphers();

	/**
	 * Sets the key exchange algorithms to use.  This cannot be called after calling
	 * {@link #start(TerminalCreatedListener, TerminalClosedListener)} otherwise an exception will be 
	 * thrown.  If this function is not called, then all supported key exchange algorithms can be used.
	 * 
	 * @param algorithms The algorithms to support.  Must be a subset of the list returned by
	 *        {@link #getSupportedKeyExchangeAlgorithms()}
	 */
	void setKeyExchangeAlgorithms(Collection<String> algorithms);

	/**
	 * Gets a collection of all supported key exchange algorithms that the SSH server can support.
	 * 
	 * @return Implemented key exchange algorithms.
	 */
	Collection<String> getSupportedKeyExchangeAlgorithms();
	
	/**
	 * Set the MAC algorithms that the SSH server should support. This cannot be called after calling
	 * {@link #start(TerminalCreatedListener, TerminalClosedListener)} otherwise an exception will be 
	 * thrown.  If this function is not called, then all supported MAC algorithms can be used.
	 * 
	 * @param macs A list of MAC algorithms.  This should be a subset of what was returned by {@link #getSupportedMacs()}
	 */
	void setMacs(Collection<String> macs);
	
	/**
	 * Return the supported MAC algorithms.
	 * 
	 * @return MAC algorithms that are implemented on the server.
	 */
	Collection<String> getSupportedMacs();

	/**
   * A builder to create a TerminalServer.
   * 
   * @return Newly created TerminalServer.
   */
  static TerminalServerBuilder builder()
  {
  	return new TerminalServerBuilder();
  }
}
