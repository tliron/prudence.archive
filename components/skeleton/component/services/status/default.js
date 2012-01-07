
importClass(
	com.threecrickets.prudence.DelegatedStatusService)
	
component.statusService = new DelegatedStatusService()
component.statusService.homeRef = new org.restlet.data.Reference('http://oops/')
