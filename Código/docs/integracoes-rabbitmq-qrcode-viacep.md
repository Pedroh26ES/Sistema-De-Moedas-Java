# Integracoes: RabbitMQ, QR Code e ViaCEP

Este documento resume as integracoes adicionadas ao Valoriza Ae.

## RabbitMQ

O sistema agora publica eventos de negocio em uma fila RabbitMQ quando acontecem operacoes importantes:

- `MOEDAS_ENVIADAS`: professor reconheceu um aluno com moedas.
- `CUPOM_GERADO`: aluno resgatou uma vantagem e recebeu um cupom.
- `CUPOM_VALIDADO`: parceiro confirmou o atendimento do cupom.
- `CUPOM_DESATIVADO`: parceiro pausou uma vantagem com cupom pendente.
- `CUPOM_REATIVADO`: parceiro publicou novamente uma vantagem pausada.

Por padrao a fila fica desligada para o projeto continuar rodando sem RabbitMQ instalado:

```properties
valoriza.rabbitmq.enabled=false
```

Para ativar, suba um RabbitMQ local e ajuste as propriedades:

```properties
valoriza.rabbitmq.enabled=true
valoriza.rabbitmq.host=localhost
valoriza.rabbitmq.port=5672
valoriza.rabbitmq.username=guest
valoriza.rabbitmq.password=guest
valoriza.rabbitmq.queue=valoriza-ae.eventos
```

O publicador nao derruba o fluxo principal se a fila estiver indisponivel. O resgate, envio de moedas e validacao continuam funcionando, e o erro fica registrado no log.

## QR Code do Cupom

Todo cupom gerado no resgate possui QR Code em:

```text
GET /api/cupons/{codigo}/qrcode
```

O QR Code contem:

- codigo do cupom;
- nome do aluno;
- vantagem resgatada;
- link para o painel da empresa com o cupom preenchido na URL.

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
