package com.app.gerador_de_orcamento.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.app.gerador_de_orcamento.dto.PrestadorServicoRequestDTO;
import com.app.gerador_de_orcamento.dto.PrestadorServicoResponseDTO;
import com.app.gerador_de_orcamento.model.PrestadorServico;
import com.app.gerador_de_orcamento.repository.PrestadorServicoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrestadorServicoService {

    private final PrestadorServicoRepository repository;
    private final AuditoriaService auditoriaService;

    // --- CONVERSÕES ---
    private PrestadorServicoResponseDTO toDTO(PrestadorServico entity) {
        PrestadorServicoResponseDTO dto = new PrestadorServicoResponseDTO();
        dto.setId(entity.getId());
        dto.setRazaoSocial(entity.getRazaoSocial());
        dto.setNomeFantasia(entity.getNomeFantasia());
        dto.setCnpj(entity.getCnpj());
        dto.setInscricaoMunicipal(entity.getInscricaoMunicipal());
        dto.setInscricaoEstadual(entity.getInscricaoEstadual());
        dto.setEndereco(entity.getEndereco());
        dto.setTelefone(entity.getTelefone());
        dto.setEmail(entity.getEmail());
        dto.setSite(entity.getSite());
        dto.setNomeResponsavel(entity.getNomeResponsavel());
        dto.setObservacaoRodape(entity.getObservacaoRodape());
        dto.setAtivo(entity.getAtivo());
        return dto;
    }

    private PrestadorServico toEntity(PrestadorServicoRequestDTO dto) {
        PrestadorServico entity = new PrestadorServico();
        entity.setRazaoSocial(dto.getRazaoSocial());
        entity.setNomeFantasia(dto.getNomeFantasia());
        entity.setCnpj(dto.getCnpj());
        entity.setInscricaoMunicipal(dto.getInscricaoMunicipal());
        entity.setInscricaoEstadual(dto.getInscricaoEstadual());
        entity.setEndereco(dto.getEndereco());
        entity.setTelefone(dto.getTelefone());
        entity.setEmail(dto.getEmail());
        entity.setSite(dto.getSite());
        entity.setNomeResponsavel(dto.getNomeResponsavel());
        entity.setObservacaoRodape(dto.getObservacaoRodape());
        entity.setAtivo(true);
        return entity;
    }

    // --- MÉTODOS DE NEGÓCIO ---

    // Cadastrar novo
    public PrestadorServicoResponseDTO salvar(PrestadorServicoRequestDTO dto) {
        // Valida CNPJ único
        PrestadorServico existenteCnpj = repository.findByCnpj(dto.getCnpj());
        if (existenteCnpj != null) {
            throw new IllegalArgumentException("CNPJ já cadastrado no sistema!");
        }

        // Mantém apenas UM cadastro ativo: inativa o anterior
        PrestadorServico atual = repository.findByAtivoTrue();
        if (atual != null) {
            atual.setAtivo(false);
            repository.save(atual);
        }

        PrestadorServico salvo = repository.save(toEntity(dto));
        auditoriaService.registrar("ADICAO", "PrestadorServico", salvo.getId(), "Configurações do Prestador criadas: " + salvo.getRazaoSocial());
        return toDTO(salvo);
    }

    // Buscar por ID (só retorna se estiver ativo)
    public PrestadorServicoResponseDTO buscarPorId(Long id) {
        PrestadorServico prestador = repository.findById(id).orElse(null);
        return (prestador != null && prestador.getAtivo()) ? toDTO(prestador) : null;
    }

    // Pegar dados atuais (o que está sendo usado no sistema)
    public PrestadorServicoResponseDTO getDadosAtuais() {
        PrestadorServico atual = repository.findByAtivoTrue();
        if (atual == null) {
            throw new IllegalArgumentException("Nenhum prestador de serviço ativo cadastrado!");
        }
        return toDTO(atual);
    }

    // Listar apenas ATIVOS
    public List<PrestadorServicoResponseDTO> listarAtivos() {
        return repository.findByAtivoTrueOrderByRazaoSocialAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Listar apenas INATIVOS
    public List<PrestadorServicoResponseDTO> listarInativos() {
        return repository.findByAtivoFalseOrderByRazaoSocialAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Listar TODOS (ativos e inativos)
    public List<PrestadorServicoResponseDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Atualizar dados
    public PrestadorServicoResponseDTO atualizar(Long id, PrestadorServicoRequestDTO dto) {
        PrestadorServico existente = repository.findById(id).orElse(null);
        if (existente == null || !existente.getAtivo()) {
            return null; // Não atualiza se não existir ou estiver inativo
        }

        // Verifica se está tentando mudar para um CNPJ que já existe em outro cadastro
        PrestadorServico cnpjExistente = repository.findByCnpj(dto.getCnpj());
        if (cnpjExistente != null && !cnpjExistente.getId().equals(id)) {
            throw new IllegalArgumentException("CNPJ já cadastrado em outro registro!");
        }

        // Atualiza os dados
        existente.setRazaoSocial(dto.getRazaoSocial());
        existente.setNomeFantasia(dto.getNomeFantasia());
        existente.setCnpj(dto.getCnpj());
        existente.setInscricaoMunicipal(dto.getInscricaoMunicipal());
        existente.setInscricaoEstadual(dto.getInscricaoEstadual());
        existente.setEndereco(dto.getEndereco());
        existente.setTelefone(dto.getTelefone());
        existente.setEmail(dto.getEmail());
        existente.setSite(dto.getSite());
        existente.setNomeResponsavel(dto.getNomeResponsavel());
        existente.setObservacaoRodape(dto.getObservacaoRodape());

        PrestadorServico salvo = repository.save(existente);
        auditoriaService.registrar("ALTERACAO", "PrestadorServico", salvo.getId(), "Configurações do Prestador atualizadas: " + salvo.getRazaoSocial());
        return toDTO(salvo);
    }

    // Inativar cadastro
    public boolean inativar(Long id) {
        PrestadorServico prestador = repository.findById(id).orElse(null);
        if (prestador != null && prestador.getAtivo()) {
            prestador.setAtivo(false);
            repository.save(prestador);
            auditoriaService.registrar("EXCLUSAO", "PrestadorServico", prestador.getId(), "Configurações do Prestador inativadas: " + prestador.getRazaoSocial());
            return true;
        }
        return false;
    }

    // Reativar cadastro
    public boolean reativar(Long id) {
        PrestadorServico prestador = repository.findById(id).orElse(null);
        if (prestador != null && !prestador.getAtivo()) {
            // Ao reativar, inativa o que estava ativo antes
            PrestadorServico ativoAnterior = repository.findByAtivoTrue();
            if (ativoAnterior != null) {
                ativoAnterior.setAtivo(false);
                repository.save(ativoAnterior);
            }
            prestador.setAtivo(true);
            repository.save(prestador);
            auditoriaService.registrar("ALTERACAO", "PrestadorServico", prestador.getId(), "Configurações do Prestador reativadas: " + prestador.getRazaoSocial());
            return true;
        }
        return false;
    }
}