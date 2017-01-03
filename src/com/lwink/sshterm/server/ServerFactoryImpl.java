package com.lwink.sshterm.server;

import java.io.File;

import com.lwink.sshterm.server.api.ServerFactory;
import com.lwink.sshterm.server.api.TerminalServer;

public class ServerFactoryImpl implements ServerFactory
{

	@Override
	public TerminalServer newSshServer(int port, File keyPath)
	{
		return new SshTerminalServer(port, keyPath);
	}

}
