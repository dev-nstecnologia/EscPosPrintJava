package type;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author matheus.mazzoni
 */
@XmlType
@XmlEnum(Integer.class)
public enum PrinterType {

    @XmlEnumValue("2") MINI_PRINTER;
}