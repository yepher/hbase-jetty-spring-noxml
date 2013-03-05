package com.mfluent.config;

import javax.servlet.*;

import org.apache.jasper.servlet.*;
import org.springframework.web.*;
import org.springframework.web.context.*;
import org.springframework.web.context.support.*;
import org.springframework.web.servlet.*;

/**
 * Replacement for web.xml
 *
 */
public class WebAppInitializer implements WebApplicationInitializer
{
	private static final String JSP_SERVLET_NAME = "jsp";
	private static final String DISPATCHER_SERVLET_NAME = "dispatcher";
	
	@Override
	public void onStartup(ServletContext aServletContext) throws ServletException
	{		
		registerListener(aServletContext);
		registerDispatcherServlet(aServletContext);
		registerJspServlet(aServletContext);
	}
	
	private void registerListener(ServletContext aContext)
	{
		AnnotationConfigWebApplicationContext root = createContext(ApplicationModule.class);
		aContext.addListener(new ContextLoaderListener(root));
	}
	
	private void registerDispatcherServlet(ServletContext aContext)
	{
		AnnotationConfigWebApplicationContext ctx = createContext(WebModule.class);
		ServletRegistration.Dynamic dispatcher = 
			aContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(ctx));
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/");
	}
	
	private void registerJspServlet(ServletContext aContext) {
		ServletRegistration.Dynamic dispatcher = 
			aContext.addServlet(JSP_SERVLET_NAME, new JspServlet());
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("*.jsp");
	}

	private AnnotationConfigWebApplicationContext createContext(final Class<?>... aModules)
	{
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(aModules);
		return ctx;
	}
}
