package br.com.fiap.adj8.phase5.prioritas.notification.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule // <--- IMPORTANTE: Importe isso
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {
    @Bean
    fun messageConverter(): MessageConverter {
        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.registerKotlinModule()
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return Jackson2JsonMessageConverter(mapper)
    }
}