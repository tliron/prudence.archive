
document.executeOnce('/sincerity/objects/')

Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Stickstick',
		description: 'A demo application for Prudence',
		author: 'Three Crickets',
		owner: 'Prudence'
	},

	errors: {
		debug: true,
		homeUrl: 'http://threecrickets/prudence/',
		contactEmail: 'info@threecrickets.com'
	},
	
	code: {
		libraries: ['libraries'],
		defrost: true,
		minimumTimeBetweenValidityChecks: 1000,
		defaultDocumentName: 'default',
		defaultExtension: 'js',
		defaultLanguageTag: 'javascript',
		sourceViewable: true
	},
	
	uploads: {
		root: 'uploads',
		sizeThreshold: 0
	},
	
	mediaTypes: {
		php: 'text/html'
	}
})

Sincerity.Objects.merge(app.globals, {
	stickstick: {
		backend: 'h2',
		username: 'root',
		password: 'root',
		host: '',
		database: String(Sincerity.Container.getFileFromHere('data', 'stickstick')),
		log: String(sincerity.container.getLogsFile('stickstick.log')) // this is only used by Python 
	}
})
