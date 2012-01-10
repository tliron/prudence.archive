
document.executeOnce('/resources/groovy/person/')

resources = [
	'person': this.class.classLoader.loadClass('Person').newInstance()
]
