package com.boilerplate.starter.controller;

import com.boilerplate.starter.annotations.DefaultExceptionMessage;
import com.boilerplate.starter.domain.common.AuthenticationRequest;
import com.boilerplate.starter.domain.common.MailDto;
import com.boilerplate.starter.domain.common.ResponseWrapper;
import com.boilerplate.starter.domain.entities.ConfirmationEmail;
import com.boilerplate.starter.domain.entities.User;
import com.boilerplate.starter.exception.ServiceException;
import com.boilerplate.starter.service.ConfirmationEmailService;
import com.boilerplate.starter.service.UserService;
import com.boilerplate.starter.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Authenticate Controller", description = "Authenticate API")
public class AuthenticateController {

    @Value("${app.local-url}")
    private String BASE_URL;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ConfirmationEmailService confirmationEmailService;


    public AuthenticateController(AuthenticationManager authenticationManager, UserService userService,
                                  JwtUtil jwtUtil, ConfirmationEmailService confirmationEmailService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.confirmationEmailService = confirmationEmailService;
    }

    /**
     * Login to application
     *
     * @param authenticationRequest authenticationRequest {@link AuthenticationRequest}
     * @return token if the user has been logged in to the app.
     * @throws ServiceException        serviceException.
     * @throws BadCredentialsException badCredentialsException.
     */
    @DefaultExceptionMessage(defaultMessage = "Bad Credentials")
    @PostMapping("/authenticate")
    @Operation(summary = "Login to application")
    public ResponseEntity<ResponseWrapper> doLogin(@RequestBody AuthenticationRequest authenticationRequest) throws ServiceException, BadCredentialsException {
        String password = authenticationRequest.getPassword();
        String username = authenticationRequest.getUsername();
        User foundUser = userService.readByUsername(username);

        assert foundUser != null : "This account does not exists!";
        if (!foundUser.getIsVerified()) {
            throw new ServiceException("Please verify your user!");
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(authentication);
        String jwtToken = jwtUtil.generateToken(foundUser, username);
        return ResponseEntity.ok(new ResponseWrapper("Login Successfully!", jwtToken));
    }

    /**
     * Create new account to the app. Send confirmation to the user email.
     *
     * @param user user to be created {@link User}
     * @return new user {@link User}
     * @throws Exception throw exception if something went wrong.
     */
    @DefaultExceptionMessage(defaultMessage = "Failed to create user, please try again")
    @PostMapping("/create-user")
    @Operation(summary = "Create new account")
    public ResponseEntity<ResponseWrapper> createAccount(@RequestBody User user) throws Exception {
        User createdUser = userService.createUser(user);
        sendEmail(createVerificationToken(user));
        return ResponseEntity.ok(new ResponseWrapper("User has been created successfully", createdUser));
    }


    /**
     * Reset your password.
     * @param user user to be reset.
     * @return
     * @throws ServiceException
     */
    @DefaultExceptionMessage(defaultMessage = "Failed to reset your password, please try again!")
    @Operation(summary = "Reset password")
    @PutMapping("/reset-password")
    public ResponseEntity<ResponseWrapper> resetPassword(@RequestBody User user, @RequestParam("token") String token) throws Exception {
        User updatedUser = userService.resetPassword(user);
        confirmationEmailService.deleteToken(confirmationEmailService.readConfirmationToken(token));
        return ResponseEntity.ok(new ResponseWrapper("User has been resetting successfully", updatedUser));

    }

    /**
     * Resetting password.
     * @param token
     * @return
     * @throws Exception
     */
    @DefaultExceptionMessage(defaultMessage = "Something went wrong, please try again!")
    @Operation(summary = "Reset password")
    @GetMapping("/reset")
    public ResponseEntity<ResponseWrapper> passwordReset(@RequestParam("token") String token) throws Exception {
        ConfirmationEmail confirmationEmail = confirmationEmailService.readConfirmationToken(token);
        return ResponseEntity.ok(new ResponseWrapper("", confirmationEmail.getUser()));

    }

    /**
     * Forgot password when user forgot forgot his password.
     * @return
     */
    @DefaultExceptionMessage(defaultMessage = "Something went wrong, please try again")
    @GetMapping("/forget")
    @Operation(summary = "Forgot password")
    public ResponseEntity<ResponseWrapper> forgotPassword(@RequestParam("email") String email) throws Exception {
        User foundUser = userService.readByEmail(email);
        sendEmail(forgotPasswordVerification(foundUser));
        if (foundUser == null) {
            throw new ServiceException("User does not exists!");
        }
        return ResponseEntity.ok(new ResponseWrapper("An email has been sent to your email address, please check it!"));
    }

    /**
     * Confirm email while you user has been created an account.
     *
     * @param token
     * @return confirmed user.
     * @throws ServiceException
     */
    @DefaultExceptionMessage(defaultMessage = "Failed to confirm email, please try again")
    @Operation(summary = "Confirm account")
    @GetMapping("/confirmation")
    public ResponseEntity<ResponseWrapper> confirmEmail(@RequestParam("token") String token) throws Exception {
        ConfirmationEmail confirmationEmail = confirmationEmailService.readConfirmationToken(token);
        User confirmUser = userService.verifyUser(confirmationEmail.getUser());
        confirmationEmailService.deleteToken(confirmationEmail);
        return ResponseEntity.ok(new ResponseWrapper("User has been confirmed!", confirmUser));
    }


    private MailDto forgotPasswordVerification(@RequestBody User user) {
        ConfirmationEmail confirmationToken = new ConfirmationEmail(user);
        confirmationToken.setIsDeleted(false);
        ConfirmationEmail saveToken = confirmationEmailService.createConfirmationToken(confirmationToken);

        return MailDto
                .builder()
                .emailTo(user.getEmail())
                .token(saveToken.getToken())
                .subject("Forgot password")
                .message("To reset your account, please click here: ")
                .url(BASE_URL + "/reset?token=")
                .build();
    }


    private MailDto createVerificationToken(@RequestBody User user) {
        ConfirmationEmail confirmationToken = new ConfirmationEmail(user);
        confirmationToken.setIsDeleted(false);
        ConfirmationEmail saveToken = confirmationEmailService.createConfirmationToken(confirmationToken);

        return MailDto
                .builder()
                .emailTo(user.getEmail())
                .token(saveToken.getToken())
                .subject("Confirm Registration")
                .message("To confirm your account, please click here: ")
                .url(BASE_URL + "/confirmation?token=")
                .build();
    }

    private void sendEmail(MailDto mailDto) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(mailDto.getEmailTo());
        mailMessage.setSubject(mailDto.getSubject());
        mailMessage.setText(mailDto.getMessage() + mailDto.getUrl() + mailDto.getToken());

        confirmationEmailService.sendEmail(mailMessage);
    }
}
