package com.integration.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.demo.dao.UserDAO;
import com.integration.demo.dto.UserDTO;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
        String userId = request.getParameter("userId");
        String jsonResponse = "";
        
        // Simulated response for user details
        try {
			UserDTO userDTO = UserDAO.getUserByUserId(userId);
			ObjectMapper objectMapper = new ObjectMapper();
	        jsonResponse = objectMapper.writeValueAsString(userDTO);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        PrintWriter out = response.getWriter();
        out.println(jsonResponse);
    }
	
	  @Override
	 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  response.setContentType("application/json");
		    
		    // Read JSON data from request body
		    BufferedReader reader = request.getReader();
		    StringBuilder json = new StringBuilder();
		    String line;
		    while ((line = reader.readLine()) != null) {
		        json.append(line);
		    }
		    
		    ObjectMapper objectMapper = new ObjectMapper();
		    
		    // Convert JSON to UserDTO using Gson or Jackson
		    
		    UserDTO user = objectMapper.readValue(json.toString(), UserDTO.class);
		    UserDAO.addNewUser(user);
		    // Send response
		    PrintWriter out = response.getWriter();
		    out.println("{ \"message\": \"User received\", \"userId\": \"" + user.getUserId() + "\" }");
	 }
}
