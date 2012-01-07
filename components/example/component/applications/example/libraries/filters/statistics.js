
document.executeOnce('/sincerity/templates/')

function getCounter() {
	var counter = application.globals.get('counter')
	if (null === counter) {
		counter = java.util.concurrent.atomic.AtomicInteger()
		var existing = application.globals.put('counter', counter)
		if (null !== existing) {
			counter = existing
		}
	}
	return counter
}

function handleBefore(conversation) {
	application.logger.info('Counting this request')
	return 'continue'
}

function handleAfter(conversation) {
	var counter = getCounter()
	var count = counter.incrementAndGet()
	application.logger.info('Counted {0} so far'.cast(count))
}
