//
// This file is part of the Prudence Foundation Library
//
// Copyright 2011-2012 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.executeOnce('/sincerity/classes/')
document.executeOnce('/sincerity/objects/')
document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/json/')

var Prudence = Prudence || {}

/**
 * Lazy, thread-safe injection of resources via a straightforward JSON-based DSL.
 * <p>
 * There are three motivations for this library:
 * <ol>
 * <li>First is that sometimes you want to signal the creation of a resource in one execution context, but have it actually
 * instantiated in a different one. The most common use case Prudence is that you configure a resource in your settings.js,
 * but want it created only when the application is up and running.</li>
 * <li>A second motivation is to allow for resources to be initialized only on demand, instead of up front. In some applications
 * this could result in significant savings. With lazy construction, instantiation happens only when the resource is
 * accessed.</li>
 * <li>Third is allowing for a straightforward DSL for object creation. This library lets you define your objects using
 * a simple JSON structure that can be easily stored, edited and marshalled.</li>
 * </ol>
 * The core of the library is the {@link Prudence.Lazy.LazyEntry} wrapper class, which relies on high-performance JVM read/write
 * locks to make sure that wrapped values are only instantiated once in concurrent situations. Behind the scenes, lazy
 * construction works via a regular JavaScript closure -- here called an "instantiator" -- that returns the resource instance.
 * <p>
 * Also useful are the {@link Prudence.Lazy.List} and {@link Prudence.Lazy.Map} classes, which provide two common use cases
 * for lazy construction. Both allow thread-safe mixing of lazy and non-lazy instances, and abstract away the differences
 * between the two, making it easy to just use the resources and not have to worry about lazy construction.
 * <p>
 * Though you can use these classes directly, the easier way to use this library is to use use {@link Prudence.Lazy#buildOne}
 * or {@link Prudence.Lazy#build} to create lazy configurations via the simple DSL, and then store the result in your
 * application.globals. You can then call {@link Prudence.Lazy#getGlobalList}, {@link Prudence.Lazy#getGlobalMap} or
 * {@link Prudence.Lazy#getGlobalEntry} to get the result on demand.
 * <p>
 * It's important to know that resources might be <i>reinstantiated</i>! This happens if any of the "reset" functions
 * are called on the entries. Why would you want this to happen? There may be many reasons for this, but a common one
 * is that during development files may be edited, and the developer would want the changes to be immediately reflected
 * without having to restart the Prudence instance. This means that you do not simply want to discard the {@link Prudence.Lazy#LazyEntry}
 * instance after you called "get" on it. Instead, you want to always call "get" whenever you want to access the resource,
 * as it may return a different result upon each call.
 * 
 * @namespace
 * 
 * @author Tal Liron
 */
