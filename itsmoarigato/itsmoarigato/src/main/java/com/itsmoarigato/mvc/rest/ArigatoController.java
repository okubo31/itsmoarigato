package com.itsmoarigato.mvc.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.itsmoarigato.Image;
import com.itsmoarigato.Message;
import com.itsmoarigato.User;
import com.itsmoarigato.model.ArigatoManager;
import com.itsmoarigato.model.ImageManager;
import com.itsmoarigato.model.Pagination;
import com.itsmoarigato.model.exception.NotFoundException;

@RestController
public class ArigatoController {
	
	@Autowired
	ArigatoManager arigatoManager;
	
	@Autowired
	ImageManager imageManager;
	
    private String me(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
    
	@RequestMapping(value="/rest/arigato",method=RequestMethod.GET)
    @ResponseBody
    List<Message> list(@RequestParam(value="type", required=false, defaultValue="around") String type) {
    	//TODO ページネーションどうしようね。
    	List<Message> messages;
    	if(type.equals(GetType.mine.name())){
    		messages = arigatoManager.getMineMessages(me(), new Pagination());
    	}else if(type.equals(GetType.wrote.name())){
    		messages = arigatoManager.getWrittenMessages(me(), me(), new Pagination());
    	}else{
    		messages = arigatoManager.getAroundMessages(me(), new Pagination());
    	}
    	return messages;
    }

    @RequestMapping(value="/rest/arigato/{id}",method=RequestMethod.GET)
    @ResponseBody
    Message detail(@PathVariable("id")String id) {
    	Message message = arigatoManager.getMessage(me(),toInt(id));
    	return message;
    }
    
    @RequestMapping(value="/rest/arigato",method=RequestMethod.POST)
    @ResponseBody
    Json add(@Valid ArigatoCommand arigato) {
    	//friend以外には送れないようにしないとね
    	int arigatoId = this.arigatoManager.add(me(),toMessage(arigato,me()));
    	return new Json("{\"success\":true,\"arigatoId\":" + arigatoId + "}");
    }

    @RequestMapping(value="/rest/arigato/{arigatoId}/image",method=RequestMethod.POST)
    @ResponseBody
    Json add(@Valid FileUploadCommand uploaded) throws IOException {
    	Message message = this.arigatoManager.getMessage(me(),uploaded.arigatoId);
    	//TODO ファイルが画像かどうかのverify
    	//TODO ファイルサイズのverify
    	
    	int imageId = imageManager.add(uploaded.file.getInputStream(),me());
    	arigatoManager.addImage(message.getHistoryId(),imageId);

    	return new Json("{\"success\":true}");
    }

	@RequestMapping(value="/rest/arigato/{arigatoId}/image/{id}",method=RequestMethod.GET)
	ByteArrayResource image(@PathVariable("arigatoId")String arigatoId,@PathVariable("id")String id,HttpServletResponse res) {
		Message message = this.arigatoManager.getMessage(me(),toInt(arigatoId));
		for(Image i:message.getImages()){
			if(i.getId() != toInt(id)){
				continue;
			}
			return new ByteArrayResource(i.getContents());
		}
		throw new NotFoundException();
	}

	private Message toMessage(ArigatoCommand arigato,String fromUserId) {
    	Message message = new Message(toUser(fromUserId),toUser(arigato.toUserId),arigato.subject,arigato.message,new ArrayList<Image>());
		return message;
	}

	private User toUser(String userId) {
		User user = new User(userId,null,null,false,null);
		return user;
	}

	@RequestMapping(value="/rest/arigato/{id}",method=RequestMethod.POST)
    @ResponseBody
    Json update(@Valid ArigatoCommand arigato) {
		//for exist check if when no exist then throw NotFoundException  
		this.arigatoManager.getMessage(me(),toInt(arigato.getId()));
    	
		this.arigatoManager.update(me(),toInt(arigato.getId()), arigato.getSubject(), arigato.getMessage());
    	return new Json("{\"success\":true}");
    }
    
	@RequestMapping(value="/rest/arigato/{id}",method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
    Json delete(@PathVariable("id")String id) {
    	this.arigatoManager.delete(me(),toInt(id));
    	return new Json("{\"success\":true}");
    }
    
    private static int toInt(String s){
    	if(isEmpty(s)){
    		return 0;
    	}
		try{
			return Integer.parseInt(s);
		}catch(NumberFormatException e){
			throw new NotFoundException(e);
		}
	}

	private static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	class Json {

	    private final String value;

	    public Json(String value) {
	        this.value = value;
	    }

	    @JsonValue
	    @JsonRawValue
	    public String value() {
	        return value;
	    }
	}
}
