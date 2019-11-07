package lib_cliente.fr.umlv;

import java.time.LocalDateTime;

public class Log {
	private final LocalDateTime timestamp;
	private final String message;
	
	public Log(LocalDateTime date, String mess) {
		this.timestamp = date;
		this.message = mess;
	}
	
	public LocalDateTime getDate() {
		return timestamp;
	}
	
	public String getMessage() {
		return message;
	}
}
