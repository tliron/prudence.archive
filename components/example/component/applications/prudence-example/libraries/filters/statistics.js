
document.executeOnce('/sincerity/templates/')

importClass(java.util.concurrent.atomic.AtomicInteger)

var logger = application.getSubLogger('statistics')

function getCounter() {
	var counter = application.globals.get('counter')
	if (null === counter) {
		counter = new AtomicInteger()
		var existing = application.globals.put('counter', counter)
		if (null !== existing) {
			counter = existing
		}
	}
	return counter
}

function handleBefore(conversation) {
	logger.fine('Statistics filter will check this request')
	return 'continue'
}

function handleAfter(conversation) {
	if (conversation.request.method == 'POST') {
		var counter = getCounter()
		var count = counter.incrementAndGet()
		logger.info('Counted {0} POSTs so far'.cast(count))
	}
}
