package com.app.gerador_de_orcamento.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.gerador_de_orcamento.dto.AgendaItemDTO;
import com.app.gerador_de_orcamento.dto.DashboardDTO;
import com.app.gerador_de_orcamento.dto.OrcamentoHistoricoDTO;
import com.app.gerador_de_orcamento.dto.OrcamentoRequestDTO;
import com.app.gerador_de_orcamento.dto.OrcamentoResponseDTO;
import com.app.gerador_de_orcamento.dto.PageResponseDTO;
import com.app.gerador_de_orcamento.model.StatusServico;
import com.app.gerador_de_orcamento.repository.OrcamentoRepository;
import com.app.gerador_de_orcamento.service.OrcamentoService;
import com.itextpdf.text.DocumentException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
@Tag(name = "Orçamentos", description = "Gerenciamento de orçamentos, incluindo criação, atualização, inativação e geração de PDFs")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;
    private final OrcamentoRepository orcamentoRepository;

    @PostMapping
    @Operation(summary = "Criar um novo orçamento")
    public ResponseEntity<?> criar(@Validated @RequestBody OrcamentoRequestDTO dto) {
        OrcamentoResponseDTO novo = orcamentoService.criar(dto);
        return new ResponseEntity<>(novo, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar orçamentos ativos")
    public ResponseEntity<List<OrcamentoResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(orcamentoService.obterTodosAtivos());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar orçamentos com filtros e paginação")
    public ResponseEntity<PageResponseDTO<OrcamentoResponseDTO>> buscar(
            @RequestParam(required = false) String clienteNome,
            @RequestParam(required = false) StatusServico status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Boolean apenasAtivos, // <-- alterado
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                orcamentoService.buscarComFiltros(clienteNome, status, dataInicio, dataFim, apenasAtivos, page, size));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar orçamentos em CSV")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(defaultValue = "true") boolean apenasAtivos) throws IOException {
        byte[] csv = orcamentoService.exportarCsv(apenasAtivos);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "orcamentos.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<OrcamentoResponseDTO>> listarInativos() {
        return ResponseEntity.ok(orcamentoService.obterTodosInativos());
    }

    @GetMapping("/todos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrcamentoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(orcamentoService.obterTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        OrcamentoResponseDTO orcamento = orcamentoService.obterPorIdIncluindoInativo(id);
        return orcamento != null ? ResponseEntity.ok(orcamento) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Histórico de alterações de status")
    public ResponseEntity<List<OrcamentoHistoricoDTO>> obterHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.obterHistorico(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Validated @RequestBody OrcamentoRequestDTO dto) {
        OrcamentoResponseDTO atualizado = orcamentoService.atualizar(id, dto);
        return atualizado != null ? ResponseEntity.ok(atualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        return orcamentoService.inativar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        return orcamentoService.reativar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> baixarPdf(@PathVariable Long id) {
        try {
            byte[] pdf = orcamentoService.gerarPdfOrcamento(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "orcamento_" + id + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DocumentException e) {
            return ResponseEntity.internalServerError().body("Erro ao gerar PDF: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro inesperado: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/assinado")
    public ResponseEntity<String> receberPdfAssinado(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo) {
        try {
            orcamentoService.salvarDocumentoAssinado(id, arquivo);
            return ResponseEntity.ok("Documento assinado salvo com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/assinado")
    public ResponseEntity<?> baixarPdfAssinado(@PathVariable Long id) {
        try {
            byte[] pdfAssinado = orcamentoService.baixarDocumentoAssinado(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "orcamento_" + id + "_ASSINADO.pdf");
            return ResponseEntity.ok().headers(headers).body(pdfAssinado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do serviço com validação de transição")
    public ResponseEntity<OrcamentoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusServico status,
            @RequestParam(required = false) String observacao) {
        OrcamentoResponseDTO dto = orcamentoService.atualizarStatus(id, status, observacao);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> obterDadosDashboard() {
        Double faturamento = orcamentoRepository.calcularFaturamentoBruto();
        Double lucro = orcamentoRepository.calcularLucroLiquido();
        Long ativos = orcamentoRepository.contarServicosAtivos();
        List<AgendaItemDTO> agenda = orcamentoRepository.buscarProximosAgendamentos(PageRequest.of(0, 5));

        DashboardDTO dto = new DashboardDTO(faturamento, lucro, ativos, agenda);
        return ResponseEntity.ok(dto);
    }
}
