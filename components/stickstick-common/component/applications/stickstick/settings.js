
document.executeOnce('/sincerity/objects/')

Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Stickstick: JavaScript Edition',
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

Sincerity.Objects.merge(app.globals, {
	stickstick: {
		backend: 'h2',
		username: 'root',
		password: 'root',
		host: '',
		database: String(Sincerity.Container.getFileFromHere('data', 'stickstick'))
	}
})
