import os
from etics.issues.basecrawler import IssueCrawler
from etics.issues.model import IssueReference



def get_README_location(module):
    #tries different standard location

    loc = module.checkout_folder + os.path.sep + "distro" + os.path.sep + "README"
    if os.path.isfile(loc):
       return loc

    loc = module.checkout_folder + os.path.sep + "gcube-distro" + os.path.sep + "README"
    if os.path.isfile(loc):
        return loc

    loc = module.checkout_folder + os.path.sep + "templates" + os.path.sep + "README"
    if os.path.isfile(loc):
        return loc

    loc = module.checkout_folder + os.path.sep + "README"
    if os.path.isfile(loc):
       return loc

    return None


class READMEExistCrawler(IssueCrawler):
    phases = ['post-vcs-checkout-success']
    severity = "high"
    type = "project-structure"
    subtype = "missing-readme"
    description = "README file not found"
    tech_debt = "20"
    hint = "Add a README file accordingly with the project guideline"
    references = [
        IssueReference('Wiki Guidelines',
                       'https://wiki.gcube-system.org/gcube/Maven_distro_directory_layout')]

    def _is_applicable(self, module, build_status, property_manager):
        if module.type != 'component':
            return False
        if module.name.endswith('servicearchive'):
            return False
        if module.name.endswith('testsuite'):
            return False
        if module.checkout_type != 'vcs':
            return False
        if module.project != 'org.gcube':
            return False
        if not os.path.isdir(module.checkout_folder):
            return False
        return True

    def _check(self, module, build_status, property_manager):
        readme_file = get_README_location(module)

        if readme_file is None:
            return True
        else:
            return False


class READMECompliant(READMEExistCrawler, IssueCrawler):
    phases = ['post-vcs-checkout-success']
    severity = "high"
    type = "project-structure"
    subtype = "invalid-readme"
    description = None
    tech_debt = "20"
    hint = "Refactor the README file accordingly with the project guidelines"
    references = [
        IssueReference('Wiki Guidelines',
                       'https://wiki.gcube-system.org/gcube/Maven_distro_directory_layout')]

    def _is_applicable(self, module, build_status, property_manager):
        return READMEExistCrawler._is_applicable(self, module, build_status, property_manager) and get_README_location(module) is not None


    def _get_issue(self, module, build_status, property_manager):
        readme_file = get_README_location(module)
        with file(readme_file) as f:
            s = f.read()

        problems_descr = []
        if not 'BlueBRIDGE (grant no. 675680)' in s \
                and not '${gcube.funding}' in s:
            problems_descr.append('reference to BlueBRIDGE project not found')

        if not 'wiki.gcube-system.org/' in s \
                and not '${gcube.wikiRoot}' in s:
            problems_descr.append('link(s) to wiki documentation not found')

        if len(problems_descr) > 0:
            descr = 'Following problems found: ' + ', '.join(problems_descr)
            return self._get_open_issue(module, description=descr)
        else:
            return self._get_cleaner_issue(module)
