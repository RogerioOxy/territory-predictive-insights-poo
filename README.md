# Territory Predictive Insights

Global Solution 2026.2, Programação Orientada a Objetos, turma 2CCPW.

Integrantes: Rogerio Deligi Ferreira Filho (RM561942) e Maria Fernanda Garavelli Dantas (RM562686).

## Público beneficiado

Nosso recorte é clima local: chuva intensa e risco de alagamento em pontos vulneráveis, aqui representados por uma boca de lobo e um trecho de córrego. Quem sofre o problema são os moradores das partes baixas próximas desses pontos. A zeladoria e a Defesa Civil usam as informações para avaliar onde agir.

A decisão apoiada é limpar a boca de lobo antes da chuva ou enviar uma equipe para avaliar o ponto, em vez de esperar uma reclamação depois do alagamento. Isso acompanha a proposta da dupla na GS: zeladoria que não espera a reclamação. O programa apresenta leituras, previsões e alertas para ajudar nessa avaliação.

O território monitorado é fictício: Bacia do Corrego Exemplo. BL-01 representa a boca de lobo, com o pluviômetro PLU-01 e o sensor de obstrução OBS-01. CR-01 representa o trecho de córrego, com o sensor de nível NIV-01. A população exposta inicial, 1.200 pessoas, também é fictícia. Não usamos endereços reais nem dados pessoais de moradores.

## Como rodar

Usamos Java 17, Spring Boot 4.1.1, Spring Data JPA e H2 em arquivo. `SistemaMonitoramento` implementa `CommandLineRunner` e recebe a entrada com `Scanner`, sem interface web.

Instale o JDK 17 e configure `JAVA_HOME` ou deixe o Java disponível no `PATH`. O Maven Wrapper está incluído, portanto não precisa instalar Maven. A primeira execução precisa de internet para baixar o Maven e as dependências.

Execute os comandos dentro de `territory-predictive-insights-fev`, onde estão `pom.xml`, `mvnw` e `mvnw.cmd`.

### Testes

No PowerShell:

```powershell
.\mvnw.cmd test
```

No Prompt de Comando:

```bat
mvnw.cmd test
```

No Linux, macOS ou Git Bash:

```sh
./mvnw test
```

### Menu

No PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

No Prompt de Comando:

```bat
mvnw.cmd spring-boot:run
```

No Linux, macOS ou Git Bash:

```sh
./mvnw spring-boot:run
```

Outra opção é gerar o JAR e executá-lo. No PowerShell:

```powershell
.\mvnw.cmd package
java -jar target/territory-predictive-insights-fev-1.0.0.jar
```

No Prompt de Comando, use:

```bat
mvnw.cmd package
java -jar target/territory-predictive-insights-fev-1.0.0.jar
```

No Linux, macOS ou Git Bash:

```sh
./mvnw package
java -jar target/territory-predictive-insights-fev-1.0.0.jar
```

Pelo Eclipse ou IntelliJ, importe como projeto Maven e execute `br.com.fiap.territorio.SistemaMonitoramento`. Confira que o diretório de trabalho é a pasta do projeto.

### Banco

Não é necessário instalar um servidor de banco. A configuração já está em `src/main/resources/application.properties`:

```properties
spring.main.web-application-type=none
spring.datasource.url=jdbc:h2:file:./dados/territorio
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
territorio.semente.ativa=true
```

Execute sempre dentro da pasta do projeto, inclusive usando o JAR. O caminho `./dados/` é relativo ao diretório de trabalho. As tabelas são criadas ou atualizadas pelo Hibernate; as medições e os alertas permanecem no arquivo após fechar o programa.

Para começar do zero, feche o programa e apague a pasta `dados` do projeto. Isso remove o histórico salvo. Na próxima abertura, a semente volta a entrar. Para desativá-la, altere a propriedade existente para:

```properties
territorio.semente.ativa=false
```

População, limiares alterados pelo menu e estado ligado/desligado dos sensores valem somente durante a execução. Eles voltam ao padrão ao reiniciar; o histórico de medições e alertas continua salvo.

## Menu e demonstração

| Opção | O que faz |
| --- | --- |
| 1 | Verifica sensores e pede de 1 a 20 ciclos. Cada ciclo tenta ler os três sensores e grava as leituras válidas. |
| 2 | Calcula previsões para a próxima leitura, explica o cálculo e o significado, e grava novos alertas ao atingir um limiar. |
| 3 | Consulta medições por sensor ou alertas emitidos e oferece exportar o histórico consultado em CSV. |
| 4 | Mostra dados e limiares, altera população, altera limiares com código e liga/desliga sensores. |
| 5 | Exibe status dos pontos, sensores, quantidades de leituras, configuração e alertas. |
| 0 | Encerra o programa. |

