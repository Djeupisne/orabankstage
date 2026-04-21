package com.orabank.tfj.service;

import com.orabank.tfj.dto.LoginRequestDTO;
import com.orabank.tfj.dto.LoginResponseDTO;
import com.orabank.tfj.dto.UserDTO;
import com.orabank.tfj.model.Employee;
import com.orabank.tfj.model.User;
import com.orabank.tfj.repository.EmployeeRepository;
import com.orabank.tfj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Identifiant incorrect"));
        
        if (!user.getActive()) {
            throw new RuntimeException("Compte désactivé");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }
        
        String token = jwtTokenProvider.generateToken(user);
        
        String fullName = null;
        Long employeeId = null;
        if (user.getEmployee() != null) {
            employeeId = user.getEmployee().getId();
            fullName = user.getEmployee().getFullName();
        }
        
        return LoginResponseDTO.builder()
            .token(token)
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .employeeId(employeeId)
            .fullName(fullName)
            .build();
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDTO);
    }
    
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà");
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Cet email existe déjà");
        }
        
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode("TempPassword123!")); // Mot de passe temporaire
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        user.setActive(userDTO.getActive() != null ? userDTO.getActive() : true);
        
        if (userDTO.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(userDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
            user.setEmployee(employee);
        }
        
        User savedUser = userRepository.save(user);
        return toDTO(savedUser);
    }
    
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new RuntimeException("Cet email existe déjà");
            }
            user.setEmail(userDTO.getEmail());
        }
        
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        
        if (userDTO.getActive() != null) {
            user.setActive(userDTO.getActive());
        }
        
        if (userDTO.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(userDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
            user.setEmployee(employee);
        }
        
        User updatedUser = userRepository.save(user);
        return toDTO(updatedUser);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        
        if (user.getEmployee() != null) {
            dto.setEmployeeId(user.getEmployee().getId());
            dto.setEmployeeFullName(user.getEmployee().getFullName());
        }
        
        return dto;
    }
}
