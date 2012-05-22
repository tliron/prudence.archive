package com.threecrickets.prudence.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicLong;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.zkoss.zuss.Locator;
import org.zkoss.zuss.Resolver;
import org.zkoss.zuss.Zuss;
import org.zkoss.zuss.impl.out.BuiltinResolver;
import org.zkoss.zuss.metainfo.ZussDefinition;

import com.threecrickets.prudence.internal.CSSMin;

/**
 * A {@link Filter} that automatically parses <a
 * href="https://github.com/tomyeh/ZUSS">ZUSS</a> code and renders CSS. Also
 * supports minifying files, if the ".min.css" extension is used. See
 * {@link CssUnifyMinifyFilter}.
 * <p>
 * This filter can track changes to the source files, updating the result file
 * on-the-fly. This makes it easy to develop and debug a live site.
 * <p>
 * Note that this instances of this class can only guarantee atomic access to
 * the rendered CSS file within the current VM.
 * 
 * @author Tal Liron
 */
public class ZussFilter extends Filter implements Locator
{
	//
	// Construction
	//

	/**
	 * Constructor using a {@link BuiltinResolver}.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param sourceDirectory
	 *        The directory where the sources are found
	 * @param minimumTimeBetweenValidityChecks
	 *        See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	public ZussFilter( Context context, Restlet next, File sourceDirectory, long minimumTimeBetweenValidityChecks )
	{
		this( context, next, sourceDirectory, minimumTimeBetweenValidityChecks, new BuiltinResolver() );
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param sourceDirectory
	 *        The directory where the sources are found
	 * @param minimumTimeBetweenValidityChecks
	 *        See {@link #getMinimumTimeBetweenValidityChecks()}
	 * @param resolver
	 *        The ZUSS resolver
	 */
	public ZussFilter( Context context, Restlet next, File sourceDirectory, long minimumTimeBetweenValidityChecks, Resolver resolver )
	{
		super( context, next );
		this.sourceDirectory = sourceDirectory;
		this.minimumTimeBetweenValidityChecks = minimumTimeBetweenValidityChecks;
		this.resolver = resolver;
		describe();
	}

	//
	// Attributes
	//

	/**
	 * A value of -1 disables all validity checking.
	 * 
	 * @return The minimum time between validity checks in milliseconds
	 * @see #setMinimumTimeBetweenValidityChecks(long)
	 */
	public long getMinimumTimeBetweenValidityChecks()
	{
		return minimumTimeBetweenValidityChecks;
	}

	/**
	 * @param minimumTimeBetweenValidityChecks
	 * @see #getMinimumTimeBetweenValidityChecks()
	 */
	public void setMinimumTimeBetweenValidityChecks( long minimumTimeBetweenValidityChecks )
	{
		this.minimumTimeBetweenValidityChecks = minimumTimeBetweenValidityChecks;
	}

	//
	// Operations
	//

	/**
	 * Translate ZUSS to CSS, only if the ZUSS source is newer. Can optionaly
	 * minify the CSS, too.
	 * 
	 * @param zussFile
	 *        The ZUSS source file
	 * @param cssFile
	 *        The CSS target file (will be overwritten)
	 * @param minify
	 *        Whether to minify the CSS
	 * @throws IOException
	 * @see {@link CSSMin}
	 */
	public void translate( File zussFile, File cssFile, boolean minify ) throws IOException
	{
		cssFile = IoUtil.getUniqueFile( cssFile );
		synchronized( cssFile )
		{
			long lastModified = zussFile.lastModified();
			if( lastModified == cssFile.lastModified() )
				return;

			BufferedReader reader = new BufferedReader( new FileReader( zussFile ) );
			try
			{
				ZussDefinition zussDefinition = Zuss.parse( reader, this, zussFile.getName() );
				if( minify )
				{
					getLogger().info( "Translating and minifying ZUSS: \"" + zussFile + "\" into file \"" + cssFile + "\"" );

					StringWriter writer = new StringWriter();
					Zuss.translate( zussDefinition, writer, resolver );
					BufferedOutputStream output = new BufferedOutputStream( new FileOutputStream( cssFile ) );
					try
					{
						CSSMin.formatFile( new StringReader( writer.toString() ), output );
					}
					finally
					{
						output.close();
					}
				}
				else
				{
					getLogger().info( "Translating ZUSS: \"" + zussFile + "\" into file \"" + cssFile + "\"" );

					BufferedWriter writer = new BufferedWriter( new FileWriter( cssFile ) );
					try
					{
						Zuss.translate( zussDefinition, writer, resolver );
					}
					finally
					{
						writer.close();
					}
				}
			}
			finally
			{
				reader.close();
			}

			if( !cssFile.setLastModified( lastModified ) )
				throw new IOException( "Could not update timestamp on file: " + cssFile );
		}
	}

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		Reference reference = request.getResourceRef();
		String path = reference.getRemainingPart( true, false );
		try
		{
			String name = reference.getLastSegment();
			String zussName = null;
			boolean minify = false;
			if( path.endsWith( CSS_MIN_EXTENSION ) )
			{
				zussName = name.substring( 0, name.length() - CSS_MIN_EXTENSION_LENGTH ) + ZUSS_EXTENSION;
				minify = true;
			}
			else if( path.endsWith( CSS_EXTENSION ) )
				zussName = name.substring( 0, name.length() - CSS_EXTENSION_LENGTH ) + ZUSS_EXTENSION;

			if( zussName != null )
			{
				long now = System.currentTimeMillis();
				long lastValidityCheck = this.lastValidityCheck.get();
				if( lastValidityCheck == 0 || ( now - lastValidityCheck > minimumTimeBetweenValidityChecks ) )
				{
					if( this.lastValidityCheck.compareAndSet( lastValidityCheck, now ) )
					{
						File zussFile = new File( sourceDirectory, zussName );

						if( !zussFile.exists() )
						{
							response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
							return Filter.STOP;
						}

						File cssFile = new File( sourceDirectory, name );
						translate( zussFile, cssFile, minify );
					}
				}
			}
		}
		catch( IOException x )
		{
			response.setStatus( Status.SERVER_ERROR_INTERNAL, x );
			return Filter.STOP;
		}

		return Filter.CONTINUE;
	}

	//
	// Locator
	//

	public Reader getResource( String name ) throws IOException
	{
		File file = new File( sourceDirectory, name );
		return new BufferedReader( new FileReader( file ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String CSS_MIN_EXTENSION = ".min.css";

	private static final int CSS_MIN_EXTENSION_LENGTH = CSS_MIN_EXTENSION.length();

	private static final String CSS_EXTENSION = ".css";

	private static final int CSS_EXTENSION_LENGTH = CSS_EXTENSION.length();

	private static final String ZUSS_EXTENSION = ".zuss";

	/**
	 * The directory where the sources are found.
	 */
	private final File sourceDirectory;

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private volatile long minimumTimeBetweenValidityChecks;

	/**
	 * The ZUSS resolver.
	 */
	private final Resolver resolver;

	/**
	 * See {@link #getMinimumTimeBetweenValidityChecks()}
	 */
	private final AtomicLong lastValidityCheck = new AtomicLong();

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( "ZussFilter" );
		setDescription( "A filter that automatically translates ZUSS source files to CSS" );
	}
}
