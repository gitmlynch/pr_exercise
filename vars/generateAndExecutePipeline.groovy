def call() {

    // main entry point

    try {

        _generateAndExecutePipeline()

        currentBuild.result = 'SUCCESS'

    } catch (error) {

        // notify via email/flowdock

        echo "Encountered unexpected error!\n$error"

        currentBuild.result = 'FAILED'

        throw error
    }
}

def _generateAndExecutePipeline() {
    
    lock (env.JOB_NAME) {

        timeout (time: 12, unit: 'HOURS') {

            def nodeLabel = getNodeLabel()

            node (nodeLabel) {

                try {

                    def pipeline    = null
                    def scmMetadata = null

                    stage('The Awesome Part') {

                        _assertValidBranchType()

                        scmMetadata = checkout scm

                        pipeline = _generatePipeline(scmMetadata)
                    }

                    pipeline()

                    _buildMasterIfReleaseBranch(scmMetadata)

                } finally {

                    deleteDir()
                }
            }
        }
    }
}

def _assertValidBranchType() {

    isReleaseOrPR = _isReleaseOrPr()
    isFeature= _isFeature()

    if (!(isReleaseOrPR || isFeature)) {

        error("Only 'feature/', 'release/', 'PR-' and 'master' branches can be built.")
    }
}

def _generatePipeline(scmMetadata) {

    def applicationDefinition = _getApplicationDefinition()

    _validateReleaseBranch(applicationDefinition, scmMetadata)

    def version = versionThenTag applicationDefinition: applicationDefinition

    def pipelineDefinition = _getPipelineDefinition(applicationDefinition)

    def phases = _generatePhases(pipelineDefinition, applicationDefinition, version, scmMetadata)

    def pipeline = { ->

        phases.each { phase ->

            phase()
        }
    }

    return pipeline
}

def _getApplicationDefinition() {

    def applicationDefinition = null
    
    try {

        applicationDefinition = _readApplicationDefinitionFile()

    } catch (error) {

        echo 'There were errors reading the application definition file. For more information go to https://github.com/EBSCOIS/platform.infrastructure.medusa/#application-definition-file'

        throw error
    }

    return applicationDefinition
}

def _readApplicationDefinitionFile() {

    def applicationDefinition = null

    def applicationDefinitionFilePath = _getApplicationDefinitionFilePath()

    if (fileExists(applicationDefinitionFilePath)) {

        runMedusaDataValidate command: 'application-definition', files: [applicationDefinitionFilePath]
    
        applicationDefinition = readYaml file: applicationDefinitionFilePath

    } else {
        
        error "Could not find application definition file at expected path '${applicationDefinitionFilePath}'!"
    }

    return applicationDefinition
}

def _getApplicationDefinitionFilePath() {

    return 'applicationDefinition.yaml'
}

def _validateReleaseBranch(applicationDefinition, scmMetadata) {

    if (isReleaseBranch()) {

        def majorVersion = applicationDefinition.Pipeline.MajorVersion
        def archetype    = applicationDefinition.Pipeline.Archetype

        _assertMajorVersionMatchesReleaseVersion(majorVersion)

    }
}

def _assertMajorVersionMatchesReleaseVersion(majorVersion) {

    relBranchNm = env.BRANCH_NAME.split('/')[-1]

    if (majorVersion.toString() != relBranchNm) {

        error("Major version in application definition, ${majorVersion}, does not match release branch name: ${relBranchNm}")
    }
}

def _getPipelineDefinition(applicationDefinition) {

    // do cool stuff here

    return pipelineDefinition
}

def _isReleaseOrPr() {

    def isRelease    = isRelease()
    def isPr         = _isPr()

    return isRelease || isPr
}

def _isPr() {

    return env.BRANCH_NAME ==~ /PR-\d+/
}

def _isFeature() {

    isTruFals = env.BRANCH_NAME ==~ /feature\/.+/

    return isTruFals
}

// ML - we are not using this at the moment, I think we are likely to use it sometime in the future.
def _getFirstKeyFromMap(map) {

    return (map.keySet() as List)[0]
}

def _generatePhases(pipelineDefinition, applicationDefinition, version, scmMetadata) {

    def phases = []

    // Do wicked cool stuff here

    return phases
}

def _getRepositoryName(scmMetadata) {

    return scmMetadata.GIT_URL.split(/\//)[-1][0..-5]
}

def _buildMasterIfReleaseBranch(scmMetadata) {

    if (isReleaseBranch()) {

        def repositoryName = _getRepositoryName(scmMetadata)

        build job: "${repositoryName}/master" // this will wait, AND fail if master fails
    }
}





