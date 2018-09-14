package hello;

public class CustomMessage {

	private String clienteId;
	private String message;

	public CustomMessage() {

	}

	public CustomMessage(String clienteId, String message) {
		this.clienteId = clienteId;
		this.message = message;
	}

	public String getClienteId() {
		return clienteId;
	}

	public void setClienteId(String clienteId) {
		this.clienteId = clienteId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}
	
}
