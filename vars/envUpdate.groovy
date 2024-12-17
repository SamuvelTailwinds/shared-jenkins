def call(String basePath, List commands) {
    if (!commands || !(commands instanceof List)) {
        error "'commands' parameter is required and must be a list of shell commands."
    }

    commands.each { cmd ->
        cd ${basePath}
        echo "Current Directory: \$(pwd)"
        echo "Executing command: ${cmd}"
        sh "${cmd}"
    }
}
