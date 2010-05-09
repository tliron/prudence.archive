importClass(
	org.restlet.data.MediaType,
	org.restlet.representation.Variant);

//Include the JSON library
document.execute('../libraries/json2');

function handleInit() {
	prudence.variants.add(new Variant(MediaType.TEXT_PLAIN));
	prudence.variants.add(new Variant(MediaType.APPLICATION_JSON));
}

function handleGet() {
	var component = prudence.resource.context.attributes.get('component');
	var servers = component.servers;

	var r = [];
	for(var i = 0; i < servers.size(); i++) {
		var server = servers.get(i);
		var protocols = server.protocols;
		var p = [];
		for(var ii = 0; ii < protocols.size(); ii++) {
			p.push(String(protocols.get(ii)));
		}
		r.push({
			name: String(server.name),
			address: String(server.address) ? server.address : null,
			port: server.port,
			protocols: p
		});
	}

	return JSON.stringify(r);
}
