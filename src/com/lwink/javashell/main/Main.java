package com.lwink.javashell.main;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.javashell.server.api.TerminalServer;
import com.lwink.javashell.shell.InputOutputShell;
import com.lwink.javashell.shell.api.Shell;
import com.lwink.javashell.terminal.api.Terminal;

public class Main extends AbstractSshServer
{
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private Map<Terminal, InputOutputShell> shellMap = new ConcurrentHashMap<>();
	public static void main(String[] args) throws Exception
	{
		Main m = new Main();
		m.start();
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
		
		int index = input.indexOf(' ');
		if (index > 0)
		{
			String command = input.substring(0, index);
			
			String params = input.substring(index + 1);
			
			switch (command)
			{
			case "prompt":
				changePrompt(shell, params);
				break;
			}
		}
	}
	
	public void changePrompt(Shell shell, String prompt)
	{
		LOG.info("Setting prompt to \"" + prompt + "\"");
		shell.setPrompt(prompt);
		
	}
}
