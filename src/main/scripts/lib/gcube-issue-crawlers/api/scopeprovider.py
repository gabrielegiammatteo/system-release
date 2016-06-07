import os
from etics.issues.basecrawler import IssueCrawler


class ScopeProviderSet(IssueCrawler):
    phases = ['post-vcs-checkout-success']
    severity = "high"
    type = "deprecated-api"
    subtype = "scopeprovider-set"
    description = None
    tech_debt = "60"
    hint = "Refactor to use the scope provided by the Authz service"
    references = []

    def _is_applicable(self, module, build_status, property_manager):
        if module.type != 'component':
            return False
        if module.checkout_type != 'vcs':
            return False
        if module.project != 'org.gcube':
            return False
        if not os.path.isdir(module.checkout_folder):
            return False
        return True

    def _get_issue(self, module, build_status, property_manager):
        # traverse all the module directory looking for .java files
        # excludes 'test' directories
        matching_files = []
        for (dirpath, dirnames, filenames) in os.walk(module.checkout_folder):
            dirnames[:] = [d for d in dirnames if d not in ['test']]
            for filename in filenames:
                if filename.endswith('.java'):
                    with open(dirpath + os.sep + filename) as f:
                        c = f.read()
                        if 'ScopeProvider.instance.set(' in c:
                            matching_files.append(filename)

        if len(matching_files) == 0:
            return self._get_cleaner_issue(module)

        if len(matching_files) > 5:
            first = ', '.join(matching_files[:3])
            others = '{0} more files'.format(len(matching_files)-3)
            descr = 'The deprecated ScopeProvider.instance.set() ' \
                    'call is used in {0} and {1}'.format(first, others)
            return self._get_open_issue(module, description=descr)
        else:
            descr = 'The deprecated ScopeProvider.instance.set() ' \
                    'call is used in ' + ', '.join(matching_files)
            return self._get_open_issue(module, description=descr)
