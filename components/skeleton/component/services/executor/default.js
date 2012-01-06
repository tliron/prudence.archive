
importClass(
	java.lang.Runtime,
	java.util.concurrent.Executors)

var executor = Executors.newScheduledThreadPool(Runtime.runtime.availableProcessors() * 2 + 1)
component.context.attributes.put('com.threecrickets.prudence.executor', executor)

/*
var fixedExecutor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors() * 2 + 1)
if(tasks.length > 0) {
	var futures = []
	var startTime = System.currentTimeMillis()
	print('Executing ' + tasks.length + ' startup tasks...\n')
	for(var i in tasks) {
		var task = tasks[i]
		futures.push(fixedExecutor.submit(task))
	}
	for(var i in futures) {
		var future = futures[i]
		try {
			future.get()
		} catch(x) {
		}
	}
	print('Finished all startup tasks in ' + ((System.currentTimeMillis() - startTime) / 1000) + ' seconds.\n')
}

for(var i = applications.iterator(); i.hasNext(); ) {
	var applicationService = new ApplicationService(i.next())
	applicationService.task(null, '/startup/', null, 'initial', 0, 0, false)
}
*/