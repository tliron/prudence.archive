
document.execute('/sincerity/container/')

var config = sincerity.container.getConfigurationFile('hazelcast.conf')
if (config.exists()) {
	try {
		importClass(
			com.hazelcast.config.FileSystemXmlConfig,
			com.hazelcast.core.Hazelcast)

		config = new FileSystemXmlConfig(config)
		Hazelcast.newHazelcastInstance(config)
	} catch (x) {}
}
else
{
	try {
		importClass(
			com.hazelcast.config.Config,
			com.hazelcast.config.MapConfig,
			com.hazelcast.config.MultiMapConfig,
			com.hazelcast.config.QueueConfig,
			com.hazelcast.config.TopicConfig,
			com.hazelcast.config.SemaphoreConfig,
			com.hazelcast.config.MergePolicyConfig,
			com.hazelcast.config.WanReplicationConfig,
			com.hazelcast.config.ExecutorConfig,
			com.hazelcast.config.NetworkConfig,
			com.hazelcast.config.ListenerConfig,
			com.hazelcast.config.SecurityConfig,
			com.hazelcast.config.ManagementCenterConfig,
			com.hazelcast.config.PartitionGroupConfig,
			com.hazelcast.core.Hazelcast)
		
		config = new Config()
		
		Sincerity.Container.executeAll(sincerity.container.getConfigurationFile('hazelcast'))
		
		Hazelcast.newHazelcastInstance(config)
	} catch (x) {}
}
