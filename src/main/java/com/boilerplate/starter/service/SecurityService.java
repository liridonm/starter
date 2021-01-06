package com.boilerplate.starter.service;

import com.boilerplate.starter.domain.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityService implements UserDetailsService {

    private final UserService userService;

    public SecurityService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        User foundedUser = loadUser(s);

        if (foundedUser == null) {
            throw new UsernameNotFoundException("user not found! " + s);
        }

        return new org.springframework.security.core.userdetails.User(foundedUser.getUsername(), foundedUser.getPassword(), listAuthorities(foundedUser));
    }

    /**
     * Check if parameter is email or username.
     *
     * @param value value. {@link String}
     * @return user by username or email {@link User}
     */
    public User loadUser(String value) {
        boolean isEmail = value.contains("@");
        return isEmail ? userService.readByEmail(value) : userService.readByUsername(value);
    }

    /**
     * List authorities.
     *
     * @param user founded user {@link User}
     * @return list of grantedAuthority {@link List<GrantedAuthority>}
     */
    private List<GrantedAuthority> listAuthorities(User user) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        return grantedAuthorities;
    }

}
