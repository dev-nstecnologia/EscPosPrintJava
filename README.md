# EscPosPrintJava

O SampleNFCePrint foi desenvolvido para que, a partir de um XML ou um JSON de NFCe, seja criado um PDF ou array de bytes ESCPOS conseguindo fazer a utilização de uma mini-impressoras. 

## Utilizando JAR:
### Integrando ao seu sistema

Para utilizar as funções de impressão ou geração de PDF utilizando JARs, você precisa realizar os seguintes passos:

1. Extraia o conteúdo da pasta compactada que você baixou;
2. Abra a pasta 'JarsAndLibs', depois dentro terá o seguinte jar: **SampleNFCePrint-1.0-jar-with-dependencies.jar**.
3. Este JAR será utiliza em seu sistema, utilizando argumentos/parametros expecificos, para gerar PDF ou imprimir uma NFCe.

--------

### Como Utilizar o SampleNFCePrint:
#### Gerar PDF atraves do JAR com as dependecias:

Para realizar a geração de PDF a partir de uma NFCe, você poderá utilizar a função gerarPDF. Veja abaixo sobre os parâmetros necessários, e um exemplo de chamada do método.

##### Parâmetros:

Parametros     | Descrição
:-------------:|:-----------
CNPJInteressado | Conteúdo de emissão do documento.
tpEvento        | Tipo de evento posto na manifestação:<ul> <li>**210200** – Confirmação da Operação</li> <li>**210240** – Operação não Realizada</li> <li>**210220** – Desconhecimento da Operação</li> <li>**210210** – Ciência da Operação</li> </ul>
nsu             | Número Sequencial Único de um DF-e determinado
xJust           | Justificativa da manifestação (Informar somente quando o tpEvento for 210240)
chave           | Chave do DF-e que deseja-se manifestar


##### Exemplo de chamada:

Após ter todos os parâmetros listados acima, você deverá fazer a chamada da função. Veja o código de exemplo abaixo:
    
    //Por nsu
    String retorno = DDFeAPI.manifestacao("11111111111111", "210200", "134", "", "");
     
    //Por chave
    String retorno = DDFeAPI.manifestacao("11111111111111", "210200", "", "", "35160324110220000136550010000000351895912462");
     
    System.out.print(retorno);


A função manifestacao fará o envio da confirmação de participação do destinatário na operação acobertada pela Nota Fiscal Eletrônica, emitida para o seu CNPJ, para API.

---------

#### Gerar impressao para miniprinter atraves do JAR com as dependencias

---------

## Utilizando JAVA:
### Integrando ao seu sistema
### Como utilizar o Sample NFCe Print
