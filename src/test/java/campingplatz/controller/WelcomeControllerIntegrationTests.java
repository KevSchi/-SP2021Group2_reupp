/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package campingplatz.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import campingplatz.user.User;
import campingplatz.user.UserRepository;


@SpringBootTest
@AutoConfigureMockMvc
class WelcomeControllerIntegrationTests {
	@Autowired
	MockMvc mvc;
	@Autowired
	UserAccountManagement userAccountManagement;

	@Test
	void showsWelcomeMessage() throws Exception {
		mvc.perform(get("/")) //
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Herzlich Willkommen!")));
	}

	@Test
	@WithMockUser(username = "boss", roles = "BOSS")
	void registration() throws Exception {
		LocalDate date = LocalDate.of(1990, 05, 05);
				
			mvc.perform(post("/registerEmployee")
			.param("firstName", "Dummy")
			.param("lastName", "User")
			.param("mail", "dummyuser@mail.de")
			.param("birthDate", date.toString())
			.param("street", "Unter der Brücke 3")
			.param("city", "EineStadt")
			.param("plz", "12345")
			.param("password", "a")
			.param("password_w", "a"))
			.andExpect(status().is3xxRedirection());
		
		 UserAccount userAccount = userAccountManagement.findByUsername("dummyuser@mail.de").get();
		 assert (userAccount.getFirstname()).equals("Dummy");
	}
}
