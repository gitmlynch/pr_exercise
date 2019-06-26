import spock.lang.Unroll

import generateAndExecutePipeline

class TestGenerateAndExecutePipeline extends PipelineStepSpecification {

    static final String DEPLOYMENT   = 'Deployment'
    static final String MASTER       = 'master'
    static final String SPEC_PR      = /PR-\d+/
    static final String SPEC_FEATURE = 'feature/.+'
    static final String FEATURE_FOO  = 'feature/foo'

    static final List SPEC_A_B = ['a', 'b']

    static final List TRUE_FALSE = [true, false]

    def setup() {

        createAndAssignStepInstance(generateAndExecutePipeline.class)
    }

    def generatePipelineDefinition() {
        
        return [Pipeline: []]
    }

    def generatePhase(name=null) {

        def phase = [
            (name ?: generateString()): [
                Steps: [
                    generateString(),
                    generateString()
                ]
            ]
        ]

        return phase
    }

    def createGenerateAndExecutePipelineMock() {

        return create0ArgMock('_generateAndExecutePipeline')
    }

    def createGetApplicationDefinitionFilePathMock() {

        return create0ArgMock('_getApplicationDefinitionFilePath')
    }

    def createGetMajorVersionMasterMock() {

        return create1ArgMock('_getMajorVersionMaster')
    }

    def createIsPrMock() {

        return create0ArgMock('_isPr')
    }

    def createIsReleaseOrPrMock() {

        return create0ArgMock('_isReleaseOrPr')
    }

    def createIsFeatureMock() {

        return create0ArgMock('_isFeature')
    }

    def 'main script calls _generateAndExecutePipeline'() {

        given:
        def mockGenerateAndExecutePipeline = createGenerateAndExecutePipelineMock()

        setCurrentBuild()

        when:
        script.call()

        then:
        1 * mockGenerateAndExecutePipeline()

        and:
        getCurrentBuild().result == 'SUCCESS'
    }

    def 'failures in _generateAndExecutePipeline bubble up'() {

        given:
        def exception = generateException()

        def mockEcho = createEchoMock()

        def mockGenerateAndExecutePipeline = createGenerateAndExecutePipelineMock()

        setCurrentBuild()

        when:
        script.call()

        then:
        1 * mockGenerateAndExecutePipeline() >> { throw exception }

        then:
        1 * mockEcho("Encountered unexpected error!\n${exception}")

        and:
        getCurrentBuild().result == 'FAILED'

        and:
        Exception actual = thrown()
        actual == exception
    }

    def '_generateAndExecutePipeline generates and...wait for it...executes the pipeline on a node'() {

        given:
        def scmMetadata = generateString()
        def job         = generateString()
        def nodeLabel   = generateString()
        def scm         = generateString()

        Closure pipeline = Mock()

        def mockCheckout     = createCheckoutMock()
        def mockDeleteDir    = createDeleteDirMock()
        def mockGetNodeLabel = createGetNodeLabelMock()

        def (mockLock, mockLeaveLock)       = createLockMock()
        def (mockTimeout, mockLeaveTimeout) = createTimeoutMock()
        def (mockNode, mockLeaveNode)       = createNodeMock()
        def (mockStage, mockLeaveStage)     = createStageMock()

        def mockSetJobProperties           = create0ArgMock('_setJobProperties')
        def mockAssertValidBranchType      = create0ArgMock('_assertValidBranchType')
        def mockAssertCodeowners           = create0ArgMock('_assertCodeowners')
        def mockAssertReleaseUsesArchetype = create0ArgMock('_assertReleaseUsesArchetype')
        def mockGeneratePipeline           = create1ArgMock('_generatePipeline')
        def mockBuildMasterIfReleaseBranch = create1ArgMock('_buildMasterIfReleaseBranch')

        setScmVariable(scm)

        setJobName(job)

        when:
        script._generateAndExecutePipeline()

        then:
        1 * mockLock(job)

        then:
        1 * mockTimeout([time: 12, unit: 'HOURS'])

        then: 
        1 * mockGetNodeLabel() >> nodeLabel

        then:
        1 * mockNode(nodeLabel)

        then:
        1 * mockStage('The Awesome Part')

        then:
        1 * mockAssertValidBranchType()

        then:
        1 * mockCheckout(scm) >> scmMetadata

        then:
        1 * mockGeneratePipeline(scmMetadata) >> pipeline

        then:
        1 * mockLeaveStage()

        then:
        1 * pipeline()

        then:
        1 * mockBuildMasterIfReleaseBranch(scmMetadata)

        then:
        1 * mockDeleteDir()

        then:
        1 * mockLeaveNode()

        then:
        1 * mockLeaveTimeout()

        then:
        1 * mockLeaveLock()
    }

