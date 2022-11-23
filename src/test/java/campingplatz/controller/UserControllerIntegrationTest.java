package campingplatz.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
	@Autowired
	MockMvc mvc;

	@Test
	void performLogIn() throws Exception {
			mvc.perform(post("/signin")
			.param("username", "a")
			.param("password", "a"))
			.andExpect(status().is3xxRedirection());

	}


}