importClass(
	org.restlet.data.MediaType,
	org.restlet.representation.Variant);

//Include the JSON library
document.container.include('../libraries/json2');

function handleInit() {
	document.container.variants.add(new Variant(MediaType.TEXT_PLAIN));
	document.container.variants.add(new Variant(MediaType.APPLICATION_JSON));
}

function handleGet() {
	var component = document.container.resource.context.attributes.get('component');
	var hosts = component.hosts;

	var r = [];
	for(var i = 0; i < hosts.size(); i++) {
		var host = hosts.get(i);
		var routes= host.routes;
		var s = [];
		for(var ii = 0; ii < routes.size(); ii++) {
			s.push(String(routes.get(ii)));
		}
		r.push({
			localHost: {
				name: String(host.localHostName),
				address: String(host.localHostAddress)
			},
			host: {
				scheme: String(host.hostScheme),
				domain: String(host.hostDomain),
				port: String(host.hostPort)
			},
			server: {
				address: String(host.serverAddress),
				port: String(host.serverPort)
			},
			resources: {
				scheme: String(host.resourceScheme),
				domain: String(host.resourceDomain),
				port: String(host.resourcePort)
			},
			routes: s
		});
	}

	return JSON.stringify(r);
}
