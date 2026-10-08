package br.gov.sispm.bridge;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Parser do layout posicional (posicoes 1-based, inclusivas, conforme planilha). */
@ApplicationScoped
public class PosicionalParser {

    public static final int TAMANHO = 487;
    private static final int CASAS_DECIMAIS = 2;

    /**
     * Uma mensagem Kafka pode ter 1..N registros: separados por quebra de linha
     * ou concatenados sem separador (multiplo de 487). Se qualquer registro for
     * invalido, a mensagem inteira e rejeitada (vai para a DLQ).
     */
    public List<PixTransacao> parseLote(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new LayoutInvalidoException("Mensagem vazia");
        }
        List<PixTransacao> out = new ArrayList<>();
        for (String linha : payload.split("\\R")) {
            if (linha.isEmpty()) continue;
            if (linha.length() % TAMANHO != 0) {
                throw new LayoutInvalidoException("Tamanho invalido: " + linha.length()
                        + " nao e multiplo de " + TAMANHO
                        + " (verifique aspas, espacos no fim da linha ou truncamento)");
            }
            for (int i = 0; i < linha.length(); i += TAMANHO) {
                out.add(parse(linha.substring(i, i + TAMANHO)));
            }
        }
        if (out.isEmpty()) {
            throw new LayoutInvalidoException("Mensagem sem registros");
        }
        return out;
    }

    public PixTransacao parse(String l) {
        if (l == null || l.length() != TAMANHO) {
            throw new LayoutInvalidoException("Tamanho invalido: esperado " + TAMANHO
                    + " mas recebido " + (l == null ? 0 : l.length()));
        }
        return new PixTransacao(
                txt(l, 1, 2),
                txt(l, 3, 37),
                valor(l, 38, 54),
                valor(l, 55, 71),
                valor(l, 72, 88),
                txt(l, 89, 138),
                txt(l, 139, 142),
                txt(l, 143, 146),
                txt(l, 147, 159),
                txt(l, 160, 163),
                txt(l, 164, 167),
                txt(l, 168, 180),
                txt(l, 181, 183),
                txt(l, 184, 191),
                txt(l, 192, 205),
                txt(l, 206, 245),
                txt(l, 246, 259),
                txt(l, 260, 299),
                data(l, 300, 324),
                txt(l, 325, 332),
                data(l, 333, 357),
                txt(l, 358, 389),
                txt(l, 390, 424),
                txt(l, 425, 425),
                txt(l, 426, 445),
                txt(l, 446, 454),
                txt(l, 455, 473),
                txt(l, 474, 487));
    }

    /** Campos texto/identificadores numericos: mantem zeros a esquerda, remove espacos. */
    private static String txt(String l, int ini, int fim) {
        String s = l.substring(ini - 1, fim).strip();
        return s.isEmpty() ? null : s;
    }

    private static BigDecimal valor(String l, int ini, int fim) {
        String s = l.substring(ini - 1, fim).strip();
        if (s.isEmpty()) return null;
        try {
            return new BigDecimal(new BigInteger(s), CASAS_DECIMAIS);
        } catch (NumberFormatException e) {
            throw new LayoutInvalidoException("Valor numerico invalido nas posicoes " + ini + "-" + fim + ": '" + s + "'", e);
        }
    }

    private static OffsetDateTime data(String l, int ini, int fim) {
        String s = l.substring(ini - 1, fim).strip();
        if (s.isEmpty()) return null;
        try {
            return OffsetDateTime.parse(s);
        } catch (DateTimeParseException e) {
            throw new LayoutInvalidoException("Timestamp invalido nas posicoes " + ini + "-" + fim + ": '" + s + "'", e);
        }
    }
}
