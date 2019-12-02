package lib_cliente.fr.umlv;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;
import javax.json.bind.JsonbBuilder;

public class LogReader {
	private ArrayList<Log> array;
	private Long read = 0L;
	private final String id;
	private final String localIP;
	private final int portSquare;
	

	public LogReader() {
		localIP = System.getenv("SQUARE_HOST");
		portSquare = Integer.parseInt(System.getenv("SQUARE_PORT"));
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
			throw new AssertionError(e);
		}
		InputStream inputStr = proc.getInputStream();
		try (java.util.Scanner scan = new java.util.Scanner(inputStr)) {
			String str = scan.useDelimiter("\\A").hasNext() ? scan.next() : "";
			res = str;
		}
		return res.replaceAll("[\r\n]+", "");
 	}

	public void parse(String str, OffsetDateTime time) {
		array.add(new Log(time, str));
	}

	public void readStream(Stream<String> stream) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        Instant instant = Instant.parse(df.format(new Date()));
        OffsetDateTime time = OffsetDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
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
			try {
				HttpClient.newHttpClient().send(requetePost, BodyHandlers.ofString());				
			} catch (IOException | InterruptedException e) {
				return;
			}
		}
}
