package br.com.fiap.adj8.phase5.prioritas.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class NotificationAppApplication

fun main(args: Array<String>) {
    runApplication<NotificationAppApplication>(*args)
}