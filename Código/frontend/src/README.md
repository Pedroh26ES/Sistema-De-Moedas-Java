# Estrutura do front-end

O front-end foi organizado para separar responsabilidades e facilitar manutenção.

## Pastas

- `App.jsx`: controla sessão, rotas e proteção por perfil.
- `main.jsx`: ponto de entrada do React.
- `config/`: constantes de marca, imagens, filtros e URLs de API.
- `services/`: comunicação HTTP com o backend.
- `hooks/`: estados reutilizáveis, como rota atual e carregamento de dashboards.
- `utils/`: formatação, navegação, parsing de benefícios e factories de formulário.
- `components/`: componentes reutilizáveis de UI, tabelas, filtros, cards e layout.
- `pages/`: telas completas do sistema, separadas por contexto.
- `styles/`: CSS dividido por camada visual.

## Regra prática

- Nova tela entra em `pages/`.
- Nova chamada HTTP entra em `services/`.
- Novo componente reutilizável entra em `components/`.
- Nova função pura entra em `utils/`.
- Novo estilo global entra no arquivo CSS da camada correspondente.
