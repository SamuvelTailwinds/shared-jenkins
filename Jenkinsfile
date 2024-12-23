@Library('md-shared-library') _

pipeline {
    agent any
    parameters {
        choice(name: 'BUILD_TYPE'choices: ['All', 'Infra Services', 'MD Services', 'Monitoring Services', 'IAM Services', 'Base Image'], description: 'Select the type of build to run')
        string(name: 'BUILD_TRIGGER_USER', defaultValue: '', description: 'Name of the user triggering the build')
    }
    environment {
        EMAIL_CREDENTIALS_ID     = '' 
        EMAIL_RECIPIENTS         = ''           
        SMTP_HOST                = 'smtp.gmail.com'                 // SMTP host (e.g., Gmail: smtp.gmail.com)
        SMTP_PORT                = '465'                              // SMTP port (587 for TLS, 465 for SSL)
        BASE_PATH                ='./navigator-manager/container_builds/'
        SONAR_CREDENTIAL         =''
        GIT_CREDENTIAL          =''
        GIT_REPO_URLS           ='' // Provide git repo URLs, separated by commas.
        GIT_BRANCH              ='main'
        RELEAS_GIT_REPO         =''
        SOURCE_PATH             =''
        ARTIFACT_NAME           =''
        TARGET_HOST             =''
        VM_CREDENTIALS_ID       =''
        ARTIFACT_PATH           =''
        DOCKER_CREDENTIALS      =''
        DOCKER_REGISTRY         =''
        DOCKER_IMAGE_TAG_1      =''
        DOCKER_IMAGE_TAG_2      =''
        CLEANUP_DOCKER_IMAGES   =true //True will remove the docker images in post declarations.
        SERVICE_01              =''
        SERVICE_02              =''
        SERVICE_03              =''
        CONTEXT_PATH_01         ='.'
        CONTEXT_PATH_02         ='.'
        CONTEXT_PATH_03         ='.' 
        DOCKERFILE_PATH_01      ='Dcokerfile'   
        DOCKERFILE_PATH_02      ='Dcokerfile'   
        DOCKERFILE_PATH_03      ='Dcokerfile'   
        }
    stages {
        stage('Clone Repositories') {
            steps {
                script {
                    echo '==========Checking out code from repository...=========='
                    cloneMultipleRepos(envVariable: env.GIT_REPO_URLS, branch: env.GIT_BRANCH, credentialsId: env.GIT_CREDENTIAL, depth: 1)
                }
            }
        }
        stage('.env Update') {
            steps {
                script {
                    echo '==========.env file update for nginx image and bin file creations...=========='
                    // Add .env file updates based on TARGET_CUSTOMER
                    if ('IFF' == 'Tailwinds') {
                        sh"echo '# Tailwinds\nREACT_APP_MAJORDOMO=Majordomo\nREACT_APP_IMG=./images/taiwindicon.png\nREACT_APP_TITLE=Tailwinds' > navigator-ui/.env"
                    } else if ('IFF' == 'IFF') {
                        sh"echo '# IFF\nREACT_APP_MAJORDOMO=Cloud Conductor\nREACT_APP_IMG=./images/iff.png\nREACT_APP_TITLE=Cloud Conductor' > navigator-ui/.env"
                    }
                    def commandSet = [
                        // Install Node.js v14 and npm
                        'curl -sL https://deb.nodesource.com/setup_14.x | sudo -E bash -'
                        'apt-get install -y nodejs'
                        
                        "cd ../../navigator-ui && npm i --force",
                        "cd ../../navigator-ui && npm run build --force",
                        "cp -r ../../navigator-ui/dist dist",
                        "cp -r ../../navigator-ui/nginx nginx",
                        // "rm -rf dist nginx",
                        "cd .. && go build -o bin/tailwinds-lgen github.com/tailwinds/navigator/pkg/license",
                        "cd ../bin && ./tailwinds-lgen admin@tailwinds.ai 120",
                        "cd .. && go build -o bin/navigator-manager github.com/tailwinds/navigator/cmd/startup",
                        "cd ../../majordomo-sse && go build -o bin/majordomo-sse main.go",
                        "cp ../../majordomo-sse/bin/majordomo-sse ../bin"
                    ]

                    envUpdate(
                        basePath: env.BASE_PATH,
                        commands: commandSet
                    )
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                script {
                    echo '==========Running SonarQube Analysis...=========='
                    scanWithSonarQube(
                        envVariable: env.GIT_REPO_URLS,
                        credentialsId: env.SONAR_CREDENTIAL,
                        sonarProjectKeyPrefix: 'md-'
                    )
                }
            }
        }
        stage('Build: MD Services') {
            when {
                expression { return params.BUILD_TYPE == 'MD Services' || params.BUILD_TYPE == 'All' }
            }
            steps {
                script {
                    imageDefinitions = [
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_01, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_01, dockerfilePath: env.DOCKERFILE_PATH_01],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_02, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_02, dockerfilePath: env.DOCKERFILE_PATH_02],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_03, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_03, dockerfilePath: env.DOCKERFILE_PATH_03],
                    ]
                    
                    buildDockerImages([
                        imageDefinitions: imageDefinitions,
                        registryCredentialsId: env.DOCKER_CREDENTIALS

                    ])
                }
            }
        }
        stage('Build: Monitoring Services') {
            when {
                expression { return params.BUILD_TYPE == 'Monitoring Services' || params.BUILD_TYPE == 'All' }
            }
            steps {
                script {
                    imageDefinitions = [
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_04, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_04, dockerfilePath: env.DOCKERFILE_PATH_04],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_05, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_05, dockerfilePath: env.DOCKERFILE_PATH_05],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_06, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_06, dockerfilePath: env.DOCKERFILE_PATH_06],
                    ]
                    
                    buildDockerImages([
                        imageDefinitions: imageDefinitions,
                        registryCredentialsId: env.DOCKER_CREDENTIALS

                    ])
                }
            }
        }
        stage('Build: IAM Services') {
            when {
                expression { return params.BUILD_TYPE == 'IAM Services' || params.BUILD_TYPE == 'All' }
            }
            steps {
                script {
                    imageDefinitions = [
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_07, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_07, dockerfilePath: env.DOCKERFILE_PATH_07],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_08, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_08, dockerfilePath: env.DOCKERFILE_PATH_08],
                    ]
                    
                    buildDockerImages([
                        imageDefinitions: imageDefinitions,
                        registryCredentialsId: env.DOCKER_CREDENTIALS

                    ])
                }
            }
        }
        stage('Build: Infra Services') {
            when {
                expression { return params.BUILD_TYPE == 'Infra Services' || params.BUILD_TYPE == 'All' }
            }
            steps {
                script {
                    imageDefinitions = [
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_09, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_09, dockerfilePath: env.DOCKERFILE_PATH_09],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_10, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_10, dockerfilePath: env.DOCKERFILE_PATH_10],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_11, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_11, dockerfilePath: env.DOCKERFILE_PATH_11],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_12, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_12, dockerfilePath: env.DOCKERFILE_PATH_12],
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_13, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_13, dockerfilePath: env.DOCKERFILE_PATH_13],
                    ]
                    
                    buildDockerImages([
                        imageDefinitions: imageDefinitions,
                        registryCredentialsId: env.DOCKER_CREDENTIALS

                    ])
                }
            }
        }
        stage('Build: Base Image') {
            when {
                expression { return params.BUILD_TYPE == 'Base Image'}
            }
            steps {
                script {
                    imageDefinitions = [
                        [dockerRegistry: env.DOCKER_REGISTRY, imageName: env.SERVICE_14, imageTag: env.DOCKER_IMAGE_TAG_1, contextPath: env.CONTEXT_PATH_14, dockerfilePath: env.DOCKERFILE_PATH_14],
                    ]
                    
                    buildDockerImages([
                        imageDefinitions: imageDefinitions,
                        registryCredentialsId: env.DOCKER_CREDENTIALS

                    ])
                }
            }
        }
        stage('Trivy Scan') {
            steps {
                script {
                    trivyImageScan(
                        imageDefinitions: imageDefinitions, 
                        reportDir: 'trivy-reports')
                }
            }
        }
        stage('Push Docker Images') {
            steps {
                script {                    
                    dockerImagesPush([
                        imageDefinitions: imageDefinitions,
                        registryCredentialsId: env.DOCKER_CREDENTIALS

                    ])
                }
            }
        }
        stage('Git Release') {
            steps {
                script {
                    createGitRelease(
                        gitRepo: env.RELEAS_GIT_REPO,
                        releaseTag: env.DOCKER_IMAGE_TAG_1,
                        extraTag: env.DOCKER_IMAGE_TAG_2,
                        artifactName: env.ARTIFACT_NAME,
                        sourcePath: env.SOURCE_PATH,
                        githubTokenId: env.GIT_CREDENTIAL,
                        releaseName: 'MajorDomo $DOCKER_IMAGE_TAG_1',
                        releaseDescription: 'This is the release description'
                    )
                }
            }
        }
        stage('Deploy to vm') {
            steps {
                script {
                    deployToUbuntu(
                        registryCredentialsId: env.DOCKER_CREDENTIALS, 
                        targetHost: env.TARGET_HOST, 
                        vmCredentials: env.VM_CREDENTIALS_ID, 
                        artifactPath: env.ARTIFACT_PATH, 
                        releaseTag: env.DOCKER_IMAGE_TAG_1
                    )
                }
            }
        }
        stage('Run Selenium Tests') {
            steps {
                echo 'Running Selenium tests...'
                runTests(
                    testDir: "${env.TEST_DIR}",
                    resultsDir: "${env.RESULTS_DIR}",
                    browser: "${env.BROWSER}",
                    additionalArgs: "${env.ADDITIONAL_ARGS}"
                )
            }
        }

    }
    post {
        always {
            script {
                archiveArtifacts artifacts: 'trivy-reports/*.json', allowEmptyArchive: true
                sendEmailWithAttachment(
                    recipients: 'team@example.com',
                    subject: "Build #${env.BUILD_NUMBER} Status",
                    body: """<p>The build has completed. Please find the attached reports.</p>""",
                    attachments: ['reports/*.json']
                )
                cleanupDockerImages(cleanupImages: env.CLEANUP_DOCKER_IMAGES)
                cleanWorkspace(excludePatterns: ['reports', 'logs'])
            }
        }
    }

}
