package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

@SpringBootTest
class UserServiceImplTest {

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void criacaoPadraoDeUsuario() {
        UserDto usuario = new UserDto(null, "João vitor", "João Missão", "Joaomissão@gmail.com", "12345678", null);

        when(userRepository.existsByEmail(usuario.email())).thenReturn(false);
        when(userRepository.existsByUsername(usuario.username())).thenReturn(false);
        when(passwordEncoder.encode(usuario.password())).thenReturn("senhacriptografada");
        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        savedUser.setFullName(usuario.fullName());
        savedUser.setUsername(usuario.username());
        savedUser.setEmail(usuario.email());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        UserDto result = userService.createUser(usuario);
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("João vitor", result.fullName());
        assertEquals("João Missão", result.username());
        assertEquals("Joaomissão@gmail.com", result.email());
        verify(userRepository).existsByEmail(usuario.email());
        verify(userRepository).existsByUsername(usuario.username());
        verify(passwordEncoder).encode(usuario.password());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void criacaoDeUsuarioQuandoJaExisteEmail(){
        UserDto usuario = new UserDto(null, "João vitor","João Missão","Joaomissão@gmail.com","12345678",null);
        when( userRepository.existsByEmail(usuario.email())).thenReturn(true);
        FieldAlreadyExistsException exc = assertThrows(FieldAlreadyExistsException.class,() -> userService.createUser(usuario));
        assertEquals("E-email already in use.", exc.getMessage());
        verify(userRepository, times(1)).existsByEmail(usuario.email());
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void criacaoDeUsuarioQuandoJaExisteUsername(){
        UserDto usuario = new UserDto(null, "João vitor","João Missão","Joaomissão@gmail.com","12345678",null);
        when( userRepository.existsByUsername(usuario.username())).thenReturn(true);
        FieldAlreadyExistsException exc = assertThrows(FieldAlreadyExistsException.class,() -> userService.createUser(usuario));
        assertEquals("Username already in use.", exc.getMessage());
        verify(userRepository, times(1)).existsByUsername(usuario.username());
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void atualizacaoDeUsuarioQuandoTemCamposNulos(){
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
        assertEquals("UserDto or UserDto.id must not be null", exception.getMessage());
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    void atualizacaoQuandoNaoAchaOUser(){
        UserDto usuario = new UserDto(1L, "João vitor","João Missão","Joaomissão@gmail.com","12345678",null);

        when(userRepository.findById(usuario.id())).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(usuario));
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(usuario.id());
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void atualizacaoPadraoDeUsuario() {
        UserDto usuario = new UserDto(1L, "João vitor", "João Missão", "Joaomissão@gmail.com", "12345678", null);

        UserEntity userNoBd = new UserEntity();
        userNoBd.setId(1L);
        userNoBd.setFullName("João vitor");
        userNoBd.setUsername("jao");
        userNoBd.setEmail("Joaomissão@gmail.com");
        when(passwordEncoder.encode(usuario.password())).thenReturn("senhacriptografada");
        when(userRepository.findById(usuario.id())).thenReturn(Optional.of(userNoBd));
        when(userRepository.save(userNoBd)).thenReturn(userNoBd);
        UserDto atualizado = userService.updateUser(usuario);
        assertNotNull(atualizado);
        assertEquals(1L, atualizado.id());
        assertEquals("João vitor", atualizado.fullName());
        assertEquals("João Missão", atualizado.username());
        assertEquals("Joaomissão@gmail.com", atualizado.email());
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode(usuario.password());
        verify(userRepository).save(userNoBd);
    }


    @Test
    void listUsuariosQuandoHouver() {
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setFullName("João");
        user1.setUsername("joao");
        user1.setEmail("joao@email.com");

        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setFullName("Maria");
        user2.setUsername("maria");
        user2.setEmail("maria@email.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        List<UserDto> result = userService.findAll();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João", result.get(0).fullName());
        assertEquals("Maria", result.get(1).fullName());
        verify(userRepository).findAll();
    }

    @Test
    void listUsuariosQUandoNaoHouver() {
        when(userRepository.findAll()).thenReturn(List.of());
        List<UserDto> result = userService.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }


    @Test
    void achePeloIdQUandoHouver() {
        Long id = 1L;
        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setFullName("Paulo Pereira");
        userEntity.setUsername("paulo");
        userEntity.setEmail("paulo@ppereira.dev");
        when(userRepository.findById(id)).thenReturn(Optional.of(userEntity));
        UserDto result = userService.findById(id);
        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("Paulo Pereira", result.fullName());
        assertEquals("paulo@ppereira.dev", result.email());
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void achePeloIdQunadonaoHouver() {
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findById(id));
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void deleteUserQUandoHouver() {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);
        userService.deleteUser(id);
        verify(userRepository, times(1)).existsById(id);
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteUserQUandoNaoHouver() {
        Long id = 999L;
        when(userRepository.existsById(id)).thenReturn(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(id));
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, times(1)).existsById(id);
        verify(userRepository, times(0)).deleteById(id);
    }
}
