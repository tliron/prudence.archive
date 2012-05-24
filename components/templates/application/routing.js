
app.hosts = {
	'default': '/${APPLICATION}/',
	internal: '/${APPLICATION}/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		// For our static files we'll cache all images on the client for the far future, and enable on-the-fly ZUSS support and JavaScript compression:
		{type: 'cacheControl', 'default': -1, mediaTypes: {'image/png': 'farFuture', 'image/jpeg': 'farFuture', 'image/gif': 'farFuture'}, next:
			{type: 'javaScriptUnifyMinify', root: Sincerity.Container.getFileFromHere('mapped', 'scripts'), next:
				{type: 'zuss', root: Sincerity.Container.getFileFromHere('mapped', 'style'), next:
					'staticWeb'}}},
		// The shared static web resources under /libraries/web/:
		{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}
	],
	// A sample implicit resource, see /libraries/resources/sample.js:
	'/sample/': {type: 'implicit', id: 'sample'}
}

// See /libraries/resources/default.js:
app.dispatch = {
	javascript: {explicit: '/prudence/dispatch/javascript/', library: '/resources/'}
}
