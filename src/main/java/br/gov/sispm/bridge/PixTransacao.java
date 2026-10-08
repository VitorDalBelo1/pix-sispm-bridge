package br.gov.sispm.bridge;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Registro do Leiaute SISPM / Plataforma Publica (487 posicoes). Ordem = ordem do layout. */
public record PixTransacao(
        String tipoTransacao,          //   1-2   Numerico
        String identificadorTransacao, //   3-37  e2eId / numCtrlSTBP
        BigDecimal vlPago,             //  38-54  17 posicoes, 2 casas implicitas
        BigDecimal vlCbsSegr,          //  55-71
        BigDecimal vlIbsSegr,          //  72-88
        String docFiscal,              //  89-138
        String agenciaDebitada,        // 139-142
        String produtoDebitada,        // 143-146
        String contaDebitada,          // 147-159
        String agenciaCreditada,       // 160-163
        String produtoCreditada,       // 164-167
        String contaCreditada,         // 168-180
        String compe,                  // 181-183
        String ispb,                   // 184-191
        String cnpjRec,                // 192-205
        String nomeRecebedor,          // 206-245
        String cnpjCpfPagEfet,         // 246-259
        String nomePagador,            // 260-299
        OffsetDateTime dtHrLiq,        // 300-324
        String cnpjRaizPspPag,         // 325-332
        OffsetDateTime dtHrPgto,       // 333-357
        String nsuOrigem,              // 358-389
        String ctxID,                  // 390-424
        String indPgtoIntegral,        // 425
        String nuStrOrig,              // 426-445
        String numPgto,                // 446-454
        String numIdentcBaixa,         // 455-473
        String cnpjRaizPspRecInd       // 474-487
) {}
