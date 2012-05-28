
importClass(java.lang.System)

System.setProperty('hazelcast.config', String(sincerity.container.getConfigurationFile('hazelcast.conf')))
try {
	com.hazelcast.core.Hazelcast.defaultInstance
} catch (x) {}
