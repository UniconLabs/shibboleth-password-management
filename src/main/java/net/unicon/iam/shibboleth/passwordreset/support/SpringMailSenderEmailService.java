package net.unicon.iam.shibboleth.passwordreset.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;

/**
 * Implementation of {@link EmailService} based on Spring's MailSender.
 */
@Slf4j
@AllArgsConstructor
public class SpringMailSenderEmailService implements EmailService {

    private JavaMailSender mailSender;

    @Override
    public boolean send(EmailProperties emailProperties, String to, String body) {
        try {
            MimeMessage message = this.mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            messageHelper.setTo(to);
            messageHelper.setText(body, emailProperties.isHtml());
            messageHelper.setSubject(emailProperties.getSubject());
            messageHelper.setFrom(emailProperties.getFrom());
            if(StringUtils.hasText(emailProperties.getReplyTo())) {
                messageHelper.setReplyTo(emailProperties.getReplyTo());
            }
            messageHelper.setValidateAddresses(emailProperties.isValidateAddresses());
            messageHelper.setPriority(1);
            if (StringUtils.hasText(emailProperties.getCc())) {
                messageHelper.setCc(emailProperties.getCc());
            }
            if(StringUtils.hasText(emailProperties.getBcc())) {
                messageHelper.setBcc(emailProperties.getBcc());
            }
            this.mailSender.send(message);
            return true;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        return false;
    }
}
