package com.app.gerador_de_orcamento.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.app.gerador_de_orcamento.dto.ItemOrcamentoDTO;
import com.app.gerador_de_orcamento.dto.OrcamentoHistoricoDTO;
import com.app.gerador_de_orcamento.dto.OrcamentoRequestDTO;
import com.app.gerador_de_orcamento.dto.OrcamentoResponseDTO;
import com.app.gerador_de_orcamento.dto.PageResponseDTO;
import com.app.gerador_de_orcamento.dto.PrestadorServicoResponseDTO;
import com.app.gerador_de_orcamento.model.Cliente;
import com.app.gerador_de_orcamento.model.ItemOrcamento;
import com.app.gerador_de_orcamento.model.Orcamento;
import com.app.gerador_de_orcamento.model.OrcamentoHistorico;
import com.app.gerador_de_orcamento.model.StatusServico;
import com.app.gerador_de_orcamento.repository.OrcamentoHistoricoRepository;
import com.app.gerador_de_orcamento.repository.OrcamentoRepository;
import com.app.gerador_de_orcamento.util.PdfGenerator;
import com.itextpdf.text.DocumentException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoHistoricoRepository historicoRepository;
    private final ClienteService clienteService;
    private final PrestadorServicoService prestadorService;
    private final PdfGenerator pdfGenerator;
    private final StatusTransicaoValidator statusTransicaoValidator;
    private final AuditoriaService auditoriaService;

    private OrcamentoResponseDTO toResponseDTO(Orcamento orcamento) {
        OrcamentoResponseDTO dto = new OrcamentoResponseDTO();
        dto.setId(orcamento.getId());

        if (orcamento.getCliente() != null && orcamento.getCliente().getId() != null) {
            dto.setCliente(clienteService.buscarPorId(orcamento.getCliente().getId()));
            if (dto.getCliente() == null) {
                dto.setCliente(clienteService.buscarPorIdIncluindoInativo(orcamento.getCliente().getId()));
            }
        }

        dto.setDataCriacao(orcamento.getDataCriacao());
        dto.setObservacoes(orcamento.getObservacoes());
        dto.setAtivo(orcamento.getAtivo());
        dto.setValorTotal(orcamento.getValorTotal());
        dto.setValorCustos(orcamento.getValorCustos());
        dto.setValorDesconto(orcamento.getValorDesconto());
        dto.setPorcentagemImposto(orcamento.getPorcentagemImposto());
        dto.setDataAgendamento(orcamento.getDataAgendamento());
        dto.setStatusServico(orcamento.getStatusServico());

        BigDecimal imposto = calcularImposto(orcamento.getValorTotal(), orcamento.getPorcentagemImposto());
        BigDecimal custos = orcamento.getValorCustos() != null ? orcamento.getValorCustos() : BigDecimal.ZERO;
        dto.setValorImpostoCalculado(imposto);
        dto.setLucroLiquidoEstimado(orcamento.getValorTotal().subtract(custos).subtract(imposto));

        dto.setItens(orcamento.getItens().stream().map(item -> {
            ItemOrcamentoDTO iDto = new ItemOrcamentoDTO();
            iDto.setDescricao(item.getDescricao());
            iDto.setQuantidade(item.getQuantidade());
            iDto.setValorUnitario(item.getValorUnitario());
            return iDto;
        }).collect(Collectors.toList()));

        dto.setStatusAssinatura(orcamento.getStatusAssinatura());
        dto.setTemDocumentoAssinado("ASSINADO".equals(orcamento.getStatusAssinatura()));

        return dto;
    }

    private BigDecimal calcularImposto(BigDecimal valorTotal, BigDecimal porcentagem) {
        BigDecimal total = valorTotal != null ? valorTotal : BigDecimal.ZERO;
        BigDecimal pct = porcentagem != null ? porcentagem : BigDecimal.ZERO;
        return total.multiply(pct).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    private Orcamento toEntity(OrcamentoRequestDTO dto, Cliente cliente) {
        Orcamento orcamento = new Orcamento();
        orcamento.setCliente(cliente);
        orcamento.setObservacoes(dto.getObservacoes());
        orcamento.setValorCustos(dto.getValorCustos() != null ? dto.getValorCustos() : BigDecimal.ZERO);
        orcamento.setValorDesconto(dto.getValorDesconto() != null ? dto.getValorDesconto() : BigDecimal.ZERO);
        orcamento.setPorcentagemImposto(
                dto.getPorcentagemImposto() != null ? dto.getPorcentagemImposto() : BigDecimal.ZERO);
        orcamento.setDataAgendamento(dto.getDataAgendamento());

        List<ItemOrcamento> itens = new ArrayList<>();
        if (dto.getItens() != null) {
            itens = dto.getItens().stream()
                    .map(iDto -> new ItemOrcamento(iDto.getDescricao(), iDto.getQuantidade(), iDto.getValorUnitario()))
                    .collect(Collectors.toList());
        }

        orcamento.setItens(itens);
        return orcamento;
    }

    private void registrarHistorico(Orcamento orcamento, StatusServico anterior, StatusServico novo,
            String observacao) {
        OrcamentoHistorico historico = new OrcamentoHistorico();
        historico.setOrcamento(orcamento);
        historico.setStatusAnterior(anterior);
        historico.setStatusNovo(novo);
        historico.setObservacao(observacao);
        historicoRepository.save(historico);
    }

    public OrcamentoResponseDTO criar(OrcamentoRequestDTO dto) {
        if (dto.getClienteId() == null) {
            throw new IllegalArgumentException("Informe o cliente para o orçamento");
        }
        var clienteDto = clienteService.buscarPorId(dto.getClienteId());
        if (clienteDto == null) {
            throw new IllegalArgumentException("Cliente não existe ou está inativo");
        }

        Cliente cliente = new Cliente();
        cliente.setId(clienteDto.getId());

        Orcamento salvo = orcamentoRepository.save(toEntity(dto, cliente));
        registrarHistorico(salvo, null, salvo.getStatusServico(), "Orçamento criado");
        auditoriaService.registrar("ADICAO", "Orcamento", salvo.getId(), "Orçamento #" + salvo.getId()
                + " criado para o cliente " + clienteDto.getNome() + " no valor de R$ " + salvo.getValorTotal());
        return toResponseDTO(salvo);
    }

    public List<OrcamentoResponseDTO> obterTodosAtivos() {
        return orcamentoRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrcamentoResponseDTO> obterTodosInativos() {
        return orcamentoRepository.findByAtivoFalse()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponseDTO> obterTodos() {
        return orcamentoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<OrcamentoResponseDTO> buscarComFiltros(
            String clienteNome,
            StatusServico status,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Boolean apenasAtivos, // <-- Boolean, não boolean
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Orcamento> pageResult = orcamentoRepository.buscarComFiltros(
                clienteNome, status, dataInicio, dataFim, apenasAtivos, pageable);
        Page<OrcamentoResponseDTO> mapped = pageResult.map(this::toResponseDTO);
        return new PageResponseDTO<OrcamentoResponseDTO>(mapped);
    }

    public OrcamentoResponseDTO obterPorId(Long id) {
        Orcamento orcamento = orcamentoRepository.findById(id).orElse(null);
        return (orcamento != null && orcamento.getAtivo()) ? toResponseDTO(orcamento) : null;
    }

    public OrcamentoResponseDTO obterPorIdIncluindoInativo(Long id) {
        return orcamentoRepository.findById(id)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    public OrcamentoResponseDTO atualizar(Long id, OrcamentoRequestDTO dto) {
        Orcamento existente = orcamentoRepository.findById(id).orElse(null);
        if (existente == null || !existente.getAtivo()) {
            return null;
        }

        var clienteDto = clienteService.buscarPorId(dto.getClienteId());
        if (clienteDto == null) {
            throw new IllegalArgumentException("Cliente inválido ou inativo");
        }

        Cliente cliente = new Cliente();
        cliente.setId(clienteDto.getId());

        existente.setCliente(cliente);
        existente.setObservacoes(dto.getObservacoes());
        existente.setValorCustos(dto.getValorCustos() != null ? dto.getValorCustos() : BigDecimal.ZERO);
        existente.setValorDesconto(dto.getValorDesconto() != null ? dto.getValorDesconto() : BigDecimal.ZERO);
        existente.setPorcentagemImposto(
                dto.getPorcentagemImposto() != null ? dto.getPorcentagemImposto() : BigDecimal.ZERO);
        existente.setDataAgendamento(dto.getDataAgendamento());

        List<ItemOrcamento> novosItens = new ArrayList<>();
        if (dto.getItens() != null) {
            novosItens = dto.getItens().stream()
                    .map(i -> new ItemOrcamento(i.getDescricao(), i.getQuantidade(), i.getValorUnitario()))
                    .collect(Collectors.toList());
        }
        existente.setItens(novosItens);

        Orcamento salvo = orcamentoRepository.save(existente);
        auditoriaService.registrar("ALTERACAO", "Orcamento", salvo.getId(),
                "Orçamento #" + salvo.getId() + " atualizado para o cliente " + clienteDto.getNome()
                        + ". Novo valor total: R$ " + salvo.getValorTotal());
        return toResponseDTO(salvo);
    }

    public boolean inativar(Long id) {
        Orcamento orcamento = orcamentoRepository.findById(id).orElse(null);
        if (orcamento != null && orcamento.getAtivo()) {
            orcamento.setAtivo(false);
            orcamentoRepository.save(orcamento);
            auditoriaService.registrar("EXCLUSAO", "Orcamento", orcamento.getId(),
                    "Orçamento #" + orcamento.getId() + " inativado.");
            return true;
        }
        return false;
    }

    public boolean reativar(Long id) {
        Orcamento orcamento = orcamentoRepository.findById(id).orElse(null);
        if (orcamento != null && !orcamento.getAtivo()) {
            orcamento.setAtivo(true);
            orcamentoRepository.save(orcamento);
            auditoriaService.registrar("ALTERACAO", "Orcamento", orcamento.getId(),
                    "Orçamento #" + orcamento.getId() + " reativado.");
            return true;
        }
        return false;
    }

    public byte[] gerarPdfOrcamento(Long id) throws DocumentException, IOException {
        OrcamentoResponseDTO orcamento = obterPorId(id);
        if (orcamento == null) {
            throw new IllegalArgumentException("Orçamento não encontrado ou inativo");
        }
        PrestadorServicoResponseDTO prestador = prestadorService.getDadosAtuais();
        return pdfGenerator.gerarOrcamentoPdf(orcamento, prestador);
    }

    public void salvarDocumentoAssinado(Long id, MultipartFile arquivo) throws IOException {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado"));

        if (!"application/pdf".equals(arquivo.getContentType())) {
            throw new IllegalArgumentException("O arquivo enviado não é um PDF válido");
        }

        byte[] dadosPdf = arquivo.getBytes();
        OrcamentoResponseDTO dto = toResponseDTO(orcamento);
        pdfGenerator.salvarArquivo(dadosPdf, dto.getId(), dto.getCliente().getNome(), true);

        orcamento.setDocumentoAssinado(dadosPdf);
        orcamento.setStatusAssinatura("ASSINADO");
        orcamentoRepository.save(orcamento);
        registrarHistorico(orcamento, orcamento.getStatusServico(), orcamento.getStatusServico(),
                "Documento assinado recebido");
        auditoriaService.registrar("ALTERACAO", "Orcamento", orcamento.getId(),
                "Documento assinado enviado para o Orçamento #" + orcamento.getId());
    }

    public byte[] baixarDocumentoAssinado(Long id) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado"));

        if (orcamento.getDocumentoAssinado() == null || orcamento.getDocumentoAssinado().length == 0) {
            throw new IllegalArgumentException("Nenhum documento assinado encontrado para este orçamento");
        }

        return orcamento.getDocumentoAssinado();
    }

    public OrcamentoResponseDTO atualizarStatus(Long id, StatusServico novoStatus, String observacao) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado"));

        StatusServico statusAnterior = orcamento.getStatusServico();
        statusTransicaoValidator.validar(statusAnterior, novoStatus);

        orcamento.setStatusServico(novoStatus);
        Orcamento atualizado = orcamentoRepository.save(orcamento);
        registrarHistorico(atualizado, statusAnterior, novoStatus,
                observacao != null ? observacao : "Status alterado");
        auditoriaService.registrar("ALTERACAO", "Orcamento", atualizado.getId(), "Status do Orçamento #"
                + atualizado.getId() + " alterado de " + statusAnterior + " para " + novoStatus);
        return toResponseDTO(atualizado);
    }

    public List<OrcamentoHistoricoDTO> obterHistorico(Long id) {
        return historicoRepository.findByOrcamentoIdOrderByDataAlteracaoDesc(id).stream()
                .map(h -> new OrcamentoHistoricoDTO(
                        h.getId(),
                        h.getOrcamento().getId(),
                        h.getStatusAnterior(),
                        h.getStatusNovo(),
                        h.getObservacao(),
                        h.getDataAlteracao()))
                .collect(Collectors.toList());
    }

    public byte[] exportarCsv(boolean apenasAtivos) throws IOException {
        List<Orcamento> orcamentos = apenasAtivos
                ? orcamentoRepository.findByAtivoTrue()
                : orcamentoRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("id;cliente;data_criacao;valor_total;valor_custos;status_servico;status_assinatura;ativo\n");

        for (Orcamento o : orcamentos) {
            OrcamentoResponseDTO dto = toResponseDTO(o);
            String cliente = dto.getCliente() != null ? escapar(dto.getCliente().getNome()) : "";
            csv.append(String.format("%d;%s;%s;%s;%s;%s;%s;%s\n",
                    dto.getId(),
                    cliente,
                    dto.getDataCriacao(),
                    dto.getValorTotal(),
                    dto.getValorCustos(),
                    dto.getStatusServico(),
                    dto.getStatusAssinatura(),
                    dto.getAtivo()));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapar(String valor) {
        if (valor == null)
            return "";
        return valor.replace(";", ",").replace("\n", " ");
    }
}
