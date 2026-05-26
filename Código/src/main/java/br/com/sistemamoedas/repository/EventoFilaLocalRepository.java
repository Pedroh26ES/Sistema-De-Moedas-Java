package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.EventoFilaLocal;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventoFilaLocalRepository implements PanacheRepository<EventoFilaLocal> {
}
