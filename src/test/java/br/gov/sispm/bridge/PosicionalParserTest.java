package br.gov.sispm.bridge;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PosicionalParserTest {

    private final PosicionalParser parser = new PosicionalParser();

    @Test
    void parseiaPrimeiraLinhaDoMock() throws Exception {
        List<String> linhas = Files.readAllLines(Path.of("src/test/resources/mock_pix_estatico_01_comercio.txt"));
        PixTransacao t = parser.parse(linhas.get(0));

        assertEquals("01", t.tipoTransacao());
        assertEquals("E104000002026093011210001001", t.identificadorTransacao());
        assertEquals(new BigDecimal("65.50"), t.vlPago());
        assertEquals(new BigDecimal("0.55"), t.vlCbsSegr());
        assertEquals(new BigDecimal("0.60"), t.vlIbsSegr());
        assertEquals("NF-EST-010001", t.docFiscal());
        assertEquals("0098765432101", t.contaDebitada());
        assertEquals("00360305", t.ispb());
        assertEquals("RECEBEDOR PIX ESTATICO 1", t.nomeRecebedor());
        assertEquals("S", t.indPgtoIntegral());
        assertEquals(-3 * 3600, t.dtHrLiq().getOffset().getTotalSeconds());
    }

    @Test
    void todasAsLinhasDoMockSaoValidas() throws Exception {
        for (String l : Files.readAllLines(Path.of("src/test/resources/mock_pix_estatico_01_comercio.txt"))) {
            assertDoesNotThrow(() -> parser.parse(l));
        }
    }

    @Test
    void rejeitaTamanhoInvalido() {
        assertThrows(LayoutInvalidoException.class, () -> parser.parse("01ABC"));
    }

    @Test
    void loteComQuebrasDeLinha() throws Exception {
        String arquivo = Files.readString(Path.of("src/test/resources/mock_pix_estatico_01_comercio.txt"));
        List<PixTransacao> r = parser.parseLote(arquivo);
        assertEquals(5, r.size());
        assertEquals(new BigDecimal("115.50"), r.get(4).vlPago());
    }

    @Test
    void loteSemSeparador() throws Exception {
        String junto = String.join("", Files.readAllLines(Path.of("src/test/resources/mock_pix_estatico_01_comercio.txt")));
        assertEquals(2435, junto.length());
        assertEquals(5, parser.parseLote(junto).size());
    }

    @Test
    void rejeitaLoteComAspas() throws Exception {
        String l = Files.readAllLines(Path.of("src/test/resources/mock_pix_estatico_01_comercio.txt")).get(0);
        assertThrows(LayoutInvalidoException.class, () -> parser.parseLote("\"" + l + "\""));
    }
}
