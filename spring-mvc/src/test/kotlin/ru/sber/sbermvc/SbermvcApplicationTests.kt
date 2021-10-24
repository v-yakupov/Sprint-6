package ru.sber.sbermvc

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import ru.sber.sbermvc.filter.AuthFilter
import ru.sber.sbermvc.filter.LogFilter
import ru.sber.sbermvc.service.Record
import javax.servlet.http.Cookie

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SbermvcApplicationTests {
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .addFilters<DefaultMockMvcBuilder>(LogFilter(), AuthFilter())
            .build()

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/app/add")
                .cookie(Cookie("auth", "0"))
                .param("name", "Spurdo Spärde")
                .param("address", "Benin")
        )
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/app/add")
                .cookie(Cookie("auth", "0"))
                .param("name", "Jean Pierre Karasique")
                .param("address", "Paris")
        )
    }

    @Test
    fun `should redirect`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/list")
                .cookie(Cookie("auth", (System.currentTimeMillis() + 99999).toString())))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `should list all records`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/list")
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().string(containsString("Spurdo Spärde")))
            .andExpect(content().string(containsString("Jean Pierre Karasique")))
            .andExpect(view().name("all"))
    }

    @Test
    fun `should list one record`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/0/view")
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().string(containsString("Spurdo Spärde")))
            .andExpect(content().string(containsString("Benin")))
            .andExpect(view().name("record"))
    }

    @Test
    fun `should find one record`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/list")
                .cookie(Cookie("auth", "0"))
                .queryParam("id", "0"))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().string(containsString("Spurdo Spärde")))
            .andExpect(content().string(containsString("Benin")))
            .andExpect(content().string(not(containsString("Jean Pierre Karasique"))))
            .andExpect(content().string(not(containsString("Paris"))))
            .andExpect(view().name("all"))
    }

    @Test
    fun `should find two records`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/list")
                .cookie(Cookie("auth", "0"))
                .queryParam("id", "0")
                .queryParam("address", "Paris"))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().string(containsString("Spurdo Spärde")))
            .andExpect(content().string(containsString("Benin")))
            .andExpect(content().string(containsString("Jean Pierre Karasique")))
            .andExpect(content().string(containsString("Paris")))
            .andExpect(view().name("all"))
    }

    @Test
    fun `should add record`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/app/add")
                .cookie(Cookie("auth", "0"))
                .param("name", "Pepe")
                .param("address", "Washington"))
            .andExpect(status().is3xxRedirection)

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/list")
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().string(containsString("Pepe")))
            .andExpect(content().string(containsString("Washington")))
            .andExpect(view().name("all"))
    }

    @Test
    fun `should remove record`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/1/delete")
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is3xxRedirection)

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/list")
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().string(not(containsString("Jean Pierre Karasique"))))
            .andExpect(content().string(not(containsString("Paris"))))
            .andExpect(view().name("all"))
    }

    @Test
    fun `should edit record`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/1/edit")
                .cookie(Cookie("auth", "0")))
            .andExpect(view().name("edit"))
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/app/1/edit")
                .cookie(Cookie("auth", "0"))
                .param("name", "Pepe")
                .param("address", "Washington"))
            .andExpect(status().is3xxRedirection)

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/app/list")
                .cookie(Cookie("auth", "0")))
            .andExpect(content().string(not(containsString("Jean Pierre Karasique"))))
            .andExpect(content().string(not(containsString("Paris"))))
            .andExpect(content().string(containsString("Pepe")))
            .andExpect(content().string(containsString("Washington")))
            .andExpect(view().name("all"))
    }

    @Test
    fun `should list one record in JSON`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/0/view")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("Spurdo Spärde"))
            .andExpect(jsonPath("$.address").value("Benin"))
    }

    @Test
    fun `should list all records in JSON`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/list")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder("Spurdo Spärde", "Jean Pierre Karasique")))
            .andExpect(jsonPath("$[*].address", containsInAnyOrder("Benin", "Paris")))
    }

    @Test
    fun `should delete record in JSON`() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/0/delete")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/list")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[*].name", not(containsInAnyOrder("Spurdo Spärde"))))
            .andExpect(jsonPath("$[*].address", not(containsInAnyOrder("Benin"))))
    }

    @Test
    fun `should update record in JSON`() {
        val user = ObjectMapper().writeValueAsString(Record("Pepe", "Washington"))

        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/0/edit")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(Cookie("auth", "0"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(user))
            .andExpect(status().is2xxSuccessful)

        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/0/view")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(Cookie("auth", "0")))
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name", not(`is`("Spurdo Spärde"))))
            .andExpect(jsonPath("$.address", not(`is`("Benin"))))
            .andExpect(jsonPath("$.name", `is`("Pepe")))
            .andExpect(jsonPath("$.address", `is`("Washington")))
    }
}
