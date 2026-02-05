package br.com.fiap.adj8.phase5.prioritas.notification.infra.adapter.out.mail.template

import br.com.fiap.adj8.phase5.prioritas.common.event.TriageNotificationEvent
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.format.DateTimeFormatter

@Service
class ThymeleafTemplateService(
    private val templateEngine: TemplateEngine
) {

    fun generateHtml(event: TriageNotificationEvent): String {
        val context = Context()

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val colorHex = resolveColor(event.riskColor)

        context.setVariable("riskLevel", event.riskLevel)
        context.setVariable("headerColor", colorHex)
        context.setVariable("formattedDate", event.assessedAt.format(formatter))
        context.setVariable("patientId", event.patientId)
        context.setVariable("triageId", event.triageId)

        context.setVariable("hasChestPain", event.vitalSigns.hasChestPain)
        context.setVariable("temperature", event.vitalSigns.temperature ?: "N/A")
        context.setVariable("heartRate", event.vitalSigns.heartRate ?: "N/A")
        context.setVariable("oxygenSaturation", event.vitalSigns.oxygenSaturation ?: "N/A")

        return templateEngine.process("triage-alert", context)
    }

    private fun resolveColor(riskColor: String): String {
        return when (riskColor.uppercase()) {
            "RED" -> "#dc3545"
            "ORANGE" -> "#fd7e14"
            "YELLOW" -> "#ffc107"
            else -> "#0d6efd"
        }
    }
}