package app.todo.test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import app.db.controller.TodoController;
import app.db.main.DbMvcTestApplication;
import app.db.test.CsvDataSetLoader;

/**
 * TODOリストのコントローラのテストクラス
 * @author aoi
 *
 */
@ExtendWith(SpringExtension.class)
@DbUnitConfiguration(dataSetLoader = CsvDataSetLoader.class)
@TestExecutionListeners({
	  DependencyInjectionTestExecutionListener.class,
	  DirtiesContextTestExecutionListener.class,
	  TransactionalTestExecutionListener.class,
	  DbUnitTestExecutionListener.class
	})
@AutoConfigureMockMvc
@SpringBootTest(classes = {TodoController.class, DbMvcTestApplication.class})
@Transactional
public class TodoControllerTest {
	
	//mockMvc TomcatサーバへデプロイすることなくHttpリクエスト・レスポンスを扱うためのMockオブジェクト
	@Autowired
	private MockMvc mockMvc;
	
	@PersistenceContext
	private EntityManager em;
	
	private TodoTestHelper helper;
	
	@BeforeEach
	void setUp() {
		this.helper = new TodoTestHelper(em);
		
	}
	
	@Test
	void init処理でviewとしてtodoが渡される() throws Exception {
		this.mockMvc.perform(get("/todo/init")).andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name("todo"));
	}
	
	/**
	 * モデルへDBから取得したレコードが設定されたか検証する
	 * 今回は複雑な処理でもないので、DBの中の1レコードがモデルに渡されていれば正常に動作しているとみなした
	 * 
	 * @throws Exception
	 */
	@Test
	@DatabaseSetup(value = "/TODO/setUp/")
	void init処理で既存のタスクがモデルへ渡される() throws Exception {
		
		// mockMvcで「/todo/init」へgetリクエストを送信
		this.mockMvc.perform(get("/todo/init"))
		// モデルへDBのレコードがリストとして渡される
			.andExpect(model().attribute("todoForm", hasProperty(
					"todoList", hasItem(
							hasProperty(
									"task", is("task1")
							)
					)
			)));
	}
	
	@Test
	@DatabaseSetup(value = "/TODO/setUp/")
	@ExpectedDatabase(value = "/TODO/create/", assertionMode=DatabaseAssertionMode.NON_STRICT)
	void save処理で新規タスクがDBへ登録される() throws Exception {
		
		this.mockMvc.perform(post("/todo/save")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("newTask", "newTask"));
		
	}
	
	@Test
	@DatabaseSetup(value = "/TODO/setUp/")
	@ExpectedDatabase(value = "/TODO/update/", assertionMode=DatabaseAssertionMode.NON_STRICT)
	void update処理で既存タスクが更新される() throws Exception{
		
		// mockMvcで「todo/update」へpostリクエストを送信
		long updateTargetId = helper.getIdForTarget();
		int updateTargetIndex = 2;
		
		this.mockMvc.perform(post("/todo/update/" + updateTargetIndex + "/" + updateTargetId)
				.param("todoList[" + updateTargetIndex + "].task", "task3mod")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				);
		
	}
	
	/**
	 * 画面で選択したタスクが削除されるかどうか検証する
	 * @throws Exception
	 */
	@Test
	@DatabaseSetup(value = "/TODO/setUp/")
	@ExpectedDatabase(value = "/TODO/delete/", assertionMode=DatabaseAssertionMode.NON_STRICT)
	void delete処理で既存タスクが消去される() throws Exception {
		long deleteTargetId = helper.getIdForTarget();
		
		this.mockMvc.perform(post("/todo/delete/" + deleteTargetId)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				);
		
	}

}
