
app.hosts = {
	'default': '/prudence-example/',
	internal: '/prudence-example/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		{type: 'explicit', implicit: {resource: '/prudence/implicit/python/', resources: '/resources/python/'}},
		//'explicit',
		'dynamicWeb',
		[
			{type: 'cacheControl', 'default': 1, mediaTypes: {'text/html': 2}, next: 'staticWeb'},
			{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}
		]
	],
	
	/*'/*': [
		{type: 'explicit', root: 'mapped', passThroughs: ['/prudence/fish/'], implicit: {resource: '/prudence/implicit/', resources: '/resources/'}},
		{type: 'dynamicWeb', root: 'mapped', fragmentsRoot: 'fragments'},
		{type: 'staticWeb', root: 'mapped'}
	],*/
	//'/static/*': staticWeb,
	//'/dynamic/*': 'dynamicWeb',
	//'/explicit/*': 'explicit',
	//'/prudence/router/': 'hidden',
	'/person/{id}/': {type: 'filter', library: '/filters/statistics/', next: 'person'}
}

app.preheat = ['/person/1/']
