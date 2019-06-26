import spock.lang.Unroll

import isReleaseBranch

class TestIsReleaseBranch extends PipelineStepSpecification {

    def setup() {

        createAndAssignStepInstance(isReleaseBranch.class)
    }

    @Unroll
    def 'isReleaseBranch returns #expected when current branch is #branch'() {

        given:
        setBranchName(branch)

        when:
        def actual = script.call()

        then:
        actual == expected

        where:
        expected | branch
        true     | "release/${generateInteger()}"
        false    | "Release/${generateInteger()}"
        false    | "release/${generateString()}"
        false    | 'master'
        false    | "feature/${generateString()}"
        false    | generateString()
    }
}

