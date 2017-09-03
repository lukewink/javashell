package com.lwink.javashell.main;

import java.util.stream.IntStream;

import com.lwink.javashell.terminal.api.Terminal;

public class TestTermMain extends AbstractSshServer
{
	public static void main(String[] args) throws Exception
	{
		TestTermMain t = new TestTermMain();
		t.start();
	}
	@Override
	public void onTerminalStarted(Terminal terminal)
	{
		String line = "0123456789";
		terminal.clearScreen();
		terminal.enterPrivateMode();
		terminal.setCursorVisible(false);
		terminal.moveCursor(0, 0);
		IntStream.range(0, 10).forEach(i -> terminal.putCharacter(line.charAt(i)));
		terminal.moveCursor(0, 1);
		IntStream.range(0, 10).forEach(i -> terminal.putCharacter(line.charAt(i)));
		terminal.moveCursor(0, 2);
		IntStream.range(0, 10).forEach(i -> terminal.putCharacter(line.charAt(i)));
		terminal.moveCursor(5, 1);
		terminal.eraseCharacters(0);
		terminal.flush();
	}

	@Override
	public void onTerminalClosed(Terminal terminal)
	{
		// TODO Auto-generated method stub
		
	}

}
