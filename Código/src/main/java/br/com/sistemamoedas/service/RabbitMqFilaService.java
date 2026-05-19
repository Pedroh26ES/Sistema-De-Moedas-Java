package br.com.sistemamoedas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RabbitMqFilaService {

    private static final Logger LOG = Logger.getLogger(RabbitMqFilaService.class);

    @ConfigProperty(name = "valoriza.rabbitmq.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "valoriza.rabbitmq.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "valoriza.rabbitmq.port", defaultValue = "5672")
    int port;

    @ConfigProperty(name = "valoriza.rabbitmq.username", defaultValue = "guest")
    String username;

    @ConfigProperty(name = "valoriza.rabbitmq.password", defaultValue = "guest")
    String password;

    @ConfigProperty(name = "valoriza.rabbitmq.queue", defaultValue = "valoriza-ae.eventos")
    String queue;

    @Inject
    ObjectMapper mapper;

    public void publicar(EventoSistema evento) {
        if (!enabled) {
            LOG.infof("RabbitMQ desativado. Evento mantido apenas no fluxo transacional: %s", evento.tipo());
            return;
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            byte[] body = mapper.writeValueAsBytes(evento);
            channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
            LOG.infof("Evento publicado no RabbitMQ: %s", evento.tipo());
        } catch (Exception e) {
            LOG.warnf(e, "Nao foi possivel publicar evento no RabbitMQ. O fluxo principal foi mantido.");
        }
    }
}
