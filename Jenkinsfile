@Library('md-shared-library') _

pipeline {
    agent any
    parameters {
        choice(name: 'GIT_BRANCH', choices: ['main', 'development-testing'], description: 'Select the Git branch to build')
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'Provide image tag')
        string(name: 'BUILD_TRIGGER_USER', defaultValue: '', description: 'Name of the user triggering the build')
    }
    environment {
        EMAIL_CREDENTIALS_ID     = '' 
        EMAIL_RECIPIENTS         = ''           
        EMAIL_ALERT              = ''           
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
                    def commandSet = [
                        // Install Node.js v14 and npm
                        "apt-get update && apt-get install -y nodejs npm",                      
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
                        sonarProjectKeyPrefix: 'project-',
                        sonarHostUrl: env.SONAR_URL
                    )
                }
            }
        }
        stage('Build Docker Images') {
            steps {
                script {
                    echo '==========Building docker images...=========='
                        def buildResult = buildDockerImages(imageName: env.SERVICE_01, contextPath: env.CONTEXT_PATH_01, dockerfilePath: env.DOCKERFILE_PATH_01, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                    }
                }
            }
        stage('Trivy Scan') {
            steps {
                script {
                    echo '==========Scanning docker images using trivy tool...=========='
                        trivyImageScan(imageName: env.SERVICE_01, reportDir: 'trivy-reports')
                    }
                }
            }
        stage('Docker Push') {
            steps {
                script {  
                    echo '==========Pushing docker images for Snapshot ...=========='
                    dockerImagesPush([imageName: env.SERVICE_01, contextPath: env.CONTEXT_PATH_01, dockerfilePath: env.DOCKERFILE_PATH_01, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                }
            }
        }
        stage('Git Release') {
            steps {
                script {
                    echo '==========Git Release with Artifacts...=========='
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
                    echo '==========Deploy majordomo to vm...=========='
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
        // stage('Run Selenium Tests') {
        //     steps {
        //         echo 'Running Selenium tests...'
        //         runTests(
        //             testDir: "${env.TEST_DIR}",
        //             resultsDir: "${env.RESULTS_DIR}",
        //             browser: "${env.BROWSER}",
        //             additionalArgs: "${env.ADDITIONAL_ARGS}"
        //         )
        //     }
        // }

    }
post {
    always {
        script {
            archiveArtifacts artifacts: 'trivy-reports/*.json', allowEmptyArchive: true
            archiveArtifacts artifacts: 'sonar-reports/*.json', allowEmptyArchive: true
            sendEmailWithAttachment(
                BUILD_STATUS: currentBuild.currentResult,
                BUILD_ID: env.BUILD_ID,
                BUILD_NUMBER: env.BUILD_NUMBER,
                BUILD_TRIGGER_USER: params.BUILD_TRIGGER_USER,
                COMMIT_ID: env.COMMIT_ID,
                BUILD_TIMESTAMP: new Date().format('yyyy-MM-dd HH:mm:ss'),
                RECIPIENT_EMAILS: env.EMAIL_RECIPIENTS,
                ATTACHMENTS_PATTERN: '*.zip',
                credentialsId: env.EMAIL_CREDENTIALS_ID,
                JOB_NAME: env.JOB_NAME
            )
            cleanupDockerImages(cleanupImages: env.CLEANUP_DOCKER_IMAGES)
            cleanWorkspace(excludePatterns: ['reports', 'logs'])
            cleanWs()
            }
        }
    }
}

