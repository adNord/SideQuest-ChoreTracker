package com.ChoreTracker.ChoreTracker.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.models.UserPrincipal;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    public final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new UserPrincipal(user);
    }
}
