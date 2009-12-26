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
	var applications = component.context.attributes.get('applications');

	var r = [];
	for(var i = 0; i < applications.size(); i++) {
		var application = applications.get(i);
		r.push({
			name: String(application.name),
			description: String(application.description),
			author: String(application.author)
		});
	}

	return JSON.stringify(r);
}
