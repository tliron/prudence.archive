//
// This file is part of the Prudence Foundation Library
//
// Copyright 2011-2012 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

/**
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
var Restlet = Restlet || function() {
	var Public = {}

	/**
	 * Finds a virtual host by its name.
	 * 
	 * @param {org.restlet.Component} The Restlet component
	 * @param {String} name The host name
	 * @returns {org.restlet.routing.VirtualHost} The host or null if not found
	 */
	Public.getHost = function(component, name) {
		if (name == 'default') {
			return component.defaultHost
		}
		else if (name == 'internal') {
			return component.internalRouter
		}

		for (var i = component.hosts.iterator(); i.hasNext(); ) {
			var host = i.next()
			if (name == host.name) {
				return host
			}
		}
		
		return null
	}
	
	/**
	 * Makes sure that an authenticator is registered with the Restlet engine.
	 * If it's not registered, a dummy authenticator will be installed.
	 * This lets us remove Restlet warnings about unsupported "Authentication"
	 * headers in HTTP.
	 * 
	 * @param {String} name The unique identifier for the authenticator
	 * @param {String} technicalName The actual string used in the HTTP "Authentication" header
	 * @param {String} description The description
	 */
	Public.registerAuthenticator = function(name, technicalName, description) {
		var engine = org.restlet.engine.Engine.instance
		var customScheme = new org.restlet.data.ChallengeScheme(name, technicalName, description)
		var authenticator = engine.findHelper(customScheme, true, false)
		if (null === authenticator) {
			authenticator = new JavaAdapter(org.restlet.engine.security.SmtpPlainHelper, {
				// Rhino won't let us implement AuthenticatorHelper directly, because it doesn't have
				// an argumentless constructor. So, we'll jerry-rig SmtpPlainHelper, which is close
				// enough. We'll just make sure to disable its formatRawResponse implementation. 
				
				formatRawResponse: function(cw, challenge, request, httpHeaders) {
					application.logger.warning('formatRawResponse should never be called!')
				}
			})
			authenticator.challengeScheme = customScheme
			engine.registeredAuthenticators.add(authenticator)
		}
	}

	return Public
}()