package ru.sber.services

import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_SINGLETON)
class SingletonService

@Component
@Scope(SCOPE_PROTOTYPE)
class PrototypeService
