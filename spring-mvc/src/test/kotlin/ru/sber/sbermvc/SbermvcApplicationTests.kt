package ru.sber.sbermvc

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.WebApplicationContext
import ru.sber.sbermvc.filter.AuthFilter
import ru.sber.sbermvc.filter.LogFilter
import ru.sber.sbermvc.service.Record
import javax.servlet.http.Cookie
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SbermvcApplicationTests {
    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @LocalServerPort
    var port: Int = 65535

    lateinit var mockMvc: MockMvc

    lateinit var restTemplate: RestTemplate

    @BeforeEach
    fun setup() {
        restTemplate  = restTemplateBuilder
            .rootUri("http://localhost:${port}/api")
            .defaultHeader("Cookie", "auth=0;") // Shortcut to
            .defaultHeader("Content-Type", "application/json")
            .build()

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
    fun `should list one record via resttemplate`() {
        val record = restTemplate.getForEntity("/0/view", Record::class.java)
        assertEquals(HttpStatus.OK, record.statusCode)
        assertEquals("Spurdo Spärde", record.body?.name)
        assertEquals("Benin", record.body?.address)
    }

    @Test
    fun `should list multiple records via resttemplate`() {
        val records = restTemplate.exchange("/list", HttpMethod.GET, null, object : ParameterizedTypeReference<Map<String, Record>>(){})
        println(records.body)
        assertEquals(records.statusCode, HttpStatus.OK)
        assertEquals("Spurdo Spärde", records.body?.get("0")?.name)
        assertEquals("Benin", records.body?.get("0")?.address)
        assertEquals("Jean Pierre Karasique", records.body?.get("1")?.name)
        assertEquals("Paris", records.body?.get("1")?.address)
    }

    @Test
    fun `should update record via resttemplate`() {
        val recordPost = restTemplate.postForEntity("/1/edit", HttpEntity<Record>(Record("Pepe", "Washington")), Record::class.java)
        assertEquals(HttpStatus.OK, recordPost.statusCode)

        val recordGet = restTemplate.getForEntity("/1/view", Record::class.java)
        assertEquals(HttpStatus.OK, recordGet.statusCode)
        assertEquals("Pepe", recordGet.body?.name)
        assertEquals("Washington", recordGet.body?.address)
    }

    @Test
    fun `should delete record via resttemplate`() {
        val recordDelete = restTemplate.getForEntity("/1/delete", Record::class.java)
        assertEquals(HttpStatus.OK, recordDelete.statusCode)

        val recordGet = restTemplate.getForEntity("/1/view", Record::class.java)
        assertEquals(HttpStatus.OK, recordGet.statusCode)
        assertEquals(null, recordGet.body?.name)
        assertEquals(null, recordGet.body?.address)
    }

    @Test
    fun `should add record via resttemplate`() {
        val recordPost = restTemplate.postForEntity("/add", HttpEntity<Record>(Record("Pepe", "Washington")), Record::class.java)
        assertEquals(HttpStatus.OK, recordPost.statusCode)

        val recordGet = restTemplate.getForEntity("/2/view", Record::class.java)
        assertEquals(HttpStatus.OK, recordGet.statusCode)
        assertEquals("Pepe", recordGet.body?.name)
        assertEquals("Washington", recordGet.body?.address)
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
