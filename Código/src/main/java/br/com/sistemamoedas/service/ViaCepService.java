package br.com.sistemamoedas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ViaCepService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    @ConfigProperty(name = "integracoes.viacep.base-url", defaultValue = "https://viacep.com.br/ws")
    String baseUrl;

    @Inject
    ObjectMapper mapper;

    public EnderecoViaCep consultar(String cepInformado) {
        String cep = cepInformado == null ? "" : cepInformado.replaceAll("\\D", "");
        if (cep.length() != 8) {
            throw new RegraNegocioException("Informe um CEP com 8 digitos.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/" + cep + "/json/"))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RegraNegocioException("Nao foi possivel consultar o CEP agora.");
            }

            JsonNode json = mapper.readTree(response.body());
            if (json.path("erro").asBoolean(false)) {
                throw new RegraNegocioException("CEP nao encontrado no ViaCEP.");
            }

            String logradouro = json.path("logradouro").asText("");
            String complemento = json.path("complemento").asText("");
            String bairro = json.path("bairro").asText("");
            String cidade = json.path("localidade").asText("");
            String uf = json.path("uf").asText("");
            return new EnderecoViaCep(
                    json.path("cep").asText(cep),
                    logradouro,
                    complemento,
                    bairro,
                    cidade,
                    uf,
                    formatar(logradouro, complemento, bairro, cidade, uf));
        } catch (RegraNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new RegraNegocioException("Nao foi possivel consultar o ViaCEP. Tente novamente em instantes.");
        }
    }

    private String formatar(String logradouro, String complemento, String bairro, String cidade, String uf) {
        StringBuilder endereco = new StringBuilder();
        if (!logradouro.isBlank()) {
            endereco.append(logradouro);
        }
        if (!complemento.isBlank()) {
            if (!endereco.isEmpty()) {
                endereco.append(", ");
            }
            endereco.append(complemento);
        }
        if (!bairro.isBlank()) {
            if (!endereco.isEmpty()) {
                endereco.append(" - ");
            }
            endereco.append(bairro);
        }
        if (!cidade.isBlank() || !uf.isBlank()) {
            if (!endereco.isEmpty()) {
                endereco.append(", ");
            }
            endereco.append(cidade);
            if (!uf.isBlank()) {
                endereco.append(" - ").append(uf);
            }
        }
        return endereco.toString();
    }
}
