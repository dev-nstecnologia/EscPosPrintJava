package commons;

import br.eti.ns.nsminiprinters.escpos.PrinterOptions;
import br.eti.ns.nsminiprinters.escpos.specs.PrinterSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import type.DetailType;
import type.YesNoType;
import type.ProductsLines;

import formatter.FormatterAll;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import schema.*;
import schema.TNFe.InfNFe.*;
import schema.TNFe.InfNFe.Det.Prod;
import schema.TNFe.InfNFe.InfAdic.ObsCont;
import schema.TNFe.InfNFe.Pag.DetPag;
import schema.TNFe.InfNFe.Pag.DetPag.Card;
import schema.TNFe.InfNFe.Total.ISSQNtot;

/**
 *
 * @author matheus.mazzoni
 */

public class CreatePrint {
    
    private final PrinterSpec printerSpec;
    private final PrinterOptions printer;
    private final String groupSeparator;
    private final Locale BR_LOCALE = new Locale("pt", "BR");
    protected final PrinterParameters parameters;

    public CreatePrint(PrinterSpec printerSpec, PrinterOptions printer, PrinterParameters parameters) {
        this.printerSpec = printerSpec;
        this.printer = printer;
        this.groupSeparator = StringUtils.leftPad("", printerSpec.getNormalColumnsCount(), "-").concat("\r\n");
        this.parameters = parameters;
    }

    
    public byte[] createNFePrint(NFCeVia via) throws Exception {

        TNFe ncfe = parameters.getNfceContent();
        
        boolean printLayoutIsNormal = parameters.isPrintLayoutIsNormal();
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write(printerSpec.getInitialPrinterSetup());

        appendHeaderContent(ncfe, stream, printLayoutIsNormal);

        if (parameters.getPrintDetail() == DetailType.NORMAL){
            appendItensContent(ncfe, stream);
        }

        appendCountersContent(ncfe, stream, printLayoutIsNormal);

        appendConsChaveAcesso(ncfe, parameters.getUrlConsult(), stream, printLayoutIsNormal);

        appendCostumerContent(ncfe, stream, printLayoutIsNormal);

        appendAditionalInfoContent(ncfe, via, stream, printLayoutIsNormal);

        appendProtocoloAutorizacao(parameters.getAuthorizationProtocol(), stream, printLayoutIsNormal);

        appendContingenciaMens(ncfe, stream, printLayoutIsNormal);

        appendQrCodeContent(parameters.getQrCodeContent(), stream, printLayoutIsNormal);

        appendInfAdFiscoContent(ncfe, stream, printLayoutIsNormal);

        appendHomologacaoMens(ncfe, stream);

        appendComplementInfoContent(ncfe, stream, printLayoutIsNormal);

        for (int i = 0; i < printerSpec.getEndBreaklinesCount(); i++) {
            stream.write(PrinterSpec.NEW_LINE_BYTES);
        }

        stream.write(printerSpec.getCutPaper());

        if (parameters.getOpenDrawer() == YesNoType.YES){
            stream.write(printerSpec.getOpenDrawerBytes());
        }

        return stream.toByteArray();

    }

    public byte[] getGroupSeparatorBytes(){
        return groupSeparator.getBytes();
    }
    
    private void appendHeaderContent(TNFe nfe, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        stream.write(printerSpec.getCenterTextOn());
        appendCompanyInfoContent(nfe, stream, isNormalLayout);
        if(isNormalLayout) {
            stream.write(getGroupSeparatorBytes());
        }
        appendRequiredInfoContent(stream, isNormalLayout);
        stream.write(printerSpec.getCenterTextOff());
        appendContingenciaMens(nfe, stream, isNormalLayout);

    }
    