    @Unroll
    def '_isReleaseOrPr returns #isReleaseOrPr when isRelease returns #isReleaseValue and _isPr returns #isPr'() {

        given:
        def mockIsRelease = createIsReleaseMock()
        def mockIsPr      = createIsPrMock()

        when:
        def actual = script._isReleaseOrPr()

        then:
        1 * mockIsRelease() >> isReleaseValue
        1 * mockIsPr() >> isPr

        then:
        actual == isReleaseOrPr

        where:
        isReleaseValue | isPr  | isReleaseOrPr
        false          | false | false
        true           | false | true
        false          | true  | true
        true           | true  | true
    }

    def '_generatePipeline returns a fully-baked, callable, parameterless pipeline'() {

        given:
        def scmMetadata           = generateString()
        def applicationDefinition = generateString()
        def pipelineDefinition    = generateString()
        def version               = generateString()

        Closure phase0 = Mock()
        Closure phase1 = Mock()

        def phases = [phase0, phase1]

        def mockVersionThenTag = createVersionThenTagMock()

        def mockGetApplicationDefinition = create0ArgMock('_getApplicationDefinition')
        def mockValidateReleaseBranch    = create2ArgMock('_validateReleaseBranch')
        def mockValidateRepositoryName   = create2ArgMock('_validateRepositoryName')
        def mockGetPipelineDefinition    = create1ArgMock('_getPipelineDefinition')
        def mockGeneratePhases           = create4ArgMock('_generatePhases')

        when:
        def pipeline = script._generatePipeline(scmMetadata)

        pipeline()

        then:
        1 * mockGetApplicationDefinition() >> applicationDefinition

        then:
        1 * mockValidateReleaseBranch(applicationDefinition, scmMetadata)

        then:
        1 * mockVersionThenTag([applicationDefinition: applicationDefinition]) >> version

        then:
        1 * mockGetPipelineDefinition(applicationDefinition) >> pipelineDefinition

        then:
        1 * mockGeneratePhases(pipelineDefinition, applicationDefinition, version, scmMetadata) >> phases

        then:
        1 * phase0()

        then:
        1 * phase1()
    }

    def '_getApplicationDefinition returns the value returned by _readApplicationDefinitionFile'() {

        given:
        def applicationDefinition = generateString()

        def mockReadApplicationDefinitionFile = createReadApplicationDefinitionFileMock()

        when:
        def actual = script._getApplicationDefinition()

        then:
        1 * mockReadApplicationDefinitionFile() >> applicationDefinition

        and:
        actual == applicationDefinition
    }

    def '_getApplicationDefinition prints a generic error when _readApplicationDefinitionFile errors, and that error bubbles up '() {

        given:
        def exception = generateException()

        def mockReadApplicationDefinitionFile = createReadApplicationDefinitionFileMock()
        def mockEcho                          = createEchoMock()

        when:
        script._getApplicationDefinition()

        then:
        1 * mockReadApplicationDefinitionFile() >> { throw exception }

        then:
        1 * mockEcho('There were errors reading the application definition file. For more information go to https://github.com/EBSCOIS/platform.infrastructure.medusa/#application-definition-file')

        and:
        Exception e = thrown()
        e == exception
    }

