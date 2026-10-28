package com.app.gerador_de_orcamento.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.app.gerador_de_orcamento.dto.OrcamentoResponseDTO;
import com.app.gerador_de_orcamento.dto.PrestadorServicoResponseDTO;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

@Component
public class PdfGenerator {

    // 📥 PEGA O CAMINHO QUE VOCÊ DEFINIU NO application.properties (com valor padrão de fallback)
    @Value("${app.pdf.storage-path:./orcamentos}")
    private String PASTA_ORCAMENTOS;

    private static final NumberFormat MOEDA_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter NOME_DATA_HORA_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");

    public byte[] gerarOrcamentoPdf(OrcamentoResponseDTO orcamento, PrestadorServicoResponseDTO prestador) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(documento, baos);
        documento.open();

        // --- FONTES ---
        Font fonteTitulo = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font fonteDados = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Font fonteDadosNegrito = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

        // CABEÇALHO
        Paragraph tituloEmpresa = new Paragraph(prestador.getRazaoSocial(), fonteTitulo);
        tituloEmpresa.setAlignment(Element.ALIGN_CENTER);
        documento.add(tituloEmpresa);

        if (prestador.getNomeFantasia() != null) {
            Paragraph nomeFantasia = new Paragraph(prestador.getNomeFantasia(), new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            nomeFantasia.setAlignment(Element.ALIGN_CENTER);
            documento.add(nomeFantasia);
        }

        Paragraph dadosEmpresa = new Paragraph(
                "CNPJ: " + prestador.getCnpj() +
                " | Tel: " + prestador.getTelefone() +
                " | E-mail: " + prestador.getEmail() + "\n" +
                "Endereço: " + prestador.getEndereco(),
                fonteDados
        );
        dadosEmpresa.setAlignment(Element.ALIGN_CENTER);
        dadosEmpresa.setSpacingAfter(20);
        documento.add(dadosEmpresa);

        LineSeparator linha = new LineSeparator();
        linha.setPercentage(100);
        documento.add(linha);
        documento.add(new Paragraph("\n"));

        Paragraph tituloOrcamento = new Paragraph("ORÇAMENTO Nº " + orcamento.getId(), fonteTitulo);
        tituloOrcamento.setAlignment(Element.ALIGN_CENTER);
        tituloOrcamento.setSpacingAfter(15);
        documento.add(tituloOrcamento);

        Paragraph dataOrcamento = new Paragraph("Emitido em: " + orcamento.getDataCriacao().format(DATA_FORMAT), fonteDadosNegrito);
        dataOrcamento.setAlignment(Element.ALIGN_RIGHT);
        dataOrcamento.setSpacingAfter(20);
        documento.add(dataOrcamento);

        // DADOS DO CLIENTE
        PdfPTable tabelaCliente = new PdfPTable(2);
        tabelaCliente.setWidthPercentage(100);
        tabelaCliente.setSpacingAfter(20);
        tabelaCliente.setWidths(new float[]{0.2f, 0.8f});

        adicionarCelula(tabelaCliente, "Cliente:", fonteDadosNegrito, true);
        adicionarCelula(tabelaCliente, orcamento.getCliente().getNome(), fonteDados, false);

        adicionarCelula(tabelaCliente, "CPF/CNPJ:", fonteDadosNegrito, true);
        String docCliente = orcamento.getCliente().getCpf() != null ? orcamento.getCliente().getCpf() : orcamento.getCliente().getCnpj();
        adicionarCelula(tabelaCliente, docCliente, fonteDados, false);

        adicionarCelula(tabelaCliente, "Contato:", fonteDadosNegrito, true);
        adicionarCelula(tabelaCliente, orcamento.getCliente().getTelefone(), fonteDados, false);

        documento.add(tabelaCliente);

        // TABELA DE ITENS
        Paragraph tituloItens = new Paragraph("DESCRIÇÃO DOS SERVIÇOS / PRODUTOS", fonteDadosNegrito);
        tituloItens.setSpacingAfter(5);
        documento.add(tituloItens);

        PdfPTable tabelaItens = new PdfPTable(4);
        tabelaItens.setWidthPercentage(100);
        tabelaItens.setSpacingAfter(20);
        tabelaItens.setWidths(new float[]{0.5f, 0.15f, 0.15f, 0.2f});

        adicionarCelula(tabelaItens, "Descrição", fonteDadosNegrito, BaseColor.LIGHT_GRAY);
        adicionarCelula(tabelaItens, "Qtd", fonteDadosNegrito, BaseColor.LIGHT_GRAY);
        adicionarCelula(tabelaItens, "Valor Unit.", fonteDadosNegrito, BaseColor.LIGHT_GRAY);
        adicionarCelula(tabelaItens, "Valor Total", fonteDadosNegrito, BaseColor.LIGHT_GRAY);

        orcamento.getItens().forEach(item -> {
            adicionarCelula(tabelaItens, item.getDescricao(), fonteDados, false);
            adicionarCelula(tabelaItens, item.getQuantidade().toString(), fonteDados, Element.ALIGN_CENTER);
            adicionarCelula(tabelaItens, MOEDA_FORMAT.format(item.getValorUnitario()), fonteDados, Element.ALIGN_RIGHT);
            BigDecimal totalItem = item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
            adicionarCelula(tabelaItens, MOEDA_FORMAT.format(totalItem), fonteDados, Element.ALIGN_RIGHT);
        });

        documento.add(tabelaItens);

        // VALOR TOTAL
        PdfPTable tabelaTotal = new PdfPTable(1);
        tabelaTotal.setWidthPercentage(100);
        PdfPCell celulaTotal = new PdfPCell(new Paragraph("VALOR TOTAL: " + MOEDA_FORMAT.format(orcamento.getValorTotal()), new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        celulaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaTotal.setPadding(10);
        celulaTotal.setBackgroundColor(new BaseColor(230, 230, 230));
        tabelaTotal.addCell(celulaTotal);
        documento.add(tabelaTotal);

        // OBSERVAÇÕES
        if (orcamento.getObservacoes() != null && !orcamento.getObservacoes().isBlank()) {
            Paragraph obsTitulo = new Paragraph("OBSERVAÇÕES:", fonteDadosNegrito);
            obsTitulo.setSpacingBefore(20);
            obsTitulo.setSpacingAfter(5);
            documento.add(obsTitulo);
            documento.add(new Paragraph(orcamento.getObservacoes(), fonteDados));
        }

        // ASSINATURAS
        documento.add(new Paragraph("\n\n\n"));
        PdfPTable tabelaAssinaturas = new PdfPTable(2);
        tabelaAssinaturas.setWidthPercentage(100);
        tabelaAssinaturas.setSpacingBefore(20);

        PdfPCell celulaPrestador = new PdfPCell(new Phrase("_______________________________\n" + prestador.getNomeResponsavel() + "\nResponsável", fonteDados));
        celulaPrestador.setBorder(PdfPCell.NO_BORDER);
        celulaPrestador.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabelaAssinaturas.addCell(celulaPrestador);

        PdfPCell celulaCliente = new PdfPCell(new Phrase("_______________________________\n" + orcamento.getCliente().getNome() + "\nCliente", fonteDados));
        celulaCliente.setBorder(PdfPCell.NO_BORDER);
        celulaCliente.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabelaAssinaturas.addCell(celulaCliente);

        documento.add(tabelaAssinaturas);

        documento.close();

        // ✅ SALVA O PDF ORIGINAL NA PASTA
        salvarArquivo(baos.toByteArray(), orcamento.getId(), orcamento.getCliente().getNome(), false);

        return baos.toByteArray();
    }

    // ✅ MÉTODO ATUALIZADO: SALVA EM PASTAS SEPARADAS
    public void salvarArquivo(byte[] dados, Long idOrcamento, String nomeCliente, boolean assinado) throws IOException {
        // Define a subpasta: "originais" ou "assinados"
        String subPasta = assinado ? "assinados" : "originais";
        
        // Caminho completo: pasta base + subpasta
        Path pastaCompleta = Paths.get(PASTA_ORCAMENTOS, subPasta);
        
        // Garante que as pastas existem
        if (!Files.exists(pastaCompleta)) {
            Files.createDirectories(pastaCompleta);
        }

        // 📝 NOME DO ARQUIVO FORMATADO DO JEITO QUE VOCÊ QUER
        String nomeArquivo = String.format("ORCAMENTO - %d - %s - %s.pdf",
                idOrcamento,
                nomeCliente.replaceAll("[^a-zA-Z0-9 áÁéÉíÍóÓúÚçÇãÃõÕ]", ""), // Remove caracteres especiais, mantém acentos
                LocalDateTime.now().format(NOME_DATA_HORA_FORMAT)
        );

        // Caminho final completo
        Path caminhoCompleto = pastaCompleta.resolve(nomeArquivo);

        // Salva o arquivo
        try (FileOutputStream fos = new FileOutputStream(caminhoCompleto.toFile())) {
            fos.write(dados);
        }
    }

    // MÉTODOS AUXILIARES
    private void adicionarCelula(PdfPTable tabela, String texto, Font fonte, boolean negrito) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBorderWidth(1);
        celula.setPadding(5);
        if (negrito) celula.setBackgroundColor(new BaseColor(240, 240, 240));
        tabela.addCell(celula);
    }

    private void adicionarCelula(PdfPTable tabela, String texto, Font fonte, BaseColor cor) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBorderWidth(1);
        celula.setPadding(5);
        celula.setBackgroundColor(cor);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabela.addCell(celula);
    }

    private void adicionarCelula(PdfPTable tabela, String texto, Font fonte, int alinhamento) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBorderWidth(1);
        celula.setPadding(5);
        celula.setHorizontalAlignment(alinhamento);
        tabela.addCell(celula);
    }
}