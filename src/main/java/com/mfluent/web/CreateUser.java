package com.mfluent.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.mfluent.data.UserDAO;
import com.mfluent.data.model.User;

@Controller
public class CreateUser {
	
	UserDAO userDAO;
	
//	@RequestMapping(value="createUser",method=RequestMethod.GET)
//	public ModelAndView getSomething()
//	{
//		// Can only
//		return new ModelAndView("index");
//	}
	
    /**
     * POST entry point.
     * @param request. Must contains information from the layers and services to be extracted
     * @param response.
     * @return 
     * @throws IOException 
     */
	@RequestMapping(value="createUser",produces="application/json",method=RequestMethod.POST)
    public ModelAndView createUser(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String data = null;
        
		BufferedReader postData = request.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String cur;
        while ((cur = postData.readLine()) != null) {
            stringBuilder.append(cur).append("\n");
        }
        
        if(stringBuilder.length() > 0) {
        	data = stringBuilder.toString();
        }
		
        if (StringUtils.isEmpty(data)) {
        	PrintWriter out = response.getWriter();
        	out.println("ERROR: no data received: ");
        	return null;
        }

        JSONObject json = (JSONObject) JSONSerializer.toJSON( data );  
        String username = json.getString("un");
        User user = userDAO.getUser(username);
        
        if (user == null) {
        	String firstName = json.optString("fn");
        	String lastName = json.optString("ln");
        	String email = json.optString("em");
        	String password = json.optString("pw");
        	String roles = json.optString("rl");
        	
        	userDAO.createUser(username, firstName, lastName, email, password, roles);
        } else {
        	PrintWriter out = response.getWriter();
        	out.println("ERROR: user already exists: " + user);
        }
        
        return null;
    }
	
	@Autowired
	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
    	
}
