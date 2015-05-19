package com.itsmoarigato;

import java.util.List;

public class Arigato {
	private int id; 
	private int historyId; 
	private User fromUser; 
	private User toUser; 
	private String subject; 
	private String message; 
	private List<Image> images;
	public Arigato(int id, int historyId, User fromUser, User toUser,
			String subject, String message, List<Image> images) {
		super();
		this.id = id;
		this.historyId = historyId;
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.subject = subject;
		this.message = message;
		this.images = images;
	}
	public int getId() {
		return id;
	}
	public int getHistoryId() {
		return historyId;
	}
	public User getFromUser() {
		return fromUser;
	}
	public User getToUser() {
		return toUser;
	}
	public String getSubject() {
		return subject;
	}
	public String getMessage() {
		return message;
	}
	public List<Image> getImages() {
		return images;
	}
}
