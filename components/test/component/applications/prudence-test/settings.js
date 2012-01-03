
document.executeOnce('/sincerity/objects/')

Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Prudence Test',
		description: 'A test application for Prudence',
		author: 'Three Crickets',
		owner: 'Free Software'
	},

	code: {
		defaultLanguageTag: 'javascript',
		defaultExtension: 'js',
		defaultDocumentName: 'default',
		minimumTimeBetweenValidityChecks: 1000,
		libraries: ['libraries'],
		sourceVisible: true
	}
})
