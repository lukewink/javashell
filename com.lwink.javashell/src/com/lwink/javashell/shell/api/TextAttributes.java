/**
 * Copyright 2017 Luke Winkenbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 **/
package com.lwink.javashell.shell.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lwink.javashell.terminal.api.TermColor;
import com.lwink.javashell.terminal.api.TextStyle;

public class TextAttributes
{
	private TermColor fgColor = TermColor.DEFAULT;
	private TermColor bgColor = TermColor.DEFAULT;
	private List<TextStyle> styles = new ArrayList<>();
	
	public TextAttributes()
	{
		
	}
	
	public TextAttributes setFgColor(TermColor color)
	{
		this.fgColor = color;
		return this;
	}
	
	public TextAttributes setBgColor(TermColor color)
	{
		this.bgColor = color;
		return this;
	}
	
	public TextAttributes setStyles(TextStyle ...styles)
	{
		this.styles.clear();
		Collections.addAll(this.styles, styles);
		return this;
	}
	
	public TermColor getFgColor()
	{
		return fgColor;
	}
	
	public TermColor getBgColor()
	{
		return bgColor;
	}
	
	public List<TextStyle> getTextStyles()
	{
		return styles;
	}
	
	public static TextAttributes fgColor(TermColor color)
	{
		return new TextAttributes().setFgColor(color);
	}
	
	public static TextAttributes bgColor(TermColor color)
	{
		return new TextAttributes().setBgColor(color);
	}
	
	public static TextAttributes styles(TextStyle ...styles)
	{
		return new TextAttributes().setStyles(styles);
	}
}
