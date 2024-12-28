@Library('md-shared-library') _

pipeline {
    agent any
    parameters {
        // 'MD Services'(nginx,manager,sse), 'Infra Services'(), 'Monitoring'(), 'IAM Services'(), 'Base Image'
        choice(name: 'GIT_BRANCH', choices: ['main', 'development-testing'], description: 'Select the Git branch to build')
        choice(name: 'BUILD_TYPE', choices: ['All Services', 'MD Services', 'Infra Services', 'Monitoring Services', 'IAM Services', 'Base Image'], description: 'Select the type of build to run')
        string(name: 'SNAPSHOT_TAG', defaultValue: '', description: 'Provide Majordomo snapshot build image tag')
        string(name: 'IFF_TAG', defaultValue: '', description: 'Provide IFF snapshot build image tag')
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
                    // Add .env file updates based on TARGET_CUSTOMER
                    sh"echo '# Tailwinds\nREACT_APP_MAJORDOMO=Majordomo\nREACT_APP_IMG=./images/taiwindicon.png\nREACT_APP_TITLE=Tailwinds' > navigator-ui/.env"
                    def commandSet = [
                        // Install Node.js v14 and npm
                        "apt-get update && apt-get install -y nodejs npm",                      
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
                        sonarProjectKeyPrefix: 'md-',
                        sonarHostUrl: env.SONAR_URL
                    )
                }
            }
        }
        stage('Build Docker Images') {
            steps {
                script {
                    echo '==========Building docker images...=========='
                    // Check and build MD Services
                    if (params.BUILD_TYPE == 'MD Services' || params.BUILD_TYPE == 'All') {
                        println "Building MD Services..."
                        def buildResult = buildDockerImages(imageName: env.SERVICE_01, contextPath: env.CONTEXT_PATH_01, dockerfilePath: env.DOCKERFILE_PATH_01, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_02, contextPath: env.CONTEXT_PATH_02, dockerfilePath: env.DOCKERFILE_PATH_02, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_03, contextPath: env.CONTEXT_PATH_03, dockerfilePath: env.DOCKERFILE_PATH_03, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                    }

                    // Check and build Monitoring Services
                    if (params.BUILD_TYPE == 'Monitoring Services' || params.BUILD_TYPE == 'All Services') {
                        println "Building Monitoring Services..."
                        def buildResult = buildDockerImages(imageName: env.SERVICE_04, contextPath: env.CONTEXT_PATH_04, dockerfilePath: env.DOCKERFILE_PATH_04, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_05, contextPath: env.CONTEXT_PATH_05, dockerfilePath: env.DOCKERFILE_PATH_05, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_06, contextPath: env.CONTEXT_PATH_06, dockerfilePath: env.DOCKERFILE_PATH_06, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                    }

                    // Check and build IAM Services
                    if (params.BUILD_TYPE == 'IAM Services' || params.BUILD_TYPE == 'All') {
                        println "Building IAM Services..."
                        def buildResult = buildDockerImages(imageName: env.SERVICE_07, contextPath: env.CONTEXT_PATH_07, dockerfilePath: env.DOCKERFILE_PATH_07, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_08, contextPath: env.CONTEXT_PATH_08, dockerfilePath: env.DOCKERFILE_PATH_08, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                    }

                    // Check and build Infra Services
                    if (params.BUILD_TYPE == 'Infra Services' || params.BUILD_TYPE == 'All') {
                        println "Building Infra Services..."
                        def buildResult = buildDockerImages(imageName: env.SERVICE_09, contextPath: env.CONTEXT_PATH_09, dockerfilePath: env.DOCKERFILE_PATH_09, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_10, contextPath: env.CONTEXT_PATH_10, dockerfilePath: env.DOCKERFILE_PATH_10, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_11, contextPath: env.CONTEXT_PATH_11, dockerfilePath: env.DOCKERFILE_PATH_11, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_12, contextPath: env.CONTEXT_PATH_12, dockerfilePath: env.DOCKERFILE_PATH_12, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                        buildResult = buildDockerImages(imageName: env.SERVICE_13, contextPath: env.CONTEXT_PATH_13, dockerfilePath: env.DOCKERFILE_PATH_13, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                    }

                    // Check and build Base Image
                    if (params.BUILD_TYPE == 'Base Image') {
                        println "Building Base Image..."
                        def buildResult = buildDockerImages(imageName: env.SERVICE_14, contextPath: env.CONTEXT_PATH_14, dockerfilePath: env.DOCKERFILE_PATH_14, registryCredentialsId: env.DOCKER_CREDENTIALS)
                        echo "Built and Pushed Image: ${buildResult.fullImageName}"
                    }
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                script {
                    echo '==========Scanning docker images using trivy tool...=========='
                    if (params.BUILD_TYPE == 'MD Services' || params.BUILD_TYPE == 'All') {
                        trivyImageScan(imageName: env.SERVICE_01, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_02, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_03, reportDir: 'trivy-reports')
                    }
                    if (params.BUILD_TYPE == 'Monitoring Services' || params.BUILD_TYPE == 'All') {
                        trivyImageScan(imageName: env.SERVICE_04, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_05, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_06, reportDir: 'trivy-reports')
                    }
                    if (params.BUILD_TYPE == 'IAM Services' || params.BUILD_TYPE == 'All') {
                        trivyImageScan(imageName: env.SERVICE_07, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_08, reportDir: 'trivy-reports')
                    }
                    if (params.BUILD_TYPE == 'Infra Services' || params.BUILD_TYPE == 'All') {
                        trivyImageScan(imageName: env.SERVICE_09, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_10, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_11, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_12, reportDir: 'trivy-reports')
                        trivyImageScan(imageName: env.SERVICE_13, reportDir: 'trivy-reports')
                    }
                    if (params.BUILD_TYPE == 'Base Image') {
                        trivyImageScan(imageName: env.SERVICE_14, reportDir: 'trivy-reports')
                    }
                }
            }
        }
        stage('Push for MD') {
            steps {
                script {  
                    echo '==========Pushing docker images for Snapshot ...=========='
                    env.DOCKER_IMAGE_TAG_1 = params.SNAPSHOT_TAG ? params.SNAPSHOT_TAG : "4.0.2.Snapshot.${env.BUILD_NUMBER}LTS"
                                        
                    if (params.BUILD_TYPE == 'MD Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_01, contextPath: env.CONTEXT_PATH_01, dockerfilePath: env.DOCKERFILE_PATH_01, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_02, contextPath: env.CONTEXT_PATH_02, dockerfilePath: env.DOCKERFILE_PATH_02, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_03, contextPath: env.CONTEXT_PATH_03, dockerfilePath: env.DOCKERFILE_PATH_03, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                    if (params.BUILD_TYPE == 'Monitoring Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_04, contextPath: env.CONTEXT_PATH_04, dockerfilePath: env.DOCKERFILE_PATH_04, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_05, contextPath: env.CONTEXT_PATH_05, dockerfilePath: env.DOCKERFILE_PATH_05, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_06, contextPath: env.CONTEXT_PATH_06, dockerfilePath: env.DOCKERFILE_PATH_06, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                    if (params.BUILD_TYPE == 'IAM Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_07, contextPath: env.CONTEXT_PATH_07, dockerfilePath: env.DOCKERFILE_PATH_07, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_08, contextPath: env.CONTEXT_PATH_08, dockerfilePath: env.DOCKERFILE_PATH_08, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                    if (params.BUILD_TYPE == 'Infra Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_09, contextPath: env.CONTEXT_PATH_09, dockerfilePath: env.DOCKERFILE_PATH_09, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_10, contextPath: env.CONTEXT_PATH_10, dockerfilePath: env.DOCKERFILE_PATH_10, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_11, contextPath: env.CONTEXT_PATH_11, dockerfilePath: env.DOCKERFILE_PATH_11, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_12, contextPath: env.CONTEXT_PATH_12, dockerfilePath: env.DOCKERFILE_PATH_12, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_13, contextPath: env.CONTEXT_PATH_13, dockerfilePath: env.DOCKERFILE_PATH_13, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                    if (params.BUILD_TYPE == 'Base Image') {
                        dockerImagesPush([imageName: env.SERVICE_14, contextPath: env.CONTEXT_PATH_14, dockerfilePath: env.DOCKERFILE_PATH_14, imageTag: env.DOCKER_IMAGE_TAG_1, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                }
            }
        }
        stage('Push for IFF') {
            steps {
                script {         
                    env.DOCKER_IMAGE_TAG_2 = params.IFF_TAG ? params.IFF_TAG : "4.0.2.Iff.${env.BUILD_NUMBER}LTS"       
                    echo '==========Pushing docker images for IFF...=========='
                    sh"echo '# IFF\nREACT_APP_MAJORDOMO=Cloud Conductor\nREACT_APP_IMG=./images/iff.png\nREACT_APP_TITLE=Cloud Conductor' > navigator-ui/.env" 
                    def buildResult = buildDockerImages(imageName: env.SERVICE_1, contextPath: env.CONTEXT_PATH_14, dockerfilePath: env.DOCKERFILE_PATH_14, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS,)
                    echo "Built and Pushed Image: ${buildResult.fullImageName}"

                    if (params.BUILD_TYPE == 'MD Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_01, contextPath: env.CONTEXT_PATH_01, dockerfilePath: env.DOCKERFILE_PATH_01, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_02, contextPath: env.CONTEXT_PATH_02, dockerfilePath: env.DOCKERFILE_PATH_02, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_03, contextPath: env.CONTEXT_PATH_03, dockerfilePath: env.DOCKERFILE_PATH_03, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                    if (params.BUILD_TYPE == 'Monitoring Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_04, contextPath: env.CONTEXT_PATH_04, dockerfilePath: env.DOCKERFILE_PATH_04, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_05, contextPath: env.CONTEXT_PATH_05, dockerfilePath: env.DOCKERFILE_PATH_05, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_06, contextPath: env.CONTEXT_PATH_06, dockerfilePath: env.DOCKERFILE_PATH_06, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                    if (params.BUILD_TYPE == 'IAM Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_07, contextPath: env.CONTEXT_PATH_07, dockerfilePath: env.DOCKERFILE_PATH_07, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_08, contextPath: env.CONTEXT_PATH_08, dockerfilePath: env.DOCKERFILE_PATH_08, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }
                    if (params.BUILD_TYPE == 'Infra Services' || params.BUILD_TYPE == 'All') {
                        dockerImagesPush([imageName: env.SERVICE_09, contextPath: env.CONTEXT_PATH_09, dockerfilePath: env.DOCKERFILE_PATH_09, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_10, contextPath: env.CONTEXT_PATH_10, dockerfilePath: env.DOCKERFILE_PATH_10, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_11, contextPath: env.CONTEXT_PATH_11, dockerfilePath: env.DOCKERFILE_PATH_11, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_12, contextPath: env.CONTEXT_PATH_12, dockerfilePath: env.DOCKERFILE_PATH_12, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                        dockerImagesPush([imageName: env.SERVICE_13, contextPath: env.CONTEXT_PATH_13, dockerfilePath: env.DOCKERFILE_PATH_13, imageTag: env.DOCKER_IMAGE_TAG_2, dockerRegistry: env.DOCKER_REGISTRY, registryCredentialsId: env.DOCKER_CREDENTIALS])
                    }

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
        // stage('Deploy to vm') {
        //     steps {
        //         script {
        //             echo '==========Deploy majordomo to vm...=========='
        //             deployToUbuntu(
        //                 registryCredentialsId: env.DOCKER_CREDENTIALS, 
        //                 targetHost: env.TARGET_HOST, 
        //                 vmCredentials: env.VM_CREDENTIALS_ID, 
        //                 artifactPath: env.ARTIFACT_PATH, 
        //                 releaseTag: env.DOCKER_IMAGE_TAG_1
        //             )
        //         }
        //     }
        // }
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
            cleanupDockerImages(cleanupImages: env.CLEANUP_DOCKER_IMAGES)
            cleanWorkspace(excludePatterns: ['reports', 'logs'])
            cleanWs()
        }
    }
    success {
        script {
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
        }
    }
        failure {
            script {
                sendEmail(
                    recipientEmails: ENV.EMAIL_ALERT,
                    credentialsId: env.EMAIL_CREDENTIALS_ID,
                )
            }
        }
    }
}

