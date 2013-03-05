package com.mfluent;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.annotations.*;
import org.eclipse.jetty.annotations.AnnotationParser.DiscoverableAnnotationHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.nio.*;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.util.thread.*;
import org.eclipse.jetty.webapp.*;

public class WebServer
{
    public static interface WebContext
    {
        public File getWarPath();
        public String getContextPath();
    }
    
    private Server server;
    private WebServerConfig config;
    
    public WebServer(WebServerConfig aConfig)
    {
        config = aConfig;
    }
    
    public void start() throws Exception
    {
        server = new Server();

        server.setThreadPool(createThreadPool());
        server.addConnector(createConnector());
        server.setHandler(createHandlers());        
        server.setStopAtShutdown(true);
                
        server.start();
    }
    
    public void join() throws InterruptedException
    {
        server.join();
    }
    
    public void stop() throws Exception
    {        
        server.stop();
    }
    
    private ThreadPool createThreadPool()
    {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName(config.getServerName());
        threadPool.setMinThreads(config.getMinThreads());
        threadPool.setMaxThreads(config.getMaxThreads());
        return threadPool;
    }
    
    private SelectChannelConnector createConnector()
    {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(config.getPort());
        connector.setHost(config.getHostInterface());
        return connector;
    }
    
    private HandlerCollection createHandlers()
    {       	    	
        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/");
        ctx.setBaseResource(Resource.newClassPathResource("META-INF/webapp"));        
        
		ctx.setConfigurations (new Configuration []
		{
			// This is necessary because Jetty out-of-the-box does not scan
			// the classpath of your project in Eclipse, so it doesn't find
			// your WebAppInitializer.
			new AnnotationConfiguration() 
			{
				@Override
				public void configure(WebAppContext context) throws Exception {
				       boolean metadataComplete = context.getMetaData().isMetaDataComplete();
				       context.addDecorator(new AnnotationDecorator(context));   
				      
				       
				       //Even if metadata is complete, we still need to scan for ServletContainerInitializers - if there are any
				       AnnotationParser parser = null;
				       if (!metadataComplete)
				       {
				           //If metadata isn't complete, if this is a servlet 3 webapp or isConfigDiscovered is true, we need to search for annotations
				           if (context.getServletContext().getEffectiveMajorVersion() >= 3 || context.isConfigurationDiscovered())
				           {
				               _discoverableAnnotationHandlers.add(new WebServletAnnotationHandler(context));
				               _discoverableAnnotationHandlers.add(new WebFilterAnnotationHandler(context));
				               _discoverableAnnotationHandlers.add(new WebListenerAnnotationHandler(context));
				           }
				       }
				       
				       //Regardless of metadata, if there are any ServletContainerInitializers with @HandlesTypes, then we need to scan all the
				       //classes so we can call their onStartup() methods correctly
				       createServletContainerInitializerAnnotationHandlers(context, getNonExcludedInitializers(context));
				       
				       if (!_discoverableAnnotationHandlers.isEmpty() || _classInheritanceHandler != null || !_containerInitializerAnnotationHandlers.isEmpty())
				       {           
				           parser = createAnnotationParser();
				           
				           parse(context, parser);
				           
				           for (DiscoverableAnnotationHandler h:_discoverableAnnotationHandlers)
				               context.getMetaData().addDiscoveredAnnotations(((AbstractDiscoverableAnnotationHandler)h).getAnnotationList());      
				       }

				}
				
				private void parse(final WebAppContext context, AnnotationParser parser) throws Exception
				{					
					List<Resource> resources = getResources(getClass().getClassLoader());
					
					for (Resource resource : resources)
					{
						if (resource == null)
							return;
		            
						parser.clearHandlers();
		                for (DiscoverableAnnotationHandler h:_discoverableAnnotationHandlers)
		                {
		                    if (h instanceof AbstractDiscoverableAnnotationHandler)
		                        ((AbstractDiscoverableAnnotationHandler)h).setResource(null); //
		                }
		                parser.registerHandlers(_discoverableAnnotationHandlers);
		                parser.registerHandler(_classInheritanceHandler);
		                parser.registerHandlers(_containerInitializerAnnotationHandlers);
		                
		                parser.parse(resource, 
		                             new ClassNameResolver()
		                {
		                    public boolean isExcluded (String name)
		                    {
		                        if (context.isSystemClass(name)) return true;
		                        if (context.isServerClass(name)) return false;
		                        return false;
		                    }
		
		                    public boolean shouldOverride (String name)
		                    {
		                        //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
		                        if (context.isParentLoaderPriority())
		                            return false;
		                        return true;
		                    }
		                });
		            }
				}

				private List<Resource> getResources(ClassLoader aLoader) throws IOException
				{
					if (aLoader instanceof URLClassLoader)
		            {
						List<Resource> result = new ArrayList<Resource>();
		                URL[] urls = ((URLClassLoader)aLoader).getURLs();		                
		                for (URL url : urls)
		                	result.add(Resource.newResource(url));
		
		                return result;
		            }
					return Collections.emptyList();					
				}
			}
		});
        
        List<Handler> handlers = new ArrayList<Handler>();
        
        handlers.add(ctx);
        
        HandlerList contexts = new HandlerList();
        contexts.setHandlers(handlers.toArray(new Handler[0]));
        
        RequestLogHandler log = new RequestLogHandler();
        log.setRequestLog(createRequestLog());
        
        HandlerCollection result = new HandlerCollection();
        result.setHandlers(new Handler[] {contexts, log});
        
        return result;
    }
    
    private RequestLog createRequestLog()
    {
        NCSARequestLog log = new NCSARequestLog();
        
        File logPath = new File(config.getAccessLogDirectory() + "yyyy_mm_dd.request.log");
        logPath.getParentFile().mkdirs();
                
        log.setFilename(logPath.getPath());
        log.setRetainDays(30);
        log.setExtended(false);
        log.setAppend(true);
        log.setLogTimeZone("UTC");
        log.setLogLatency(true);
        return log;
    }    
}