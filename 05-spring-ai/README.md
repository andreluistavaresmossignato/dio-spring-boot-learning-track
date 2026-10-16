# Assistente de controle financeiro com Spring AI

Projeto final do módulo Spring AI da trilha Spring Boot da DIO. A aplicação permite registrar e consultar transações financeiras por uma API REST e também interagir por comandos de voz.

## O que o projeto faz

- Registra uma transação com descrição, valor em centavos e categoria.
- Lista transações de uma categoria.
- Recebe áudio em português, transcreve o comando, usa o modelo de IA para escolher uma ferramenta de aplicação e devolve a resposta em áudio MP3.

As categorias disponíveis são `GROCERIES`, `PHARMA` e `AUTO`. O modelo pode acionar as ferramentas de registro e consulta, mas as operações continuam passando pelos casos de uso da aplicação.

## Tecnologias usadas

- Java 25
- Spring Boot 4
- Spring AI com modelos da OpenAI para chat, transcrição e síntese de voz
- Spring Web e Spring Data JPA
- MySQL 9
- Gradle
- JUnit 5, AssertJ e Mockito para testes

## Como executar

### Requisitos

- JDK 25
- Docker com Docker Compose
- Chave da API da OpenAI para usar os recursos de IA

### Iniciar o banco de dados

Na pasta `05-spring-ai`, execute:

```bash
docker compose up -d database
```

O Compose expõe o MySQL na porta `3307` e cria o banco e o usuário usados pela configuração do projeto.

### Configurar a chave da OpenAI

Configure `OPENAI_API_KEY` no ambiente antes de iniciar a aplicação. Não coloque a chave no código ou no README.

PowerShell:

```powershell
$env:OPENAI_API_KEY = "sua_chave"
```

Linux/macOS:

```bash
export OPENAI_API_KEY="sua_chave"
```

O uso da API OpenAI é cobrado separadamente de uma assinatura do ChatGPT e pode gerar custos de acordo com o uso.

### Iniciar a aplicação

Windows:

```powershell
.\gradlew.bat bootRun
```

Linux/macOS:

```bash
./gradlew bootRun
```

## Melhoria implementada

A gravação de transações agora valida os dados antes de persistir:

- A descrição não pode ser nula nem vazia (incluindo apenas espaços).
- O valor precisa ser maior que zero.

A validação fica no `PersistTransactionUseCase`, compartilhado pela rota REST e pela ferramenta de Tool Calling. Assim, os dados inválidos são rejeitados independentemente de terem vindo de uma requisição HTTP ou de uma ação acionada pela IA. Nas chamadas REST, a aplicação responde com `400 Bad Request` e uma mensagem explicando o problema.

## Como testar o fluxo principal

Com o MySQL iniciado e a aplicação em execução, registre uma transação usando `amount` em centavos:

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Compras no mercado","category":"GROCERIES","amount":2590}'
```

Consulte as transações dessa categoria:

```bash
curl http://localhost:8080/transactions/GROCERIES
```

Confira a validação enviando uma descrição em branco ou um valor igual a zero. A API deve responder `400 Bad Request` com o detalhe do erro:

```bash
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"  ","category":"GROCERIES","amount":0}'
```

Para testar o assistente por voz, envie um arquivo de áudio no campo multipart `file` para `POST /transactions/ai`. Esse fluxo chama os serviços de transcrição, chat e síntese de voz da OpenAI e requer `OPENAI_API_KEY` válida.

## Testes automatizados

Execute a suíte:

```bash
./gradlew test
```

No Windows, use `./gradlew.bat test`. O teste unitário `PersistTransactionUseCaseTest` verifica a aceitação de dados válidos e a rejeição de descrição nula ou em branco e de valor zero ou negativo, sem precisar de banco ou chave da OpenAI.

Os testes de integração que dependem da OpenAI requerem uma chave configurada e podem consumir créditos da API.

## O que aprendi

Este desafio ajudou a praticar a integração do Spring AI com uma aplicação em camadas: o modelo interpreta o pedido e seleciona ferramentas, enquanto os casos de uso executam as operações de negócio. Também reforcei a importância de validar os dados no ponto compartilhado de gravação, para que a mesma regra seja aplicada tanto à API REST quanto ao Tool Calling.

## Referências

- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/index.html)
- [ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Tool Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [Transcrição de áudio](https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html)
- [Síntese de voz](https://docs.spring.io/spring-ai/reference/api/audio/speech.html)
