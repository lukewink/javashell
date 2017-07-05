package com.lwink.javashell.server.api;

import java.io.File;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;

import com.lwink.javashell.server.SshTerminalServer;

public class ServerFactory
{
	/**
	 * Creates a new SSH Terminal Server.  The created server will not perform authentication.
	 * 
	 * @param port Port to listen on.
	 * @param keyPath The path to save the server's SSH key.
	 * @return The newly created SSH terminal server.
	 */
	public TerminalServer newSshServer(int port, File keyPath)
	{
		return new SshTerminalServer(port, keyPath, null);
	}
	
	/**
	 * Creates a new SSH Terminal Server.
	 * 
	 * @param port Port to listen on.
	 * @param keyPath The path to save the server's SSH key.
	 * @param authenticator The authenticator to use for username and password authentication.
	 * @return The newly created SSH terminal server.
	 */
	public TerminalServer newSshServer(int port, File keyPath, PasswordAuthenticator authenticator)
	{
		return new SshTerminalServer(port, keyPath, authenticator);
	}

}
