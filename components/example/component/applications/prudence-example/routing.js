
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
	'/pythonperson/{id}/': {type: 'implicit', id: 'person', dispatch: 'python'},
	'/groovyperson/{id}/': {type: 'implicit', id: 'person', dispatch: 'groovy'},
	'/phpperson/{id}/': {type: 'implicit', id: 'person', dispatch: 'php'},
	'/rubyperson/{id}/': {type: 'implicit', id: 'person', dispatch: 'ruby'},
	'/clojureperson/{id}/': {type: 'implicit', id: 'person', dispatch: 'clojure'}
}

app.dispatch = {
	javascript: {explicit: '/prudence/dispatch/javascript/', library: '/resources/javascript/'},
	python: {explicit: '/prudence/dispatch/python/', library: '/resources/python/'},
	ruby: {explicit: '/prudence/dispatch/ruby/', library: '/resources/ruby/'},
	groovy: {explicit: '/prudence/dispatch/groovy/', library: '/resources/groovy/'},
	clojure: {explicit: '/prudence/dispatch/clojure/', library: '/resources/clojure/'},
	php: {explicit: '/prudence/dispatch/php/', library: '/resources/php/'}
}

app.preheat = [
	'/person/1/'
]

if (executable.manager.getAdapterByTag('jython')) {
	app.preheat.push('/pythonperson/1/')
}
if (executable.manager.getAdapterByTag('groovy')) {
	app.preheat.push('/groovyperson/1/')
}
if (executable.manager.getAdapterByTag('php')) {
	app.preheat.push('/phpperson/1/')
}
if (executable.manager.getAdapterByTag('ruby')) {
	app.preheat.push('/rubyperson/1/')
}
if (executable.manager.getAdapterByTag('clojure')) {
	app.preheat.push('/clojureperson/1/')
}
