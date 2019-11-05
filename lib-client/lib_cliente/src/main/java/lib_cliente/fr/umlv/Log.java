package lib_cliente.fr.umlv;

import java.time.LocalDateTime;

public class Log {
	private final LocalDateTime date;
	private final String message;
	
	public Log(LocalDateTime date, String mess) {
		this.date = date;
		this.message = mess;
	}
	
	public LocalDateTime getDate() {
		return date;
	}
	
	public String getMessage() {
		return message;
	}
}
