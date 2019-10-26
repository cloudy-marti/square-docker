package fr.umlv.square.controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JSONConfigurator implements ContextResolver<Jsonb> {

    @Override
    public Jsonb getContext(Class<?> type) {
        JsonbConfig config = new JsonbConfig().
                withPropertyVisibilityStrategy(new PrivateVisibilityStrategy());
        return JsonbBuilder.newBuilder().
                withConfig(config).
                build();
    }
    

    private class PrivateVisibilityStrategy implements PropertyVisibilityStrategy {

		@Override
		public boolean isVisible(Field field) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isVisible(Method method) {
			// TODO Auto-generated method stub
			return true;
		}
    
    }
}