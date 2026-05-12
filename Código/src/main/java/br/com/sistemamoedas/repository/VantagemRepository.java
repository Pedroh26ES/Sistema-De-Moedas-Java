package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Vantagem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class VantagemRepository implements PanacheRepository<Vantagem> {

    public List<Vantagem> ativas() {
        return list("ativa = true order by custoMoedas asc, titulo asc");
    }

    public List<Vantagem> daEmpresa(EmpresaParceira empresa) {
        return list("empresa = ?1 order by ativa desc, titulo asc", empresa);
    }
}
