package br.com.sistemamoedas.app;

import br.com.sistemamoedas.service.RabbitMqFilaService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RabbitMqStartupCheck {

    private static final Logger LOG = Logger.getLogger(RabbitMqStartupCheck.class);

    @Inject
    RabbitMqFilaService fila;

    @ConfigProperty(name = "valoriza.rabbitmq.fail-on-startup", defaultValue = "false")
    boolean failOnStartup;

    void onStart(@Observes StartupEvent event) {
        boolean pronto = fila.verificarDisponivel();
        if (!pronto && failOnStartup) {
            fila.exigirDisponivel();
        }
        if (!pronto) {
            LOG.warn("RabbitMQ ainda nao esta acessivel. A aplicacao abre, mas envios/resgates exigem a fila ativa.");
        }
    }
}
