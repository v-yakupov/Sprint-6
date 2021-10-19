package ru.sber.xml

import org.junit.jupiter.api.Test
import org.springframework.context.support.ClassPathXmlApplicationContext
import kotlin.test.assertEquals

class ServicesTest {

    @Test
    fun `should create context and get bean successfully`() {
        // given
        val context = ClassPathXmlApplicationContext("applicationContext.xml")

        // when
        val firstService = context.getBean("firstService")

        // then
        assertEquals("I am firstService", firstService.toString())
    }

    @Test
    fun `getBean should return firstService successfully`() {
        // given
        val context = ClassPathXmlApplicationContext("applicationContext.xml")

        // when
        val firstService = context.getBean("firstService")

        // then
        assertEquals("I am firstService", firstService.toString())
    }

    @Test
    fun `getBean should return secondService successfully`() {
        // given
        val context = ClassPathXmlApplicationContext("applicationContext.xml")

        // when
        val secondService = context.getBean("secondService")

        // then
        assertEquals("I am secondService", secondService.toString())
    }

    @Test
    fun `getBean should return thirdService successfully`() {
        // given
        val context = ClassPathXmlApplicationContext("applicationContext.xml")

        // when
        val thirdService = context.getBean("thirdService")

        // then
        assertEquals("I am thirdService", thirdService.toString())
    }
}