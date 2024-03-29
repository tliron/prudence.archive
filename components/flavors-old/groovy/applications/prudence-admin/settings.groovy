//
// Prudence Admin Settings
//

document.execute('/defaults/application/settings/')

applicationName = 'Prudence Admin'
applicationDescription = 'Runtime management of Prudence'
applicationAuthor = 'Tal Liron'
applicationOwner = 'Three Crickets'
applicationHomeURL = 'http://threecrickets.com/prudence/'
applicationContactEmail = 'prudence@threecrickets.com'

hosts = [[componentInstance.defaultHost, '/'], [mysiteHost, '/']]

showDebugOnError = true
