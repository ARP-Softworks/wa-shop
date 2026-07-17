package uy.washop.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uy.washop.auth.domain.User;
import uy.washop.auth.domain.UserRole;
import uy.washop.auth.infrastructure.UserRepository;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AdminUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (user.getRole() != UserRole.ADMIN) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        return new AdminUserDetails(user);
    }
}
