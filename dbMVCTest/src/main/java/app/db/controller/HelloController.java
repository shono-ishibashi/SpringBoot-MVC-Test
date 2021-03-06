package app.db.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import app.db.entity.User;
import app.db.form.DbForm;

/**
 * リクエストの正しさを検証する際に利用するコントローラ
 * モデルにいくつか値を設定し、ビューを返すのみのシンプルな処理を行う
 * @author aoi
 *
 */
@Controller
@RequestMapping("/hello")
public class HelloController {
	
	@RequestMapping("/init")
	private String init(Model model) {
		
		// ユーザリスト まずは手動で生成
		List<User> userList = new ArrayList<User>();
		User user = new User();
		user.setUserId(0L);
		user.setUserName("test0");
		User user2 = new User();
		user2.setUserId(1L);
		user2.setUserName("test1");
		
		userList.add(user);
		userList.add(user2);
		
		// フォームにユーザのリストを設定し、モデル追加することでモデルへ正常に追加されたか検証するための土台を整える
		DbForm form = new DbForm();
		form.setUserList(userList);
		
		model.addAttribute("message", "hello!");
		
		model.addAttribute("user", user);
		
		model.addAttribute("dbForm", form);
		
		return "hello";
	}

}
