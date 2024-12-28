def call(Map params = [:]) {
    def buildStatus = currentBuild.currentResult
    def buildNumber = env.BUILD_NUMBER
    def triggeredBy = currentBuild.getBuildCauses().find { it.shortDescription }?.shortDescription ?: 'Unknown'
    def buildTimestamp = new Date().format('yyyy-MM-dd HH:mm:ss')
    def failureCause = currentBuild.description ?: 'No specific failure cause provided.'
    def stageStatuses = ''

    currentBuild.rawBuild.getExecution().getStages().each { stage ->
        stageStatuses += "${stage.name}: ${stage.status}\n"
    }

    def recipientEmails = params.get('recipientEmails', '')
    def credentialsId = params.credentialsId

    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'EMAIL_USER', passwordVariable: 'EMAIL_PASS')]) {
        // Generate the Python script
        def pythonScript = """
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import sys

def send_email(sender_email, sender_password, build_number, build_status, triggered_by, timestamp, failure_cause, stage_statuses, recipient_emails):
    subject = f"Pipeline Failed: Build #{build_number}"
    body = f\"\"\"
    <html>
    <body>
        <h2>Pipeline Failure Notification</h2>
        <p><strong>Build Number:</strong> {build_number}</p>
        <p><strong>Build Status:</strong> {build_status}</p>
        <p><strong>Triggered By:</strong> {triggered_by}</p>
        <p><strong>Timestamp:</strong> {timestamp}</p>
        <p><strong>Failure Cause:</strong> {failure_cause}</p>
        <p><strong>Message:</strong> Check, the pipeline failed, so the release did not happen.</p>
        <h3>Stage Statuses:</h3>
        <pre>{stage_statuses}</pre>
    </body>
    </html>
    \"\"\"

    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"] = sender_email
    msg["To"] = ", ".join(recipient_emails)

    msg.attach(MIMEText(body, "html"))

    try:
        with smtplib.SMTP("smtp.gmail.com", 587) as server:
            server.starttls()
            server.login(sender_email, sender_password)
            server.sendmail(sender_email, recipient_emails, msg.as_string())
            print("Email sent successfully.")
    except Exception as e:
        print(f"Failed to send email: {e}")

if __name__ == "__main__":
    sender_email = sys.argv[1]
    sender_password = sys.argv[2]
    build_number = sys.argv[3]
    build_status = sys.argv[4]
    triggered_by = sys.argv[5]
    timestamp = sys.argv[6]
    failure_cause = sys.argv[7]
    stage_statuses = sys.argv[8]
    recipient_emails = sys.argv[9].split(",")
    send_email(sender_email, sender_password, build_number, build_status, triggered_by, timestamp, failure_cause, stage_statuses, recipient_emails)
"""

        writeFile file: 'send_email.py', text: pythonScript

        // Execute the Python script
        sh """
        python3 send_email.py \\
            '${EMAIL_USER}' \\
            '${EMAIL_PASS}' \\
            '${buildNumber}' \\
            '${buildStatus}' \\
            '${triggeredBy}' \\
            '${buildTimestamp}' \\
            '${failureCause}' \\
            '${stageStatuses}' \\
            '${recipientEmails}'
        """
    }
}
