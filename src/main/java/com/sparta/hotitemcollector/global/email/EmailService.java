package com.sparta.hotitemcollector.global.email;

import com.sparta.hotitemcollector.domain.user.User;
import com.sparta.hotitemcollector.domain.user.UserService;
import com.sparta.hotitemcollector.global.exception.CustomException;
import com.sparta.hotitemcollector.global.exception.ErrorCode;
import com.sparta.hotitemcollector.global.jwt.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.security.SecureRandom;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final SpringTemplateEngine templateEngine;
    private final UserService userService;

    public void sendPasswordResetEmail(EmailMessage emailMessage) {
        // Generate and set the new password
        String authNum = createCode();
        User findUser = userService.findByEmail(emailMessage.getTo());
        findUser.updatePassword(userService.passwordEncoder(authNum));
        userService.saveUser(findUser);

        // Send email
        sendEmail(emailMessage.getTo(), "Password Reset Request", "password", authNum);
    }

    public String sendAuthCodeEmail(EmailMessage emailMessage) {
        // Check for existing auth code
        if (redisUtil.hasKey(emailMessage.getTo())) {
            throw new CustomException(ErrorCode.DUPLICATE_AUTHCODE);
        }

        // Generate and save the auth code
        String authNum = createCode();
        redisUtil.saveAuthCode(emailMessage.getTo(), authNum, 3);

        // Send email
        sendEmail(emailMessage.getTo(), "Authentication Code", "email", authNum);

        return authNum;
    }

    // Common method to send emails
    private void sendEmail(String to, String subject, String templateName, String code) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(setContext(code, templateName), true);

            javaMailSender.send(mimeMessage);
            log.info("{} email sent successfully to {}", subject, to);

        } catch (MessagingException e) {
            log.error("Failed to send {} email to {}: {}", subject, to, e.getMessage());
            throw new RuntimeException("Failed to send " + subject.toLowerCase() + " email", e);
        }
    }

    // Generate authentication code
    private String createCode() {
        SecureRandom random = new SecureRandom(); // Use SecureRandom for better security
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);
            switch (index) {
                case 0:
                    key.append((char) (random.nextInt(26) + 'a')); // Lowercase letter
                    break;
                case 1:
                    key.append((char) (random.nextInt(26) + 'A')); // Uppercase letter
                    break;
                default:
                    key.append(random.nextInt(10)); // Digit
            }
        }
        return key.toString();
    }

    // Validate authentication code
    public void validateAuthCode(String email, String authCode) {
        String storedCode = redisUtil.getAuthCode(email);
        boolean isValid = authCode != null && authCode.equals(storedCode);
        if (isValid) {
            redisUtil.deleteAuthCode(email);
        } else {
            throw new CustomException(ErrorCode.INCORRECT_AUTHCODE);
        }
    }

    // Set the context for Thymeleaf templates
    private String setContext(String code, String templateName) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(templateName, context);
    }
}
