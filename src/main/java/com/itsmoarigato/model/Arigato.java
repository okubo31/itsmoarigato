package com.itsmoarigato.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.itsmoarigato.Image;
import com.itsmoarigato.Message;
import com.itsmoarigato.User;

@Component
public class Arigato {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public void add(Message message){
		int arigatoId = saveArigato(message);
		saveHistory(arigatoId,message.getSubject(),message.getContents());
		//TODO 画像の扱い
	}

	private int saveArigato(Message message) {
		jdbcTemplate.update("insert into arigato_tbl (from_user ,to_user ,created) values (?,?,sysdate)",
				new Object[]{message.getFromUser().getEmail(),message.getToUser().getEmail()});
		Integer arigatoId = jdbcTemplate.queryForObject("select max(id) from arigato_tbl where from_user = ? and to_user = ?", 
				Integer.class,
				new Object[]{message.getFromUser().getEmail(),message.getToUser().getEmail()});

		return arigatoId;
	}

	private void saveHistory(int arigatoId, String subject, String message) {
		jdbcTemplate.update("insert into arigato_history_tbl (arigato_id,subject ,message, created) values (?,?,?,sysdate)", 
				new Object[]{arigatoId,subject,message});
	}

	private static final String select_from_arigato = 
			"select "
			+ "a.id,"
			+ "from_user ,"
			+ "to_user ,"
			+ "subject ,"
			+ "message ,"
			+ "h.created "
			+ "from arigato_tbl a "
			+ "inner join arigato_history_tbl h "
			+ "on (a.id = h.arigato_id "
			+ "and h.id = select max(id) from arigato_history_tbl where arigato_id = h.arigato_id　group by arigato_id"
			+ ") ";

	private static class ArigatoRowMapper implements RowMapper<Message>{
		@Override
		public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
			int id = rs.getInt("id"); 
			User fromUser = toUser(rs.getString("from_user")); 
			User toUser = toUser(rs.getString("to_user"));
			String subject = rs.getString("subject"); 
			String contents = rs.getString("message"); 
			Date created = rs.getDate("created");
			List<Image> images = null;
			return new Message(id,fromUser, toUser, subject, contents, created, images);
		}
	}
	
	public List<Message> getMineMessages(String me,Pagination page) {
		boolean afterAtPoint = page.hasAtPoint();
		
		StringBuilder sql = new StringBuilder();
		sql.append(select_from_arigato);
		sql.append("where to_user = ? ");
		if(afterAtPoint){
			sql.append("and h.created < ? ");
		}
		sql.append("order by h.created　desc, h.id desc ");
		sql.append("limit ? offset ? ");
		Object[] params;
		if(afterAtPoint){
			params = new Object[]{me,page.getAtPoint(),page.getLimit(),page.getOffset()};
		}else{
			params = new Object[]{me,page.getLimit(),page.getOffset()};
		}

		return jdbcTemplate.query(sql.toString(), 
				new ArigatoRowMapper(),
				params);
	}

	public List<Message> getArroundMessages(String me,Pagination page) {
		boolean afterAtPoint = page.hasAtPoint();

		StringBuilder sql = new StringBuilder();
		sql.append(select_from_arigato);
		sql.append("where to_user in (");
		sql.append("select ");
		sql.append("friend ");
		sql.append("from friend_tbl ");
		sql.append("where me = ?");
		sql.append(") ");
		if(afterAtPoint){
			sql.append("and h.created < ? ");
		}
		sql.append("order by h.created　desc, h.id desc ");
		sql.append("limit ? offset ? ");

		Object[] params;
		if(afterAtPoint){
			params = new Object[]{me,page.getAtPoint(),page.getLimit(),page.getOffset()};
		}else{
			params = new Object[]{me,page.getLimit(),page.getOffset()};
		}

		return jdbcTemplate.query(sql.toString() , 
				new ArigatoRowMapper(),
				params);
	}

	private static User toUser(String email) {
		return new User(email, "");//TODO nameの取得
	}

	public Message getMessage(int messageId) {
		return jdbcTemplate.queryForObject(
				select_from_arigato + 
				"where a.id = ?", 
				new ArigatoRowMapper(),
				new Object[]{messageId});
	}

	public void update(int arigatoId,String subject,String message) {
		saveHistory(arigatoId, subject, message);
	}
}
