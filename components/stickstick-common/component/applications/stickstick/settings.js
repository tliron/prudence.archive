
document.executeOnce('/sincerity/objects/')

Sincerity.Objects.merge(app.settings, {
	description: {
		name: 'Stickstick',
		description: 'A demo application for Prudence',
		author: 'Three Crickets',
		owner: 'Prudence'
	},

	code: {
		defaultLanguageTag: 'javascript',
		defaultExtension: 'js',
		defaultDocumentName: 'default',
		minimumTimeBetweenValidityChecks: 1000,
		libraries: ['libraries'],
		sourceVisible: true
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
