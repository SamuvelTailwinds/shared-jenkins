/**
 * Updates the .env file, builds multiple Go binaries, and copies them to the target directory.
 *
 * @param params Map containing:
 *               - targetCustomer: Customer name (e.g., 'Tailwinds', 'IFF').
 *               - envFilePath: Path to the .env file to create/update.
 *               - goProjects: List of maps containing:
 *                   - projectPath: Path to the Go project to build.
 *                   - binaryName: Name of the output binary.
 *                   - buildCommand: Optional custom Go build command.
 *                   - postCommand: Optional command to execute after building.
 *               - binaryOutputPath: Directory to place all the final binary files.
 */
def call(Map params) {
    // Validate required parameters
    if (!params.targetCustomer) {
        error "'targetCustomer' parameter is required."
    }
    if (!params.envFilePath) {
        error "'envFilePath' parameter is required."
    }
    if (!params.goProjects || !(params.goProjects instanceof List)) {
        error "'goProjects' must be a list of Go projects with their configurations."
    }
    if (!params.binaryOutputPath) {
        error "'binaryOutputPath' parameter is required."
    }

    // Step 1: Update .env File
    def envContent = ""
    switch (params.targetCustomer) {
        case 'Tailwinds':
            envContent = '''# Tailwinds
REACT_APP_MAJORDOMO=Majordomo
REACT_APP_IMG=./images/taiwindicon.png
REACT_APP_TITLE=Tailwinds
'''
            break
        case 'IFF':
            envContent = '''# IFF
REACT_APP_MAJORDOMO=Cloud Conductor 
REACT_APP_IMG=./images/iff.png
REACT_APP_TITLE=Cloud Conductor
'''
            break
        default:
            error "Unsupported target customer: ${params.targetCustomer}"
    }

    echo "Updating .env file at: ${params.envFilePath}"
    writeFile file: params.envFilePath, text: envContent
    sh "cat ${params.envFilePath}"

    // Step 2: Build Go Projects and Execute Commands
    params.goProjects.each { project ->
        if (!project.projectPath || !project.binaryName) {
            error "Each project must have 'projectPath' and 'binaryName'."
        }

        echo "Building Go project at: ${project.projectPath}"
        def buildCommand = project.buildCommand ?: "cd ${project.projectPath} && go build -o bin/${project.binaryName} main.go"
        sh buildCommand

        // Execute post-build command if provided
        if (project.postCommand) {
            echo "Executing post-build command for ${project.binaryName}..."
            sh project.postCommand
        }

        // Copy binary to output directory
        def copyCommand = """
            mkdir -p ${params.binaryOutputPath} && \
            cp ${project.projectPath}/bin/${project.binaryName} ${params.binaryOutputPath}/
        """
        echo "Copying binary '${project.binaryName}' to: ${params.binaryOutputPath}"
        sh copyCommand
    }

    // Confirm binaries in output path
    echo "Binaries in output directory:"
    sh "ls -l ${params.binaryOutputPath}"
}
