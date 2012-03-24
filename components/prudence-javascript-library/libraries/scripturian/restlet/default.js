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

var Restlet = Restlet || function() {
	var Public = {}

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

	return Public
}()