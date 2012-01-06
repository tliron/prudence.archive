
importClass(
	Packages.it.sauronsoftware.cron4j.Scheduler)

var scheduler = new Scheduler()
component.context.attributes.put('com.threecrickets.prudence.scheduler', scheduler)

startupTasks.push(function() {
	scheduler.start()
})
