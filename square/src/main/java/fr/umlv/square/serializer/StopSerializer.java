package fr.umlv.square.serializer;

import fr.umlv.square.models.Stop;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class StopSerializer implements JsonbSerializer<Stop> {
    @Override
    public void serialize(Stop obj, JsonGenerator generator, SerializationContext ctx) {
        generator.writeStartObject();
        generator.write("id", obj.getApp().getId());
        generator.write("app", obj.getApp().getApp());
        generator.write("port", obj.getApp().getPort());
        generator.write("service-port", obj.getApp().getServicePort());
        generator.write("docker-instance", obj.getApp().getDockerInst());
        generator.write("elapsed_time", obj.getApp().getElapsedTime());
        generator.writeEnd();
    }
}