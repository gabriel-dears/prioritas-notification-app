package br.com.fiap.adj8.phase5.prioritas.notification.infra.adapter.out.mail

import br.com.fiap.adj8.phase5.prioritas.common.event.TriageNotificationEvent
import br.com.fiap.adj8.phase5.prioritas.notification.application.port.out.mail.SendTriageEmailPort
import jakarta.mail.MessagingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class SmtpEmailAdapter(
    private val mailSender: JavaMailSender,

    @Value("\${prioritas.mail.from}")
    private val mailFrom: String,

    @Value("\${prioritas.mail.target-list}")
    private val targetEmails: String
) : SendTriageEmailPort {

    private val logger = LoggerFactory.getLogger(SmtpEmailAdapter::class.java)

    // Mecanismo de Retry: Tenta 3 vezes com intervalo de 2 segundos se der erro de conexão
    @Retryable(
        retryFor = [MailException::class, MessagingException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 2000)
    )
    override fun sendCriticalAlert(event: TriageNotificationEvent) {
        logger.info("📧 Preparando envio de email para Triagem ID: ${event.triageId}")

        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "utf-8")

        val htmlContent = buildHtmlTemplate(event)
        val subject = "🚨 ALERTA MÉDICO: Prioritas - Paciente Risco ${event.riskLevel}"

        helper.setFrom(mailFrom)
        helper.setTo(targetEmails.split(",").toTypedArray())
        helper.setSubject(subject)
        helper.setText(htmlContent, true) // true = isHtml

        try {
            mailSender.send(mimeMessage)
            logger.info("✅ Email enviado com sucesso para: $targetEmails")
        } catch (e: Exception) {
            logger.error("❌ Falha ao enviar email (Tentativa de Retry...)", e)
            throw e
        }
    }

    // --- HTML Template Builder ---
    private fun buildHtmlTemplate(event: TriageNotificationEvent): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val formattedDate = event.assessedAt.format(formatter)

        // Define cor baseada no risco recebido
        val colorHex = when (event.riskColor.uppercase()) {
            "RED" -> "#dc3545" // Vermelho Bootstrap
            "ORANGE" -> "#fd7e14"
            "YELLOW" -> "#ffc107"
            else -> "#0d6efd" // Azul padrão
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
                .header { background-color: $colorHex; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; }
                .alert-box { background-color: #fff3cd; border: 1px solid #ffeeba; color: #856404; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                .data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                .data-table th, .data-table td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
                .footer { background-color: #f8f9fa; padding: 10px; text-align: center; font-size: 12px; color: #666; }
                .highlight { font-weight: bold; color: $colorHex; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>⚠️ ALERTA DE TRIAGEM</h1>
                    <h2>Risco: ${event.riskLevel}</h2>
                </div>
                <div class="content">
                    <div class="alert-box">
                        <strong>Atenção Equipe Médica:</strong> Um paciente foi classificado com alta prioridade e requer avaliação imediata.
                    </div>

                    <h3>📋 Dados do Atendimento</h3>
                    <table class="data-table">
                        <tr><th>Horário:</th><td>$formattedDate</td></tr>
                        <tr><th>ID do Paciente:</th><td>${event.patientId}</td></tr>
                        <tr><th>ID da Triagem:</th><td>${event.triageId}</td></tr>
                    </table>

                    <h3>🩺 Sinais Vitais Coletados</h3>
                    <table class="data-table">
                        <tr>
                            <th>Dor no Peito:</th>
                            <td style="color: ${if (event.vitalSigns.hasChestPain) "red" else "green"}; font-weight: bold;">
                                ${if (event.vitalSigns.hasChestPain) "SIM 🚨" else "NÃO"}
                            </td>
                        </tr>
                        <tr><th>Temperatura:</th><td>${event.vitalSigns.temperature ?: "N/A"} °C</td></tr>
                        <tr><th>Batimentos:</th><td>${event.vitalSigns.heartRate ?: "N/A"} bpm</td></tr>
                        <tr><th>Saturação O2:</th><td>${event.vitalSigns.oxygenSaturation ?: "N/A"}%</td></tr>
                        <tr><th>Pressão Arterial:</th><td>${event.vitalSigns.systolicPressure}/${event.vitalSigns.diastolicPressure} mmHg</td></tr>
                    </table>
                    
                    <p style="margin-top: 20px;">
                        Acesse o sistema <strong>Prioritas</strong> para visualizar o histórico completo do paciente.
                    </p>
                </div>
                <div class="footer">
                    Prioritas System v1.0 • Mensagem automática, não responda.
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}