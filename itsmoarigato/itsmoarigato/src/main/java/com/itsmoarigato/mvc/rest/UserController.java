package com.itsmoarigato.mvc.rest;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.itsmoarigato.User;
import com.itsmoarigato.model.Pagination;
import com.itsmoarigato.model.UserManager;

@RestController
public class UserController {
	
	@Autowired
	UserManager userManager;

    private String me(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
    
    @RequestMapping(value="rest/user/{email}",method=RequestMethod.GET)
    User friend(@PathVariable("email")String email){
    	return userManager.getUser(me(), email);
    }
    
	@RequestMapping(value="/rest/user/{email}/friend",method=RequestMethod.GET)
	List<User> friends(@PathVariable("email")String email){
		return userManager.getFriends(me(), email, new Pagination());
	}
	
	@RequestMapping(value="/rest/user/{email}/image/{id}",method=RequestMethod.GET)
	ByteArrayResource image(@PathVariable("email")String email,@PathVariable("id")String id,HttpServletResponse res) {
		User user = userManager.getUser(me(),email);
		
		return new ByteArrayResource(user.getImage().getContents());
	}
}
