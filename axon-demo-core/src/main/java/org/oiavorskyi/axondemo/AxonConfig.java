package org.oiavorskyi.axondemo;

import static com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaTerminalFactory.from;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventBusTerminal;
import org.axonframework.eventhandling.SimpleCluster;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.fs.FileSystemEventStore;
import org.axonframework.eventstore.fs.SimpleEventFileResolver;
import org.oiavorskyi.axondemo.aggregates.CargoTracking;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;
import com.viadeo.axonframework.eventhandling.cluster.ClassnameDynamicClusterSelectorFactory;
import com.viadeo.axonframework.eventhandling.cluster.ClusterFactory;
import com.viadeo.axonframework.eventhandling.cluster.ClusterSelectorFactory;
import com.viadeo.axonframework.eventhandling.terminal.kafka.ConsumerFactory;
import com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaTerminal;
import com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaTerminalFactory;
import com.viadeo.axonframework.eventhandling.terminal.kafka.PrefixTopicStrategy;
import com.viadeo.axonframework.eventhandling.terminal.kafka.TopicStrategy;
import com.viadeo.axonframework.eventhandling.terminal.kafka.TopicStrategyFactory;

import kafka.producer.Producer;
import kafka.producer.ProducerConfig;

@Configuration
public class AxonConfig {

	public static final ClusterFactory CLUSTER_FACTORY = new ClusterFactory() {
		@Override
		public Cluster create(String name) {
			return new SimpleCluster(name);
		}
	};

	public static KafkaTerminalFactory createKafkaTerminalFactory(final Map<String, String> properties) {
		return new KafkaTerminalFactory(from(properties));
	}

	private static final String PREFIX = "com.viadeo.axonframework.eventhandling.cluster";
	public static final ImmutableMap<String, String> KAFKA_PROPERTIES_MAP = ImmutableMap.<String, String>builder()
			// PRODUCER
			.put("metadata.broker.list", "localhost:9092").put("request.required.acks", "1")
			.put("producer.type", "sync")

			// CONSUMER
			.put("zookeeper.connect", "localhost:2181")
			// this property will be overridden by the cluster
			.put("group.id", "testgroup")
			// !important; without the following property then this suite is
			// unstable (due to the process of the auto creation topic)
			.put("auto.offset.reset", "smallest")

			.put("zookeeper.session.timeout.ms", "400").put("zookeeper.sync.time.ms", "300")
			.put("auto.commit.interval.ms", "1000").build();

	public static ClusterSelectorFactory createClusterSelectorFactory(final String prefix) {
		return new ClassnameDynamicClusterSelectorFactory(prefix, new ClusterFactory() {
			@Override
			public Cluster create(final String name) {
				return new SimpleCluster(name);
			}
		});
	}

	@Bean
	public EventBus eventBus() {

		Properties producerProps = new Properties();
		producerProps.put("metadata.broker.list", "localhost:9092");
		producerProps.put("zookeeper.connect", "localhost:2181");
		// producerProps.put("serializer.class",
		// "kafka.serializer.DefaultEncoder");
		producerProps.put("request.required.acks", "1");
		producerProps.put("group.id", "testgroup");
		producerProps.put("zookeeper.session.timeout.ms", "400");
		producerProps.put("zookeeper.sync.time.ms", "300");
		producerProps.put("auto.commit.interval.ms", "1000");
		ProducerConfig producerConfig = new ProducerConfig(producerProps);
		Producer producer = new Producer<Integer, String>(producerConfig);

		final KafkaTerminalFactory terminalFactory = new KafkaTerminalFactory(producerProps);
		//
		TopicStrategyFactory currentTopicStrategyFactory = new TopicStrategyFactory() {
			@Override
			public TopicStrategy create() {
				final String prefix = UUID.randomUUID().toString();

				// set the generated prefix as property, TODO found a better way
				// in order to pass this restriction to our consumer
				terminalFactory.setConsumerProperty(ConsumerFactory.CONSUMER_TOPIC_FILTER_REGEX, prefix + ".*");

				return new PrefixTopicStrategy(prefix);
			}
		};
		TopicStrategy currentTopicStrategy = currentTopicStrategyFactory.create();
		EventBusTerminal currentTerminal = terminalFactory.with(currentTopicStrategy).create();
		KafkaTerminal kt = createKafkaTerminalFactory(KAFKA_PROPERTIES_MAP).create();
		return new ClusteringEventBus(createClusterSelectorFactory(PREFIX).create(), kt);

	}

	@Bean
	public EventSourcingRepository<CargoTracking> cargoTrackingRepository(EventStore eventStore) {
		EventSourcingRepository<CargoTracking> repository = new EventSourcingRepository<>(CargoTracking.class,
				eventStore);
		repository.setEventBus(eventBus());

		return repository;
	}

	@Bean
	EventStore eventStore() throws IOException {
		// TODO: Change to the proper version of event store (e.g. RDBMS or
		// Redis-based)
		Path tempDirectory = Files.createTempDirectory("axon-demo-events");
		return new FileSystemEventStore(new SimpleEventFileResolver(tempDirectory.toFile()));
	}

	// CargoTrackingCommandMessage commandmessage;

}
