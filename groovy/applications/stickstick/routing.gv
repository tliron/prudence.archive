//
// Stickstick Routing
//

document.execute('/defaults/application/routing/')

import com.threecrickets.prudence.util.CssUnifyMinifyFilter
import com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter

router.captureAndHide(resourcesBaseURL + 'data/note/{id}/', '/data/note/')

// Wrap the static web with unify-minify filters
cssFilter = new CssUnifyMinifyFilter(null, new File(applicationBasePath + staticWebBasePath), minimumTimeBetweenValidityChecks)
javaScriptFilter = new JavaScriptUnifyMinifyFilter(null, new File(applicationBasePath + staticWebBasePath), minimumTimeBetweenValidityChecks)
router.filterBase(staticWebBaseURL, cssFilter, staticWeb)
router.filterBase(staticWebBaseURL, javaScriptFilter, cssFilter)
