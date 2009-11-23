;
; This script implements and handles a REST resource. Simply put, it is a state,
; addressed by a URL, that responds to verbs. Verbs represent logical operations
; on the state, such as create, read, update and delete (CRUD). They are primitive
; communications, which include very minimal session and no transaction state. As such,
; they are very straightforward to implement, and can lead to very scalable
; applications. 
;
; The exact URL of this resource depends on its its filename and/or its location in
; your directory structure. See your prudence.conf for more information.
;

(import
	'(java.io File)
	'(java.util.concurrent.locks ReentrantReadWriteLock)
	'(org.restlet.representation Variant)
	'(org.restlet.data MediaType)
	'(org.restlet.ext.json JsonRepresentation)
)

; Include the context library
(.include (.getContainer document) "test/clojure/context.clj")

; Include the JSON library
(add-classpath (.toURL (File. (str (.getBasePath (.getSource (.getContainer document))) "/test/clojure"))))
(require '(org.danlarkin [json :as json]))

; State
;
; These make sure that our state is properly stored in the context,
; so that we always use the same state, even if this script is recompiled.

(defn getStateLock []
	(getContextAttribute "clojure.stateLock"
		(fn [] (new ReentrantReadWriteLock))
	)
)

(defn getState []
	(getContextAttribute "clojure.state"
		(fn [] {"name" "Coraline" "media" "Film" "rating" "A+" "characters" ["Coraline" "Wybie" "Mom" "Dad"]})
	)
)

(defn setState [value]
	(.put (.getAttributes (.getContext (.getResource (.getContainer document)))) "clojure.state" value)
)

; This function is called when the resource is initialized. We will use it to set
; general characteristics for the resource.

(defn handleInit []
	; The order in which we add the variants is their order of preference.
	; Note that clients often include a wildcard (such as "*/*") in the
	; "Accept" attribute of their request header, specifying that any media type
	; will do, in which case the first one we add will be used.

	(.add (.getVariants (.getContainer document)) (Variant. MediaType/TEXT_PLAIN))
	(.add (.getVariants (.getContainer document)) (Variant. MediaType/APPLICATION_JSON))
)

; This function is called for the GET verb, which is expected to behave as a
; logical "read" of the resource's state.
;
; The expectation is that it return one representation, out of possibly many, of the
; resource's state. Returned values can be of any explicit sub-class of
; org.restlet.resource.Representation. Other types will be automatically converted to
; string representation using the client's requested media type and character set.
; These, and the language of the representation (defaulting to nil), can be read and
; changed via document.container.mediaType, document.container.characterSet, and
; document.container.language.
;
; Additionally, you can use document.container.variant to interrogate the client's provided
; list of supported languages and encoding.

(defn handleGet []
	(def stateLock (getStateLock))
	(def state (getState))

	(.lock (.readLock stateLock))
	(try
		(def r (json/encode-to-str state))
		()
		(.unlock (.readLock stateLock))
	)
	
	; Return a representation appropriate for the requested media type
	; of the possible options we created in handleInit
	
	(if (= (.getMediaType (.getContainer document)) MediaType/APPLICATION_JSON)
		(def r (JsonRepresentation. (str r)))
	)
	
	r
)

; This function is called for the POST verb, which is expected to behave as a
; logical "update" of the resource's state.
;
; The expectation is that document.container.entity represents an update to the state,
; that will affect future calls to handleGet. As such, it may be possible
; to accept logically partial representations of the state.
;
; You may optionally return a representation, in the same way as handleGet.
; Because Clojure function return the last value by default,
; you must explicitly return a nil if you do not want to return a representation
; to the client.

(defn handlePost []
	(def update (json/decode-from-str (.getText (.getEntity (.getContainer document)))))
	(def stateLock (getStateLock))
	(def state (getState))
	
	; Update our state
	(.lock (.writeLock stateLock))
	(try
		(setState (merge state update))
		()
		(.unlock (.writeLock stateLock))
	)
	
	(handleGet)
)

; This function is called for the PUT verb, which is expected to behave as a
; logical "create" of the resource's state.
;
; The expectation is that document.container.entity represents an entirely new state,
; that will affect future calls to . Unlike handlePost,
; it is expected that the representation be logically complete.
;
; You may optionally return a representation, in the same way as handleGet.
; Because JavaScript functions return the last statement's value by default,
; you must explicitly return a null if you do not want to return a representation
; to the client.

(defn handlePut []
	(def update (json/decode-from-str (.getText (.getEntity (.getContainer document)))))
	(setState update)
	
	(handleGet)
)

; This function is called for the DELETE verb, which is expected to behave as a
; logical "delete" of the resource's state.
;
; The expectation is that subsequent calls to handleGet will fail. As such,
; it doesn't make sense to return a representation, and any returned value will
; ignored. Still, it's a good idea to return null to avoid any passing of value.

(defn handleDelete []
	(setState {})
	
	nil
)