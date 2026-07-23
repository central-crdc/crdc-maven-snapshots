package br.com.crdc.f1.commons.cadastro.util;

public final class DocumentoMasker {

    private DocumentoMasker() {}

    private static final int CPF_LENGTH = 11;
    private static final int CNPJ_LENGTH = 14;

    public static String mask(String documento) {
        if (documento == null) {
            return "***";
        }
        String digits = documento.replaceAll("\\D", "");
        if (digits.length() == CPF_LENGTH) {
            String last2 = digits.substring(CPF_LENGTH - 2);
            return "***.***.***-" + last2;
        }
        if (digits.length() == CNPJ_LENGTH) {
            String branch = digits.substring(8, 12);
            String last2 = digits.substring(CNPJ_LENGTH - 2);
            return "**.***.***/" + branch + "-" + last2;
        }
        return "***";
    }
}
