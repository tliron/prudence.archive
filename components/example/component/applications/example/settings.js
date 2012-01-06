
document.executeOnce('/sincerity/objects/')

Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Skeleton',
		description: 'The example application for the Prudence skeleton',
		author: 'Three Crickets',
		owner: 'Free Software'
	},

	code: {
		minimumTimeBetweenValidityChecks: 1000,
		defaultDocumentName: 'default',
		defaultExtension: 'js',
		defaultLanguageTag: 'javascript',
		libraries: ['libraries'],
		sourceVisible: true //TODO
	},
	
	programs: {
		root: 'programs',
		startup: '/startup/'
	},
	
	handlers: {
		root: 'handlers'
	}
})
