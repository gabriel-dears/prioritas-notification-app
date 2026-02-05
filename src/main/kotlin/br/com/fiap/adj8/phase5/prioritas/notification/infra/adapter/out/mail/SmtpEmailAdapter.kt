package br.com.fiap.adj8.phase5.prioritas.notification.infra.adapter.out.mail

import br.com.fiap.adj8.phase5.prioritas.common.event.TriageNotificationEvent
import br.com.fiap.adj8.phase5.prioritas.notification.application.port.out.mail.SendTriageEmailPort
import br.com.fiap.adj8.phase5.prioritas.notification.infra.adapter.out.mail.template.ThymeleafTemplateService
import jakarta.mail.MessagingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
class SmtpEmailAdapter(
    private val mailSender: JavaMailSender,
    private val templateService: ThymeleafTemplateService,

    @param:Value("\${prioritas.mail.from}")
    private val mailFrom: String,

    @param:Value("\${prioritas.mail.target-list}")
    private val targetEmails: String
) : SendTriageEmailPort {

    private val logger = LoggerFactory.getLogger(SmtpEmailAdapter::class.java)

    @Retryable(
        retryFor = [MailException::class, MessagingException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000)
    )
    override fun sendCriticalAlert(event: TriageNotificationEvent) {
        logger.info("📧 Preparando envio de email para Triagem ID: ${event.triageId}")

        val htmlContent = templateService.generateHtml(event)
        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "utf-8")

        helper.setFrom(mailFrom)
        helper.setTo(targetEmails.split(",").toTypedArray())
        helper.setSubject("🚨 ALERTA MÉDICO: Risco ${event.riskLevel}")
        helper.setText(htmlContent, true)

        try {
            mailSender.send(mimeMessage)
            logger.info("✅ Email enviado com sucesso.")
        } catch (e: Exception) {
            logger.error("❌ Falha ao enviar email", e)
            throw e
        }
    }
}