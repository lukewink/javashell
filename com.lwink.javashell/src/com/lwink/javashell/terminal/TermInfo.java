package com.lwink.javashell.terminal;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lwink.javashell.terminal.api.KeyPress;
import com.lwink.javashell.terminal.api.KeyPress.Type;
import com.lwink.javashell.terminal.api.KeyPressReceiver;

public class TermInfo
{
	public static final Logger LOG = LoggerFactory.getLogger(TermInfo.class);
	private static final Map<String, KeyPress> known = new HashMap<>();
	private StringBuilder buffer = new StringBuilder();
	private KeyPressReceiver receiver;
	
	static 
	{
		load();
	}
	
	public TermInfo(KeyPressReceiver receiver)
	{
		this.receiver = receiver;
	}
	
	public void addCh(char c)
	{
		if (c == '\r' || c == '\u001b')
		{
			// This is to get us out of situations where we have an unknown sequence in
			// the buffer.  This allows users to just press the Enter key to get out of
			// the bad state.  If any valid sequence contains a newline character, this 
			// will need to be rethought out.
			buffer.setLength(0);
		}
		buffer.append(c);
		String sequence = buffer.toString();
		KeyPress keyPress = known.get(sequence);
		if (keyPress != null)
		{
			buffer.setLength(0);
			LOG.debug("KeyPress {}", keyPress);
			receiver.onKeyPress(keyPress);
		}
		else
		{
			// This assumes that all sequences start with an escape
			if (buffer.charAt(0) != '\u001b')
			{
				buffer.setLength(0);
			}
		}
	}
	
	private static void load()
	{
		loadAscii();
		loadControl();
		
		// Arrow keys
		known.put("\u001b[A", KeyPress.builder().type(Type.ARROW_UP).build());
		known.put("\u001b[1;2A", KeyPress.builder().type(Type.ARROW_UP).shift(true).build());
		known.put("\u001b[1;5A", KeyPress.builder().type(Type.ARROW_UP).ctrl(true).build());
		known.put("\u001b[1;6A", KeyPress.builder().type(Type.ARROW_UP).shift(true).ctrl(true).build());
		known.put("\u001b[B", KeyPress.builder().type(Type.ARROW_DOWN).build());
		known.put("\u001b[1;2B", KeyPress.builder().type(Type.ARROW_DOWN).shift(true).build());
		known.put("\u001b[1;5B", KeyPress.builder().type(Type.ARROW_DOWN).ctrl(true).build());
		known.put("\u001b[1;6B", KeyPress.builder().type(Type.ARROW_DOWN).shift(true).ctrl(true).build());
		known.put("\u001b[C", KeyPress.builder().type(Type.ARROW_RIGHT).build());
		known.put("\u001b[1;2C", KeyPress.builder().type(Type.ARROW_RIGHT).shift(true).build());
		known.put("\u001b[1;5C", KeyPress.builder().type(Type.ARROW_RIGHT).ctrl(true).build());
		known.put("\u001b[1;6C", KeyPress.builder().type(Type.ARROW_RIGHT).shift(true).ctrl(true).build());
		known.put("\u001b[D", KeyPress.builder().type(Type.ARROW_LEFT).build());
		known.put("\u001b[1;2D", KeyPress.builder().type(Type.ARROW_LEFT).shift(true).build());
		known.put("\u001b[1;5D", KeyPress.builder().type(Type.ARROW_LEFT).ctrl(true).build());
		known.put("\u001b[1;6D", KeyPress.builder().type(Type.ARROW_LEFT).shift(true).ctrl(true).build());
		
		// Page up/down
		known.put("\u001b[5~", KeyPress.builder().type(Type.PAGE_UP).build());
		known.put("\u001b[5;2~", KeyPress.builder().type(Type.PAGE_UP).shift(true).build());
		known.put("\u001b[5;5~", KeyPress.builder().type(Type.PAGE_UP).ctrl(true).build());
		known.put("\u001b[5;6~", KeyPress.builder().type(Type.PAGE_UP).shift(true).ctrl(true).build());
		known.put("\u001b[6~", KeyPress.builder().type(Type.PAGE_DOWN).build());
		known.put("\u001b[6;2~", KeyPress.builder().type(Type.PAGE_DOWN).shift(true).build());
		known.put("\u001b[6;5~", KeyPress.builder().type(Type.PAGE_DOWN).ctrl(true).build());
		known.put("\u001b[6;6~", KeyPress.builder().type(Type.PAGE_DOWN).shift(true).ctrl(true).build());
		
		known.put("\u0008", KeyPress.builder().type(Type.BACKSPACE).build());
		known.put("\u007f", KeyPress.builder().type(Type.BACKSPACE).build());
	}
	
	private static void loadAscii()
	{
		// Add the normal printable characters
		for (int i = 32; i <= 126; i++)
		{
			known.put(String.valueOf((char)i), KeyPress.builder().ch((char)i).type(Type.NORMAL).build());
		}
		
		known.put("\n", KeyPress.builder().ch('\n').type(Type.NORMAL).build());
		known.put("\r", KeyPress.builder().ch('\r').type(Type.NORMAL).build());
	}
	
	private static void loadControl()
	{
		for (int i = 1; i <= 26; i++)
		{
			known.put(String.valueOf((char)i), KeyPress.builder().ch((char)(i + 96)).type(Type.CONTROL).ctrl(true).build());
			known.put(String.valueOf((char)(i + 128)), KeyPress.builder().ch((char)(i + 96)).type(Type.NORMAL).shift(true).ctrl(true).build());
		}
	}
}
