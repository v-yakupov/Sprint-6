package ru.sber.sbermvc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@SpringBootApplication
@ServletComponentScan
class SbermvcApplication

fun main(args: Array<String>) {
    runApplication<SbermvcApplication>(*args)
}