Prudence.Lazy = Prudence.Lazy || function() {
	/** @exports Public as Prudence.Lazy */
    var Public = {}

	/**
	 * Creates the right kind of entry instance for a value. If the value is an instantiator,
	 * a {@link Prudence.Lazy.LazyEntry} is used, otherwise it's just a {@link Prudence.Lazy.Entry}.
	 * <p>
	 * Note that a function <i>or a string</i> will both be considered as instantiators!
	 * Strings will be treated as the source code of the instantiator function.
	 * 
	 * @param value The value
	 * @returns {Prudence.Lazy.Entry|Prudence.Lazy.LazyEntry}
	 */
	Public.createEntry = function(value) {
		return isConstructor(value) ? new Public.LazyEntry(value) : new Public.Entry(value)
	}
	
	/**
	 * Can either return the entry instance or the wrapped value. If you
	 * want the wrapped value, you must supply the "createFn" param.
	 * <p>
	 * An extra application.global will be used to store the {@link Prudence.Lazy.Entry} or {@link Prudence.Lazy.LazyEntry}
	 * instance. It will be named as the name param plus ".lazy".
	 *
	 * @param {String} name The name of the application.global where the entry's configuration is stored
	 * @param {Prudence.Logging.Logger} logger The logger (used for creation)
	 * @param {Function} [createFn] The creator function (see {@link Prudence.Lazy.LazyEntry#get})
	 * @returns {Prudence.Lazy.Entry|Prudence.Lazy.LazyEntry|Object} Null if not found
	 */
	Public.getGlobalEntry = function(name, logger, createFn) {
		var lazyName = name + '.lazy'
		var lazyEntry = application.globals.get(lazyName)

		if (!Sincerity.Objects.exists(lazyEntry)) {
			var value = application.globals.get(name)
			if (Sincerity.Objects.exists(value)) {
				lazyEntry = Public.createEntry(value)
				lazyEntry = application.getGlobal(lazyName, lazyEntry)
			}
		}
		
		if (lazyEntry && createFn) {
			lazyEntry = lazyEntry.get(createFn, logger)
			return lazyEntry ? lazyEntry.instance : null
		}
		
		return lazyEntry
	}

	/**
	 * Can either return the {@link Prudence.Lazy.List} instance or a JavaScript array of
	 * the wrapped values. If you want the array, you must supply the "createFn" param.
	 * <p>
	 * An extra application.global will be used to store the {@link Prudence.Lazy.List} instance.
	 * It will be named as the name param plus ".lazy".
	 * 
	 * @param {String} name The name of the application.global where the list's configuration is stored
	 * @param {Prudence.Logging.Logger} logger The logger (used for creation)
	 * @param {Function} [createFn] The creator function (see {@link Prudence.Lazy.LazyEntry#get})
	 * @returns {Prudence.Lazy.List|Array}
	 */
	Public.getGlobalList = function(name, logger, createFn) {
		var lazyName = name + '.lazy'
		var lazyList = application.globals.get(lazyName)

		if (!Sincerity.Objects.exists(lazyList)) {
			var list = application.globals.get(name)
			if (Sincerity.Objects.exists(list)) {
				lazyList = new Public.List({logger: logger})
				lazyList.addAll(list)
				lazyList = application.getGlobal(lazyName, lazyList)
			}
		}
		
		return lazyList && createFn ? lazyList.toArray(createFn) : lazyList
	}
		
	/**
	 * Can either return the {@link Prudence.Lazy.Map} instance or a JavaScript dict of
	 * the wrapped values. If you want the dict, you must supply the "createFn" param.
	 * <p>
	 * An extra application.global will be used to store the {@link Prudence.Lazy.Map} instance.
	 * It will be named as the name param plus ".lazy".
	 * 
	 * @param {String} name The name of the application.global where the map's configuration is stored
	 * @param {Prudence.Logging.Logger} logger The logger (used for creation)
	 * @param {Function} [createFn] The creator function (see {@link Prudence.Lazy.LazyEntry#get})
	 * @returns {Prudence.Lazy.Map|Object}
	 */
	Public.getGlobalMap = function(name, logger, createFn) {
		var lazyName = name + '.lazy'
		var lazyMap = application.globals.get(lazyName)

		if (!Sincerity.Objects.exists(lazyMap)) {
			var map = application.globals.get(name)
			if (Sincerity.Objects.exists(map)) {
				lazyMap = new Public.Map({logger: logger})
				lazyMap.putAll(map)
				lazyMap = application.getGlobal(lazyName, lazyMap)
			}
		}
		
		return lazyMap && createFn ? lazyMap.toDict(createFn) : lazyMap
	}
	
	/**
	 * Builds the source code for an instantiator function based on a simple DSL.
	 * 
	 * @param config
	 * @param {String} name The name of the constructor to instantiate (via keyword "new")
	 * @param {String[]|String} [config.dependencies] One or more documents to executeOnce before instantiation
	 * @param [config.config] Optional config to be sent to the constructor
	 * @returns {String} The JavaScript source code (can be evaled)
	 */
	Public.buildOne = function(config) {
		var fn = 'function(){\n'
		if (config.dependencies) {
			config.dependencies = Sincerity.Objects.array(config.dependencies)
			for (var d in config.dependencies) {
				fn += 'document.executeOnce(\'' + config.dependencies[d].escapeSingleQuotes() + '\');\n'
			}
		}
		if (config.config) {
			fn += 'document.executeOnce(\'/sincerity/json/\');\n'
		}
		fn += 'return new ' + config.name + '('
		if (config.config) {
			fn += 'Sincerity.JSON.fromExtendedJSON(' + Sincerity.JSON.to(config.config, true) + ')'
		}
		fn += ');\n}'
		return fn
	}

	/**
	 * Builds an array or dict of instantiators based on a simple DSL.
	 * 
	 * @returns {Array|Object}
	 */
	Public.build = function(configs) {
		var r
		if (Sincerity.Objects.isArray(configs)) {
			r = []
		}
		else {
			r = {}
		}
		
		for (var c in configs) {
			var config = configs[c]
			config.config = config.config || {}
			config.config.name = config.config.name || c
			r[c] = Public.buildOne(config)
		}
		
		return r
	}
		
	/**
	 * A trivial wrapper over a value. Exists to allow for a common interface with
	 * {@link Prudence.Lazy.LazyEntry}.
	 * 
	 * @class
	 * @name Prudence.Lazy.Entry
	 * @param instance The value to wrap
	 */
	Public.Entry = Sincerity.Classes.define(function() {
		/** @exports Public as Prudence.Lazy.Entry */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(instance) {
	    	this.instance = instance
	    }

	    /**
		 * Retrieves the current instance, plus information about whether
		 * it was lazily created or already existed.
		 * 
		 * @param {Function} createFn A function that receives the source code for am instantiator function, and should
		 *        return the created instance; it will be called only if the instance has to be instantiated
		 * @returns A dict in the form {instance: instance, create: true/false}
		 */
	    Public.get = function(createFn, logger) {
			return {
				instance: this.instance,
				created: false
			}
		}
		
		/**
		 * Resets the instance, if supported.
		 * 
		 * @returns {Boolean} True if was reset, false if was not or could not be reset
		 */
		Public.reset = function() {
			return false
		}
		
		return Public
	}())

	/**
	 * @class
	 * @name Prudence.Lazy.LazyEntry
	 * @augments Prudence.Lazy.Entry
	 * @param {Function|String} instantiator The function used to create the wrapped value
	 */
	Public.LazyEntry = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Lazy.LazyEntry */
	    var Public = {}
	    
	    /** @ignore */
	    Public._inherit = Module.Entry

	    /** @ignore */
	    Public._construct = function(instantiator) {
			// We will need to eval this later so that we don't use Rhino's compiled version,
			// which contains optimizations that would have the wrong scope for us
			this.instantiator = String(instantiator)

			this.instance = null
			this.lock = Sincerity.JVM.newLock(true)
	    }

	    Public.get = function(createFn, logger) {
	    	logger = logger || application.logger
	    	this.lock.readLock().lock()
			try {
				if (this.instance) {
					return {
						instance: this.instance,
						created: false
					}
				}

				this.lock.readLock().unlock()
				this.lock.writeLock().lock()
				try {
					if (this.instance) {
						return {
							instance: this.instance,
							created: false
						}
					}

					try {
						this.instance = createFn(this.instantiator)
					}
					catch (x) {
						logger.warning(x)
						throw x
					}
					return {
						instance: this.instance,
						created: true
					}
				}
				finally {
					this.lock.writeLock().unlock()
					this.lock.readLock().lock()
				}
			}
			finally {
				this.lock.readLock().unlock()
			}
		}
		
	    Public.reset = function() {
			this.lock.writeLock().lock()
			try {
				if (this.instance) {
					this.instance = null
					return true
				}
				
				return false
			}
			finally {
				this.lock.writeLock().unlock()
			}
		}
		
		return Public
	}(Public))
	
	/**
	 * A thread-safe list of values, some of which may be lazily constructed.
	 * 
	 * @class
	 * @name Prudence.Lazy.List
	 * @param config
	 * @param {Prudence.Logging.Logger} config.logger The logger
	 * @param {java.util.List} [config.list] You can provide your own (thread-safe) list, or let the class create its own
	 */
	Public.List = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Lazy.List */
	    var Public = {}

	    /** @ignore */
	    Public._construct = function(config) {
			this.list = config.list || Sincerity.JVM.newList(true)
			this.logger = config.logger || application.logger
	    }

	    /**
	     * Resets all entries.
	     * 
	     * @see Prudence.Lazy.Entry#reset
	     */
	    Public.reset = function() {
			var index = 0
			for (var i = this.list.iterator(); i.hasNext(); ) {
				var entry = i.next()
				if (entry.reset()) {
					this.logger.info('Reset lazy entry: ' + index)
				}
				index++
			}
		}
		
	    /**
	     * Gets a value, transparently constructing lazy entries if necessary.
	     * 
	     * @param {Number} index The list index
		 * @param {Function} createFn A function that receives the source code for am instantiator function, and should
		 *        return the created instance; it will be called only if the instance has to be instantiated
	     * @returns The value or null if not found
	     */
	    Public.get = function(index, createFn) {
			var entry = this.list.get(index)
			if (entry) {
				entry = entry.get(createFn, this.logger)
				if (entry.created) {
					this.logger.info('Created lazy entry: ' + index)
				}
				return entry.instance
			}
			return null
		}
		
	    /**
	     * Sets a value, internally wrapping it with an entry instance.
	     * 
	     * @param {Number} index The list index
	     * @param value The value
	     * @see Prudence.Lazy#createEntry
	     */
	    Public.set = function(index, value) {
	    	this.list.set(index, Module.createEntry(value))
		}
		
	    /**
	     * Adds an entry to the end of the list, internally wrapping it with an entry instance.
	     * 
	     * @param value The value
	     * @see Prudence.Lazy#createEntry
	     */
	    Public.add = function(value) {
	    	this.list.add(Module.createEntry(value))
		}

	    /**
	     * Adds all values in the array to the end of the list, internally wrapping them with a entry instances.
	     * <p>
	     * Note that this operation is not atomic, and each value is added separately. Concurrent calls may
	     * result in a different order than is expected.
	     *
	     * @param {Array} array The values
	     * @see Prudence.Lazy#createEntry
	     */
	    Public.addAll = function(array) {
			for (var a in array) {
				this.add(array[a])
			}
		}
		
	    /**
	     * Converts the list to a JavaScript array of values, transparently constructing lazy entries if necessary.
	     * 
		 * @param {Function} createFn A function that receives the source code for am instantiator function, and should
		 *        return the created instance; it will be called only if the instance has to be instantiated
		 * @returns {Array}
	     */
	    Public.toArray = function(createFn) {
			var array = []
			var index = 0
			for (var i = this.list.iterator(); i.hasNext(); ) {
				var entry = i.next()
				entry = entry.get(createFn, this.logger)
				if (Sincerity.Objects.exists(entry)) {
					if (entry.created) {
						this.logger.info('Created lazy entry: ' + index)
					}
					array.push(entry.instance)
				}
				index++
			}
			return array
		}
		
		return Public
	}(Public))

	/**
	 * @class
	 * @name Prudence.Lazy.Map
	 * @param config
	 * @param {Prudence.Logging.Logger} config.logger The logger
	 * @param {java.util.ConcurrentMap} [config.map] You can provide your own (thread-safe) map, or let the class create its own
	 */
	Public.Map = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Lazy.Map */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(config) {
			this.map = config.map || Sincerity.JVM.newMap(true)
			this.logger = config.logger || application.logger
	    }

	    /**
	     * Resets all entries.
	     * 
	     * @see Prudence.Lazy.Entry#reset
	     */
	    Public.reset = function() {
			for (var i = this.map.entrySet().iterator(); i.hasNext(); ) {
				var entry = i.next()
				if (entry.value.reset()) {
					this.logger.info('Reset lazy entry: ' + entry.key)
				}
			}
		}
		
	    /**
	     * Gets a value, transparently constructing lazy entries if necessary.
	     * 
	     * @param {Number} name The map key
		 * @param {Function} createFn A function that receives the source code for am instantiator function, and should
		 *        return the created instance; it will be called only if the instance has to be instantiated
	     * @returns The value or null if not found
	     */
	    Public.get = function(name, createFn) {
			var entry = this.map.get(name)
			if (entry) {
				entry = entry.get(createFn, this.logger)
				if (entry.created) {
					this.logger.info('Created lazy entry: ' + name)
				}
				return entry.instance
			}
			return null
		}
		
	    /**
	     * Puts a value in the mae, internally wrapping it with an entry instance.
	     * 
	     * @param {Number} name The map key
	     * @param value The value
	     * @see Prudence.Lazy#createEntry
	     */
	    Public.put = function(name, value) {
	    	this.map.put(name, Module.createEntry(value))
		}
		
	    /**
	     * Puts all values in the dict into the map, internally wrapping them with a entry instances.
	     * <p>
	     * Note that this operation is not atomic, and each value is put separately.
	     *
	     * @param {Object} dict The values
	     * @see Prudence.Lazy#createEntry
	     */
	    Public.putAll = function(dict) {
			for (var name in dict) {
				this.put(name, dict[name])
			}
		}
		
	    /**
	     * Converts the list to a JavaScript dict of values, transparently constructing lazy entries if necessary.
	     * 
		 * @param {Function} createFn A function that receives the source code for am instantiator function, and should
		 *        return the created instance; it will be called only if the instance has to be instantiated
		 * @returns {Object}
	     */
	    Public.toDict = function(createFn) {
			var dict = {}
			for (var i = this.map.entrySet().iterator(); i.hasNext(); ) {
				var mapEntry = i.next()
				var entry = mapEntry.value.get(createFn, this.logger)
				if (Sincerity.Objects.exists(entry)) {
					if (entry.created) {
						this.logger.info('Created lazy entry: ' + mapEntry.key)
					}
					dict[mapEntry.key] = entry.instance
				}
			}
			return dict
		}
		
		return Public
	}(Public))

	//
	// Private
	//
	
	function isConstructor(value) {
		return (typeof value == 'function') || Sincerity.Objects.isString(value)
	}

	return Public
}()
