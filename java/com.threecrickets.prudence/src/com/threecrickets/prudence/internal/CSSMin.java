package com.threecrickets.prudence.internal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

/**
 * <a href="http://barryvan.github.com/CSSMin/">CSSMin</a> takes in well-formed,
 * human-readable CSS and reduces its size substantially. It removes unnecessary
 * whitespace and comments, and orders the contents of CSS selectors
 * alphabetically to enhance GZIP compression. Originally by Barry van
 * Oudtshoorn, with bug reports, fixes, and contributions by
 * <ul>
 * <li>Kevin de Groote</li>
 * <li>Pedro Pinheiro</li>
 * </ul>
 * <p>
 * License: Mozilla Public License
 * 
 * @author Barry van Oudtshoorn
 */
public class CSSMin
{
	protected static boolean bDebug = false;

	/**
	 * Main entry point for CSSMin from the command-line. <b>Usage:</b> CSSMin
	 * <i>[Input file]</i>, <i>[Output file]</i>, <i>[DEBUG]</i>
	 * 
	 * @param args
	 *        The command-line arguments
	 */
	public static void main( String[] args )
	{
		if( args.length < 1 )
		{
			System.out.println( "Usage: " );
			System.out.println( "CSSMin [Input file] [Output file] [DEBUG]" );
			System.out.println( "If no output file is specified, stdout will be used." );
			return;
		}

		bDebug = ( args.length > 2 );

		PrintStream out;

		if( args.length > 1 )
		{
			try
			{
				out = new PrintStream( args[1] );
			}
			catch( Exception e )
			{
				System.err.println( "Error outputting to " + args[1] + "; redirecting to stdout" );
				out = System.out;
			}
		}
		else
			out = System.out;

		formatFile( args[0], out );
	}

	/**
	 * Process a file from a filename.
	 * 
	 * @param filename
	 *        The file name of the CSS file to process.
	 * @param out
	 *        Where to send the result
	 */
	public static void formatFile( String filename, OutputStream out )
	{
		try
		{
			formatFile( new FileReader( filename ), out );
		}
		catch( java.io.FileNotFoundException e )
		{
			System.out.println( e.getMessage() );
		}
	}

	/**
	 * Process input from a reader.
	 * 
	 * @param input
	 *        Where to read the CSS from
	 * @param output
	 *        Where to send the result
	 */
	public static void formatFile( Reader input, OutputStream out )
	{
		formatFile( input, new PrintStream( out ) );
	}

	/**
	 * Minify CSS from a reader to a printstream.
	 * 
	 * @param input
	 *        Where to read the CSS from
	 * @param out
	 *        Where to write the result to
	 */
	public static void formatFile( Reader input, PrintStream out )
	{
		try
		{
			int k, n;

			BufferedReader br = new BufferedReader( input );
			StringBuffer sb = new StringBuffer();

			if( bDebug )
				System.err.println( "Reading file into StringBuffer..." );

			String s;
			while( ( s = br.readLine() ) != null )
			{
				if( s.trim().equals( "" ) )
					continue;
				sb.append( s );
			}

			if( bDebug )
				System.err.println( "Removing comments..." );

			// Find the start of the comment
			while( ( n = sb.indexOf( "/*" ) ) != -1 )
			{
				// TAL LIRON: This is broken!
				// It's also unclear why we would need this
				// if( sb.charAt( n + 2 ) == '*' )
				// {
				// Retain special comments
				// n += 2;
				// continue;
				// }
				k = sb.indexOf( "*/", n + 2 );
				if( k == -1 )
					throw new Exception( "Unterminated comment. Aborting." );
				sb.delete( n, k + 2 );
			}
			if( bDebug )
			{
				System.err.println( sb.toString() );
				System.err.println( "\n\n" );
			}

			if( bDebug )
				System.err.println( "Parsing and processing selectors..." );

			Vector<Selector> selectors = new Vector<Selector>();
			n = 0;
			while( ( k = sb.indexOf( "}", n ) ) != -1 )
			{
				try
				{
					selectors.addElement( new Selector( sb.substring( n, k + 1 ) ) );
				}
				catch( Exception e )
				{
					System.out.println( e.getMessage() );
				}
				n = k + 1;
			}

			for( Selector selector : selectors )
				out.print( selector.toString() );
			out.print( "\r\n" );

			out.close();

			if( bDebug )
				System.err.println( "Process completed successfully." );
		}
		catch( Exception e )
		{
			System.out.println( e.getMessage() );
		}

	}
}

class Selector
{
	private Property[] properties;

	private String selector;

