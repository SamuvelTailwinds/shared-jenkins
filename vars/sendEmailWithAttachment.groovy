def call(Map params = [:]) {
    def credentialsId = params.credentialsId ?: 'default-smtp-credentials'
    
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'SMTP_USER', passwordVariable: 'SMTP_PASS')]) {
        
        // Write the Python script to a file
        writeFile file: 'send_email.py', text: """
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from email.mime.base import MIMEBase
from email import encoders
import os
import glob

# Retrieve environment variables
SMTP_HOST = os.getenv("SMTP_HOST", "smtp.example.com")  # Replace with your SMTP host
SMTP_PORT = int(os.getenv("SMTP_PORT", 587))           # Replace with your SMTP port
SMTP_USER = os.getenv("SMTP_USER")
SMTP_PASS = os.getenv("SMTP_PASS")
RECIPIENT_EMAILS = os.getenv("RECIPIENT_EMAILS")
BUILD_STATUS = os.getenv("BUILD_STATUS")
BUILD_ID = os.getenv("BUILD_ID")
BUILD_NUMBER = os.getenv("BUILD_NUMBER")
BUILD_TRIGGER_USER = os.getenv("BUILD_TRIGGER_USER")
BUILD_TIMESTAMP = os.getenv("BUILD_TIMESTAMP")
ATTACHMENTS_PATTERN = os.getenv("ATTACHMENTS_PATTERN")
COMMIT_ID = os.getenv("COMMIT_ID")
JOB_NAME = os.getenv("JOB_NAME", "Jenkins Pipeline")

# Create email content
email_subject = f"Jenkins Pipeline Notification and Reports - {JOB_NAME} is {BUILD_STATUS}"
email_body = f\"\"\"
Hello,

The Jenkins pipeline {JOB_NAME} has completed with the following status: {BUILD_STATUS}.

Build Details:
Build ID: {BUILD_ID}
Build Number: {BUILD_NUMBER}
Triggered by: {BUILD_TRIGGER_USER}
Commit ID: {COMMIT_ID}
Build Time: {BUILD_TIMESTAMP}

If you have any questions or need further assistance, please let us know.

Best regards,
DevOps Team.
\"\"\"

# Split recipients into a list
recipient_list = [email.strip() for email in RECIPIENT_EMAILS.split(',')]
msg = MIMEMultipart()
msg['Subject'] = email_subject
msg['From'] = SMTP_USER
msg['To'] = ', '.join(recipient_list)

# Attach email content
msg.attach(MIMEText(email_body, 'plain'))

# Attach files matching the pattern
if ATTACHMENTS_PATTERN:
    files = glob.glob(ATTACHMENTS_PATTERN)
    for file in files:
        try:
            with open(file, 'rb') as f:
                part = MIMEBase('application', 'octet-stream')
                part.set_payload(f.read())
            encoders.encode_base64(part)
            part.add_header('Content-Disposition', f'attachment; filename={os.path.basename(file)}')
            msg.attach(part)
        except Exception as e:
            print(f"Failed to attach file {file}: {e}")

try:
    server = smtplib.SMTP(SMTP_HOST, SMTP_PORT)
    server.starttls()
    server.login(SMTP_USER, SMTP_PASS)
    server.sendmail(SMTP_USER, recipient_list, msg.as_string())
    server.quit()
    print(f"Email sent successfully to {', '.join(recipient_list)}.")
except Exception as e:
    print(f"Failed to send email: {e}")
"""
        
        // Execute the Python script with the necessary environment variables
        sh """
            BUILD_STATUS="${params.BUILD_STATUS ?: currentBuild.currentResult}" \
            BUILD_ID="${params.BUILD_ID ?: env.BUILD_ID}" \
            BUILD_NUMBER="${params.BUILD_NUMBER ?: env.BUILD_NUMBER}" \
            BUILD_TRIGGER_USER="${params.BUILD_TRIGGER_USER ?: env.BUILD_TRIGGER_USER}" \
            RECIPIENT_EMAILS="${params.RECIPIENT_EMAILS ?: 'your-email@example.com'}" \
            COMMIT_ID="${params.COMMIT_ID ?: ''}" \
            BUILD_TIMESTAMP="${params.BUILD_TIMESTAMP ?: new Date().format('yyyy-MM-dd HH:mm:ss')}" \
            ATTACHMENTS_PATTERN="${params.ATTACHMENTS_PATTERN ?: ''}" \
            SMTP_USER="${SMTP_USER}" \
            SMTP_PASS="${SMTP_PASS}" \
            JOB_NAME="${params.JOB_NAME ?: env.JOB_NAME}" \
            python3 send_email.py
        """
    }
}