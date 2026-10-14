package dev.skillforge.iam.adapter.out.mail;

import dev.skillforge.iam.config.PasswordResetProperties;
import dev.skillforge.iam.domain.model.Email;
import dev.skillforge.iam.domain.port.out.PasswordResetMailer;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Envoi de l'e-mail de réinitialisation via SMTP (JavaMailSender). L'envoi est bloquant,
 * on l'exécute donc sur l'ordonnanceur {@code boundedElastic} pour ne pas bloquer l'event loop.
 * En local, un conteneur Mailpit capte les e-mails (interface web sur le port 8025).
 */
@Component
public class JavaMailPasswordResetMailer implements PasswordResetMailer {

    private final JavaMailSender mailSender;
    private final String from;

    public JavaMailPasswordResetMailer(JavaMailSender mailSender, PasswordResetProperties props) {
        this.mailSender = mailSender;
        this.from = props.mailFrom();
    }

    @Override
    public Mono<Void> sendResetLink(Email recipient, String resetUrl) {
        return Mono.<Void>fromRunnable(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient.value());
            message.setSubject("Reinitialisation de votre mot de passe SkillForge");
            message.setText(
                    "Bonjour,\n\n"
                            + "Tu as demande la reinitialisation de ton mot de passe SkillForge.\n"
                            + "Clique sur le lien ci-dessous pour en definir un nouveau :\n\n"
                            + resetUrl + "\n\n"
                            + "Ce lien est valable un temps limite et ne fonctionne qu'une seule fois.\n"
                            + "Si tu n'es pas a l'origine de cette demande, ignore cet e-mail.\n");
            mailSender.send(message);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
