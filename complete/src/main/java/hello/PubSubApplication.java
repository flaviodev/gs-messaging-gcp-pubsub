package hello;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubOperations;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;

@SpringBootApplication
public class PubSubApplication {

	private static final Log LOGGER = LogFactory.getLog(PubSubApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PubSubApplication.class, args);
	}

	@Bean
	public MessageChannel pubsubInputChannel() {
		return new DirectChannel();
	}

	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
			@Qualifier("pubsubInputChannel") MessageChannel inputChannel, PubSubOperations pubSubTemplate) {
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "testSubscription");
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.MANUAL);

		adapter.setPayloadType(CustomMessage.class);
		ObjectMapper mapper = new Jackson2ObjectMapperBuilder().build();
		adapter.setMessageConverter(new JacksonPubSubMessageConverter(mapper));

		return adapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "pubsubInputChannel")
	public MessageHandler messageReceiverClient() {
		return message -> {
			LOGGER.info("clientId: " + message.getHeaders().get("clientId"));
			LOGGER.info("orgId: " + message.getHeaders().get("orgId"));
			LOGGER.info("message: " + message.getPayload());

			AckReplyConsumer consumer = (AckReplyConsumer) message.getHeaders().get(GcpPubSubHeaders.ACKNOWLEDGEMENT);
			consumer.ack();
		};
	}

	@Bean
	@ServiceActivator(inputChannel = "pubsubOutputChannel")
	public MessageHandler messageSender(PubSubOperations pubsubTemplate) {

		PubSubMessageHandler adapter = new PubSubMessageHandler(pubsubTemplate, "testTopic");
		adapter.setPublishCallback(new ListenableFutureCallback<String>() {
			@Override
			public void onFailure(Throwable ex) {
				LOGGER.info("There was an error sending the message.");
			}

			@Override
			public void onSuccess(String result) {
				LOGGER.info("Message was sent successfully.");
			}
		});

		return adapter;
	}

	@MessagingGateway(defaultRequestChannel = "pubsubOutputChannel", defaultHeaders = @GatewayHeader(name = "orgId", value = "1"))
	public interface PubsubOutboundGateway {

		void sendToPubsub(@Header("clientId") String clientId, Object payload);
	}
}
