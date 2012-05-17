
var scheduler = new Packages.it.sauronsoftware.cron4j.Scheduler()
component.context.attributes.put('com.threecrickets.prudence.scheduler', scheduler)

initializers.push(function() {
	scheduler.start()
})
