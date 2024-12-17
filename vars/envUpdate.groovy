def call(Map params) {
    if (!params.containsKey('basePath') || !params.basePath) {
        error "'basePath' parameter is required."
    }
    if (!params.containsKey('commands') || !params.commands) {
        error "'commands' parameter is required and should contain a list of commands."
    }

    def basePath = params.basePath
    def commands = params.commands

    commands.each { cmd ->
        cd ${basePath}
        echo "Current Directory: \$(pwd)"
        echo "Executing command: ${cmd}"
        sh "${cmd}"
    }
}

