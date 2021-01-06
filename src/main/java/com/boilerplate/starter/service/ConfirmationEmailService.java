package com.boilerplate.starter.service;

import com.boilerplate.starter.domain.entities.ConfirmationEmail;
import com.boilerplate.starter.exception.ServiceException;
import com.boilerplate.starter.repository.ConfirmationEmailRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ConfirmationEmailService {

    private final ConfirmationEmailRepository confirmationEmailRepository;

    private final JavaMailSender javaMailSender;

    public ConfirmationEmailService(ConfirmationEmailRepository confirmationEmailRepository, JavaMailSender javaMailSender) {
        this.confirmationEmailRepository = confirmationEmailRepository;
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public ConfirmationEmail createConfirmationToken(ConfirmationEmail confirmationToken) {
        return confirmationEmailRepository.save(confirmationToken);
    }

    @Async
    public void sendEmail(SimpleMailMessage email) {
        javaMailSender.send(email);
    }

    public ConfirmationEmail readConfirmationToken(String token) throws ServiceException {
        ConfirmationEmail confirmationEmail = confirmationEmailRepository.findByToken(token).orElse(null);

        if (confirmationEmail == null) {
            throw new ServiceException("This token does not exist!");
        }

        if (!confirmationEmail.isTokenValid(confirmationEmail.getExpiryDate())) {
            throw new ServiceException("Token has been expired!");
        }

        return confirmationEmail;
    }

    /**
     * Delete token
     * @param confirmationEmail token to be deleted.
     */
    public void deleteToken(ConfirmationEmail confirmationEmail) {
        confirmationEmail.setIsDeleted(true);
        confirmationEmailRepository.save(confirmationEmail);
    }

}