    def '_readApplicationDefinitionFile reads file and returns application definition data'() {

        given:
        def applicationDefinition = generateUniqueValue()
        def path                  = generateUniqueValue()

        def mockGetApplicationDefinitionFilePath = createGetApplicationDefinitionFilePathMock()
        def mockRunMedusaDataValidate            = createRunMedusaValidateMock()
        def mockFileExists                       = createFileExistsMock()
        def mockReadYaml                         = createReadYamlMock()

        when:
        def actual = script._readApplicationDefinitionFile()

        then:
        1 * mockGetApplicationDefinitionFilePath() >> path

        then:
        1 * mockFileExists(path) >> true

        then:
        1 * mockRunMedusaDataValidate([command: 'application-definition', files: [path]])

        then:
        1 * mockReadYaml([file: path]) >> applicationDefinition

        and:
        actual == applicationDefinition
    }

    def '_readApplicationDefinitionFile prints helpful error if application definition file does not exist'() {

        given:
        def exception = generateException()

        def mockGetApplicationDefinitionFilePath = createGetApplicationDefinitionFilePathMock()
        def mockFileExists                       = createFileExistsMock()
        def mockError                            = createErrorMock()

        def path = generateString()

        when:
        script._readApplicationDefinitionFile()

        then:
        1 * mockGetApplicationDefinitionFilePath() >> path

        then:
        1 * mockFileExists(path) >> false

        then:
        1 * mockError("Could not find application definition file at expected path '${path}'!") >> { throw exception }

        and:
        Exception e = thrown()
        e == exception
    }

    def '_getApplicationDefinitionFilePath returns the correct value'() {

        when:
        def actual = script._getApplicationDefinitionFilePath()

        then:
        actual == 'applicationDefinition.yaml'
    }

    @Unroll
    def '_isPr returns #isPr for branch name #branchName'() {

        given:
        setBranchName(branchName)

        when:
        def actual = script._isPr()

        then:
        actual == isPr

        where:
        branchName                    | isPr
        "PR-${generateInteger()}"     | true
        MASTER                        | false
        "feature/${generateString()}" | false
    }

    @Unroll
    def '_getFirstKeyFromMap returns the first key found in the given map #phase'() {

        when:
        def actual = script._getFirstKeyFromMap(phase)

        then:
        actual == expected

        where:
        expected << [generateString(), generateString()]
        phase = [(expected): []]
    }

    @Unroll
    def '_validateReleaseBranch validates required characteristics when isReleaseBranch is #isReleaseBranchValue'() {

        given:
        def majorVersion = generateInteger()
        
        def scmMetadata = generateUniqueValue()

        def applicationDefinition = generateApplicationDefinition()

        def archetype = applicationDefinition.Pipeline.Archetype

        applicationDefinition.Pipeline.MajorVersion = majorVersion

        def mockIsReleaseBranch = createIsReleaseBranchMock()

        def mockAssertMajorVersionMatchesReleaseVersion = create1ArgMock('_assertMajorVersionMatchesReleaseVersion')
        def mockAssertValidReleaseBranchArchetype       = create1ArgMock('_assertValidReleaseBranchArchetype')
        def mockAssertAheadOfMaster                     = create2ArgMock('_assertAheadOfMaster')

        when:
        script._validateReleaseBranch(applicationDefinition, scmMetadata)

        then:
        1 * mockIsReleaseBranch() >> isReleaseBranchValue

        then:
        interactions * mockAssertMajorVersionMatchesReleaseVersion(majorVersion)
        interactions * mockAssertValidReleaseBranchArchetype(archetype)
        interactions * mockAssertAheadOfMaster(majorVersion, scmMetadata)

        where:
        isReleaseBranchValue << TRUE_FALSE
        interactions = isReleaseBranchValue ? 1 : 0
    }

