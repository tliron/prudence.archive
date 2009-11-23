//
// Prudence Hosts
//
// Prudence supports virtual hosting, allowing you to serve different applications
// or otherwise route differently per domain name, protocol, port, etc. This
// feature lets you run multiple sites from the same Prudence installation.
//
// Note that virtual hosts are only indirectly related to Prudence's servers.
// See servers.js for more information.
//

importClass(
	org.restlet.routing.VirtualHost);

//
// Wildcard
//
// Our "wildcard" host will accept all incoming requests.
//

var wildcardHost = new VirtualHost(component.context);
wildcardHost.name = 'wildcard';

component.hosts.add(wildcardHost);

//
// mysite.org
//
// This is an example of a virtual host which only accepts requests to
// a specific set of domains.
//

var mysiteHost = new VirtualHost(component.context);
mysiteHost.name = 'mysite.org';
mysiteHost.hostScheme = 'http';
mysiteHost.hostDomain = 'mysite.org|www.mysite.org';
mysiteHost.hostPort = '80';

component.hosts.add(mysiteHost);

//
// Default Host
//
// Applications by default will attach only to this host, though they can
// choose to attach to any hosts defined here. See the application's
// routing.js.
//

component.defaultHost = wildcardHost;

//
// Welcome
//

print('Available virtual hosts: ');
for(var i = 0; i < component.hosts.size(); i++) {
	var host = component.hosts.get(i);
	print('"' + host.name + '"');
	if(i < component.hosts.size() - 1) {
		print(', ');
	}
}
print('.\n');
