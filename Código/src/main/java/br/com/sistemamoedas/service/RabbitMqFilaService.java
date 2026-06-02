package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.EventoFilaLocal;
import br.com.sistemamoedas.repository.EventoFilaLocalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.File;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RabbitMqFilaService {

    private static final Logger LOG = Logger.getLogger(RabbitMqFilaService.class);

    @ConfigProperty(name = "valoriza.rabbitmq.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "valoriza.rabbitmq.port", defaultValue = "5672")
    int port;

    @ConfigProperty(name = "valoriza.rabbitmq.username", defaultValue = "valoriza")
    String username;

    @ConfigProperty(name = "valoriza.rabbitmq.password", defaultValue = "valoriza-local-rabbitmq")
    String password;

    @ConfigProperty(name = "valoriza.rabbitmq.queue", defaultValue = "valoriza-ae.eventos")
    String queue;

    @ConfigProperty(name = "valoriza.rabbitmq.auto-start", defaultValue = "true")
    boolean autoStart;

    @ConfigProperty(name = "valoriza.rabbitmq.local-fallback", defaultValue = "true")
    boolean localFallback;

    @Inject
    ObjectMapper mapper;

    @Inject
    EventoFilaLocalRepository eventosLocais;

    public boolean verificarDisponivel() {
        if (LaunchMode.current() == LaunchMode.TEST) {
            LOG.info("Teste automatizado: verificacao externa do RabbitMQ ignorada.");
            return true;
        }
        try (Connection connection = novaConexao();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            LOG.infof("RabbitMQ pronto. Fila declarada: %s", queue);
            return true;
        } catch (Exception e) {
            LOG.errorf(e, "RabbitMQ obrigatorio indisponivel na inicializacao.");
            if (tentarAutoStart()) {
                return verificarDisponivelSemAutoStart();
            }
            if (localFallback) {
                LOG.warn("RabbitMQ indisponivel. Modo local de fila ativado para desenvolvimento via Quarkus.");
                return true;
            }
            return false;
        }
    }

    public void exigirDisponivel() {
        if (!verificarDisponivel()) {
            throw new RegraNegocioException(
                    "RabbitMQ indisponivel. Inicie a fila para concluir esta operacao com rastreabilidade.");
        }
    }

    @Transactional
    public void publicar(EventoSistema evento) {
        if (LaunchMode.current() == LaunchMode.TEST) {
            LOG.infof("Teste automatizado: evento nao enviado ao RabbitMQ externo: %s", evento.tipo());
            return;
        }

        try (Connection connection = novaConexao();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            byte[] body = mapper.writeValueAsBytes(evento);
            channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
            LOG.infof("Evento publicado no RabbitMQ: %s", evento.tipo());
        } catch (Exception e) {
            LOG.errorf(e, "RabbitMQ obrigatorio indisponivel. Operacao cancelada: %s", evento.tipo());
            if (tentarAutoStart() && publicarAposAutoStart(evento)) {
                return;
            }
            if (localFallback) {
                registrarFallbackLocal(evento, "RabbitMQ indisponivel");
                return;
            }
            throw new RegraNegocioException(
                    "RabbitMQ indisponivel. Inicie a fila para concluir esta operacao com rastreabilidade.");
        }
    }

    private void registrarFallbackLocal(EventoSistema evento, String origem) {
        try {
            eventosLocais.persist(new EventoFilaLocal(evento.tipo(), mapper.writeValueAsString(evento), origem));
            LOG.warnf("Evento registrado na fila local de desenvolvimento: %s", evento.tipo());
        } catch (Exception erro) {
            LOG.errorf(erro, "Nao foi possivel registrar evento na fila local: %s", evento.tipo());
            throw new RegraNegocioException(
                    "Fila local indisponivel. Nao foi possivel concluir a operacao com rastreabilidade.");
        }
    }

    private boolean publicarAposAutoStart(EventoSistema evento) {
        try (Connection connection = novaConexao();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            byte[] body = mapper.writeValueAsBytes(evento);
            channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
            LOG.infof("Evento publicado no RabbitMQ apos inicializacao automatica: %s", evento.tipo());
            return true;
        } catch (Exception erro) {
            LOG.errorf(erro, "RabbitMQ continuou indisponivel apos tentativa automatica.");
            return false;
        }
    }

    private boolean verificarDisponivelSemAutoStart() {
        try (Connection connection = novaConexao();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            LOG.infof("RabbitMQ pronto apos inicializacao automatica. Fila declarada: %s", queue);
            return true;
        } catch (Exception erro) {
            LOG.errorf(erro, "RabbitMQ continuou indisponivel apos inicializacao automatica.");
            return false;
        }
    }

    private boolean tentarAutoStart() {
        if (!autoStart || LaunchMode.current() == LaunchMode.TEST) {
            return false;
        }
        File compose = new File("docker-compose.yml");
        if (!compose.exists()) {
            LOG.warn("docker-compose.yml nao encontrado. Auto-start do RabbitMQ ignorado.");
            return false;
        }
        try {
            LOG.info("Tentando subir RabbitMQ automaticamente com docker compose up -d rabbitmq...");
            Process processo = new ProcessBuilder("docker", "compose", "up", "-d", "rabbitmq")
                    .directory(new File("."))
                    .redirectErrorStream(true)
                    .start();
            boolean finalizou = processo.waitFor(Duration.ofSeconds(45).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finalizou || processo.exitValue() != 0) {
                LOG.warnf("Docker Compose nao confirmou a subida do RabbitMQ. Finalizou=%s, codigo=%s",
                        finalizou, finalizou ? processo.exitValue() : "pendente");
                if (!finalizou) {
                    processo.destroyForcibly();
                }
                return false;
            }
            return aguardarRabbitMq();
        } catch (Exception erro) {
            LOG.warnf(erro, "Nao foi possivel iniciar RabbitMQ automaticamente. Verifique se o Docker Desktop esta aberto.");
            return false;
        }
    }

    private boolean aguardarRabbitMq() {
        for (int tentativa = 1; tentativa <= 20; tentativa++) {
            try (Connection connection = novaConexao();
                    Channel channel = connection.createChannel()) {
                channel.queueDeclare(queue, true, false, false, null);
                return true;
            } catch (Exception erro) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException interrompido) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private Connection novaConexao() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory.newConnection();
    }
}