	/**
	 * Creates a new Selector using the supplied strings.
	 * 
	 * @param selector
	 *        The selector; for example,
	 *        "div { border: solid 1px red; color: blue; }"
	 * @throws Exception
	 *         If the selector is incomplete and cannot be parsed.
	 */
	public Selector( String selector ) throws Exception
	{
		String[] parts = selector.split( "\\{" );

		// We have to escape the { with a \ for the regex, which itself requires
		// escaping for the string. Sigh.
		if( parts.length < 2 )
			throw new Exception( "Warning: Incomplete selector: " + selector );

		this.selector = parts[0].trim().replaceAll( ", ", "," );
		String contents = parts[1].trim();
		if( CSSMin.bDebug )
		{
			System.err.println( "Parsing selector: " + this.selector );
			System.err.println( "\t" + contents );
		}

		if( contents.charAt( contents.length() - 1 ) != '}' )
		{
			// Ensure that we have a leading and trailing brace.
			throw new Exception( "\tUnterminated selector: " + selector );
		}

		if( contents.length() == 1 )
			throw new Exception( "\tEmpty selector body: " + selector );

		contents = contents.substring( 0, contents.length() - 2 );
		this.properties = parseProperties( contents );
		sortProperties( this.properties );
	}

	/**
	 * Prints out this selector and its contents nicely, with the contents
	 * sorted alphabetically.
	 * 
	 * @returns A string representing this selector, minified.
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( this.selector ).append( "{" );
		for( Property p : this.properties )
			sb.append( p.toString() );
		sb.append( "}" );
		return sb.toString();
	}

	/**
	 * Parses out the properties of a selector's body.
	 * 
	 * @param contents
	 *        The body; for example, "border: solid 1px red; color: blue;"
	 * @returns An array of properties parsed from this selector.
	 */
	private Property[] parseProperties( String contents )
	{
		ArrayList<String> parts = new ArrayList<String>();
		boolean bCanSplit = true;
		int j = 0;
		String substr;
		for( int i = 0; i < contents.length(); i++ )
		{
			if( !bCanSplit )
			{ // If we're inside a string
				bCanSplit = ( contents.charAt( i ) == '"' );
			}
			else if( contents.charAt( i ) == '"' )
			{
				bCanSplit = false;
			}
			else if( contents.charAt( i ) == ';' )
			{
				substr = contents.substring( j, i );
				if( !( substr.trim().equals( "" ) || ( substr == null ) ) )
					parts.add( substr );
				j = i + 1;
			}
		}
		substr = contents.substring( j, contents.length() );
		if( !( substr.trim().equals( "" ) || ( substr == null ) ) )
			parts.add( substr );
		Property[] results = new Property[parts.size()];

		for( int i = 0; i < parts.size(); i++ )
		{
			try
			{
				results[i] = new Property( parts.get( i ) );
			}
			catch( Exception e )
			{
				System.out.println( e.getMessage() );
				results[i] = null;
			}
		}

		return results;
	}

	/**
	 * Sorts the properties array to enhance gzipping.
	 * 
	 * @param properties
	 *        The array to be sorted.
	 */
	private void sortProperties( Property[] properties )
	{
		Arrays.sort( properties );
	}
}

class Property implements Comparable<Property>
{
	protected String property;

	protected Part[] parts;

	/**
	 * Creates a new Property using the supplied strings. Parses out the values
	 * of the property selector.
	 * 
	 * @param property
	 *        The property; for example, "border: solid 1px red;" or
	 *        "-moz-box-shadow: 3px 3px 3px rgba(255, 255, 0, 0.5);".
	 * @throws Exception
	 *         If the property is incomplete and cannot be parsed.
	 */
	public Property( String property ) throws Exception
	{
		try
		{
			// Parse the property.
			ArrayList<String> parts = new ArrayList<String>();
			boolean bCanSplit = true;
			int j = 0;
			String substr;

			if( CSSMin.bDebug )
				System.err.println( "\t\tExamining property: " + property );

			for( int i = 0; i < property.length(); i++ )
			{
				if( !bCanSplit )
					// If we're inside a string
					bCanSplit = ( property.charAt( i ) == '"' );
				else if( property.charAt( i ) == '"' )
					bCanSplit = false;
				else if( property.charAt( i ) == ':' )
				{
					substr = property.substring( j, i );
					if( !( substr.trim().equals( "" ) || ( substr == null ) ) )
						parts.add( substr );
					j = i + 1;
				}
			}
			substr = property.substring( j, property.length() );
			if( !( substr.trim().equals( "" ) || ( substr == null ) ) )
				parts.add( substr );
			if( parts.size() < 2 )
				throw new Exception( "\t\tWarning: Incomplete property: " + property );
			this.property = parts.get( 0 ).trim().toLowerCase();

			this.parts = parseValues( parts.get( 1 ).trim().replaceAll( ", ", "," ) );

		}
		catch( PatternSyntaxException e )
		{
			// Invalid regular expression used.
		}
	}

