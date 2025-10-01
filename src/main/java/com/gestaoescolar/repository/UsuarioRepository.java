package com.gestaoescolar.repository;

import com.gestaoescolar.model.Usuario;
import com.gestaoescolar.model.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    List<Usuario> findByPerfil(PerfilUsuario perfil);

    List<Usuario> findByAtivoTrue();

    @Query("SELECT u FROM Usuario u WHERE u.ativo = true AND u.perfil IN :perfis")
    List<Usuario> findAtivosByPerfis(@Param("perfis") List<PerfilUsuario> perfis);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.ativo = true")
    long countUsuariosAtivos();
}