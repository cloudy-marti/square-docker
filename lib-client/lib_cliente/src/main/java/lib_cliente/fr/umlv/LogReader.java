package lib_cliente.fr.umlv;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Flow.Subscriber;
import java.util.stream.Stream;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.bind.JsonbBuilder;

public class LogReader {
	private ArrayList<Log> array;
	private Long read = 0L;
	private final int id;
	

	public LogReader() {
		id = setId();
		this.array = new ArrayList<Log>();
	}

	private int setId() {
		int res = -1;
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
			res = Integer.parseInt(str);
		}
		return res;
 	}

	public void parse(String str, LocalDateTime time) {
		array.add(new Log(time, str));
	}

	public void readStream(Stream<String> stream) {
		LocalDateTime time = LocalDateTime.now();
		stream.skip(read).forEach(e -> this.parse(e, time));
		sendData();

	}

	private void sendData() {
		String obj = JsonbBuilder.create().toJson(this.array);
        InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			throw new UndeclaredThrowableException(e1);
		}
		HttpRequest requetePost = HttpRequest.newBuilder()
				.uri(URI.create("http://" + inetAddress.getHostAddress() + ":8080/logs"))
				.setHeader("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(obj))
				.build();

		HttpClient req = HttpClient.newHttpClient();
		try {
			HttpResponse<String> response = req.send(requetePost, BodyHandlers.ofString());
			System.out.println("Status  : " + response.statusCode());
			System.out.println("Headers : " + response.headers());
			System.out.println("Body    : " + response.body());

		} catch (IOException | InterruptedException e) {
			throw new UndeclaredThrowableException(e);
		}
	}
}