import javax.mail.*
import javax.mail.internet.*
import javax.activation.*
import java.util.Properties

/**
 * Sends an email with attachments using SMTP.
 *
 * @param params Map of email parameters:
 *        - smtpHost: SMTP server host
 *        - smtpPort: SMTP server port
 *        - username: SMTP username
 *        - password: SMTP password
 *        - to: Recipient email address (comma-separated for multiple)
 *        - from: Sender email address
 *        - subject: Email subject
 *        - body: Email body text
 *        - attachments: List of file paths to attach
 */
def call(Map params) {
    def props = new Properties()
    props.put("mail.smtp.host", params.smtpHost)
    props.put("mail.smtp.port", params.smtpPort)
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")

    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(params.username, params.password)
        }
    })

    try {
        MimeMessage message = new MimeMessage(session)
        message.setFrom(new InternetAddress(params.from))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(params.to))
        message.setSubject(params.subject)

        // Email body
        MimeBodyPart textPart = new MimeBodyPart()
        textPart.setText(params.body, "utf-8")

        // Attachments
        Multipart multipart = new MimeMultipart()
        multipart.addBodyPart(textPart)

        if (params.attachments) {
            params.attachments.each { filePath ->
                MimeBodyPart attachmentPart = new MimeBodyPart()
                FileDataSource source = new FileDataSource(filePath)
                attachmentPart.setDataHandler(new DataHandler(source))
                attachmentPart.setFileName(new File(filePath).getName())
                multipart.addBodyPart(attachmentPart)
            }
        }

        message.setContent(multipart)

        // Send email
        Transport.send(message)
        println "Email sent successfully to ${params.to}"
    } catch (MessagingException e) {
        println "Failed to send email: ${e.message}"
        throw e
    }
}
