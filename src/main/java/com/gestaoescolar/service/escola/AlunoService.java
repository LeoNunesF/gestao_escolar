package com.gestaoescolar.service.escola;

import com.gestaoescolar.model.Aluno;
import com.gestaoescolar.model.AlunoResponsavel;
import com.gestaoescolar.model.Responsavel;
import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.repository.AlunoRepository;
import com.gestaoescolar.repository.AlunoResponsavelRepository;
import com.gestaoescolar.repository.ResponsavelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AlunoResponsavelRepository alunoResponsavelRepository;

    public AlunoService(AlunoRepository alunoRepository,
                        ResponsavelRepository responsavelRepository,
                        AlunoResponsavelRepository alunoResponsavelRepository) {
        this.alunoRepository = alunoRepository;
        this.responsavelRepository = responsavelRepository;
        this.alunoResponsavelRepository = alunoResponsavelRepository;
    }

    // ===== Permissão =====
    private void requireAdmin(Usuario usuario) {
        if (usuario == null || !usuario.isAdministrativo()) {
            throw new SecurityException("Acesso restrito à administração");
        }
    }

    // ===== Alunos =====

    @Transactional
    public Aluno createStudent(Aluno novo, Usuario usuario) {
        requireAdmin(usuario);
        normalizeStudentFields(novo);
        validateStudent(novo, true);
        novo.setAtivo(true);
        return alunoRepository.save(novo);
    }

    @Transactional
    public Aluno updateStudent(Long id, Aluno dados, Usuario usuario) {
        requireAdmin(usuario);
        Aluno existente = alunoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado"));

        normalizeStudentFields(dados);
        // CPF do aluno é opcional; se alterar, garantir unicidade quando informado
        if (dados.getCpf() != null && !dados.getCpf().isBlank()) {
            if (!dados.getCpf().equals(existente.getCpf()) && alunoRepository.existsByCpf(dados.getCpf())) {
                throw new IllegalArgumentException("Já existe um aluno com este CPF");
            }
        }
        validateStudent(dados, false);

        existente.setNomeCompleto(dados.getNomeCompleto());
        existente.setNomeSocial(dados.getNomeSocial());
        existente.setDataNascimento(dados.getDataNascimento());
        existente.setGenero(dados.getGenero());
        existente.setCorRaca(dados.getCorRaca());

        existente.setDocTipo(dados.getDocTipo());
        existente.setDocNumero(dados.getDocNumero());
        existente.setCpf(dados.getCpf());
        existente.setInep(dados.getInep());
        existente.setNis(dados.getNis());
        existente.setJustificativaDocumentos(dados.getJustificativaDocumentos());

        existente.setCep(dados.getCep());
        existente.setLogradouro(dados.getLogradouro());
        existente.setNumero(dados.getNumero());
        existente.setComplemento(dados.getComplemento());
        existente.setBairro(dados.getBairro());
        existente.setCidade(dados.getCidade());
        existente.setUf(dados.getUf());

        existente.setTelefone(dados.getTelefone());
        existente.setEmail(dados.getEmail());

        existente.setAlergias(dados.getAlergias());
        existente.setObservacoesSaude(dados.getObservacoesSaude());

        existente.setObservacoes(dados.getObservacoes());

        return alunoRepository.save(existente);
    }

    public List<Aluno> listStudents(Usuario usuario) {
        requireAdmin(usuario);
        return alunoRepository.findAllByOrderByNomeCompletoAsc();
    }

    public List<Aluno> searchStudentsByName(String nome, Usuario usuario) {
        requireAdmin(usuario);
        return alunoRepository.findByNomeCompletoContainingIgnoreCase(nome);
    }

    public Aluno findStudentByCpf(String cpf, Usuario usuario) {
        requireAdmin(usuario);
        String normalized = normalizeDigits(cpf);
        return alunoRepository.findByCpf(normalized).orElse(null);
    }

    @Transactional
    public void deactivateStudent(Long id, Usuario usuario) {
        requireAdmin(usuario);
        Aluno a = alunoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado"));
        a.setAtivo(false);
        alunoRepository.save(a);
    }

    @Transactional
    public void reactivateStudent(Long id, Usuario usuario) {
        requireAdmin(usuario);
        Aluno a = alunoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado"));
        a.setAtivo(true);
        alunoRepository.save(a);
    }

    private void validateStudent(Aluno a, boolean creating) {
        if (a.getNomeCompleto() == null || a.getNomeCompleto().isBlank()) {
            throw new IllegalArgumentException("Nome completo é obrigatório");
        }
        if (a.getDataNascimento() == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
        if (a.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento inválida");
        }
        if (a.getCpf() != null && !a.getCpf().isBlank()) {
            if (!isValidCpf(a.getCpf())) {
                throw new IllegalArgumentException("CPF do aluno inválido");
            }
            if (creating && alunoRepository.existsByCpf(a.getCpf())) {
                throw new IllegalArgumentException("Já existe um aluno com este CPF");
            }
        }
        if (a.getCep() != null && !a.getCep().isBlank() && !isValidCep(a.getCep())) {
            throw new IllegalArgumentException("CEP inválido");
        }
        if (a.getTelefone() != null && !a.getTelefone().isBlank() && !isValidPhone(a.getTelefone())) {
            throw new IllegalArgumentException("Telefone inválido");
        }
        if (a.getEmail() != null && !a.getEmail().isBlank() && !isValidEmail(a.getEmail())) {
            throw new IllegalArgumentException("E-mail inválido");
        }
    }

    private void normalizeStudentFields(Aluno a) {
        a.setCpf(normalizeDigits(a.getCpf()));
        a.setCep(normalizeDigits(a.getCep()));
        a.setTelefone(normalizeDigits(a.getTelefone()));
        // docNumero pode conter letras (certidão), não normalizamos
        if (a.getEmail() != null) a.setEmail(a.getEmail().trim());
    }

    // ===== Responsáveis =====

    @Transactional
    public Responsavel upsertGuardianByCpf(Responsavel data, Usuario usuario) {
        requireAdmin(usuario);
        // Normaliza primeiro
        data.setCpf(normalizeDigits(data.getCpf()));
        data.setTelefone1(normalizeDigits(data.getTelefone1()));
        data.setTelefone2(normalizeDigits(data.getTelefone2()));
        if (data.getEmail() != null) data.setEmail(data.getEmail().trim());

        // Valida
        if (data.getCpf() == null || data.getCpf().isBlank()) {
            throw new IllegalArgumentException("CPF do responsável é obrigatório");
        }
        if (!isValidCpf(data.getCpf())) {
            throw new IllegalArgumentException("CPF do responsável inválido");
        }
        if (data.getNome() == null || data.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do responsável é obrigatório");
        }
        if (data.getParentesco() == null || data.getParentesco().isBlank()) {
            throw new IllegalArgumentException("Parentesco é obrigatório");
        }
        if (data.getTelefone1() != null && !data.getTelefone1().isBlank() && !isValidPhone(data.getTelefone1())) {
            throw new IllegalArgumentException("Telefone 1 do responsável inválido");
        }
        if (data.getTelefone2() != null && !data.getTelefone2().isBlank() && !isValidPhone(data.getTelefone2())) {
            throw new IllegalArgumentException("Telefone 2 do responsável inválido");
        }
        if (data.getEmail() != null && !data.getEmail().isBlank() && !isValidEmail(data.getEmail())) {
            throw new IllegalArgumentException("E-mail do responsável inválido");
        }

        return responsavelRepository.findByCpf(data.getCpf())
                .map(existing -> {
                    existing.setNome(data.getNome());
                    existing.setParentesco(data.getParentesco());
                    existing.setRg(data.getRg());
                    existing.setTelefone1(data.getTelefone1());
                    existing.setTelefone2(data.getTelefone2());
                    existing.setEmail(data.getEmail());
                    existing.setDocGuarda(data.getDocGuarda());
                    return responsavelRepository.save(existing);
                })
                .orElseGet(() -> responsavelRepository.save(data));
    }

    @Transactional
    public void attachGuardian(Long alunoId,
                               Responsavel guardianData,
                               boolean didatico,
                               boolean financeiro,
                               boolean legal,
                               Usuario usuario) {
        requireAdmin(usuario);
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado"));

        Responsavel responsavel = upsertGuardianByCpf(guardianData, usuario);

        var existingLinkOpt = alunoResponsavelRepository.findByAlunoIdAndResponsavelId(alunoId, responsavel.getId());
        if (existingLinkOpt.isPresent()) {
            AlunoResponsavel link = existingLinkOpt.get();
            link.setAtivo(true);
            link.setResponsavelDidatico(didatico);
            link.setResponsavelFinanceiro(financeiro);
            link.setResponsavelLegal(legal);
            alunoResponsavelRepository.save(link);
            return;
        }

        AlunoResponsavel link = new AlunoResponsavel();
        link.setAluno(aluno);
        link.setResponsavel(responsavel);
        link.setResponsavelDidatico(didatico);
        link.setResponsavelFinanceiro(financeiro);
        link.setResponsavelLegal(legal);
        link.setAtivo(true);
        alunoResponsavelRepository.save(link);
    }

    @Transactional
    public void detachGuardian(Long alunoId, Long responsavelId, Usuario usuario) {
        requireAdmin(usuario);
        var linkOpt = alunoResponsavelRepository.findByAlunoIdAndResponsavelId(alunoId, responsavelId);
        if (linkOpt.isEmpty()) {
            throw new IllegalArgumentException("Vínculo não encontrado");
        }
        alunoResponsavelRepository.delete(linkOpt.get());
    }

    public List<AlunoResponsavel> listActiveGuardians(Long alunoId, Usuario usuario) {
        requireAdmin(usuario);
        return alunoResponsavelRepository.findByAlunoIdAndAtivoTrue(alunoId);
    }

    // ===== Helpers: normalização e validação =====

    private String normalizeDigits(String s) {
        if (s == null) return null;
        String d = s.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }

    private boolean isValidCpf(String cpf) {
        if (cpf == null) return false;
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11) return false;
        if (d.chars().distinct().count() == 1) return false; // evita 000... ou 111...
        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) sum += (d.charAt(i) - '0') * (10 - i);
            int r = 11 - (sum % 11);
            int dv1 = (r == 10 || r == 11) ? 0 : r;
            if (dv1 != (d.charAt(9) - '0')) return false;

            sum = 0;
            for (int i = 0; i < 10; i++) sum += (d.charAt(i) - '0') * (11 - i);
            r = 11 - (sum % 11);
            int dv2 = (r == 10 || r == 11) ? 0 : r;
            return dv2 == (d.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidCep(String cep) {
        if (cep == null) return false;
        String d = cep.replaceAll("\\D", "");
        return d.length() == 8;
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String d = phone.replaceAll("\\D", "");
        return d.length() == 10 || d.length() == 11;
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}