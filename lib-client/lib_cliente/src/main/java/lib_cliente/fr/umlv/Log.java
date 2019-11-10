package lib_cliente.fr.umlv;

import java.time.OffsetDateTime;

public class Log {
	private final OffsetDateTime timestamp;
	private final String message;
	
	public Log(OffsetDateTime date, String mess) {
		this.timestamp = date;
		this.message = mess;
	}
	
	public OffsetDateTime getDate() {
		return timestamp;
	}
	
	public String getMessage() {
		return message;
	}
}
