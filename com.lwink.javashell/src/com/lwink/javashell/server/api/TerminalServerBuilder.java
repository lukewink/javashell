package com.lwink.javashell.server.api;

import java.io.File;

import com.lwink.javashell.server.SshTerminalServer;

public class TerminalServerBuilder
{
	private File keyFile = new File("ssh-key");
	private int port = 22;
	private Authenticator authenticator = (user, password) -> true;
	
	/**
	 * @param keyFile A file to store the server's key pair.
	 * @return this TerminalServerBuilder.
	 */
	public TerminalServerBuilder keyFile(File keyFile)
	{
		this.keyFile = keyFile;
		return this;
	}
	
	/**
	 * @param port The socket port to bind to.
	 * @return this TerminalServerBuilder
	 */
	public TerminalServerBuilder port(int port)
	{
		this.port = port;
		return this;
	}
	
	/**
	 * @param authenticator Class to perform authentications.
	 * @return this TerminalServerBuilder
	 */
	public TerminalServerBuilder authenticator(Authenticator authenticator)
	{
		this.authenticator = authenticator;
		return this;
	}
	
	/**
	 * Builds a TerminalServer with the settings that have been passed in.
	 * 
	 * @return A TerminalServer that is ready to be used.
	 */
	public TerminalServer build()
	{
		return new SshTerminalServer(port, keyFile, authenticator);
	}
}
