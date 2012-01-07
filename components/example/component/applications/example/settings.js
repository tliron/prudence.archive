
document.executeOnce('/sincerity/objects/')

Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Skeleton',
		description: 'The example application for the Prudence skeleton',
		author: 'Three Crickets',
		owner: 'Free Software'
	},

	errors: {
		debug: true, // TODO: problems with false
		homeUrl: 'http://threecrickets/prudence/', // Only used when debug=false
		contactEmail: 'info@threecrickets.com' // Only used when debug=false
	},
	
	code: {
		minimumTimeBetweenValidityChecks: 1000,
		defaultDocumentName: 'default',
		defaultExtension: 'js',
		defaultLanguageTag: 'javascript',
		libraries: ['libraries'], // Handlers and tasks will be found here
		sourceViewable: true
	},
	
	mediaTypes: {
		php: 'text/html'
	}
})
