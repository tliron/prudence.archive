
Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Prudence Test',
		description: 'A test application for Prudence',
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
