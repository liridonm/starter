package com.boilerplate.starter.service;

import com.boilerplate.starter.domain.entities.User;
import com.boilerplate.starter.domain.enums.UserState;
import com.boilerplate.starter.exception.ServiceException;
import com.boilerplate.starter.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Read user by username
     * @param username username parameter {@link String}
     * @return founded user {@link User}
     */
    public User readByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Read user by email.
     * @param email email parameter {@link String}
     * @return founded user {@link User}
     */
    public User readByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * List all users.
     * @return
     */
    public List<User> getAll() {
        return userRepository.findAll();
    }


    /**
     * Create user.
     * @param user user to be created {@link User}
     * @return new user {@link User}
     */
    @Transactional
    public User createUser(User user) throws ServiceException {
        User foundUserByEmail = readByEmail(user.getEmail());
        User foundUserByUsername = readByUsername(user.getUsername());

        if(foundUserByEmail != null) {
            throw new ServiceException("This user already exists, please change your email");
        }

        if(foundUserByUsername != null) {
            throw new ServiceException("This user already exists, please change your username");
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setIsVerified(false);
        user.setIsDeleted(false);
        return userRepository.save(user);
    }


    /**
     * Verify user when email has been confirmed.
     * @param user user to be verified. {@link User}
     * @return verified User. {@link User}
     */
    @Transactional
    public User verifyUser(User user) {
        user.setIsVerified(true);
        user.setState(UserState.ACTIVE);
        return userRepository.save(user);
    }

    /**
     * Delete user
     * @param id if of user
     * @throws ServiceException
     */
    @Transactional
    public void deleteUser(Integer id) throws ServiceException {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ServiceException("This user does not exist");
        }
        user.setIsVerified(false);
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    /**
     * Reset password.
     * @param user user to be reset {@link User}
     * @return updated user.
     * @throws ServiceException
     */
    @Transactional
    public User resetPassword(User user) throws ServiceException {
        User foundUser = userRepository.findByEmail(user.getEmail()).orElse(null);
        if (foundUser == null) {
            throw new ServiceException("User with email does not exists: " + user.getEmail());
        }
        foundUser.setState(UserState.ACTIVE);
        foundUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(foundUser);
    }

}
