def call(Map params) {
    def recipients = params.get('recipients', '')
    def subject = params.get('subject', 'Build Notification')
    def body = params.get('body', '')
    def attachments = params.get('attachments', [])

    if (!recipients) {
        error "'recipients' parameter is required."
    }

    def attachmentsStr = attachments.join(',')

    // Use the emailext step from the Extended Email Plugin
    emailext(
        to: recipients,
        subject: subject,
        body: body,
        attachLog: false, // Set to true if you want to include the build log
        attachmentsPattern: attachmentsStr
    )
}
