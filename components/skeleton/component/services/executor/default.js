
var executorTasks = []
var executor = java.util.concurrent.Executors.newScheduledThreadPool(java.lang.Runtime.runtime.availableProcessors() * 2 + 1)
component.context.attributes.put('com.threecrickets.prudence.executor', executor)

initializers.push(function() {
	var fixedExecutor = java.util.concurrent.Executors.newFixedThreadPool(java.lang.Runtime.runtime.availableProcessors() * 2 + 1)
	if (executorTasks.length > 0) {
		var futures = []
		var startTime = java.lang.System.currentTimeMillis()
		println('Executing ' + executorTasks.length + ' startup tasks...')
		for (var t in executorTasks) {
			futures.push(fixedExecutor.submit(executorTasks[t]))
		}
		for (var f in futures) {
			try {
				futures[f].get()
			} catch(x) {}
		}
		println('Finished all startup tasks in ' + ((java.lang.System.currentTimeMillis() - startTime) / 1000) + ' seconds.')
	}
	
	/*for (var i = applications.iterator(); i.hasNext(); ) {
		var applicationService = new ApplicationService(i.next())
		applicationService.task(null, '/startup/', null, 'initial', 0, 0, false)
	}*/
})
