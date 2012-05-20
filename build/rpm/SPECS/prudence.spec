Name:           prudence
Summary:        A RESTful multilingual web development platform for the JVM.
Version:        2.0beta1
Release:        0
Group:          Three Crickets
License:        ApacheV2

%description 
A RESTful multilingual web development platform for the JVM.

%prep

%build

%clean 

%install

%post
mkdir -p /usr/lib/prudence/.sincerity

mkdir -p /var/logs/prudence
chmod a+w /var/logs/prudence
ln -fsT /var/logs/prudence /usr/lib/prudence/logs

mkdir -p /var/cache/prudence
chmod a+w /var/cache/prudence
ln -fsT /var/cache/prudence /usr/lib/prudence/cache 

chmod a+w -R /var/lib/prudence

ln -fsT /var/lib/prudence/programs /usr/lib/prudence/programs 
ln -fsT /var/lib/prudence/libraries /usr/lib/prudence/libraries 
ln -fsT /var/lib/prudence/component /usr/lib/prudence/component 
ln -fsT /etc/prudence /usr/lib/prudence/configuration 

%preun
rm -rf /usr/lib/prudence/.sincerity
rm -f /usr/lib/prudence/logs
rm -f /usr/lib/prudence/cache
rm -f /usr/lib/prudence/programs
rm -f /usr/lib/prudence/libraries
rm -f /usr/lib/prudence/component
rm -f /usr/lib/prudence/configuration

%files
/*

%changelog
* Thu May 10 2012 Tal Liron <tal.liron@threecrickets.com>
- Initial release
