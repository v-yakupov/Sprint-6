package ru.sber.services.processors

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import ru.sber.services.BeanFactoryPostProcessorBean
import javax.annotation.PostConstruct

@Component
class MyBeanFactoryPostProcessor : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        BeanFactoryPostProcessorBean::class.java.declaredMethods.forEach { method ->
            if (AnnotationUtils.findAnnotation(method, PostConstruct::class.java) != null) {
                beanFactory.getBeanDefinition("beanFactoryPostProcessorBean").initMethodName = method.name;
            }
        }
    }
}