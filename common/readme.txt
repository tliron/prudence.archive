
Prudence is hosted at:

 http://threecrickets.com/prudence/

Access to complete source code and support forums is available there.

See "license.txt" for licensing information.


============
REQUIREMENTS
============

#if($os == 'windows')
This "readme-windows.txt" file is intended for Windows operating systems. If you are running Linux,
Solaris, OS X, or other Unix-like operating systems, see the main "readme.txt".
#else
This "readme.txt" file is intended for Linux, Solaris, OS X, and other Unix-like operating systems.
If you are running Windows, see "readme-windows.txt".
#end

You need a JVM-enabled operating system. Prudence requires at least at least JVM version 5. The
"Standard Edition" JVM is just fine; Prudence does not require or use the "Enterprise Edition"
features.

Open a terminal, and run "java" to see if you have a JVM installed.

Otherwise, an excellent free JVM is available from the OpenJDK project:

  http://openjdk.java.net/
#if(($distribution == 'php') || ($distribution == 'kitchensink'))

The Prudence distribution you've downloaded also requires the Quercus PHP Engine library. We were
unable to include it in our download due to licensing restrictions (it is GPL).

To install Quercus, download it from here (9.5 mb):

  http://quercus.caucho.com/

Open the .war file (it's a standard .zip) and extract the "resin.jar" file. Put this file in
Prudence's /libraries/ directory. (We also like to rename it to "com.caucho.quercus.jar" for
consistency.)

Prudence has been tested with Quercus 4.0.
#end


============
INSTALLATION
============

Your Prudence distribution is designed to run in "console" mode directly from the installation
folder, so no further copying of files anywhere else is necessary.

Console mode is absolutely good enough for the development phase of your project.

If the future, if you need to running Prudence in daemon mode, you will need further installation
steps, which are detailed in the Prudence Manual:

http://threecrickets.com/prudence/manual/daemon/


=============================================
TESTING AND TROUBLESHOOTING YOUR INSTALLATION
=============================================

#if($os == 'windows')
Open a command line interface (cmd.exe) and run "bin\run.bat" from your Prudence distribution.
This should output usage information.

If that worked, you can now try to run Prudence in console mode: "bin\run.bat console".
#else
Open a terminal and run "bin/run.sh" from your Prudence distribution. This should output usage
information.

If that didn't work, it means you may may need to change the permissions of the "bin/run.sh" file
to allow execution. This command should work on Unix-like operating systems:

  chmod a+rx bin/run.sh

If that worked, you can now try to run Prudence in console mode: "bin/run.sh console".
#end

If all goes well, Prudence should announce its version and let you know when it's up and running!
You can then point your web browser to "http://localhost:8080/" to see the demo site.

If that did not work, it could be that your JVM is improperly installed. See REQUIREMENTS above.
#if($os != 'windows')

If the JVM seems to be installed and Prudence still cannot start up, it could be that Prudence is
unable to find your JVM.

The "run.sh" script respects the standard JAVA_HOME environment variable, so you can set that to
point to the base of your JVM installation. A quick way to test if this is the issue is to set it
as a prefix in the command line:

  JAVA_HOME=/usr/lib/jvm/java-6-openjdk bin/run.sh  
#end


=====================
STILL HAVING TROUBLE?
=====================

Join the Prudence Community, and tell us where you're stuck! We're very happy to help newcomers
get up and running:

  http://groups.google.com/group/prudence-community
