package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.EmpresaParceira;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmpresaParceiraRepository implements PanacheRepository<EmpresaParceira> {

    public boolean cnpjEmUso(String cnpj) {
        return find("cnpj", cnpj).firstResultOptional().isPresent();
    }
}
