package com.onsae.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class OnsaeApiApplication

fun main(args: Array<String>) {
    runApplication<OnsaeApiApplication>(*args)
}