	/**
	 * Prints out this property nicely.
	 * 
	 * @returns A string representing this property, minified.
	 */
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( this.property ).append( ":" );
		for( Part p : this.parts )
			sb.append( p.toString() ).append( "," );
		// Delete the trailing comma.
		sb.deleteCharAt( sb.length() - 1 );
		sb.append( ";" );
		if( CSSMin.bDebug )
		{
			System.err.println( sb.toString() );
		}
		return sb.toString();
	}

	/**
	 * Compare this property with another.
	 */
	public int compareTo( Property other )
	{
		return this.property.compareTo( other.property );
	}

	/**
	 * Parse the values out of a property.
	 * 
	 * @param contents
	 *        The property to parse
	 * @returns An array of Parts
	 */
	private Part[] parseValues( String contents )
	{
		String[] parts = contents.split( "," );
		Part[] results = new Part[parts.length];

		for( int i = 0; i < parts.length; i++ )
		{
			try
			{
				results[i] = new Part( parts[i] );
			}
			catch( Exception e )
			{
				System.out.println( e.getMessage() );
				results[i] = null;
			}
		}

		return results;
	}
}

class Part
{
	String contents;

	/**
	 * Create a new property by parsing the given string.
	 * 
	 * @param contents
	 *        The string to parse.
	 * @throws Exception
	 *         If the part cannot be parsed.
	 */
	public Part( String contents ) throws Exception
	{
		// Many of these regular expressions are adapted from those used in the
		// YUI CSS Compressor.

		// For simpler regexes.
		this.contents = " " + contents;

		// Replace 0in, 0cm, etc. with just 0
		this.contents = this.contents.replaceAll( "(\\s)(0)(px|em|%|in|cm|mm|pc|pt|ex)", "$1$2" );

		// Replace 0.6 with .6
		this.contents = this.contents.replaceAll( "(\\s)0+\\.(\\d+)", "$1.$2" );

		this.contents = this.contents.trim();

		// Simplify multiple zeroes
		if( this.contents.equals( "0 0 0 0" ) )
			this.contents = "0";
		if( this.contents.equals( "0 0 0" ) )
			this.contents = "0";
		if( this.contents.equals( "0 0" ) )
			this.contents = "0";

		// simplifyColours();
	}

	// Current non-functional
	/*
	 * private void simplifyColours() { if (CSSMin.bDebug) {
	 * System.out.println("Simplifying colours; contents is " + this.contents);
	 * } // Convert rgb() colours to Hex if
	 * (this.contents.toLowerCase().indexOf("rgb(") == 0) { String[] parts =
	 * this.contents.substring(4, this.contents.indexOf(")")).split(","); if
	 * (parts.length == 3) { int r = Integer.parseInt(parts[0], 10); int g =
	 * Integer.parseInt(parts[1], 10); int b = Integer.parseInt(parts[2], 10);
	 * StringBuffer sb = new StringBuffer(); sb.append("#"); if (r < 16)
	 * sb.append("0"); sb.append(Integer.toHexString(r)); if (g < 16)
	 * sb.append("0"); sb.append(Integer.toHexString(g)); if (b < 16)
	 * sb.append("0"); sb.append(Integer.toHexString(b)); this.contents =
	 * sb.toString(); } } // Replace #223344 with #234 if
	 * ((this.contents.indexOf("#") == 0) && (this.contents.length() == 7)) {
	 * this.contents = this.contents.toLowerCase(); // Always have hex colours
	 * in lower case. if ((this.contents.charAt(1) == this.contents.charAt(2))
	 * && (this.contents.charAt(3) == this.contents.charAt(4)) &&
	 * (this.contents.charAt(5) == this.contents.charAt(6))) { StringBuffer sb =
	 * new StringBuffer();
	 * sb.append("#").append(this.contents.charAt(1)).append(
	 * this.contents.charAt(3)).append(this.contents.charAt(5)); this.contents =
	 * sb.toString(); } } }
	 */

	/**
	 * Returns itself.
	 * 
	 * @returns this part's string representation.
	 */
	@Override
	public String toString()
	{
		return this.contents;
	}
}