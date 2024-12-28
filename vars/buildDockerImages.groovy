def call(Map params) {
    if (!params.containsKey('imageName') || !params.imageName) {
        error "'imageName' parameter is required and should specify the name of the Docker image."
    }

    if (!params.containsKey('contextPath') || !params.contextPath) {
        error "'contextPath' parameter is required and should specify the build context directory."
    }

    // if (!params.containsKey('dockerRegistry') || !params.dockerRegistry) {
    //     error "'dockerRegistry' parameter is required and should specify the Docker registry."
    // }

    if (!params.containsKey('registryCredentialsId') || !params.registryCredentialsId) {
        error "'registryCredentialsId' parameter is required and should specify the Jenkins credentials ID for the Docker registry."
    }

    def imageName = params.imageName
    def contextPath = params.contextPath
    def dockerfilePath = params.dockerfilePath ?: 'Dockerfile'
    // def dockerRegistry = params.dockerRegistry
    // def imageTag = params.imageTag ?: 'latest'

    def fullImageName = "${imageName}:latest"

    withCredentials([usernamePassword(
        credentialsId: params.registryCredentialsId,
        usernameVariable: 'DOCKER_USERNAME',
        passwordVariable: 'DOCKER_PASSWORD'
    )]) {
        // Login to Docker registry
        sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"

        // Build the Docker image
        echo "Building image: ${fullImageName} from context: ${contextPath} with Dockerfile: ${dockerfilePath}"
        sh "docker build -f ${dockerfilePath} -t ${fullImageName} ${contextPath}"

        echo "${fullImageName} Image Build is ready"
    }

    return [
        fullImageName: fullImageName,
        registry: dockerRegistry
    ]
}
