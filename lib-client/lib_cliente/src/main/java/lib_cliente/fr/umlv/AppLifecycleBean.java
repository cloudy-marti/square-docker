package lib_cliente.fr.umlv;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class AppLifecycleBean {
	void onStart(@Observes StartupEvent ev) {
		LogReader lr = new LogReader();
		String fileName = "workspace/log.log";
		
		
		Thread mainT = new Thread(() -> {
			while (true) {
				try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
					lr.readStream(stream);
					Thread.sleep(15_000);
				} catch (IOException | InterruptedException e) {
					throw new UndeclaredThrowableException(e);
				}
			}
		});
		mainT.start();
	}
}