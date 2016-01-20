package com.itsmoarigato.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for sending the user to the login view.
 *
 * @author Rob Winch
 *
 */
@Controller
public class IndexController {
	@RequestMapping("/")
	public String index() {
		return "main";
	}
	
	@RequestMapping("/my")
	public String my() {
		return "my";
	}

	//FIXME UserIdをRequestparamから取得する
	@RequestMapping("/create")
	public String create() {
		return "create";
	}

	@RequestMapping("/update/{id}")
	public String update(@PathVariable("id")String id,Model model) {
		model.addAttribute("arigatoId", id);
		return "update";
	}
	
	@RequestMapping("/friend/{email}")
	public String friend(@PathVariable("email")String email,Model model){
		model.addAttribute("friendEmail",email);
		return "friend";
	}
}
