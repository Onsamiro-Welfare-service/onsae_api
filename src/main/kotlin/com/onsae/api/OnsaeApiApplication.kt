package com.onsae.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OnsaeApiApplication

fun main(args: Array<String>) {
    runApplication<OnsaeApiApplication>(*args)
}