def deployToUbuntu(String targetHost, String user, String sshKeyPath, String artifactPath) {
    try {
        withSSHAgent([sshKeyPath]) {
            // SCP the artifact to the target machine
            sh "scp -o StrictHostKeyChecking=no ${artifactPath} ${user}@${targetHost}:/tmp/"

            // SSH into the target machine to unzip and deploy
            sh """
                ssh -o StrictHostKeyChecking=no ${user}@${targetHost} << 'EOF'

                # Unzip the artifacts
                echo "Unzipping artifacts..."
                mkdir -p /tmp/majordomo
                unzip /tmp/${tag}-docker-compose.zip -d /tmp/majordomo

                cd /tmp/majordomo

                # Edit the .env file (Replace environment variables dynamically)
                echo "Editing .env file with environment variables..."
                sed -i "s|DOCKER_REGISTRY=.*|DOCKER_REGISTRY=${DOCKER_REGISTRY}|" .env

                # Start services using docker-compose
                echo "Deploying services using docker-compose..."
                docker-compose up -d

                # Verify that services are running
                sleep 60  # Allow some time for deployment
                echo "Verifying deployment status..."
                if docker-compose ps | grep -q 'Up'; then
                    echo "All services are running."
                else
                    echo "Services are not running, restarting..."
                    docker-compose restart
                fi

                EOF
            """
        }
    } catch (Exception e) {
        error "Deployment process failed: ${e.message}"
    }
}
