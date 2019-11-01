# EscPosPrintJava

O SampleNFCePrint foi desenvolvido para que, a partir de um XML ou um JSON de NFCe, seja criado um PDF ou array de bytes ESCPOS conseguindo fazer a utilização de uma mini-impressoras. 


## Utilizando JAR com as Dependencias:

### Integrando ao seu sistema

Para utilizar as funções de impressão ou geração de PDF utilizando JARs, você precisa realizar os seguintes passos:

1. Extraia o conteúdo da pasta compactada que você baixou;
2. Abra a pasta 'JarsAndLibs', depois dentro terá o seguinte jar: **SampleNFCePrint-1.0-jar-with-dependencies.jar**.
3. Este JAR será utiliza em seu sistema, utilizando argumentos/parametros expecificos, para gerar PDF ou imprimir uma NFCe.

### Gerar PDF atraves do JAR com as dependecias:

Para realizar a geração de PDF a partir de uma NFCe, você poderá utilizar a função gerarPDF. Veja abaixo sobre os parâmetros necessários, e um exemplo de chamada do método.

#### Parâmetros:

Parametros       | Descrição
:---------------:|:-----------
operacao         | Qual a operação que deve ser feita:<ul><li>**GERARPDFNFCE**</li></ul>
pathConteudo     | Caminho do arquivo que xml ou json da NFCe .
pathSave         | Caminho da pasta onde será salvo o PDF gerado
pathLogo         | Caminho do PNG do logo do emitente(*ainda não implementado, deve-se por o valor ""*)
paper            | Tamanho do papel de NFCe em mm:<ul><li>**58**</li><li>**80**</li></ul>
lines            | Números de linhas que os itens devem apresentar:<ul><li>**1**(Uma linha)</li><li>**2**(Duas linhas)</li><li>**3**(Linhas Dinâmicas)</li></ul>
hasDiscount      | Se terá disconto na nota ou não:<ul><li>**true**</li><li>**false**</li></ul>
hasDetail        | Se deve imprimir os itens da nota:<ul><li>**true**</li><li>**false**</li></ul>

#### Exemplo de chamada:
Após ter todos os parâmetros listados acima, você deverá fazer a chamada da função pelo cmd. Veja o exemplo abaixo:
    
    //Deve ser chamado assim pelo seu sistema
    C:\JarsAndLibs>java -jar SampleNFCePrint-1.0-jar-with-dependencies.jar GERARPDFNFCE C:\teste.xml C:\NFCePDFs "" 80 1 false true
     
A função de geração de PDF fará a converção do XML acima para um documento PDF com as configurações dispostas na linha de comando no exemplo anterior.

### Gerar impressao para miniprinter atraves do JAR com as dependencias
Para realizar a impressão atraves de uma mini-impressora a partir de um XML ou JSON NFCe, você poderá utilizar a função imprimirNFCe. Veja abaixo sobre os parâmetros necessários, e um exemplo de chamada do método.

#### Parâmetros:

            <item value="115200"/>
            <item value="128000"/>
            <item value="256000"/>
Parametros   | Descrição
:-----------:|:-----------
operacao     | Qual a operação que deve ser feita:<ul><li>**IMPRIMIRNFCE**</li></ul>
printer      | Nome da mini impressora utilizada, tendo como possibilidades: <ul><li>**"BEMATECH MP-4200 TH"**</li><li>**"BEMATECH MP-2500 TH"**</li><li>**"DARUMA DR700"**</li><li>**"EPSON T20"**</li><li>**"EPSON T70"**</li><li>**"ELGIN I9"**</li></ul>
port         | Qual porta COM será utilizada, por exemplo: "COM3"
portSpeed    | Velocidade da porta COM que utilizará: <ul><li>**110**</li><li>**300**</li><li>**600**</li><li>**1200**</li><li>**4800**</li><li>**9600**</li><li>**14400**</li><li>**19200**</li><li>**38400**</li><li>**57600**</li><li>**115200**</li><li>**128000**</li><li>**256000**</li></ul>
pathContent  | Caminho do conteudo do arquivo XML ou JSON
paper        | Tamanho do papel de NFCe em mm:<ul><li>**58**</li><li>**80**</li></ul>
lines        | Números de linhas que os itens devem apresentar:<ul><li>**1**(Uma linha)</li><li>**2**(Duas linhas)</li><li>**3**(Linhas Dinâmicas)</li></ul>
layout       | Se será uma impressão normal(true) ou ecologica(false)
hasDiscount  | Se terá disconto na nota ou não:<ul><li>**true**</li><li>**false**</li></ul>
hasDrawer    | Se deve abrir a registradora: <ul><li>**true**</li><li>**false**</li></ul>


#### Exemplo de chamada:

Após ter todos os parâmetros listados acima, você deverá fazer a chamada da função pelo cmd. Veja o exemplo abaixo:
    
    //Deve ser chamado assim pelo seu sistema
    C:\JarsAndLibs>java -jar SampleNFCePrint-1.0-jar-with-dependencies.jar IMPRIMIRNFCE "BEMATECH MP-4200 TH" "COM3" 19200 C:\teste.xml 80 1 true false false
     
A função de impressão de NFCe diretamente com uma mini-impressora fará a converção do XML para bytes(ESCPOS) enviando direto para impressão com as configurações dispostas na linha de comando no exemplo anterior.

---------

## Utilizando JAVA:
### Integrando ao seu sistema
#### Projeto Maven
#### Utilizando JARs

