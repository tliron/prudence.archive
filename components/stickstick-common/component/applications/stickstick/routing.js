
document.executeOnce('/sincerity/objects/')
document.executeOnce('/prudence/')

app.hosts = {
	'default': '/'
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
	'/data/note/{id}/': '/data/note/',
	'/data/note/': 'hidden'
}
