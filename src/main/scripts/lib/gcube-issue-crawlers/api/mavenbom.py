import os
from etics.issues.basecrawler import IssueCrawler


class MavenBom(IssueCrawler):
    phases = ['post-vcs-checkout-success']
    severity = "high"
    type = "deprecated-api"
    subtype = "mavenbom"
    description = None
    tech_debt = "60"
    hint = "Refactor to use the portal-bom"
    references = []

    def _is_applicable(self, module, build_status, property_manager):
        if module.type != 'component':
            return False
        if module.checkout_type != 'vcs':
            return False
        if module.project != 'org.gcube':
            return False
        if 'gcore' in module.project_config:
            return False
        if not os.path.isdir(module.checkout_folder):
            return False
        if not os.path.isfile(self.__pom_location(module)):
            return False
        return True

    def _check(self, module, build_status, property_manager):

        maven_bom_string = '<artifactId>maven-bom</artifactId>'

        return maven_bom_string in open(self.__pom_location(module)).read()


    def __pom_location(self, module):
        return module.checkout_folder + os.sep + 'pom.xml'