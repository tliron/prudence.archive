
app.hosts = {
	'default': '/stickstick/'
}

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		{type: 'zuss', next: [
			'staticWeb',
			{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}]}
	],
	'/data/note/{id}/': '/data/note/',
	'/data/note/': 'hidden'
}
