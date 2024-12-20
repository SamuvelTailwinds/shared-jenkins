def call(Map params= [:]) {
    // Ensure required parameters are provided
    def smtpHost = params.smtpHost ?: error("SMTP host is required.")
    def smtpPort = params.smtpPort ?: '587'
    def emailTo = params.to ?: error("Recipient email address is required.")
    def emailSubject = params.subject ?: "No Subject"
    def emailBody = params.body ?: "No Content"
    def emailAttachments = params.attachments ?: []
    def credentialsId = params.credentialsId ?: error("Jenkins credentials ID is required.")

    // Retrieve Jenkins credentials
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'SMTP_USERNAME', passwordVariable: 'SMTP_PASSWORD')]) {
        // Create environment variables for Java class
        def envVars = [
            "SMTP_HOST=${smtpHost}",
            "SMTP_PORT=${smtpPort}",
            "SMTP_USERNAME=${env.SMTP_USERNAME}",
            "SMTP_PASSWORD=${env.SMTP_PASSWORD}",
            "EMAIL_TO=${emailTo}",
            "EMAIL_SUBJECT=${emailSubject}",
            "EMAIL_BODY=${emailBody}",
            "EMAIL_ATTACHMENTS=${emailAttachments.join(',')}"
        ].join(' ')

        // Embed Java code as a string
        def javaCode = '''
            import javax.mail.*;
            import javax.mail.internet.*;
            import javax.activation.*;
            import java.util.*;
            import java.io.File;

            public class EmailSender {
                public static void main(String[] params) {
                    try {
                        // Read environment variables
                        String smtpHost = System.getenv("SMTP_HOST");
                        String smtpPort = System.getenv("SMTP_PORT");
                        String username = System.getenv("SMTP_USERNAME");
                        String password = System.getenv("SMTP_PASSWORD");
                        String from = System.getenv("EMAIL_FROM");
                        String to = System.getenv("EMAIL_TO");
                        String subject = System.getenv("EMAIL_SUBJECT");
                        String body = System.getenv("EMAIL_BODY");
                        String attachments = System.getenv("EMAIL_ATTACHMENTS");

                        // Validate required parameters
                        if (smtpHost == null || username == null || password == null || to == null) {
                            System.err.println("Missing required parameters. Ensure all necessary environment variables are set.");
                            System.exit(1);
                        }

                        // Configure SMTP properties
                        Properties props = new Properties();
                        props.put("mail.smtp.host", smtpHost);
                        props.put("mail.smtp.port", smtpPort != null ? smtpPort : "587");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.starttls.enable", "true");

                        // Authenticate with SMTP server
                        Session session = Session.getInstance(props, new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                        // Create the email message
                        MimeMessage message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(from != null ? from : username));
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                        message.setSubject(subject != null ? subject : "No Subject");

                        // Add email body
                        MimeBodyPart textBodyPart = new MimeBodyPart();
                        textBodyPart.setText(body != null ? body : "No Content");

                        // Add attachments
                        Multipart multipart = new MimeMultipart();
                        multipart.addBodyPart(textBodyPart);

                        if (attachments != null && !attachments.isEmpty()) {
                            for (String filePath : attachments.split(",")) {
                                MimeBodyPart attachmentPart = new MimeBodyPart();
                                attachmentPart.attachFile(filePath.trim());
                                multipart.addBodyPart(attachmentPart);
                            }
                        }

                        message.setContent(multipart);

                        // Send email
                        Transport.send(message);
                        System.out.println("Email sent successfully.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        '''

        // Write the Java code to a file in workspace
        writeFile file: 'EmailSender.java', text: javaCode

        // Compile and execute the Java code
        sh """
            javac EmailSender.java
            ${envVars} java EmailSender
        """
    }
}
