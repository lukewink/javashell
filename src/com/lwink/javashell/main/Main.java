package com.lwink.javashell.main;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.javashell.server.api.ServerFactory;
import com.lwink.javashell.server.api.TerminalServer;
import com.lwink.javashell.shell.InputOutputShell;
import com.lwink.javashell.shell.api.Shell;
import com.lwink.javashell.terminal.api.Terminal;

public class Main
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private Map<Terminal, InputOutputShell> shellMap = new ConcurrentHashMap<>();
	public static void main(String[] args) throws Exception
	{
		Main m = new Main();
		m.start();
	}
	
	public synchronized void start() throws Exception
	{
		ServerFactory factory = new ServerFactory();
		TerminalServer server = factory.newSshServer(6667, new File("ssh-key"));
		server.start(this::onTerminalStarted, this::onTerminalClosed);
		this.wait();
	}
	
	public void onTerminalStarted(Terminal terminal)
	{
		LOG.info("Terminal started");
		InputOutputShell shell = new InputOutputShell(terminal);
		shell.registerInputCallback(this::onInputEntered);
		shellMap.put(terminal, shell);
	}
	
	public void onTerminalClosed(Terminal terminal)
	{
		LOG.info("Terminal closed");
		InputOutputShell shell = shellMap.get(terminal);
		if (shell != null)
		{
			shell.close();
		}
	}
	
	public void onInputEntered(String input, Shell shell)
	{
		shell.addOutput("Input received: " + input);
	}
}
