<html>
<head>
	<title>PHP Scriptlets Demo</title>
	<link rel="stylesheet" type="text/css" href="../../style/soft-cricket.css" />
</head>
<body>
<table width="100%"><tr valign="top"><td>
<?php

//
// Accessing the request
//

$form = $conversation->resource->request->resourceRef->queryAsForm;

//
// Stream this page
//

if($form->getFirstValue('stream') == 'true') if($conversation->stream()) exit();

//
// Cache this page
//

$prudence->cacheDuration = 5000;
$prudence->cacheKey = '{name}?{rq}'; // Include query in cache key

//
// Calling Java
//

import java.lang.System;
print '<p>This page was dynamically generated at ' . System::currentTimeMillis() . '</p>';

//
// Including a document
//
// (see comments in fragments/quercus.html)
//
// This is identical to:
//
//   include $basePath . '/path';
//

?>
<?& '../fragments/test/quercus.php' ?>
<?

//
// An example of a function
//

function print_adapter($adapter) {
?>
<p>
	<i>Adapter:</i> <?= $adapter->attributes->get('name') ?> version <?= $adapter->attributes->get('version') ?><br />
	<i>Language:</i> <?= $adapter->attributes->get('language.name') ?> version <?= $adapter->attributes->get('language.version') ?><br />
	<i>Tags:</i> 
<?
	$tags = $adapter->attributes->get('tags')->toArray();
	for($i = 0; $i < count($tags); $i++) {
		print($tags[$i]);
		if($i < count($tags) - 1) {
			print ', ';
		}
	}
?>
</p>
<?
}
?>
<h3>Language used:</h3>
<?
print_adapter($executable->context->adapter);
?>
<h3>Available languages:</h3>
<?
$adapters = $executable->context->manager->adapters->toArray();
foreach($adapters as $adapter) {
	print_adapter($adapter);
}
?>
</td><td>
<h3>The "id" attribute in the URL query is:</h3>
<p><?= $form->getFirstValue('id') ?></p>
<h3>A few tests:</h3>
<p>
<?
for($i = 0; $i < 10; $i++) {
?>
A multiple of three: 
<?
	print_triple($i);
?>
<br />
<?
}
?>
</p>
</td></tr></table>
</body>
<html>