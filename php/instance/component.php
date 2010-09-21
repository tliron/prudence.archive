<?php
//
// Prudence Component
//
// Copyright 2009-2010 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

global $component;

$document->execute('defaults/instance/component/');

import com.threecrickets.prudence.cache.H2Cache;
import com.threecrickets.prudence.cache.ChainCache;

// Create an H2-database-backed cache chained after the default memory cache
$default_cache = $component->context->attributes['com.threecrickets.prudence.cache'];
$chain_cache = new ChainCache();
$chain_cache->caches->add($default_cache);
$chain_cache->caches->add(new H2Cache($document->source->basePath->path . '/cache/prudence/prudence'));
$component->context->attributes['com.threecrickets.prudence.cache'] = $chain_cache;
?>