package br.com.fiap.adj8.phase5.prioritas.notification.infra.adapter.`in`.messaging

import br.com.fiap.adj8.phase5.prioritas.common.event.TRIAGE_QUEUE
import br.com.fiap.adj8.phase5.prioritas.common.event.TriageNotificationEvent
import br.com.fiap.adj8.phase5.prioritas.notification.application.port.`in`.TriageNotificationListener
import br.com.fiap.adj8.phase5.prioritas.notification.application.port.out.mail.SendTriageEmailPort
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class TriageNotificationListenerImpl(
    private val sendEmailPort: SendTriageEmailPort
): TriageNotificationListener {

    private val logger = LoggerFactory.getLogger(TriageNotificationListenerImpl::class.java)

    @RabbitListener(queues = [TRIAGE_QUEUE])
    override fun handleMessage(event: TriageNotificationEvent) {
        logger.info("📩 [RABBITMQ] Nova triagem recebida!")
        logger.info("   PACIENTE: ${event.patientId}")
        logger.info("   RISCO: ${event.riskLevel} (${event.riskColor})")

        if (event.riskLevel == "EMERGENCY" || event.riskLevel == "URGENT") {
            sendEmailPort.sendCriticalAlert(event)
        } else {
            logger.info("ℹ️ Paciente adicionado à fila regular de atendimento.")
        }
    }
}