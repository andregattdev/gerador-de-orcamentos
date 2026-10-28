package com.app.gerador_de_orcamento.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.gerador_de_orcamento.dto.ClienteRequestDTO;
import com.app.gerador_de_orcamento.dto.ClienteResponseDTO;
import com.app.gerador_de_orcamento.model.Cliente;
import com.app.gerador_de_orcamento.model.TipoCliente;
import com.app.gerador_de_orcamento.repository.ClienteRepository;
import com.app.gerador_de_orcamento.util.DocumentoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    // Converter Entidade → ResponseDTO
    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setTipo(cliente.getTipo());
        dto.setNome(cliente.getNome());
        dto.setEndereco(cliente.getEndereco());
        dto.setTelefone(cliente.getTelefone());
        dto.setEmail(cliente.getEmail());
        dto.setAtivo(cliente.getAtivo());

        if (cliente.getTipo() == TipoCliente.FISICA) {
            dto.setCpf(cliente.getCpf());
            dto.setRg(cliente.getRg());
        } else {
            dto.setCnpj(cliente.getCnpj());
            dto.setRazaoSocial(cliente.getRazaoSocial());
            dto.setInscricaoEstadual(cliente.getInscricaoEstadual());
        }
        return dto;
    }

    // Converter RequestDTO → Entidade
    private Cliente toEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setTipo(dto.getTipo());
        cliente.setNome(dto.getNome());
        cliente.setEndereco(dto.getEndereco());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());

        if (dto.getTipo() == TipoCliente.FISICA) {
            cliente.setCpf(dto.getCpf());
            cliente.setRg(dto.getRg());
            cliente.setCnpj(null);
            cliente.setRazaoSocial(null);
            cliente.setInscricaoEstadual(null);
        } else {
            cliente.setCnpj(dto.getCnpj());
            cliente.setRazaoSocial(dto.getRazaoSocial());
            cliente.setInscricaoEstadual(dto.getInscricaoEstadual());
            cliente.setCpf(null);
            cliente.setRg(null);
        }
        return cliente;
    }

    private void validarDocumentos(ClienteRequestDTO dto) {
        if (dto.getTipo() == TipoCliente.FISICA) {
            if (dto.getCpf() == null || dto.getCpf().isBlank()) {
                throw new IllegalArgumentException("Pessoa Física deve informar CPF");
            }
            if (!DocumentoUtil.cpfValido(dto.getCpf())) {
                throw new IllegalArgumentException("CPF inválido");
            }
            dto.setCpf(DocumentoUtil.somenteDigitos(dto.getCpf()));
        }
        if (dto.getTipo() == TipoCliente.JURIDICA) {
            if (dto.getCnpj() == null || dto.getCnpj().isBlank()) {
                throw new IllegalArgumentException("Pessoa Jurídica deve informar CNPJ");
            }
            if (!DocumentoUtil.cnpjValido(dto.getCnpj())) {
                throw new IllegalArgumentException("CNPJ inválido");
            }
            dto.setCnpj(DocumentoUtil.somenteDigitos(dto.getCnpj()));
        }
    }

    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        validarDocumentos(dto);

        if (dto.getTipo() == TipoCliente.FISICA) {
            if (clienteRepository.findByCpf(dto.getCpf()) != null) {
                throw new IllegalArgumentException("CPF já cadastrado");
            }
        }
        if (dto.getTipo() == TipoCliente.JURIDICA) {
            if (clienteRepository.findByCnpj(dto.getCnpj()) != null) {
                throw new IllegalArgumentException("CNPJ já cadastrado");
            }
        }

        Cliente salvo = clienteRepository.save(toEntity(dto));
        auditoriaService.registrar("ADICAO", "Cliente", salvo.getId(), "Cliente criado: " + salvo.getNome() + " (" + (salvo.getTipo() == TipoCliente.FISICA ? "CPF: " + salvo.getCpf() : "CNPJ: " + salvo.getCnpj()) + ")");
        return toResponseDTO(salvo);
    }

    public List<ClienteResponseDTO> listarAtivos() {
        return clienteRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ClienteResponseDTO> listarInativos() {
        return clienteRepository.findByAtivoFalse()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        return (cliente != null && cliente.getAtivo()) ? toResponseDTO(cliente) : null;
    }

    public ClienteResponseDTO buscarPorIdIncluindoInativo(Long id) {
        return clienteRepository.findById(id).map(this::toResponseDTO).orElse(null);
    }

    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente existente = clienteRepository.findById(id).orElse(null);
        if (existente == null || !existente.getAtivo()) return null;

        validarDocumentos(dto);

        existente.setNome(dto.getNome());
        existente.setEndereco(dto.getEndereco());
        existente.setTelefone(dto.getTelefone());
        existente.setEmail(dto.getEmail());

        if (existente.getTipo() == TipoCliente.FISICA) {
            existente.setCpf(dto.getCpf());
            existente.setRg(dto.getRg());
        } else {
            existente.setCnpj(dto.getCnpj());
            existente.setRazaoSocial(dto.getRazaoSocial());
            existente.setInscricaoEstadual(dto.getInscricaoEstadual());
        }

        Cliente salvo = clienteRepository.save(existente);
        auditoriaService.registrar("ALTERACAO", "Cliente", salvo.getId(), "Cliente atualizado: " + salvo.getNome());
        return toResponseDTO(salvo);
    }

    public boolean inativar(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null && cliente.getAtivo()) {
            cliente.setAtivo(false);
            clienteRepository.save(cliente);
            auditoriaService.registrar("EXCLUSAO", "Cliente", cliente.getId(), "Cliente inativado: " + cliente.getNome());
            return true;
        }
        return false;
    }

    public boolean reativar(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente != null && !cliente.getAtivo()) {
            cliente.setAtivo(true);
            clienteRepository.save(cliente);
            auditoriaService.registrar("ALTERACAO", "Cliente", cliente.getId(), "Cliente reativado: " + cliente.getNome());
            return true;
        }
        return false;
    }
}