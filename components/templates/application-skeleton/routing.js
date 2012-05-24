
app.hosts = {
	'default': '/${APPLICATION}/',
	internal: '/${APPLICATION}/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		[
			{type: 'zuss', root: Sincerity.Container.getFileFromHere('mapped', 'style'), next: 'staticWeb'},
			{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}
		]
	],
	'/sample/': {type: 'implicit', id: 'sample'}
}

app.dispatch = {
	javascript: {explicit: '/prudence/dispatch/javascript/', library: '/resources/'}
}
