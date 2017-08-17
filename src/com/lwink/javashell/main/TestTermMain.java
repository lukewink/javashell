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
		terminal.enterPrivateMode();
		terminal.moveCursor(0, 0);
		IntStream.range(0, 10).forEach(i -> terminal.putCharacter('1'));
		terminal.moveCursor(0, 1);
		IntStream.range(0, 10).forEach(i -> terminal.putCharacter('2'));
		terminal.moveCursor(0, 2);
		IntStream.range(0, 10).forEach(i -> terminal.putCharacter('3'));
		terminal.flush();
	}

	@Override
	public void onTerminalClosed(Terminal terminal)
	{
		// TODO Auto-generated method stub
		
	}

}
