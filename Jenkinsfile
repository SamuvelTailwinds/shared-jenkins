@Library('md-shared-library') _

pipeline {
    agent any
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
        stage('Build Docker Images') {
            steps {
                script {  
                    echo '==============Starting the build process...=================' 
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
                    deployToUbuntu(registryCredentialsId: env.DOCKER_CREDENTIALS, targetHost: env.TARGET_HOST, vmCredentials: env.VM_CREDENTIALS_ID, artifactPath: env.ARTIFACT_PATH, releaseTag: env.DOCKER_IMAGE_TAG_1)
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
