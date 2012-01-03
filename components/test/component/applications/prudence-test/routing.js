
document.executeOnce('/sincerity/objects/')
document.executeOnce('/prudence/')

//var staticWeb = new Prudence.StaticWeb({root: 'static'})

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
