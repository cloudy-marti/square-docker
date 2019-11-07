package lib_cliente.fr.umlv;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;
import javax.json.bind.JsonbBuilder;

public class LogReader {
	private ArrayList<Log> array;
	private Long read = 0L;
	private final String id;
	private final String localIP;
	private final int portSquare;
	

	public LogReader() {
		localIP = "192.168.1.67";
		portSquare = 8080;
		id = setId();
		this.array = new ArrayList<Log>();
	}

	private String setId() {
		String res = "";
		ProcessBuilder pb = new ProcessBuilder("hostname");
		Process proc;
		try {
			proc = pb.start();
		} catch (IOException e) {
			throw new UndeclaredThrowableException(e);
		}
		InputStream inputStr = proc.getInputStream();
		try (java.util.Scanner scan = new java.util.Scanner(inputStr)) {
			String str = scan.useDelimiter("\\A").hasNext() ? scan.next() : "";
			res = str;
		}
		return res.replaceAll("[\r\n]+", "");
 	}

	public void parse(String str, LocalDateTime time) {
		array.add(new Log(time, str));
	}

	public void readStream(Stream<String> stream) {
		LocalDateTime time = LocalDateTime.now();
		stream.skip(read).forEach(e -> this.parse(e, time));
		sendData();
		read += Long.valueOf(array.size());
		array.clear();

	}

	private void sendData() {
		if(array.size()==0)
			return;
		String obj = JsonbBuilder.create().toJson(this.array);
		URI uriC = URI.create("http://"+localIP+":"+portSquare+"/logs?idC=" + id);
		HttpRequest requetePost = HttpRequest.newBuilder()
				.uri(uriC)
				.setHeader("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(obj))
				.build();

		HttpClient req = HttpClient.newHttpClient();
		try {
			req.send(requetePost, BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new UndeclaredThrowableException(e);
		}
	}
}
