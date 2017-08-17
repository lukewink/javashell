package com.lwink.javashell.main;

import java.io.File;

import com.lwink.javashell.server.api.TerminalServer;
import com.lwink.javashell.terminal.api.Terminal;

public abstract class AbstractSshServer
{
	public void start() throws Exception
	{
		TerminalServer server = TerminalServer.builder()
				.port(6667)
				.keyFile(new File("ssh-key"))
				.authenticator((user, password) -> user.equals("admin") && password.equals("12345"))
				.build();
		server.start(this::onTerminalStarted, this::onTerminalClosed);
		server.waitForStop();
	}
	
	public abstract void onTerminalStarted(Terminal terminal);
	
	public abstract void onTerminalClosed(Terminal terminal);
}
