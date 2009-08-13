
              Introduction to the Prudence Distribution


This is a demo application for using scripting languages with REST principles.
It is designed to run as is on any Java 5 platform and above. All dependent
libraries are included. It contains demos for all supported scripting
languages.

The file structure of this application:

run.sh -      Run this to start the application. On most operating systems, you
              need to enable the "executable" permission on this file first.
              After running, you can browse to the application at
              http://localhost:8080/
              
              Edit this file to make sure you are using the correct Java.

run.bat -     As above, for Windows.

main.script - This script file runs first. It is in charge of starting the
              server, logging, etc. It is configured by conf/restoration.conf.

conf/

   These are editable configuration files.

   restoration.conf - Main configuration. It is used by main.script. Here you can
                      configure the HTTP port used, directory paths, etc.

   logging.conf -     Configuration file for logging. Used by log4j.

resources/

   These are your REST resources. Their actual URL is based on the directory
   structure and definitions in restoration.conf.

web/

   This is the root URL of the server. Files put here are served as is, unless
   they end in ".page", in which case they are considered HTML (or other text)
   files with embedded scriptlets. You can change the default extension used
   via restoration.conf.
   
lib/

   Java libraries used by Prudence.
   
licenses/

   Licenses for the Java libraries mentioned above.
   