Para alterar limiares, use a opção 4, item 3, escolha o tipo de sensor e informe `ZELADORIA-2026`. Esse é o código acadêmico da constante `CODIGO_ACESSO_PADRAO`, disponibilizado aqui para a correção.

| Grandeza | ATENCAO | ALERTA | CRITICO | Faixa física aceita |
| --- | --- | --- | --- | --- |
| Chuva | 15 | 30 | 50 | 0 a 300 mm/h |
| Nível do córrego | 1,8 | 2,4 | 2,8 | 0 a 6 m |
| Obstrução | 40 | 60 | 80 | 0 a 100% |

Na primeira abertura, sem medições no banco, `SementeHistorico` fornece 19 leituras: oito de chuva, subindo de 3,2 a 38,5 mm/h; oito de nível, de 0,9 a 2,0 m; e três de obstrução, representando um sensor instalado há pouco. A opção 2 já permite observar previsão, alerta e falta de histórico.

Com essa semente, a chuva tem média móvel de 24,96 mm/h, ATENCAO, e comparação de 50,10 mm/h, CRITICO. O nível tem média de 1,64 m, sem alerta, e comparação de 2,30 m, ATENCAO. A obstrução ainda não tem previsão. São necessárias mais duas leituras válidas para sua média móvel e três para sua comparação. Falhas e descartes podem exigir ciclos adicionais.

Para conferir a persistência, consulte a opção 5, gere leituras na opção 1, rode previsões na opção 2 e saia com 0. Reabra pelo mesmo diretório e consulte novamente. As contagens da opção 5 continuam as mesmas depois de reabrir, e a semente não entra de novo. Repetir as previsões sem novas leituras informa que o alerta já foi registrado. As quantidades variam entre execuções porque as leituras são aleatórias.

### Exceções

Para provocar `FonteIndisponivelException`, desligue um sensor na opção 4, item 4, volte ao menu principal e tente gerar leituras na opção 1. A exceção guarda o código do sensor. Também há probabilidade configurada de 4% de falha de resposta durante uma tentativa com sensor ligado.

Para provocar `DadosInvalidosException`, informe população negativa na opção 4, item 2, ou limiares fora de ordem, como 40, 30 e 50, no item 3. O construtor de `Medicao` também rejeita valores fora da faixa física. Depois da verificação de falha, há uma chance configurada de 4% de leitura espúria: chuva ou nível negativos, ou obstrução acima de 100%. Ela é descartada antes da gravação.

Para provocar `AcessoNegadoException`, informe código errado na opção 4, item 3. As três exceções são capturadas no menu, que explica o problema e continua. Não há `catch` vazio.

### CSV

O bônus escolhido é exportar o histórico consultado. Na opção 3, consulte um sensor ou os alertas e responda `s` à pergunta de exportação. O arquivo vai para `exportacoes/`, com separador ponto e vírgula e vírgula decimal, para abrir no Excel. O console informa o caminho. A exportação usa a lista exibida na consulta.

### Saída real

Trecho literal de uma execução real, na primeira execução com banco vazio:

```text
PLU-01 (Pluviometro) - Media movel
  Valor previsto para a proxima leitura: 24,96 mm/h.
  Calculo: Media das ultimas 5 leituras = 24,96.
  Previsao atinge ATENCAO (limite 15,00 mm/h). Zeladoria: programar vistoria e limpeza nos proximos dias.
  Novo alerta gravado no banco: ATENCAO: Pluviometro, sensor PLU-01, ponto BL-01. Media movel: previsao 24,96 mm/h, limite atingido 15,00 mm/h. Zeladoria: programar vistoria e limpeza nos proximos dias.
PLU-01 (Pluviometro) - Comparacao com o periodo anterior
  Valor previsto para a proxima leitura: 50,10 mm/h.
  Calculo: Grupos de 3 leituras: media anterior 12,77, media recente 31,43, variacao +18,67. Previsao = media recente + variacao.
  Previsao atinge CRITICO (limite 50,00 mm/h). Acionar a Defesa Civil para avaliar o ponto e orientar a limpeza.
  Novo alerta gravado no banco: CRITICO: Pluviometro, sensor PLU-01, ponto BL-01. Comparacao com o periodo anterior: previsao 50,10 mm/h, limite atingido 50,00 mm/h. Acionar a Defesa Civil para avaliar o ponto e orientar a limpeza.
OBS-01 (Obstrucao da boca de lobo) - Media movel
  Sem previsao: 3 leituras disponiveis; sao necessarias 5. Use a opcao 1 para completar o historico.
OBS-01 (Obstrucao da boca de lobo) - Comparacao com o periodo anterior
  Sem previsao: 3 leituras disponiveis; sao necessarias 6. Use a opcao 1 para completar o historico.
```

## Organização

