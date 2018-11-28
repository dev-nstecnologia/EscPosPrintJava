/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package type;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by alissonlima on 9/22/15.
 */
@XmlType
@XmlEnum(Integer.class)
public enum ProductsLines {

    @XmlEnumValue("1") UMA_LINHA(1, "1 linha"),
    @XmlEnumValue("2") DUAS_LINHAS(2, "2 linhas"),
    @XmlEnumValue("3") LINHAS_DINAMICAS(3, "Linhas dinâmicas");

    private final int value;
    private final String description;

    ProductsLines(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

}
