package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import hello.PubSubApplication.PubsubOutboundGateway;

@RestController
public class WebAppController {

	@Autowired
	private PubsubOutboundGateway messagingGateway;

	@PostMapping("/publishMessage")
	public RedirectView publishMessage(@RequestParam("clientId") String clientId, @RequestParam("message") String message) {
		messagingGateway.sendToPubsub(clientId, message);
		return new RedirectView("/");
	}
	
}
