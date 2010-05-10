//
// Stickstick Routing
//

document.execute('defaults/application/routing/');

importClass(com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter);

router.capture(fixURL(resourcesBaseURL + '/data/note/{id}/'), '/data/note/');

//Wrap the static web with a JavaScriptUnifyMinifyFilter
router.detach(staticWeb);
router.attach(fixURL(staticWebBaseURL),
	new JavaScriptUnifyMinifyFilter(applicationInstance.context, staticWeb, new File(applicationBasePath + staticWebBasePath), dynamicWebMinimumTimeBetweenValidityChecks))
	.matchingMode = Template.MODE_STARTS_WITH;
