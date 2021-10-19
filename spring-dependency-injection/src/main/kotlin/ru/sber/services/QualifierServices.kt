package ru.sber.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

interface QualifierInterface

@Component
class FirstQualifierServiceImpl : QualifierInterface {
    override fun toString(): String {
        return "FirstQualifierServiceImpl"
    }
}

@Component
class SecondQualifierServiceImpl : QualifierInterface {
    override fun toString(): String {
        return "SecondQualifierServiceImpl"
    }
}

@Component
class QualifierBeanInjectionService {
    @Autowired
    @Qualifier("secondQualifierServiceImpl")
    private lateinit var qualifierServices: Set<QualifierInterface>

    override fun toString(): String {
        return "QualifierBeanInjectionService(qualifierServices=$qualifierServices)"
    }
}