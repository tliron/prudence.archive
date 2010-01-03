;
; Prudence Application Settings
;

;
; Information
;
; These are for administrative purposes only.
;

;(def application-name "Prudence Application") ; Defaults to the application directory name
(def application-description "This is a Prudence application.")
(def application-author "Anonymous")
(def application-owner "Public Domain")
(def application-home-url "http://www.threecrickets.com/prudence/")
(def application-contact-email "prudence@threecrickets.com")

;
; Debugging
;

; Set to true to show debug information on error.

(def show-debug-on-error false)

;
; Logging
;
; Logger defaults to the application's directory name. Configure logging at
; conf/logging.conf.
;

;(def application-logger-name "prudence-application")

;
; Hosts
;
; This map matches the virtual hosts to which our application will be attached
; with their base URLs on the hosts. See component/hosts.py for more information.
; Specify None for the URL to default to the application's directory name.
;

(def hosts {(.getDefaultHost component) nil})

;
; Resources
;
; Sets up a directory under which you can place script files that implement
; RESTful resources. The directory structure underneath the base directory
; is directly linked to the base URL.
;

(def resource-base-url "/")
(def resource-base-path "/resources/")

; If the URL points to a directory rather than a file, and that directory
; contains a file with this name, then it will be used. This allows
; you to use the directory structure to create nice URLs without relying
; on filenames.

(def resource-default-name "default")

; This is so we can see the source code for scripts by adding ?source=true
; to the URL. You probably wouldn't want this for most applications.

(def resource-source-viewable true)

; This is the time (in milliseconds) allowed to pass until a script file
; is tested to see if it was changed. During development, you'd want this
; to be low, but during production, it should be high in order to avoid
; unnecessary hits on the filesystem.

(def resource-minimum-time-between-validity-checks 1000)

;
; Dynamic Web
;
; Sets up a directory under which you can place text files that support embedded scriptlets.
; Note that the generated result can be cached for better performance.
;

(def dynamic-web-base-url "/")
(def dynamic-web-base-path "/web/dynamic/")

; If the URL points to a directory rather than a file, and that directory
; contains a file with this name, then it will be used. This allows
; you to use the directory structure to create nice URLs that do not
; contain filenames.

(def dynamic-web-default-document "index")

; This is so we can see the source code for scripts by adding ?source=true
; to the URL. You probably wouldn't want this for most applications.

(def dynamic-web-source-viewable true)

; This is the time (in milliseconds) allowed to pass until a script file
; is tested to see if it was changed. During development, you'd want this
; to be low, but during production, it should be high in order to avoid
; unnecessary hits on the filesystem.

(def dynamic-web-minimum-time-between-validity-checks 1000)

;
; Static Web
;
; Sets up a directory under which you can place static files of any type.
; Servers like Grizzly and Jetty can use non-blocking I/O to stream static
; files efficiently to clients. 
;

(def static-web-base-url "/")
(def static-web-base-path "/web/static/")

; If the URL points to a directory rather than a file, then this will allow
; automatic creation of an HTML page with a directory listing.

(def static-web-directory-listing-allowed true)

;
; URL manipulation
;

; The URLs in this array will automatically be redirected to have a trailing
; slash added to them if it's missing.

(def url-add-trailing-slash [dynamic-web-base-url static-web-base-url])

;
; Runtime Attributes
;
; These will be available to your code via the application's context.
;

(def runtime-attributes {})
