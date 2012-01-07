
importClass(
	Packages.it.sauronsoftware.cron4j.Scheduler)

var scheduler = new Scheduler()
component.context.attributes.put('com.threecrickets.prudence.scheduler', scheduler)

initializers.push(function() {
	scheduler.start()
})
