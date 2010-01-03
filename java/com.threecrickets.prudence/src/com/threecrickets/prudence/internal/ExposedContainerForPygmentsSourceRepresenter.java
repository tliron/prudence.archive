/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.prudence.internal;

import com.threecrickets.prudence.util.PygmentsSourceRepresenter;

/**
 * @author Tal Liron
 * @see PygmentsSourceRepresenter
 */
public class ExposedContainerForPygmentsSourceRepresenter
{
	//
	// Construction
	//

	public ExposedContainerForPygmentsSourceRepresenter( String language, String title, String style, String text )
	{
		this.language = language;
		this.title = title;
		this.style = style;
		this.text = text;
	}

	//
	// Attributes
	//

	public String getLanguage()
	{
		return language;
	}

	public String getTitle()
	{
		return title;
	}

	public String getStyle()
	{
		return style;
	}

	public String getText()
	{
		return text;
	}

	public void setText( String text )
	{
		this.text = text;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String language;

	private final String title;

	private final String style;

	private String text;
}