O pacote-base é `br.com.fiap.territorio`, dentro de `src/main/java`.

| Local | Papel |
| --- | --- |
| Pacote-base | `SistemaMonitoramento` conduz o menu; `Mensagens` e `Formatador` organizam textos e números. |
| `dominio` | Componentes territoriais, sensores, estratégias, limiares e entidades `Medicao` e `Alerta`. |
| `excecao` | As três exceções próprias do projeto. |
| `persistencia` | `MedicaoRepository` e `AlertaRepository`. |
| `servico` | Monitoramento, configuração, Factory, mapa, semente e exportador CSV. |
| `src/main/resources` | Configuração do Spring e do banco. |
| `src/test/java` | Testes de domínio e serviço. |

## Domínio e persistência

`ComponenteTerritorial` é abstrata, guarda id, nome, região e status, e declara `verificarStatus()`. Sua filha `PontoVulneravel` verifica os sensores instalados para definir OPERANDO, PARCIAL ou SEM_MONITORAMENTO.

A interface `Sensor` declara leitura, funcionamento, tipo, código e alteração de funcionamento. `SensorSimulado` implementa o comum: `Random`, falhas, leituras espúrias e continuidade da série. `Pluviometro`, `SensorNivelCorrego` e `SensorObstrucaoBueiro` especializam a variação: chuva oscila, nível oscila menos e obstrução tende a crescer pelo acúmulo de lixo.

`DadosTerritorio` mantém atributos privados e valida alterações. `EstrategiaPrevisao` concentra janela, mínimo de leituras e regra de silêncio. `PrevisaoMediaMovel` e `PrevisaoComparacao` chamam `super(...)` e sobrescrevem `prever()`. O serviço percorre uma lista de `EstrategiaPrevisao` e chama esse método sem `instanceof` ou decisão por tipo.

`Medicao` e `Alerta` são entidades JPA. Os repositories usam consultas derivadas dos nomes dos métodos, sem SQL montado por concatenação. Spring Data JPA gerencia recursos e transações das operações de persistência, dispensando o gerenciamento manual de conexão JDBC. Cada leitura válida é salva; alertas novos também. Antes de gravar um alerta, o serviço verifica sensor, estratégia, nível, limite aplicado e última medição para evitar repetição. Se o limiar mudar, a nova avaliação fica registrada: ao mudar os limiares de chuva de 15/30/50 para 20/30/50, a mesma previsão de 24,96 mm/h gera um novo alerta ATENCAO com limite atingido de 20,00 mm/h.

## Padrões criacionais

A Factory `FabricaPrevisao` usa um `EnumMap` de `TipoPrevisao` para funções criadoras, lendo janela e mínimo da configuração. É o único lugar com `new PrevisaoMediaMovel` e `new PrevisaoComparacao`; os testes também obtêm estratégias pela fábrica.

Ela fica na criação das estratégias porque ali o tipo concreto muda conforme as estratégias ativas e seus parâmetros. O serviço recebe `EstrategiaPrevisao` e chama `prever()`. Uma terceira estratégia exige a classe e uma entrada no mapa, além de registrar seu tipo no enum e ativá-la na configuração. Não colocamos Factory nos sensores, cujos tipos são montados juntos no mapa territorial, nem nas medições e alertas, que recebem dados prontos. Não haveria o mesmo ganho.

O Singleton `ConfiguracaoSistema` tem construtor privado, instância estática e `getInstance()`. Um `@Bean` de `ConfiguracaoAplicacao` entrega essa mesma instância ao Spring. Ele reúne parâmetros de previsão e o `DadosTerritorio`, dono dos limiares. Isso evita o menu alterar uma cópia enquanto o serviço consulta outra e usa o limite errado. Não usamos Builder porque os objetos não exigem montagem em várias etapas opcionais.

## Decisões da equipe

1. **Spring Boot e JPA:** foram as ferramentas usadas mais recentemente pela dupla na Sprint 4 e nos CPs. O enunciado permite JPA ou JDBC.
2. **H2 em arquivo:** atende à permanência dos dados após reiniciar, sem instalar banco. Em memória, o histórico desapareceria ao fechar.
3. **Semente apenas sem medições:** facilita a primeira correção com exemplos prontos. As três leituras de obstrução também demonstram o silêncio.
4. **Mínimo de histórico:** a média móvel exige cinco leituras; a comparação exige seis para formar dois grupos completos de três.
5. **Descarte antes de salvar:** uma leitura impossível contaminaria a média. A validação recusa o valor e o menu informa o descarte.
6. **Um dono dos limiares:** eles ficam em `DadosTerritorio`, dentro do Singleton, e a alteração exige código. População, limiares e funcionamento dos sensores não persistem.
7. **Alerta sem repetição:** sensor, estratégia, nível, limite aplicado e última medição identificam o alerta. Repetir a previsão sem mudar nada não duplica o registro. Ao mudar os limiares de chuva de 15/30/50 para 20/30/50, a nova avaliação da previsão de 24,96 mm/h gera outro alerta ATENCAO, agora com limite atingido de 20,00 mm/h.
8. **Textos centralizados:** as mensagens do programa ficam em `Mensagens.java`, sem acentos para evitar problemas no console Windows/Eclipse. Os números usam pt-BR.
9. **Exceção de acesso própria:** código incorreto tem significado diferente de um valor inválido, por isso usamos `AcessoNegadoException`.
10. **Próxima leitura:** esse é o horizonte da previsão. A comparação soma a média recente à variação entre grupos, nunca fica negativa e é limitada pelo serviço à faixa física do sensor.

