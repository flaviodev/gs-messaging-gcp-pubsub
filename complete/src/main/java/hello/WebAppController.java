package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hello.PubSubApplication.PubsubOutboundGateway;

@RestController
public class WebAppController {

	@Autowired
	private PubsubOutboundGateway messagingGateway;

	@PostMapping("/publishMessage")
	public RedirectView publishMessage(@RequestParam("clientId") String clientId,
			@RequestParam("message") String message) throws JsonProcessingException {

		CustomMessage customMessage = new CustomMessage(clientId, message);
		ObjectMapper mapper = new Jackson2ObjectMapperBuilder().build();

		messagingGateway.sendToPubsub(clientId, mapper.writeValueAsString(customMessage));
		return new RedirectView("/");
	}

}
