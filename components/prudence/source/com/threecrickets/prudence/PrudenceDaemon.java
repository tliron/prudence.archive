/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence;

import org.restlet.Component;

import com.threecrickets.prudence.util.InstanceUtil;
import com.threecrickets.scripturian.Main;
import com.threecrickets.scripturian.ScripturianDaemon;

/**
 * Delegates to {@link Main}, using the <a
 * href="http://commons.apache.org/daemon/">Apache Commons Daemon</a>.
 * 
 * @author Tal Liron
 */
public class PrudenceDaemon extends ScripturianDaemon
{
	//
	// Daemon
	//

	@Override
	public void stop() throws Exception
	{
		PrudenceDaemon.stop( null );
	}

	static void stop( String[] args ) throws Exception
	{
		Component component = InstanceUtil.getComponent();
		if( component != null )
			component.stop();
	}
}
