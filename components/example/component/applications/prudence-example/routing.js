
app.hosts = {
	'default': '/prudence-example/',
	internal: '/prudence-example/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'explicit',
		{type: 'filter', library: '/filters/statistics/', next: 'dynamicWeb'},
		{type: 'cacheControl', 'default': 10, mediaTypes: {'text/html': 15}, next:
			{type: 'javaScriptUnifyMinify', next:
				{type: 'zuss', next: [
					'staticWeb',
					{type: 'staticWeb', root: sincerity.container.getLibrariesFile('web')}]}}}
	],
	'/person/{id}/': {type: 'implicit', id: 'person', dispatcher: 'javascript'},
	'/pythonperson/{id}/': {type: 'implicit', id: 'person', dispatcher: 'python'},
	'/groovyperson/{id}/': {type: 'implicit', id: 'person', dispatcher: 'groovy'},
	'/phpperson/{id}/': {type: 'implicit', id: 'person', dispatcher: 'php'},
	'/rubyperson/{id}/': {type: 'implicit', id: 'person', dispatcher: 'ruby'},
	'/clojureperson/{id}/': {type: 'implicit', id: 'person', dispatcher: 'clojure'}
}

app.dispatchers = {
	javascript: {explicit: '/prudence/dispatch/javascript/', library: '/resources/javascript/'},
	python: {explicit: '/prudence/dispatch/python/', library: '/resources/python/'},
	ruby: {explicit: '/prudence/dispatch/ruby/', library: '/resources/ruby/'},
	groovy: {explicit: '/prudence/dispatch/groovy/', library: '/resources/groovy/'},
	clojure: {explicit: '/prudence/dispatch/clojure/', library: '/resources/clojure/'},
	php: {explicit: '/prudence/dispatch/php/', library: '/resources/php/'}
}

//
// Preheat
//

if (null !== executable.manager.getAdapterByTag('javscript')) {
	app.preheat.push('/scriptlets/javascript/')
	app.preheat.push('/explicit/javascript/')
	app.preheat.push('/person/1/')
}
if (null !== executable.manager.getAdapterByTag('jython')) {
	app.preheat.push('/scriptlets/python/')
	app.preheat.push('/explicit/python/')
	app.preheat.push('/pythonperson/1/')
}
if (null !== executable.manager.getAdapterByTag('groovy')) {
	app.preheat.push('/scriptlets/groovy/')
	app.preheat.push('/explicit/groovy/')
	app.preheat.push('/groovyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('php')) {
	app.preheat.push('/scriptlets/php/')
	app.preheat.push('/explicit/php/')
	app.preheat.push('/phpperson/1/')
}
if (null !== executable.manager.getAdapterByTag('ruby')) {
	app.preheat.push('/scriptlets/ruby/')
	app.preheat.push('/explicit/ruby/')
	app.preheat.push('/rubyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('clojure')) {
	app.preheat.push('/scriptlets/clojure/')
	app.preheat.push('/explicit/clojure/')
	app.preheat.push('/clojureperson/1/')
}
if (null !== executable.manager.getAdapterByTag('velocity')) {
	app.preheat.push('/scriptlets/velocity/')
}
