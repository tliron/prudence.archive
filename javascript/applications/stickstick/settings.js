
document.container.include('defaults/application/settings');

applicationName = 'Stickstick';
applicationDescription = 'Share online sticky notes';
applicationAuthor = 'Tal Liron';
applicationOwner = 'Three Crickets';
applicationHomeURL = 'http://www.threecrickets.com/prudence/stickstick/';
applicationContactEmail = 'prudence@threecrickets.com';

runtimeAttributes['stickstick.backend'] = 'mysql+zxjdbc';
runtimeAttributes['stickstick.username'] = 'root';
runtimeAttributes['stickstick.password'] = 'root';
runtimeAttributes['stickstick.host'] = 'localhost';
runtimeAttributes['stickstick.database'] = 'stickstick';

showDebugOnError = true;
