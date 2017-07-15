package com.lwink.javashell.shell.window;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lwink.javashell.terminal.api.CursorPosition;
import com.lwink.javashell.terminal.api.KeyPressReceiver;
import com.lwink.javashell.terminal.api.ResizeObserver;
import com.lwink.javashell.terminal.api.TermColor;
import com.lwink.javashell.terminal.api.TermSize;
import com.lwink.javashell.terminal.api.Terminal;

public class InputWindowTest
{
	TestTerminal terminal;
	InputWindow w;
	int width = 15;
	int height = 20;
	int inputWindowRow = 5;
	
	public InputWindowTest()
	{
		terminal = new TestTerminal(width, height);
		w = new InputWindow(terminal, width-1, inputWindowRow);
	}

	@Before
	public void before()
	{
		terminal.clearScreen();
	}
	
	@After
	public void after()
	{
	}
	
	@Test
	public void testBackspace()
	{
		w.addChar('a');
		w.addChar('b');
		w.deleteCharBehindCursorPos();
		w.addChar('c');
		verify("ac");
	}

	@Test
	public void testAddChars()
	{
		w.addChar('a');
		w.addChar('b');
		w.addChar('c');
		verify("abc");
	}
	
	@Test
	public void testScroll()
	{
		// Add 20 characters
		addChars("0123456789abcdefghij");
	}
	
	public void addChars(String chars)
	{
		for (int i = 0; i < chars.length(); i++)
		{
			w.addChar(chars.charAt(i));
		}
	}
	
	public void verify(String expected)
	{
		verify(expected, expected);
	}
	
	public void verify(String expected, String visible)
	{
		w.refresh();
		Assert.assertEquals(expected, w.getWindowContents());
		String termStr = terminal.getRowString(inputWindowRow);
		Assert.assertEquals(padToWidth(visible, width), termStr);
	}
	
	public String padToWidth(String str, int width)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(str);
		for (int i = str.length(); i < width; i++)
		{
			sb.append(' ');
		}
		return sb.toString();
	}
	
	static class TestTerminal implements Terminal
	{
		int width;
		int height;
		int cursorCol = 0;
		int cursorRow = 0;
		char[][] charArray;
		
		public TestTerminal(int width, int height)
		{
			this.width = width;
			this.height = height;
			charArray = new char[width][height];
			clearScreen();
		}
		
		public String getRowString(int row)
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < width; i++)
			{
				sb.append(charArray[i][row]);
			}
			return sb.toString();
		}

		@Override
		public void stop()
		{
		}

		@Override
		public TermSize getTerminalSize()
		{
			return new TermSize(width, height);
		}

		@Override
		public void registerResizeObserver(ResizeObserver observer)
		{
		}

		@Override
		public void registerKeyPressReceiver(KeyPressReceiver keyPressReceiver)
		{
		}

		@Override
		public void moveCursor(int col, int row)
		{
			Assert.assertTrue(col < width);
			Assert.assertTrue(row < height);
			cursorCol = col;
			cursorRow = row;
		}

		@Override
		public void setCursorVisible(boolean visible)
		{
		}

		@Override
		public void putCharacter(char c)
		{
			charArray[cursorCol++][cursorRow] = c;
		}

		@Override
		public void clearScreen()
		{
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					charArray[i][j] = ' ';
				}
			}
		}

		@Override
		public void enterPrivateMode()
		{
		}

		@Override
		public void exitPrivateMode()
		{
		}

		@Override
		public void flush()
		{
		}

		@Override
		public void bell()
		{
		}

		@Override
		public void eraseLineWithCursor(CursorPosition where)
		{
			switch (where)
			{
			case AFTER_CURSOR:
				for (int i = cursorCol; i < width; i++)
				{
					charArray[i][cursorRow] = ' ';
				}
				break;
			case BEFORE_CURSOR:
				for (int i = 0; i <= cursorCol; i++)
				{
					charArray[i][cursorRow] = ' ';
				}
				break;
			case BEFORE_AND_AFTER:
				for (int i = 0; i < width; i++)
				{
					charArray[i][cursorRow] = ' ';
				}
				break;
			}
		}

		@Override
		public void setForegroundColor(TermColor color)
		{
		}

		@Override
		public void setBackgroundColor(TermColor color)
		{
		}

		@Override
		public void resetAttributes()
		{
		}

		@Override
		public void resetColorToDefaults()
		{
		}
		
	}

}
