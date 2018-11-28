package type;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author AlissonLima
 */
@XmlType
@XmlEnum(Integer.class)
public enum YesNoType {
    
    @XmlEnumValue("1") YES(true),
    @XmlEnumValue("0") NO(false);

    private final boolean booleanVal;

    YesNoType(boolean booleanVal) {
        this.booleanVal = booleanVal;
    }
}
