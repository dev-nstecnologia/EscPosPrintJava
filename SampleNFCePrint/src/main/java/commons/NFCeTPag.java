package commons;

/**
 *
 * @author Alisson Lima
 */
public enum NFCeTPag {
    
    DINHEIRO("01", "Dinheiro"),
    CHEQUE("02", "Cheque"),
    CARTAO_CREDITO("03", "Cartão de Crédito"),
    CARTAO_DEBITO("04", "Cartão de Débito"),
    CREDITO_LOJA("05", "Crédito Loja"),
    VALE_ALIMENTACAO("10", "Vale Alimentação"),
    VALE_REFEICAO("11", "Vale Refeição"),
    VALE_PRESENTE("12", "Vale Presente"),
    VALE_COMBUSTIVEL("13", "Vale Combustível"),
    BOLETO_BANCARIO("15", "Boleto Bancário"),
    SEM_PAGAMENTO("90", "Sem Pagamento"),
    OUTROS("99", "Outros");

    NFCeTPag(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    private final String code;
    private final String description;
    
    public static NFCeTPag getByCodigo(String code){
        for (NFCeTPag tPag : NFCeTPag.values()) {
            if (tPag.getCodigo().equals(code)) return tPag;
        }
        return null;        
    }

    public String getCodigo() {
        return code;
    }

    public String getDescricao() {
        return description;
    }
    
}
