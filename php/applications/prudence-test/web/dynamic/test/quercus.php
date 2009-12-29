<html>
<head>
	<title>PHP Scriptlets Demo</title>
	<link rel="stylesheet" type="text/css" href="../style/soft-cricket.css" />
</head>
<body>
<table width="100%"><tr valign="top"><td>
<?php

//
// Accessing the request
//

$form = $prudence->resource->request->resourceRef->queryAsForm;

//
// Stream this document
//

if($form->getFirstValue('stream') == 'true') if($prudence->stream()) exit();

$document->cacheDuration = 5000;

//
// Calling Java
//

import java.lang.System;
print 'This page was dynamically generated at ' . System::currentTimeMillis();

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

function printFactory($factory) {
?>
<p>
	<i>Engine:</i> <?= $factory->engineName ?> version <?= $factory->engineVersion ?><br />
	<i>Language:</i> <?= $factory->languageName ?> version <?= $factory->languageVersion ?><br />
	<i>Names:</i> 
<?
	$names = $factory->names->toArray();
	for($i = 0; $i < count($names); $i++) {
		print($names[$i]);
		if($i < count($names) - 1) {
			print ', ';
		}
	}
?>
</p>
<?
}
?>
<h3>Script engine used:</h3>
<?
printFactory($document->engine->factory);
?>
<h3>Available script engines:</h3>
<?
$factories = $document->engineManager->engineFactories->toArray();
foreach($factories as $factory) {
	printFactory($factory);
}
?>
</td><td>
<h3>The "id" attribute in the URL query is:</h3>
<p><?= $form->getFirstValue('id') ?></p>
<h3>A few tests:</h3>
<?
global $george;
for($i = 0; $i < 10; $i++) {
?>
A multiple of three: 
<?
	printTriple($i);
?>
<br />
<?
}
?>
</td></tr></table>
</body>
<html>