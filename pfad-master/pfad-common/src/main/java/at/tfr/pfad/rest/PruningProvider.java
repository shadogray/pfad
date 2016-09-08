package at.tfr.pfad.rest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.tfr.pfad.model.Payment;

@Provider
public class PruningProvider implements WriterInterceptor {

	private ThreadLocal<AtomicInteger> level = ThreadLocal.withInitial(() -> new AtomicInteger());
	private int MAX = 2;
	@Context
	private Providers providers;
	
	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
		if (level.get().getAndIncrement() > MAX) {
			return;
		}
		try {
			context.proceed();
		} finally {
			level.get().decrementAndGet();
		}
	}
}