    private void appendCompanyInfoContent(TNFe nfe, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOn());
        }
        stream.write(printerSpec.getExpandedOn());

        Emit emit = nfe.getInfNFe().getEmit();

        String xFant = emit.getXFant();
        if(StringUtils.isNotBlank(xFant)) {
            stream.write(printerSpec.stringToEncodedBytes(WordUtils.wrap(xFant.toUpperCase().concat("\r\n"), printerSpec.getExpandedColumnsCount())));

        }else {
            stream.write(printerSpec.stringToEncodedBytes(WordUtils.wrap(emit.getXNome().toUpperCase().concat("\r\n"), printerSpec.getExpandedColumnsCount())));
        }
        stream.write(printerSpec.getExpandedOff());

        String cnpj = FormatterAll.formatCnpjString(emit.getCNPJ());
        String ie = emit.getIE();

        stream.write(String.format("CNPJ: %1s IE: %1s\r\n", cnpj, ie).getBytes());

        TEnderEmi enderEmi = emit.getEnderEmit();

        String endereco = String.format("%1s, %1s, %1s, %1s - %1s",
                enderEmi.getXLgr(),
                enderEmi.getNro(),
                enderEmi.getXBairro(),
                enderEmi.getXMun(),
                enderEmi.getUF().name());

        String emitFone = enderEmi.getFone();
        if (StringUtils.isNotBlank(emitFone)){
            try {
                emitFone = FormatterAll.formatFoneNumber(emitFone);
            } catch (Exception e){}
            endereco += " - " + emitFone;
        }

        endereco = WordUtils.wrap(endereco, printerSpec.getNormalColumnsCount());

        stream.write(printerSpec.stringToEncodedBytes(endereco + "\r\n"));
        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOff());
        }
    }
    
    private void appendRequiredInfoContent(ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        stream.write(printerSpec.getCondensedOn());
        if (!isNormalLayout) {
            stream.write(printerSpec.stringToEncodedBytes(WordUtils.wrap("Doc Auxiliar da Nota Fiscal de Consumidor Eletrônica\r\n", printerSpec.getCondensedColumnsCount())));
        }else{
            stream.write(printerSpec.stringToEncodedBytes(WordUtils.wrap("Documento Auxiliar da Nota Fiscal de Consumidor Eletrônica\r\n", printerSpec.getCondensedColumnsCount())));
        }
        stream.write(printerSpec.getCondensedOff());
    }


    
    private void appendContingenciaMens(TNFe nfe, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        if (!nfe.getInfNFe().getIde().getTpEmis().equals("1")){
            if(!isNormalLayout){
                stream.write(printerSpec.getCondensedOn());
            }
            stream.write(printerSpec.getCenterTextOn());
            stream.write(printerSpec.getBoldOn());
            stream.write(printerSpec.stringToEncodedBytes("EMITIDA EM CONTINGÊNCIA\r\n"));
            stream.write(printerSpec.stringToEncodedBytes("Pendente de autorização\r\n"));
            stream.write(printerSpec.getBoldOff());
            stream.write(printerSpec.getCenterTextOff());
            if(!isNormalLayout){
                stream.write(printerSpec.getCondensedOff());
            }
        }
    }
    
    private void appendItensContent(TNFe nfe, ByteArrayOutputStream stream) throws IOException {
        appendItensHeaderContent(stream);

        appendItensDetailsContent(nfe, stream);
    }
    
    private void appendItensHeaderContent(ByteArrayOutputStream stream) throws IOException {
        stream.write(getGroupSeparatorBytes());
        stream.write(printerSpec.getCondensedOn());
        stream.write(printerSpec.stringToEncodedBytes(getItensHeader()));
        stream.write(printerSpec.getCondensedOff());
        stream.write(getGroupSeparatorBytes());
    }

    public String getItensHeader() {
        if ((parameters.getPrintProductsLines() == ProductsLines.DUAS_LINHAS)
                || (printer.paperWidth == PrinterOptions.PAPERWIDTH.PAPER_58MM)){
            String header = "Codigo         Descricao\r\n";

            String spaces = StringUtils.rightPad("", printerSpec.getCondensedColumnsCount() - 33);
            header += spaces + "Qtd   Un    x      VlUn     VlTot\r\n";

            return header;

        }else {
            String spaces = StringUtils.rightPad("", printerSpec.getCondensedColumnsCount() - 39);

            return "Codigo Descricao" + spaces + "Qtd Un     VlUn   VlTot\r\n";
        }
    }
    
    private void appendItensDetailsContent(TNFe nfe, ByteArrayOutputStream stream) throws IOException {
        stream.write(printerSpec.getCondensedOn());

        List<Det> detList = nfe.getInfNFe().getDet();
        StringBuilder text = new StringBuilder();

        for (Det det : detList) {
            Prod product = det.getProd();
            text.append(getItemContent(product));
        }

        stream.write(printerSpec.stringToEncodedBytes(text.toString()));
        stream.write(printerSpec.getCondensedOff());
    }

    public String getItemContent(Prod product) {
        StringBuilder text = new StringBuilder();

        if ((parameters.getPrintProductsLines() == ProductsLines.DUAS_LINHAS)
                || (printer.paperWidth == PrinterOptions.PAPERWIDTH.PAPER_58MM)) {
            appendItemContentTwoLines(product, text);
        }else if(parameters.getPrintProductsLines() == ProductsLines.LINHAS_DINAMICAS){
            if(product.getXProd().length() > (printerSpec.getCondensedColumnsCount() - 33)){
                appendItemContentTwoLinesDinamic(product, text);
            }else{
                appendItemContentOneLine(product, text);
            }

        } else {
            appendItemContentOneLine(product, text);

        }
        appendItemDiscountIfEnabled(product, text);
        text.append("\r\n");

        return text.toString();

    }

    private void appendItemContentOneLine(Prod product, StringBuilder text) {
        text.append(StringUtils.leftPad(StringUtils.right(product.getCProd(), 6), 6)).append(" ");

        int xProdLength = printerSpec.getCondensedColumnsCount() - 33;
        text.append(StringUtils.rightPad(StringUtils.left(product.getXProd(), xProdLength), xProdLength));

        String qCom = FormatterAll.convertToFormat(product.getQCom(), "###0.####", BR_LOCALE);
        text.append(StringUtils.leftPad(StringUtils.right(qCom, 5), 6)).append(" ");

        text.append(StringUtils.left(product.getUCom(), 2)).append(" ");
        String vUnCom = FormatterAll.convertToFormat(product.getVUnCom(), "###0.00", BR_LOCALE);
        String vProd = FormatterAll.convertToFormat(product.getVProd(), "###0.00", BR_LOCALE);

        text.append(StringUtils.leftPad(StringUtils.right(vUnCom, 8), 8));
        text.append(StringUtils.leftPad(StringUtils.right(vProd, 8), 8));

    }

    private void appendItemContentTwoLines(Prod product, StringBuilder text) {
        text.append(StringUtils.rightPad(StringUtils.right(product.getCProd(), 14), 14)).append(" ");

        int xProdLength = printerSpec.getCondensedColumnsCount() - 15;
        text.append(StringUtils.rightPad(StringUtils.left(product.getXProd(), xProdLength),xProdLength));
        text.append("\r\n");

        text.append(StringUtils.rightPad("", printerSpec.getCondensedColumnsCount() - 37));

        String qCom = FormatterAll.convertToFormat(product.getQCom(), "###0.####", BR_LOCALE);
        text.append(StringUtils.leftPad(StringUtils.right(qCom, 7), 7)).append("   ");

        text.append(StringUtils.left(product.getUCom(), 2)).append("    x");
        String vUnCom = FormatterAll.convertToFormat(product.getVUnCom(), "###0.00", BR_LOCALE);
        String vProd = FormatterAll.convertToFormat(product.getVProd(), "###0.00", BR_LOCALE);

        text.append(StringUtils.leftPad(StringUtils.right(vUnCom, 10), 10));
        text.append(StringUtils.leftPad(StringUtils.right(vProd, 10), 10));
    }

    private void appendItemContentTwoLinesDinamic(Prod product, StringBuilder text) {
        text.append(StringUtils.leftPad(StringUtils.right(product.getCProd(), 6), 6)).append(" ");

        int xProdLength = printerSpec.getCondensedColumnsCount() - 7;
        text.append(StringUtils.rightPad(StringUtils.left(product.getXProd(), xProdLength),xProdLength));
        text.append("\r\n");

        text.append(StringUtils.rightPad("", printerSpec.getCondensedColumnsCount() - 26));

        String qCom = FormatterAll.convertToFormat(product.getQCom(), "###0.####", BR_LOCALE);
        text.append(StringUtils.leftPad(StringUtils.right(qCom, 5), 6)).append(" ");

        text.append(StringUtils.left(product.getUCom(), 2)).append(" ");
        String vUnCom = FormatterAll.convertToFormat(product.getVUnCom(), "###0.00", BR_LOCALE);
        String vProd = FormatterAll.convertToFormat(product.getVProd(), "###0.00", BR_LOCALE);

        text.append(StringUtils.leftPad(StringUtils.right(vUnCom, 8), 8));
        text.append(StringUtils.leftPad(StringUtils.right(vProd, 8), 8));
    }

    private void appendItemDiscountIfEnabled(Prod product, StringBuilder text) {
        if ((parameters.getPrintProductsDiscount() == YesNoType.YES) && (product.getVDesc() != null) && (Double.parseDouble(product.getVDesc()) > 0)){
            text.append("\r\n");
            text.append("DESCONTO");

            String vDesc = "-" + StringUtils.left(FormatterAll.convertToFormat(product.getVDesc(), "###0.00", BR_LOCALE), 9);
            text.append(StringUtils.leftPad(vDesc, printerSpec.getCondensedColumnsCount() - 8));
        }
    }

    private void appendCountersContent(TNFe nfe, ByteArrayOutputStream stream, boolean isNormalLayout) throws ParseException, IOException {
        stream.write(getGroupSeparatorBytes());
        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOn());
        }

        StringBuilder text = new StringBuilder();

        text.append(StringUtils.rightPad("QTD. TOTAL DE ITENS", printerSpec.getNormalColumnsCount() - 4));

        text.append(StringUtils.leftPad(String.valueOf(nfe.getInfNFe().getDet().size()), 4, "0"));
        text.append("\r\n");

        text.append("VALOR DOS PRODUTOS");
        double vProdServ = Double.parseDouble(nfe.getInfNFe().getTotal().getICMSTot().getVProd());

        ISSQNtot issqNtot = nfe.getInfNFe().getTotal().getISSQNtot();
        if (issqNtot != null){
            vProdServ += Double.parseDouble(issqNtot.getVServ());
        }

        String formattedVProd = FormatterAll.convertToFormat(String.valueOf(vProdServ), "#,##0.00", BR_LOCALE);
        text.append(StringUtils.leftPad(formattedVProd, printerSpec.getNormalColumnsCount() - 18));
        text.append("\r\n");

        if(!nfe.getInfNFe().getTotal().getICMSTot().getVDesc().equals("0.00")) {
            text.append("DESCONTO");
            String formattedVDesc = FormatterAll.convertToFormat(nfe.getInfNFe().getTotal().getICMSTot().getVDesc(), "#,##0.00", BR_LOCALE);
            text.append(StringUtils.leftPad(formattedVDesc, printerSpec.getNormalColumnsCount() - 8));
            text.append("\r\n");
        }

        Double outrasDespesas = Double.parseDouble(nfe.getInfNFe().getTotal().getICMSTot().getVST())
                + Double.parseDouble(nfe.getInfNFe().getTotal().getICMSTot().getVST())
                + Double.parseDouble(nfe.getInfNFe().getTotal().getICMSTot().getVFrete())
                + Double.parseDouble(nfe.getInfNFe().getTotal().getICMSTot().getVSeg())
                + Double.parseDouble(nfe.getInfNFe().getTotal().getICMSTot().getVOutro());
        if(outrasDespesas > 0) {
            text.append("OUTRAS DESPESAS");
            text.append(StringUtils.leftPad(FormatterAll.convertToFormat(outrasDespesas.toString(), "#,##0.00", BR_LOCALE), printerSpec.getNormalColumnsCount() - 15));
            text.append("\r\n");
        }

        if(!nfe.getInfNFe().getTotal().getICMSTot().getVNF().equals("0.00")) {
            text.append("VALOR TOTAL R$");
            String formattedVNF = FormatterAll.convertToFormat(nfe.getInfNFe().getTotal().getICMSTot().getVNF(), "#,##0.00", BR_LOCALE);
            text.append(StringUtils.leftPad(formattedVNF, printerSpec.getNormalColumnsCount() - 14));
            text.append("\r\n");
        }

        if(!isNormalLayout){
            appendPaymentModesContent(nfe, text, isNormalLayout);
            stream.write(printerSpec.getCondensedOff());
        }else{
            appendPaymentModesContent(nfe, text, isNormalLayout);
        }

        appendRecebidoAndTrocoIfSet(nfe, text);

        stream.write(printerSpec.stringToEncodedBytes(text.toString()));

    }

    private void appendRecebidoAndTrocoIfSet(TNFe nfe, StringBuilder text) {
        InfAdic infAdic = nfe.getInfNFe().getInfAdic();
        if (infAdic != null && !infAdic.getObsCont().isEmpty()){

            String valorRec = null, valorTroco = null;
            for (ObsCont obsCont : infAdic.getObsCont()) {
                if ("valorRec".equals(obsCont.getXCampo())){
                    valorRec = obsCont.getXTexto();
                } else if ("valorTroco".equals(obsCont.getXCampo())){
                    valorTroco = obsCont.getXTexto();
                }
            }

            if (valorRec != null && valorTroco != null){
                text.append("VALOR RECEBIDO");
                text.append(StringUtils.leftPad(valorRec, printerSpec.getNormalColumnsCount() - 14)).append("\r\n");
                text.append("TROCO");
                text.append(StringUtils.leftPad(valorTroco, printerSpec.getNormalColumnsCount() - 5)).append("\r\n");
            }
        }
    }

    private void appendPaymentModesContent(TNFe nfe, StringBuilder text, boolean isNormalLayout) throws UnsupportedEncodingException {
        if(isNormalLayout) {
            text.append(groupSeparator);
        }
        text.append(getPaymentsModesHeader());

        if ("3.10".equals(nfe.getInfNFe().getVersao())){
            appendV310Payment(nfe, text);
        } else {
            appendVGreaterthan310Payment(nfe, text);
        }

    }

    private void appendV310Payment(TNFe nfe, StringBuilder text) {
        
        for (DetPag detPag : nfe.getInfNFe().getPag().getDetPag()) {
          
            String formaPagto = NFCeTPag.getByCodigo(detPag.getTPag()).getDescricao();

            if (detPag.getCard() != null){
                Card card = detPag.getCard();
                NFCeTBand nfceTBand = NFCeTBand.getByCodigo(card.getTBand());

                if (nfceTBand != null && nfceTBand != NFCeTBand.OUTROS){
                    formaPagto += " - " + NFCeTBand.getByCodigo(card.getTBand()).getDescricao();
                }
            }

            text.append(StringUtils.rightPad(StringUtils.left(formaPagto, 32), printerSpec.getNormalColumnsCount() - 14));
            String formattedVPag = FormatterAll.convertToFormat(detPag.getVPag(), "#,##0.00", BR_LOCALE);
            text.append(StringUtils.leftPad(formattedVPag, 14)).append("\r\n");
        }
    }

    private void appendVGreaterthan310Payment(TNFe nfe, StringBuilder text) {
        
        for (DetPag detPag : nfe.getInfNFe().getPag().getDetPag()) {
            String formaPagto = NFCeTPag.getByCodigo(detPag.getTPag()).getDescricao();

            if (detPag.getCard() != null){
                Card card = detPag.getCard();
                NFCeTBand nfceTBand = NFCeTBand.getByCodigo(card.getTBand());

                if (nfceTBand != null && nfceTBand != NFCeTBand.OUTROS){
                    formaPagto += " - " + NFCeTBand.getByCodigo(card.getTBand()).getDescricao();
                }
            }

            text.append(StringUtils.rightPad(StringUtils.left(formaPagto, 32), printerSpec.getNormalColumnsCount() - 14));
            String formattedVPag = FormatterAll.convertToFormat(detPag.getVPag(), "#,##0.00", BR_LOCALE);
            text.append(StringUtils.leftPad(formattedVPag, 14)).append("\r\n");
        }
    }

    public String getPaymentsModesHeader(){
        return StringUtils.rightPad("FORMAS DE PAGAMENTO", printerSpec.getNormalColumnsCount() - 10)
                .concat("VALOR PAGO\r\n");

    }

    private void appendInfAdFiscoContent(TNFe nfe, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException{
        if (nfe.getInfNFe().getInfAdic() != null){
            String infAdFisco = nfe.getInfNFe().getInfAdic().getInfAdFisco();
            if (StringUtils.isNotBlank(infAdFisco)){
                if(!isNormalLayout){
                    stream.write(printerSpec.getCondensedOn());
                }
                stream.write("\r\n".getBytes());
                stream.write(printerSpec.stringToEncodedBytes(infAdFisco.concat("\r\n")));
                if(!isNormalLayout){
                    stream.write(printerSpec.getCondensedOff());
                }
            }
        }
    }

    private void appendHomologacaoMens(TNFe nfe, ByteArrayOutputStream stream) throws IOException {
        if (nfe.getInfNFe().getIde().getTpAmb().equals("2")){
            stream.write(printerSpec.getCenterTextOn());
            stream.write(printerSpec.getCondensedOn());
            stream.write(printerSpec.getBoldOn());
            stream.write(printerSpec.stringToEncodedBytes(WordUtils.wrap("EMITIDA EM AMBIENTE DE HOMOLOGAÇÃO - SEM VALOR FISCAL\r\n", printerSpec.getNormalColumnsCount())));
            stream.write(printerSpec.getBoldOff());
            stream.write(printerSpec.getCondensedOff());
            stream.write(printerSpec.getCenterTextOff());
        }
    }

    private void appendComplementInfoContent(TNFe nfe, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException{
        appendTributesContent(nfe, stream);

        if (nfe.getInfNFe().getInfAdic() != null){
            String infCpl = nfe.getInfNFe().getInfAdic().getInfCpl();
            if (StringUtils.isNotBlank(infCpl)){
                if(!isNormalLayout){
                    stream.write(printerSpec.getCondensedOn());
                }
                stream.write("\r\n".getBytes());
                String wrapedText = Arrays.stream(infCpl.split("#BR#"))
                        .map(line -> WordUtils.wrap(line, printerSpec.getNormalColumnsCount()).concat("\r\n"))
                        .collect(Collectors.joining());
                stream.write(printerSpec.stringToEncodedBytes(wrapedText.concat("\r\n")));
                if(!isNormalLayout){
                    stream.write(printerSpec.getCondensedOn());
                }
            }
        }

    }

    private void appendTributesContent(TNFe nfe, ByteArrayOutputStream stream) throws IOException {
        stream.write("\r\n".getBytes());
        stream.write(printerSpec.getCondensedOn());
        stream.write(printerSpec.stringToEncodedBytes(WordUtils.wrap("Inf. dos Trib. Totais Incidentes (Lei Federal 12.741/2012) R$ ", printerSpec.getCondensedColumnsCount())));

        String vTrib = nfe.getInfNFe().getTotal().getICMSTot().getVTotTrib();
        if (StringUtils.isNotBlank(vTrib)){
            stream.write(FormatterAll.convertToFormat(vTrib, "#,##0.00", BR_LOCALE).getBytes());
        }else{
            stream.write("0.00".getBytes());
        }
        stream.write("\r\n".getBytes());
        stream.write(printerSpec.getCondensedOff());
    }

    private void appendAditionalInfoContent(TNFe nfe, NFCeVia via, ByteArrayOutputStream stream, boolean isNormalLayout) throws ParseException, IOException {
        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOn());
        }else{
            stream.write(getGroupSeparatorBytes());
        }

        stream.write(printerSpec.getCenterTextOn());

        String numNFCe = nfe.getInfNFe().getIde().getNNF();
        String serie = nfe.getInfNFe().getIde().getSerie();
        String emissao = FormatterAll.dataSefazToTimestampBRString(nfe.getInfNFe().getIde().getDhEmi());

        stream.write(printerSpec.stringToEncodedBytes(WordUtils.wrap(String.format("Número:%1s Série:%1s Emissão:%1s\n", numNFCe, serie, emissao), printerSpec.getNormalColumnsCount())));

        if (via == NFCeVia.CONSUMIDOR){
            stream.write("Via - Consumidor\r\n".getBytes());
        }else{
            stream.write("Via - Estabelecimento\r\n".getBytes());
        }

        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOff());
        }
    }

    protected void appendProtocoloAutorizacao(String protocolo, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        if (StringUtils.isNotBlank(protocolo)) {
            if(!isNormalLayout){
                stream.write(printerSpec.getCondensedOn());
                stream.write(printerSpec.getCenterTextOn());
                stream.write(printerSpec.stringToEncodedBytes("Protocolo de Autorização: "));
                stream.write(WordUtils.wrap(protocolo, printerSpec.getNormalColumnsCount()).concat("\r\n").getBytes());
                stream.write(printerSpec.getCenterTextOff());
                stream.write(printerSpec.getCondensedOff());
            }else{
                stream.write(printerSpec.getCenterTextOn());
                stream.write(printerSpec.stringToEncodedBytes("Protocolo de Autorização\r\n"));
                stream.write(WordUtils.wrap(protocolo, printerSpec.getNormalColumnsCount()).concat("\r\n").getBytes());
                stream.write(printerSpec.getCenterTextOff());
            }

        }
    }

    private void appendConsChaveAcesso(TNFe nfe, String urlConsulta, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        stream.write(getGroupSeparatorBytes());

        stream.write(printerSpec.getCondensedOn());
        stream.write(printerSpec.getCenterTextOn());

        stream.write(String.format("Consulte pela Chave de Acesso em:\r\n%1s\r\n", urlConsulta).getBytes());

        String chave = FormatterAll.formatChaveAcessoWithSpaces(nfe.getInfNFe().getId().substring(3));
        stream.write(WordUtils.wrap(chave.concat("\r\n"), printerSpec.getCondensedColumnsCount()).getBytes());

        stream.write(printerSpec.getCenterTextOff());
        stream.write(printerSpec.getCondensedOff());
    }

    private void appendCostumerContent(TNFe nfe, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOn());
        }else{
            stream.write(getGroupSeparatorBytes());
        }

        Dest dest = nfe.getInfNFe().getDest();

        stream.write(printerSpec.getCenterTextOn());
        if (dest != null){
            stream.write("CONSUMIDOR\r\n".getBytes());

            if (StringUtils.isNotBlank(dest.getXNome())){
                stream.write(printerSpec.stringToEncodedBytes(dest.getXNome().concat("\r\n")));
            }

            if (StringUtils.isNotBlank(dest.getIdEstrangeiro())){
                stream.write("ID Estrangeiro: ".concat(dest.getIdEstrangeiro()).concat("\r\n").getBytes());
            }else if (StringUtils.length(dest.getCNPJ()) == 14){
                stream.write("CNPJ: ".concat(FormatterAll.formatCnpjString(dest.getCNPJ())).concat("\r\n").getBytes());
            }else {
                stream.write("CPF: ".concat(dest.getCPF()).concat("\r\n").getBytes());
            }

            if (dest.getEnderDest() != null){
                appendEnderDest(dest, stream);
            }

        } else {
            stream.write("CONSUMIDOR NAO IDENTIFICADO\r\n".getBytes());
        }
        stream.write(printerSpec.getCenterTextOff());
        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOff());
        }
    }

    private void appendEnderDest(Dest dest, ByteArrayOutputStream stream) throws IOException {
        TEndereco enderDest = dest.getEnderDest();
        StringBuilder enderDestStr = new StringBuilder();

        if (StringUtils.isNotBlank(enderDest.getXLgr())){
            enderDestStr.append(enderDest.getXLgr());
        }

        if (StringUtils.isNotBlank(enderDest.getNro())){
            enderDestStr.append(", ").append(enderDest.getNro());
        }

        if (StringUtils.isNotBlank(enderDest.getXBairro())){
            enderDestStr.append(", ").append(enderDest.getXBairro());
        }

        if (StringUtils.isNotBlank(enderDest.getXMun())){
            enderDestStr.append(", ").append(enderDest.getXMun());
        }

        if (StringUtils.isNotBlank(enderDest.getUF().name())){
            enderDestStr.append(" - ").append(enderDest.getUF().name());
        }

        if (enderDestStr.length() > 0){
            enderDestStr.append("\r\n");
            stream.write(printerSpec.stringToEncodedBytes(enderDestStr.toString()));
        }
    }

    private void appendQrCodeContent(String qrCodeString, ByteArrayOutputStream stream, boolean isNormalLayout) throws IOException {
        stream.write(getGroupSeparatorBytes());
        stream.write(printerSpec.getCenterTextOn());
        if(!isNormalLayout){
            stream.write(printerSpec.getCondensedOn());
            stream.write("Consulta via leitor de QR Code\r\n\r\n".getBytes());
            stream.write(printerSpec.getCondensedOff());
        }else{
            stream.write("Consulta via leitor de QR Code\r\n\r\n".getBytes());
        }
        if(qrCodeString.contains("?p=")){
            qrCodeString = StringUtils.rightPad(qrCodeString, 330);
        }
        stream.write(printerSpec.getQrCodeBytes(qrCodeString));
        stream.write(printerSpec.getCenterTextOff());
        stream.write("\r\n".getBytes());
    }

}

