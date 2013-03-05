package com.mfluent.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GetSomething {
	
	@RequestMapping(value="getSomething",method=RequestMethod.GET)
	public ModelAndView getSomething()
	{
		return new ModelAndView("index");
	}
	
//	@RequestMapping(value="getSomething",produces="application/json",method=RequestMethod.POST)
	public ModelAndView postSomething()
	{
		return new ModelAndView("index");
	}
	
	
    /**
     * POST entry point.
     * @param request. Must contains information from the layers and services to be extracted
     * @param response.
     * @return 
     * @throws IOException 
     */
	@RequestMapping(value="getSomething",produces="application/json",method=RequestMethod.POST)
    public ModelAndView handlePOSTRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String str = request.getParameter("data");

        if (str == null) {
            // there is no "data" param: we should parse raw post data
            BufferedReader postData = request.getReader();
            StringBuilder stringBuilder = new StringBuilder();
            String cur;
            while ((cur = postData.readLine()) != null) {
                stringBuilder.append(cur).append("\n");
            }
            if(stringBuilder.length() > 0) {
                str = stringBuilder.toString();
            }
        }

        Map<String, Object> model = createModelFromString(request, str,false);

        return new ModelAndView("index", "c", model);
    }

    
    private Map<String,Object> createModelFromString(HttpServletRequest request, String str, boolean val) {
    	System.out.println("POST DATA: " + str);
    	
    	
    	
//        JSONObject jsonData;
//        JSONArray jsonLayers, jsonServices;
//        String layers, services;
//        try {
//            jsonData = new JSONObject(str);
//        } catch (JSONException e) {
//            throw new RuntimeException("Cannot parse the json post data", e);
//        }
//        
//        boolean debug;
//        if (request.getParameter("debug") == null) {
//            try {
//                debug = jsonData.getBoolean("debug");
//            } catch (JSONException e) {
//                debug = false;
//            }
//        }
//        else {
//            debug = Boolean.parseBoolean(request.getParameter("debug"));
//        }
//
//        try {
//            jsonLayers = jsonData.getJSONArray("layers");
//            layers = jsonLayers.toString(1);
//        } catch (JSONException e) {
//            layers = "[]";
//        }
//        try {
//            jsonServices = jsonData.getJSONArray("services");
//            services = jsonServices.toString(1);
//        } catch (JSONException e) {
//            services = "[]";
//        }

        Map<String,Object> model = new HashMap<String,Object>();
        model.put("fake", false);
        model.put("debug", "true");
        model.put("layers", "Some Layers");
        model.put("services", "Some Services");
        return model;
    }
	
}
