;
; This script implements and handles a REST resource. Simply put, it is a state,
; addressed by a URL, that responds to verbs. Verbs represent logical operations
; on the state, such as create, read, update and delete (CRUD). They are primitive
; communications, which include very minimal session and no transaction state. As such,
; they are very straightforward to implement, and can lead to very scalable
; applications. 
;
; The exact URL of this resource depends on its its filename and/or its location in
; your directory structure. See your settings.clj for more information.
;

; Include the context library
(.. prudence (include "../libraries/clojure/context/"))

; Include the JSON library
(use
	'clojure.contrib.json.read
	'clojure.contrib.json.write)

(import
	'java.io.File
	'org.restlet.ext.json.JsonRepresentation)

; State
;
; These make sure that our state is properly stored in the context,
; so that we always use the same state, even if this script is recompiled.

(defn get-state [conversation]
	;
	; Important! Clojure maps are not regular old Java maps. They are *persistent*, meaning that on the
	; the one hand they are immutable, and on the other hand they maintain performance behavior when
	; extended into new forms. Bottom line for us here is that we do not need to do any locking to read
	; or "modify" our state. In other flavors of Prudence, you will find it more difficult (and error-
	; prone) to deal with state. Viva Clojure!
	;

	(get-context-attribute conversation "clojure.state"
		#(identity {"name" "Coraline", "media" "Film", "rating" "A+", "characters" ["Coraline" "Wybie" "Mom" "Dad"]})))

(defn set-state [conversation value]
	(.. conversation getResource getContext getAttributes (put "clojure.state" value)))

; This function is called when the resource is initialized. We will use it to set
; general characteristics for the resource.

(defn handle-init [conversation]
	; The order in which we add the variants is their order of preference.
	; Note that clients often include a wildcard (such as "*/*") in the
	; "Accept" attribute of their request header, specifying that any media type
	; will do, in which case the first one we add will be used.

	(.. conversation (addMediaTypeByName "text/plain"))
	(.. conversation (addMediaTypeByName "application/json")))

; This function is called for the GET verb, which is expected to behave as a
; logical "read" of the resource's state.
;
; The expectation is that it return one representation, out of possibly many, of the
; resource's state. Returned values can be of any explicit sub-class of
; org.restlet.resource.Representation. Other types will be automatically converted to
; string representation using the client's requested media type and character set.
; These, and the language of the representation (defaulting to nil), can be read and
; changed via conversation.mediaType, conversation.characterSet, and
; conversation.language.
;
; Additionally, you can use conversation.variant to interrogate the client's provided
; list of supported languages and encoding.

(defn handle-get [conversation]
	(let [state (get-state conversation), r (json-str state)]

		; Return a representation appropriate for the requested media type
		; of the possible options we created in handle-init
	
		(if (= (.. conversation getMediaTypeName) "application/json")
			(JsonRepresentation. (str r))
			r)))

; This function is called for the POST verb, which is expected to behave as a
; logical "update" of the resource's state.
;
; The expectation is that conversation.entity represents an update to the state,
; that will affect future calls to handle-get. As such, it may be possible
; to accept logically partial representations of the state.
;
; You may optionally return a representation, in the same way as handle-get.
; Because Clojure function return the last value by default,
; you must explicitly return a nil if you do not want to return a representation
; to the client.

(defn handle-post [conversation]
	(let [update (read-json (.. conversation getEntity getText))
		state (get-state conversation)]
		(set-state conversation (merge state update)))
	(handle-get conversation))

; This function is called for the PUT verb, which is expected to behave as a
; logical "create" of the resource's state.
;
; The expectation is that conversation.entity represents an entirely new state,
; that will affect future calls to handle-get. Unlike handle-post,
; it is expected that the representation be logically complete.
;
; You may optionally return a representation, in the same way as handle-get.
; Because JavaScript functions return the last statement's value by default,
; you must explicitly return a null if you do not want to return a representation
; to the client.

(defn handle-put [conversation]
	(let [update (read-json (.. conversation getEntity getText))]
		(set-state conversation update))
	(handle-get conversation))

; This function is called for the DELETE verb, which is expected to behave as a
; logical "delete" of the resource's state.
;
; The expectation is that subsequent calls to handle-get will fail. As such,
; it doesn't make sense to return a representation, and any returned value will
; ignored. Still, it's a good idea to return null to avoid any passing of value.

(defn handle-delete [conversation]
	(set-state conversation {})
	
	nil)