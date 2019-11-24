package fr.umlv.square.serializer;

import fr.umlv.square.database.entities.Application;
import fr.umlv.square.models.Stop;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import java.util.Objects;

public class StopSerializer implements JsonbSerializer<Stop> {
	/**
	 * This method serialize a Stop object
	 */
    @Override
    public void serialize(Stop obj, JsonGenerator generator, SerializationContext ctx) {
        Objects.requireNonNull(obj);
        Objects.requireNonNull(generator);
        Objects.requireNonNull(ctx);

        Application tmp = obj.getApp();

        generator.writeStartObject();
        generator.write("id", tmp.getId());
        generator.write("app", tmp.getApp());
        generator.write("port", tmp.getPort());
        generator.write("service-port", tmp.getServicePort());
        generator.write("docker-instance", tmp.getDockerInst());
        generator.write("elapsed_time", tmp.getElapsedTime());
        generator.writeEnd();
    }
}