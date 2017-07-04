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
package com.lwink.sshterm.server.api;

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
}
