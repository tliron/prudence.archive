
document.executeOnce('/sincerity/container/')
document.executeOnce('/prudence/')

var app = new Prudence.Application()

Sincerity.Container.execute('settings')
Sincerity.Container.execute('routing')

app = app.create(component)

// Restlets
Sincerity.Container.executeAll('restlets')
