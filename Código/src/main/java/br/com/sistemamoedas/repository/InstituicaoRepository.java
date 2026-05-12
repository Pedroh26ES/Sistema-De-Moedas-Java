package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.Instituicao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InstituicaoRepository implements PanacheRepository<Instituicao> {
}
