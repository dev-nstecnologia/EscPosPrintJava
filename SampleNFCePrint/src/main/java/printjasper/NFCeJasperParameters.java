/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package printjasper;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author AlissonLima 
 */
public class NFCeJasperParameters{
    
    public String logoEmit;
    
    public boolean imprimeDetalhe;
        
    public String urlConsulta; //
    
    public InputStream qrCodeImage; //
    
    public String qrCodePath; //
    
    public boolean isProc; //

    public int printItemsLines;

    public boolean printItemDiscount;

    public int itemsCount;

    public String valorRec;

    public String valorTroco; //

    public boolean isConsumerTicket = true;

    public PAPERWIDTH paperWidth; //

    public int formatoLayoutImpressao = 1;

    /**
     * Creates a MAP with the value of the properties.
     * The map key are: LOGO_EMIT, IMPRIME_DETALHE, SUBREPORT_DIR, URL_CONSULTA,
     * QRCODE_IMAGE, QRCODE_PATH
     * @return
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap();

        map.put("LOGO_EMITENTE", logoEmit);
        map.put("IMPRIME_DETALHE", imprimeDetalhe);

        if (PAPERWIDTH.PAPER_58MM == paperWidth){
            map.put("SUBREPORT_DIR", "jasper_58/");

        } else{
            map.put("SUBREPORT_DIR", "jasper/");
        }

        map.put("URL_CONSULTA", urlConsulta);
        map.put("QRCODE_IMAGE", qrCodeImage);
        map.put("QRCODE_PATH", qrCodePath);
        map.put("IS_PROC", isProc);
        map.put("IMPRESSAO_PROD_LINHAS", printItemsLines);
        map.put("IMPRIME_DESC_ITEM", printItemDiscount);
        map.put("QUANT_ITENS", itemsCount);
        map.put("VALOR_REC", valorRec);
        map.put("VALOR_TROCO", valorTroco);
        map.put("IS_CONSUMER_TICKET", isConsumerTicket);
        map.put("FORMATO_LAYOUT_IMPRESSAO", formatoLayoutImpressao);
        return map;
    }

    @XmlType
    @XmlEnum(String.class)
    public enum PAPERWIDTH {
        @XmlEnumValue("80mm")PAPER_80MM("80mm"),
        @XmlEnumValue("58mm")PAPER_58MM("58mm");

        private final String name;

        PAPERWIDTH(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static PAPERWIDTH getByName(String name){
            for (PAPERWIDTH paperwidth : PAPERWIDTH.values()) {
                if (paperwidth.getName().equals(name)){
                    return paperwidth;
                }
            }
            return null;
        }
    }
}
