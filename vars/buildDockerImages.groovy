def call(Map params) {
    if (!params.containsKey('imageDefinitions') || !params.imageDefinitions) {
        error "'imageDefinitions' parameter is required and should provide a list of image definitions."
    }

    if (!params.containsKey('registryCredentialsId') || !params.registryCredentialsId) {
        error "'registryCredentialsId' parameter is required and should specify the Jenkins credentials ID for the Docker registry."
    }

    def imageDefinitions = params.imageDefinitions

    withCredentials([usernamePassword(
        credentialsId: params.registryCredentialsId,
        usernameVariable: 'DOCKER_USERNAME',
        passwordVariable: 'DOCKER_PASSWORD'
    )]) {
        imageDefinitions.each { definition ->
            def baseImageName = definition.imageName
            def contextPath = definition.contextPath
            def dockerfilePath = definition.get('dockerfilePath', 'Dockerfile')
            def buildArgs = definition.get('buildArgs', [])

            if (!baseImageName || !contextPath) {
                error "Each image definition must have 'imageName' and 'contextPath'."
            }

            def tag = "${env.BUILD_NUMBER ?: 'latest'}"
            def fullImageName = "${baseImageName}:${tag}"

            echo "Building image: ${fullImageName} from context: ${contextPath} with Dockerfile: ${dockerfilePath}"

            def buildArgsString = buildArgs.collect { "--build-arg ${it}" }.join(' ')

            sh "docker build -t ${fullImageName} -f ${contextPath}/${dockerfilePath} ${buildArgsString} ${contextPath}"

            echo "Pushing image: ${fullImageName}"
            sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
            sh "docker push ${fullImageName}"
        }
    }
}
