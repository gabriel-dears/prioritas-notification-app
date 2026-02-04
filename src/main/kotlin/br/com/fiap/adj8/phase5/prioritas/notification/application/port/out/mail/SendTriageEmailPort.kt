package br.com.fiap.adj8.phase5.prioritas.notification.application.port.out.mail

import br.com.fiap.adj8.phase5.prioritas.common.event.TriageNotificationEvent

interface SendTriageEmailPort {
    fun sendCriticalAlert(event: TriageNotificationEvent)
}