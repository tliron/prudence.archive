
document.executeOnce('/sincerity/objects/')
document.executeOnce('/prudence/')

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

app.hosts = {
	'default': '/',
	internal: '/test/'
}
