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
	int inputWindowWidth = 10;
	int inputWindowRow = 5;
	String expectedAfterInputWindow;
	
	public InputWindowTest()
	{
		terminal = new TestTerminal(width, height);
		w = new InputWindow(terminal, inputWindowWidth, inputWindowRow);
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < width - inputWindowWidth; i++)
		{
			sb.append(' ');
		}
		expectedAfterInputWindow = sb.toString();
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
		w.refresh();
		verify("ac");
	}

	@Test
	public void testAddChars()
	{
		addChars("abc");
		verify("abc");
	}
	
	@Test
	public void testScroll()
	{
		// Add 20 characters when the terminal width is only 10
		String twentyChars = "0123456789abcdefghij";
		addChars(twentyChars);
		verify(twentyChars, "bcdefghij ", 9);
	}
	
	@Test
	public void testCursor()
	{
		w.cursorLeft();
		verify("", 0);
		w.cursorRight();
		verify("", 0);
	}
	
	@Test
	public void testPrompt()
	{
		String prompt = "123";
		w.setPrompt(prompt);
		w.refresh();
		verify("", "123       ", 3);
		addChars("abcde");
		verify("abcde", "123abcde  ", 8);
		w.cursorLeft();
		verify("abcde", "123abcde  ", 7);
		w.cursorLeft();
		w.cursorLeft();
		verify("abcde", "123abcde  ", 5);
		w.cursorRight();
		verify("abcde", "123abcde  ", 6);
		w.cursorRight();
		w.cursorRight();
		w.cursorRight();
		verify("abcde", "123abcde  ", 8);
	}
	
	public void addChars(String chars)
	{
		for (int i = 0; i < chars.length(); i++)
		{
			w.addChar(chars.charAt(i));
		}
		w.refresh();
	}
	
	public void verify(String expected)
	{
		verify(expected, padToWidth(expected, inputWindowWidth));
	}
	
	public void verify(String expected, String visible)
	{
		verify(expected, visible, -1);
	}
	
	public void verify(String expected, int cursorCol)
	{
		verify(expected, padToWidth(expected, inputWindowWidth), cursorCol);
	}
	
	/**
	 * Verifies aspects of the terminal and input window state.
	 * 
	 * @param expected The expected value of the input window contents
	 * @param visible The expected value of the terminal column that the input window is on.
	 * @param cursorCol If this value is not negative, this function will verify that the cursor is
	 *        at the passed column.
	 */
	public void verify(String expected, String visible, int cursorCol)
	{
		//w.refresh();
		Assert.assertEquals(expected, w.getWindowContents());
		String termStr = terminal.getRowString(inputWindowRow);
		String termInputWindow = termStr.substring(0, inputWindowWidth);
		Assert.assertEquals(visible, termInputWindow);
		
		// Make sure the window didn't write too far
		String afterInputWindow = termStr.substring(inputWindowWidth);
		Assert.assertEquals(expectedAfterInputWindow, afterInputWindow);
		
		if (cursorCol > 0)
		{
			Assert.assertEquals(cursorCol, terminal.cursorCol);
		}
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

		@Override
		public void eraseCharacters(int numChars)
		{
			numChars = Math.min(width - cursorCol, numChars);
			for (int i = 0; i < numChars; i++)
			{
				charArray[cursorCol + i][cursorRow] = ' ';
			}
		}

		@Override
		public void deleteCharacters(int numChars)
		{
			numChars = Math.min(cursorCol + 1, numChars);
			for (int i = 0; i < numChars; i++)
			{
				charArray[cursorCol - i][cursorRow] = ' ';
			}
		}
	}

}
