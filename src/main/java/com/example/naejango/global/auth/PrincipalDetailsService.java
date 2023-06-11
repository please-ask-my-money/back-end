package com.example.naejango.global.auth;

import com.example.naejango.domain.user.entity.User;
import com.example.naejango.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PrincipalDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("user 찾기");
        User findUser = userRepository.findByUserKey(username);
        System.out.println("findUser = " + findUser.getPassword());
        return new PrincipalDetails(findUser);
    }
}
