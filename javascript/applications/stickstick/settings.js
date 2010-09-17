//
// Stickstick Settings
//

document.execute('defaults/application/settings/')

applicationName = 'Stickstick'
applicationDescription = 'Share online sticky notes'
applicationAuthor = 'Tal Liron'
applicationOwner = 'Three Crickets'
applicationHomeURL = 'http://threecrickets.com/prudence/stickstick/'
applicationContactEmail = 'prudence@threecrickets.com'

predefinedGlobals['stickstick.backend'] = 'h2'
predefinedGlobals['stickstick.username'] = 'root'
predefinedGlobals['stickstick.password'] = 'root'
predefinedGlobals['stickstick.host'] = ''
predefinedGlobals['stickstick.database'] = applicationBase + 'data/stickstick'

showDebugOnError = true

preheatResources = ['data/']
