//
// Stickstick Routing
//

document.execute('defaults/application/routing/');

importClass(
	com.threecrickets.prudence.util.CssUnifyMinifyFilter,
	com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter);

router.capture(fixURL(resourcesBaseURL + '/data/note/{id}/'), '/data/note/');

// Wrap the static web with unify-minify filters
router.detach(staticWeb);
var wrappedStaticWeb = new CssUnifyMinifyFilter(applicationInstance.context, staticWeb, new File(applicationBasePath + staticWebBasePath), dynamicWebMinimumTimeBetweenValidityChecks);
var wrappedStaticWeb = new JavaScriptUnifyMinifyFilter(applicationInstance.context, wrappedStaticWeb, new File(applicationBasePath + staticWebBasePath), dynamicWebMinimumTimeBetweenValidityChecks);
router.attachBase(fixURL(staticWebBaseURL), wrappedStaticWeb);
