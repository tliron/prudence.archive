
app.hosts = {
	'default': '/',
	internal: '/prudence-admin/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		{type: 'javaScriptUnifyMinify', next:
			{type: 'zuss', next: [
				'staticWeb',
				{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}]}}
	]
}
