package com.gov.ma.saoluis.agendamento.service;

import com.gov.ma.saoluis.agendamento.DTO.EnderecoCreateDTO;
import com.gov.ma.saoluis.agendamento.model.Endereco;
import com.gov.ma.saoluis.agendamento.model.EnderecoFoto;
import com.gov.ma.saoluis.agendamento.repository.EnderecoFotoRepository;
import com.gov.ma.saoluis.agendamento.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final EnderecoFotoRepository enderecoFotoRepository;

    @Transactional
    public Endereco criar(EnderecoCreateDTO dto) {
        if (dto.logradouro() == null || dto.logradouro().isBlank())
            throw new RuntimeException("Logradouro é obrigatório");
        if (dto.cidade() == null || dto.cidade().isBlank())
            throw new RuntimeException("Cidade é obrigatória");
        if (dto.uf() == null || dto.uf().isBlank())
            throw new RuntimeException("UF é obrigatória");

        Endereco e = new Endereco();
        e.setLogradouro(dto.logradouro());
        e.setNumero(dto.numero());
        e.setBairro(dto.bairro());
        e.setCidade(dto.cidade());
        e.setUf(dto.uf());
        e.setCep(dto.cep());
        e.setComplemento(dto.complemento());
        e.setLatitude(dto.latitude());
        e.setLongitude(dto.longitude());

        return enderecoRepository.save(e);
    }

    @Transactional(readOnly = true)
    public List<Endereco> listarTodos() {
        List<Endereco> enderecos = enderecoRepository.findAll();

        String baseUrl = "http://localhost:8080";

        enderecos.forEach(e -> {
            e.getFotos().forEach(f -> {
                if (!f.getCaminho().startsWith("http")) {
                    f.setCaminho(baseUrl + f.getCaminho());
                }
            });
        });

        return enderecos;
    }

    public void salvarFoto(Long enderecoId, MultipartFile file, String descricao) throws IOException {

        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));

        String nomeArquivo = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destino = Paths.get("uploads/enderecos/" + enderecoId + "/" + nomeArquivo);

        Files.createDirectories(destino.getParent());
        Files.copy(file.getInputStream(), destino);

        EnderecoFoto foto = new EnderecoFoto();
        foto.setEndereco(endereco);
        foto.setCaminho("/uploads/enderecos/" + enderecoId + "/" + nomeArquivo);
        foto.setDescricao(descricao);

        enderecoFotoRepository.save(foto);
    }
}
