package fr.umlv.square.serializer;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import fr.umlv.square.models.Application;

public class ApplicationSerializer implements JsonbSerializer<Application> {
	@Override
	public void serialize(Application obj, JsonGenerator generator, SerializationContext ctx) {
        generator.writeStartObject();
        generator.write("id", obj.getId());
        generator.write("app", obj.getApp());
        generator.write("port", obj.getPort());
        generator.write("service-port", obj.getServicePort());
        generator.write("docker-instance", obj.getDockerInst());
        generator.writeEnd();			
	}
}