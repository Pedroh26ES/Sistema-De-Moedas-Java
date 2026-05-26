# Integracoes: RabbitMQ, QR Code e ViaCEP

Este documento resume as integracoes adicionadas ao Valoriza Ae.

## RabbitMQ

O sistema agora publica eventos de negocio em uma fila RabbitMQ quando acontecem operacoes importantes:

- `MOEDAS_ENVIADAS`: professor reconheceu um aluno com moedas.
- `CUPOM_GERADO`: aluno resgatou uma vantagem e recebeu um cupom.
- `CUPOM_VALIDADO`: parceiro confirmou o atendimento do cupom.
- `CUPOM_DESATIVADO`: parceiro pausou uma vantagem com cupom pendente.
- `CUPOM_REATIVADO`: parceiro publicou novamente uma vantagem pausada.

O RabbitMQ e uma dependencia obrigatoria do fluxo operacional. Sem a fila ativa, operacoes que precisam gerar evento de auditoria sao canceladas:

```properties
valoriza.rabbitmq.host=localhost
valoriza.rabbitmq.port=5672
valoriza.rabbitmq.username=guest
valoriza.rabbitmq.password=guest
valoriza.rabbitmq.queue=valoriza-ae.eventos
```

Isso garante que envio de moedas, resgate de vantagem, validacao de cupom e mudancas de status fiquem rastreaveis na fila. Se o RabbitMQ estiver indisponivel, o sistema retorna erro e nao conclui a operacao.

No codigo do projeto, o RabbitMQ ja esta preparado para uso local e para execucao completa em Docker:

- `docker-compose.yml`: sobe RabbitMQ com painel de administracao e tambem pode subir a aplicacao.
- `start.ps1`: sobe o RabbitMQ, aguarda a fila ficar pronta, compila o frontend e inicia o Quarkus local.
- `start-docker.ps1`: sobe aplicacao e RabbitMQ juntos via Docker Compose.
- `RabbitMqStartupCheck`: valida a fila na inicializacao da aplicacao fora dos testes.

Com Docker Desktop aberto, o comando recomendado para desenvolvimento e:

```powershell
.\start.ps1
```

Para subir tudo em containers:

```powershell
.\start-docker.ps1
```

O painel do RabbitMQ fica em:

```text
http://localhost:15672
```

Usuario e senha:

```text
guest / guest
```

## QR Code do Cupom

Todo cupom gerado no resgate possui QR Code em:

```text
GET /api/cupons/{codigo}/qrcode
```

O QR Code contem uma URL unica de validacao. Ao escanear, o parceiro abre a rota:

```text
/empresa?cupom={codigo}
```

Assim o campo de validacao ja aparece preenchido no painel da empresa. Os emails de cupom tambem registram o codigo, a URL do QR Code e o link de validacao.

No painel do aluno, o QR Code aparece no cupom recente e nas vantagens ja adquiridas. O aluno pode apresentar o QR Code ao parceiro no atendimento.

## ViaCEP

O cadastro de aluno e o cadastro de empresa agora consultam endereco pelo CEP usando:

```text
GET /api/cep/{cep}
```

O endpoint valida CEP com 8 digitos, consulta `https://viacep.com.br/ws` e devolve o endereco formatado para preencher o campo de endereco automaticamente.

Config:

```properties
integracoes.viacep.base-url=https://viacep.com.br/ws
```
