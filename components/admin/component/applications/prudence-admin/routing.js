
app.hosts = {
	'default': '/',
	internal: '/skeleton/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		[
			'staticWeb',
			{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}
		]
	]
}

app.preheat = ['/person/1/']
