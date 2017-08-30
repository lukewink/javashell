package com.lwink.javashell.shell.window;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
	
	

}
