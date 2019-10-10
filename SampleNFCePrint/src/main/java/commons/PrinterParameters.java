package commons;

import schema.TNFe;
import type.DetailType;
import type.ProductsLines;
import type.YesNoType;

/**
 * Created by alissonlima on 30/08/15.
 */
public class PrinterParameters {

    private String printerName; //Nome da Impressora
    private TNFe nfceContent; //xml NFCe
    private boolean printLayoutIsNormal; //Tipo de layout
    private DetailType printDetail; //Normal ou ecologica
    private ProductsLines printProductsLines; //Linhas dos Prods
    private YesNoType printProductsDiscount; //Tem desconto na nota
    private YesNoType openDrawer;// abre a gaveta/caixa
    private String qrCodeContent; //conteudo QR code
    private String urlConsult;//Url para se fazer a consulta
    private String authorizationProtocol;//Protocolo que mostra se foi autorizado

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public boolean isPrintLayoutIsNormal() {
        return printLayoutIsNormal;
    }

    public void setPrintLayoutIsNormal(boolean printLayoutIsNormal) {
        this.printLayoutIsNormal = printLayoutIsNormal;
    }

    public TNFe getNfceContent() {
        return nfceContent;
    }

    public void setNfceContent(TNFe nfceContent) {
        this.nfceContent = nfceContent;
    }

    public DetailType getPrintDetail() {
        return printDetail;
    }

    public void setPrintDetail(DetailType printDetail) {
        this.printDetail = printDetail;
    }

    public ProductsLines getPrintProductsLines() {
        return printProductsLines;
    }

    public void setPrintProductsLines(ProductsLines printProductsLines) {
        this.printProductsLines = printProductsLines;
    }

    public YesNoType getPrintProductsDiscount() {
        return printProductsDiscount;
    }

    public void setPrintProductsDiscount(YesNoType printProductsDiscount) {
        this.printProductsDiscount = printProductsDiscount;
    }

    public YesNoType getOpenDrawer() {
        return openDrawer;
    }

    public void setOpenDrawer(YesNoType openDrawer) {
        this.openDrawer = openDrawer;
    }

    public String getQrCodeContent() {
        return qrCodeContent;
    }

    public void setQrCodeContent(String qrCodeContent) {
        this.qrCodeContent = qrCodeContent;
    }

    public String getUrlConsult() {
        return urlConsult;
    }

    public void setUrlConsult(String urlConsult) {
        this.urlConsult = urlConsult;
    }

    public String getAuthorizationProtocol() {
        return authorizationProtocol;
    }

    public void setAuthorizationProtocol(String authorizationProtocol) {
        this.authorizationProtocol = authorizationProtocol;
    }
}
