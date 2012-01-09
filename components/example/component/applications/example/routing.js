
app.hosts = {
	'default': '/example/',
	internal: '/example/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		[
			'staticWeb',
			{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}
		]
	],
	
	/*'/*': [
		{type: 'explicit', root: 'mapped', passThroughs: ['/prudence/fish/'], implicit: {routerDocumentName: '/prudence/implicit/', resourcesDocumentName: '/resources/'}},
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
