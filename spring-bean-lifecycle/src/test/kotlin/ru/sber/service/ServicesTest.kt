package ru.sber.service

import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ru.sber.config.ServicesConfig
import ru.sber.services.BeanFactoryPostProcessorBean
import ru.sber.services.CallbackBean
import ru.sber.services.CombinedBean
import kotlin.test.assertEquals

class ServicesTest {

    @Test
    fun `getBean should return bean and call afterPropertiesSet`() {
        // given
        val context = AnnotationConfigApplicationContext(ServicesConfig::class.java)

        // when
        val callbackBean = context.getBean("callbackBean") as CallbackBean

        // then
        assertEquals("Hello! My name is callbackBean!", callbackBean.greeting)
    }

    @Test
    fun `getBean should return bean and call destroy`() {
        // given
        val context = AnnotationConfigApplicationContext(ServicesConfig::class.java)

        // when
        val callbackBean = context.getBean("callbackBean") as CallbackBean
        context.beanFactory.destroyBean(callbackBean)

        // then
        assertEquals("Sorry, but I really have to go.", callbackBean.greeting)
    }

    @Test
    fun `getBean should return bean and call the methods in the correct order`() {
        // given
        val context = AnnotationConfigApplicationContext(ServicesConfig::class.java)

        // when
        val combinedBean = context.getBean("combinedBean") as CombinedBean

        // then
        assertEquals(
            "postProcessBeforeInitialization() is called",
            combinedBean.postProcessBeforeInitializationOrderMessage
        )
        assertEquals("postConstruct() is called", combinedBean.postConstructOrderMessage)
        assertEquals("afterPropertiesSet() is called", combinedBean.afterPropertiesSetOrderMessage)
        assertEquals("customInit() is called", combinedBean.customInitOrderMessage)
        assertEquals(
            "postProcessAfterInitialization() is called",
            combinedBean.postProcessAfterInitializationOrderMessage
        )
    }

    @Test
    fun `getBean should return bean and correct preConfiguredProperty`() {
        // given
        val context = AnnotationConfigApplicationContext(ServicesConfig::class.java)

        // when
        val beanFactoryPostProcessorBean =
            context.getBean("beanFactoryPostProcessorBean") as BeanFactoryPostProcessorBean

        // then
        assertEquals("Done!", beanFactoryPostProcessorBean.preConfiguredProperty)
    }
}