package br.com.fiap.adj8.phase5.prioritas.notification.application.port.`in`

import br.com.fiap.adj8.phase5.prioritas.common.event.TriageNotificationEvent

interface TriageNotificationListener {
    fun handleMessage(event: TriageNotificationEvent)
}