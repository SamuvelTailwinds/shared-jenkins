// def call(Map params) {
//     def smtpHost = params.get('smtpHost', 'smtp.example.com')      // Default SMTP host
//     def smtpPort = params.get('smtpPort', '587')                  // Default SMTP port (TLS)
//     def credentialsId = params.get('credentialsId')               // Jenkins credentials ID
//     def recipients = params.get('recipients', '')                 // Recipient email addresses
//     def subject = params.get('subject', 'Build Notification')     // Email subject
//     def body = params.get('body', 'Build completed successfully') // Email body
//     def attachments = params.get('attachments', '')               // Attachments pattern

//     if (!credentialsId || !recipients) {
//         error("Missing required parameters: 'credentialsId' or 'recipients'.")
//     }

//     // Retrieve credentials securely
//     withCredentials([usernamePassword(
//         credentialsId: credentialsId,
//         usernameVariable: 'SMTP_USER',
//         passwordVariable: 'SMTP_PASSWORD'
//     )]) {
//         script {
//             // Set SMTP configuration dynamically
//             System.setProperty("mail.smtp.host", smtpHost)
//             System.setProperty("mail.smtp.port", smtpPort)
//             System.setProperty("mail.smtp.auth", "true")
//             System.setProperty("mail.smtp.starttls.enable", "true")
//         }

//         // Send email with attachment
//         emailext(
//             to: recipients,
//             subject: subject,
//             body: body,
//             mimeType: 'text/html',
//             attachLog: false,
//             attachmentsPattern: attachments,
//             from: params.get('from', "Jenkins <${SMTP_USER}>"),
//             replyTo: params.get('replyTo', ''),
//             smtpUsername: SMTP_USER,
//             smtpPassword: SMTP_PASSWORD,
//             charset: 'UTF-8',
//             contentType: 'text/html'
//         )
//     }
// }

// def call(Map params) {
//     if (!params.credentialsId || !params.smtpHost || !params.smtpPort || !params.recipients || !params.subject || !params.body) {
//         error "Missing required parameters. Ensure 'credentialsId', 'smtpHost', 'smtpPort', 'recipients', 'subject', and 'body' are provided."
//     }

//     def attachments = params.get('attachments', '')

//     withCredentials([usernamePassword(
//         credentialsId: params.credentialsId,
//         usernameVariable: 'SMTP_USERNAME',
//         passwordVariable: 'SMTP_PASSWORD'
//     )]) {
//         emailext(
//             to: params.recipients,
//             subject: params.subject,
//             body: params.body,
//             mimeType: 'text/html',
//             attachmentsPattern: attachments,
//             replyTo: params.get('replyTo', ''),
//             from: params.get('from', "Jenkins <${SMTP_USERNAME}>"),
//             smtpServer: params.smtpHost,
//             smtpPort: params.smtpPort,
//             authUsername: SMTP_USERNAME,
//             authPassword: SMTP_PASSWORD,
//             charset: 'UTF-8',
//             contentType: 'text/html'
//         )
//     }
// }

import hudson.plugins.emailext.ExtendedEmailPublisherDescriptor
import jenkins.model.JenkinsLocationConfiguration

def call(Map params) {
    if (!params.credentialsId || !params.recipients || !params.subject || !params.body || !params.smtpServer || !params.smtpPort) {
        error "Missing required parameters. Ensure 'credentialsId', 'recipients', 'subject', 'body', 'smtpServer', and 'smtpPort' are provided."
    }

    def useSsl = params.get('useSsl', true)
    // def defaultReplyTo = params.get('replyTo', '')
    def defaultFrom = params.get('from', 'noreply@example.com')

    // Dynamically configure SMTP
    def emailDescriptor = Jenkins.instance.getDescriptorByType(ExtendedEmailPublisherDescriptor)
    emailDescriptor.smtpServer = params.smtpServer
    emailDescriptor.smtpPort = params.smtpPort
    emailDescriptor.useSsl = useSsl
    // emailDescriptor.defaultReplyTo = defaultReplyTo
    emailDescriptor.defaultSuffix = ''
    emailDescriptor.charset = 'UTF-8'

    JenkinsLocationConfiguration.get().adminAddress = defaultFrom

    // Set credentials for SMTP authentication
    withCredentials([usernamePassword(
        credentialsId: params.credentialsId,
        usernameVariable: 'SMTP_USERNAME',
        passwordVariable: 'SMTP_PASSWORD'
    )]) {
        emailDescriptor.smtpAuthUsername = SMTP_USERNAME
        emailDescriptor.smtpAuthPassword = SMTP_PASSWORD
        emailDescriptor.save()

        // Send email
        emailext(
            to: params.recipients,
            subject: params.subject,
            body: params.body,
            mimeType: params.get('mimeType', 'text/html'),
            // attachmentsPattern: params.get('attachments', '')
        )
    }
}
