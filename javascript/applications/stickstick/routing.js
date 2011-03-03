//
// Stickstick Routing
//

document.execute('/defaults/application/routing/')

importClass(
	com.threecrickets.prudence.util.CssUnifyMinifyFilter,
	com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter)

router.captureAndHide(resourcesBaseURL + 'data/note/{id}/', '/data/note/')

// Wrap the static web with unify-minify filters
var cssFilter = new CssUnifyMinifyFilter(null, new File(applicationBasePath + staticWebBasePath), dynamicWebMinimumTimeBetweenValidityChecks)
var javaScriptFilter = new JavaScriptUnifyMinifyFilter(null, new File(applicationBasePath + staticWebBasePath), dynamicWebMinimumTimeBetweenValidityChecks)
router.filterBase(staticWebBaseURL, cssFilter, staticWeb)
router.filterBase(staticWebBaseURL, javaScriptFilter, cssFilter)
