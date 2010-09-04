<?php
//
// Prudence Component
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