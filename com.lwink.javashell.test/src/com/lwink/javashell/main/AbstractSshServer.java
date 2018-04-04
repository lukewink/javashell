package com.lwink.javashell.main;

import java.io.File;
import java.util.stream.Collectors;

import com.lwink.javashell.server.api.TerminalServer;
import com.lwink.javashell.terminal.api.Terminal;

public abstract class AbstractSshServer
{
	public void start() throws Exception
	{
		TerminalServer server = TerminalServer.builder()
				.port(6667)
				.keyFile(new File("ssh-key"))
				//.authenticator((user, password) -> user.equals("admin") && password.equals("12345"))
				.build();
		
		// Don't support arcfour ciphers since they are not as secure
		server.setCiphers(server.getSupportedCiphers()
				.stream()
				.filter(c -> !c.contains("arcfour"))
				.collect(Collectors.toList()));
		
		// Only support Elliptic-curve Diffie-Hellman algorithms
		server.setKeyExchangeAlgorithms(server.getSupportedKeyExchangeAlgorithms()
				.stream()
				.filter(a -> a.startsWith("ecdh"))
				.collect(Collectors.toList()));
		
		server.start(this::onTerminalStarted, this::onTerminalClosed);
		server.waitForStop();
	}
	
	public abstract void onTerminalStarted(Terminal terminal);
	
	public abstract void onTerminalClosed(Terminal terminal);
}