## Testes

São 38 testes unitários em oito classes: `MedicaoTest`, `LimiaresTest`, `DadosTerritorioTest`, `SensoresTest`, `PrevisaoMediaMovelTest`, `PrevisaoComparacaoTest`, `FabricaPrevisaoTest` e `ServicoMonitoramentoTest`. Usam JUnit Jupiter 6 (vem com o spring-boot-starter-test) e organização Arrange, Act e Assert, às vezes reunindo Act e Assert no mesmo comentário. Nenhum inicia Spring ou banco. Há `assertThrows` para as três exceções.

No serviço, Mockito simula os dois repositories. `previsaoAcimaDoLimiarCriticoGravaAlertaCritico` fornece seis leituras de 55 mm/h e verifica duas gravações com `verify(alertaRepository, times(2)).save(...)`, ambas CRITICO, com limite 50. Outros testes usam `never()` para valores abaixo do limiar, histórico insuficiente e alertas já registrados.

Resultado de uma execução real dos testes, com linhas literais:

```text
[INFO] Tests run: 38, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Os testes verificam regras e chamadas aos repositories. A execução real do menu também permitiu conferir a persistência após reiniciar.

## Reflexão crítica

### Quando se calar

Com menos de cinco leituras, uma média pode ficar perto de um chute: um único valor estranho pesa muito no resultado. Preferimos informar que ainda falta histórico a mostrar um número com aparência de certeza. A média móvel exige cinco leituras e a comparação exige seis. Mesmo atingindo esse mínimo, o cálculo pode exagerar uma tendência. Na semente do pluviômetro, a comparação projeta 50,10 mm/h, CRITICO, enquanto a média móvel indica 24,96 mm/h, ATENCAO. Exibir as duas ajuda a perceber essa diferença. Um sensor desligado não fornece novas leituras. Porém, a implementação ainda permite prever usando seu histórico salvo: bloquear previsões com dados antigos seria uma melhoria, e não uma proteção já existente.

### O que os dados não representam

Um pluviômetro em um ponto não informa como choveu na rua de cima. A obstrução de uma boca de lobo não descreve toda a rede de drenagem. Além disso, usamos sensores simulados com `Random`: os números demonstram o software, não as condições de uma cidade real. Para nossa proposta, também importa pensar em quem fica fora dos dados. Se a entrada viesse apenas de reclamações, como as do 156, um bairro que reclama menos poderia aparecer menos, mesmo sofrendo tantos alagamentos quanto outro. Ausência de dado não significa ausência de problema. Usar sensores no ponto reduz a dependência da reclamação, mas não elimina essa limitação: alguém escolhe onde instalar cada sensor, e os locais sem equipamento continuam sem representação direta.

### Por que apoiar a pessoa

O sistema não vê todo o lixo acumulado fora do alcance do sensor, uma obra na rua ou um evento que muda a circulação. Limpeza e visita consomem equipe, tempo e dinheiro, e definir prioridades exige conhecer o local. O alerta apresenta sensor, valor previsto e limite atingido para que a pessoa confira a situação e decida. As diferenças entre estratégias reforçam essa necessidade de avaliação. Nenhuma delas garante que haverá alagamento. Por isso o console lembra, ao fim de cada rodada, que a zeladoria ou a Defesa Civil deve conferir no local e decidir sobre limpeza ou visita.

## Uso de IA

A lógica do código foi escrita com apoio do Claude (Anthropic). Os textos, incluindo mensagens do console, comentários do código e este README, foram escritos com apoio do ChatGPT (OpenAI). A dupla revisou e estudou o código.

A revisão incluiu 38 testes automatizados rodando verdes; execução do menu com entrada por arquivo; teste de reinício para conferir a persistência; clone limpo seguindo este README; revisão do código contra a rubrica por uma segunda ferramenta de IA; e leitura e estudo do código pela dupla para a conversa de correção.

## Divisão de tarefas

[ROGER PREENCHE: quem fez o quê]
