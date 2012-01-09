
app.hosts = {
	'default': '/prudence-example/',
	internal: '/prudence-example/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'explicit',
		'dynamicWeb',
		[
			{type: 'cacheControl', 'default': 1, mediaTypes: {'text/html': 2}, next: 'staticWeb'},
			{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}
		]
	],
	'/person/{id}/': {type: 'filter', library: '/filters/statistics/', next: 'person'},
	'/pythonperson/{id}/': {type: 'implicit', id: 'person', delegate: 'python'}
}

app.delegates = {
	javascript: {explicit: '/prudence/implicit/javascript/', library: '/resources/javascript/'},
	python: {explicit: '/prudence/implicit/python/', library: '/resources/python/'},
	ruby: {explicit: '/prudence/implicit/ruby/', library: '/resources/ruby/'},
	groovy: {explicit: '/prudence/implicit/groovy/', library: '/resources/groovy/'},
	clojure: {explicit: '/prudence/implicit/clojure/', library: '/resources/clojure/'},
	php: {explicit: '/prudence/implicit/php/', library: '/resources/php/'}
}

app.preheat = ['/person/1/']