    def '_assertMajorVersionMatchesReleaseVersion throws an error when major version and release branch name mismatch'() {

        given:
        def exception = generateException()

        def majorVersion      = generateInteger()
        def releaseBranchName = majorVersion + 1

        def mockError = createErrorMock()

        setBranchName("release/${releaseBranchName}")

        when:
        script._assertMajorVersionMatchesReleaseVersion(majorVersion)

        then:
        1 * mockError("Major version in application definition, ${majorVersion}, does not match release branch name: ${releaseBranchName}") >> { throw exception }

        and:
        Exception actual = thrown()
        actual == exception
    }

    def '_assertMajorVersionMatchesReleaseVersion does not throw an error when major version and release branch name match'() {

        given:
        def majorVersion = generateInteger()

        def mockError = createErrorMock()

        setBranchName("release/${majorVersion}")

        when:
        script._assertMajorVersionMatchesReleaseVersion(majorVersion)

        then:
        0 * mockError(_)
    }

    def '_assertValidBranchType throws an error when the branch is not feature/, release/, PR-, or master'() {

        given:
        def exception = generateException()

        def mockIsReleaseOrPR = createIsReleaseOrPrMock()
        def mockIsFeature     = createIsFeatureMock()
        def mockError         = createErrorMock()

        setBranchName(generateString())

        when:
        script._assertValidBranchType()

        then:
        1 * mockIsReleaseOrPR() >> false
        1 * mockIsFeature() >> false

        then:
        1 * mockError("Only 'feature/', 'release/', 'PR-' and 'master' branches can be built.") >> { throw exception }

        and:
        Exception actual = thrown()
        actual == exception
    }

    @Unroll
    def '_assertValidBranchType does not throw an error when _isReleaseOrPR returns #isReleaseOrPr and _isFeature returns #isFeature'() {

        given:
        def mockIsReleaseOrPR = createIsReleaseOrPrMock()
        def mockIsFeature     = createIsFeatureMock()
        def mockError         = createErrorMock()

        setBranchName(generateString())

        when:
        script._assertValidBranchType()

        then:
        1 * mockIsReleaseOrPR() >> isReleaseOrPr
        1 * mockIsFeature() >> isFeature

        then:
        0 * mockError(*_)

        where:
        isReleaseOrPr | isFeature
        true          | false
        false         | true
    }

    @Unroll
    def '_isFeature returns #isFeature for branch name #branchName'() {

        given:
        setBranchName(branchName)

        when:
        def actual = script._isFeature()

        then:
        actual == isFeature

        where:
        branchName                                | isFeature
        "feature/${generateString()}"             | true
        "${generateString()}/${generateString()}" | false
        MASTER                                    | false
        "PR-${generateInteger()}"                 | false
        generateString()                          | false
    }

    def '_getRepositoryName returns the repository name part of given scm metadata'() {

        given:
        def repoName = generateString()

        def scmMetadata = [
            GIT_URL: "http://${generateString()}/${generateString()}/${repoName}.git"
        ]

        when:
        def actual = script._getRepositoryName(scmMetadata)

        then:
        actual == repoName
    }

    @Unroll
    def '_buildMasterIfReleaseBranch builds the master branch #interactions times when isReleaseBranch returns #isReleaseBranchValue'() {

        given:
        def name        = generateString()
        def scmMetadata = generateString()

        def mockIsReleaseBranch   = createIsReleaseBranchMock()
        def mockGetRepositoryName = createGetRepositoryNameMock()
        def mockBuild             = createBuildMock()

        when:
        script._buildMasterIfReleaseBranch(scmMetadata)

        then:
        1 * mockIsReleaseBranch() >> isReleaseBranchValue

        then:
        interactions * mockGetRepositoryName(scmMetadata) >> name

        then:
        interactions * mockBuild([job: "${name}/master"])

        where:
        isReleaseBranchValue << TRUE_FALSE
        interactions = isReleaseBranchValue ? 1 : 0
    }
}
