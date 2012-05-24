
app.settings = {
	description: {
		name: 'Prudence Administration',
		description: 'The administration application for Prudence',
		author: 'Three Crickets',
		owner: 'Prudence'
	},

	errors: {
		debug: true, // TODO: problems with false
		homeUrl: 'http://threecrickets.com/prudence/', // Only used when debug=false
		contactEmail: 'info@threecrickets.com' // Only used when debug=false
	},
	
	code: {
		libraries: ['libraries'], // Handlers and tasks will be found here
		defrost: true,
		minimumTimeBetweenValidityChecks: 1000,
		defaultDocumentName: 'default',
		defaultExtension: 'js',
		defaultLanguageTag: 'javascript',
		sourceViewable: true
	},
	
	mediaTypes: {
		php: 'text/html'
	}
}
