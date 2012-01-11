
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
	'/pythonperson/{id}/': {type: 'implicit', id: 'person', delegate: 'python'},
	'/groovyperson/{id}/': {type: 'implicit', id: 'person', delegate: 'groovy'},
	'/phpperson/{id}/': {type: 'implicit', id: 'person', delegate: 'php'}
}

app.delegates = {
	javascript: {explicit: '/prudence/delegate/javascript/', library: '/resources/javascript/'},
	python: {explicit: '/prudence/delegate/python/', library: '/resources/python/'},
	ruby: {explicit: '/prudence/delegate/ruby/', library: '/resources/ruby/'},
	groovy: {explicit: '/prudence/delegate/groovy/', library: '/resources/groovy/'},
	clojure: {explicit: '/prudence/delegate/clojure/', library: '/resources/clojure/'},
	php: {explicit: '/prudence/delegate/php/', library: '/resources/php/'}
}

app.preheat = ['/person/1/']
