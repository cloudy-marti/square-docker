package fr.umlv.square.serializer;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import fr.umlv.square.models.Application;
import fr.umlv.square.models.LogsApplication;

public class LogsApplicationSerializer implements JsonbSerializer<LogsApplication> {
	@Override
	public void serialize(LogsApplication obj, JsonGenerator generator, SerializationContext ctx) {
		var app = obj.getApplication();
        generator.writeStartObject();
        generator.write("id", app.getId());
        generator.write("app", app.getApp());
        generator.write("port", app.getPort());
        generator.write("service-port", app.getServicePort());
        generator.write("docker-instance", app.getDockerInst());
        generator.write("message", obj.getMessage());
        generator.write("timestamp", obj.getTimestamp());
        generator.writeEnd();			
	}
}