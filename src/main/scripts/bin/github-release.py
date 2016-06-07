import os
import re
import sys
from xml.etree.ElementTree import Element
import xml.etree.ElementTree as ElementTree
import pprint
import subprocess
from etics.model.BuildStatus import BuildStatus
from etics.model import Module
import tarfile
from contextlib import closing

SRC_PACKAGE_SUFFIX = '-src.tar.gz'

DIST_SVNPATH_TAG = 'svnpath'
DIST_GITHUBPATH_TAG = 'githubpath'
GITHUB_RELEASE_REPO_URL = 'https://github.com/gcube-system/gcube-releases/tree/master'


def load_env():
        if 'GCUBE_RELEASE_TOOLKIT_HOME' not in os.environ:
            raise Exception("GCUBE_RELEASE_TOOLKIT_HOME environment "
                            "variable not defined. Cannot continue")

        bootstrap_file = '{0}/etc/bootstrap.sh'.format(
            os.environ['GCUBE_RELEASE_TOOLKIT_HOME'])
        command = ['bash', '-c', 'source {0} && env'.format(bootstrap_file)]

        proc = subprocess.Popen(command, stdout=subprocess.PIPE)

        for line in proc.stdout:
                (key, _, value) = line.partition("=")
                os.environ[key] = value.rstrip()
        proc.communicate()


class DistributionReport():
    """
    model the distribuion.xml report of gCube builds. It is used by the
    distribution site to popolate the release download page
    """

    xdoc = None
    tree = None
    file = None

    # eticsmodule -> package dictionary
    packages = {}

    def __init__(self, file):
        self.__load_from_xml(file)

    def __load_from_xml(self, file):
        self.file = file
        self.tree = ElementTree.parse(file)
        self.xdoc = self.tree.getroot()
        
        # in Python > 2.6: iter('Package'):
        for package in self.xdoc.getiterator('Package'):
            etics_module = package.find('ETICSRef')
            print etics_module.text
            if etics_module is not None:
                self.packages[etics_module.text] = DistributionPackage(package)

    def has_package_for_module(self, module_name):
        return module_name in self.packages

    def get_package_for_module(self, module_name):
        return self.packages[module_name]

    def save(self):
        self.tree.write(self.file)

        
class DistributionPackage():
    '''
    at the moment only svnpath, githubapath  and ETICSRef are modeled
    '''
    ETICSRef = None
    svnpath = None
    githubpath = None
    xdoc = None

    def __init__(self, xmlnode):
        self.__load_from_xml(xmlnode)
        # self.set_svnpath('') -- reset svnpath for all packages

    def __load_from_xml(self, xmlnode):
        self.xdoc = xmlnode
        self.ETICSRef = xmlnode.find('ETICSRef').text
        if xmlnode.find(DIST_SVNPATH_TAG) is not None:
            self.svnpath = xmlnode.find(DIST_SVNPATH_TAG).text
        if xmlnode.find(DIST_GITHUBPATH_TAG) is not None:
            self.svnpath = xmlnode.find(DIST_GITHUBPATH_TAG).text

    def set_svnpath(self, new_svnpath):
        self.svnpath = new_svnpath
        if self.xdoc.find(DIST_SVNPATH_TAG) is None:
            ElementTree.SubElement(self.xdoc, DIST_SVNPATH_TAG)
        self.xdoc.find(DIST_SVNPATH_TAG).text = new_svnpath

    def set_githubpath(self, new_githubpath):
        self.svnpath = new_githubpath
        if self.xdoc.find(DIST_GITHUBPATH_TAG) is None:
            ElementTree.SubElement(self.xdoc, DIST_GITHUBPATH_TAG)
        self.xdoc.find(DIST_GITHUBPATH_TAG).text = new_githubpath


def update_distribution_svnpath(distribution_report, modules):

    print("\n>>> Updating svnpath in distribution report")
    
    c = 0
    for m in modules:

        if m.name.endswith('-servicearchive'):
            continue

        etics_ref = m.name
        
        if not distribution_report.has_package_for_module(etics_ref):
            # try with the '-servicearchive'
            etics_ref = m.name + '-servicearchive'
            if not distribution_report.has_package_for_module(etics_ref):
                print("  WARNING: no distribution package found "
                      "for module {0}".format(m.name))
                continue
        
        if m.scmpath is None:
            print("  WARNING: no scmpath found for module {0}".format(m.name))
            continue

        package = distribution_report.get_package_for_module(etics_ref)
        package.set_svnpath(m.scmpath)
        c += 1

    distribution_report.save()
    print("  * Tot. modules: {0}"
          "  Tot. dist.pkgs: {1}"
          "  Updated svnpaths: {2} *".format(
            len(modules), len(distribution_report.packages), c))


def update_distribution_githubpath(distribution_report, github_paths):

    print("\n>>> Updating githubpath in distribution report")

    c = 0
    for module_name, release_path in github_paths.iteritems():
        if not distribution_report.has_package_for_module(module_name):
            print("WARNING no distribution package found "
                  "for {0}".format(module_name))
            continue
        package = distribution_report.get_package_for_module(module_name)
        package.set_githubpath(GITHUB_RELEASE_REPO_URL + release_path)
        c += 1

    distribution_report.save()
    print("  * Tot. modules: {0}"
          "  Tot. dist.pkgs: {1}"
          "  Updated githubpaths: {2} *".format(
            len(github_paths), len(distribution_report.packages), c))

    
def extract_version_from_src_pkg(src_pkg_file):

    begin = re.search(r"-[0-9]", src_pkg_file)
    end = src_pkg_file.find(SRC_PACKAGE_SUFFIX)

    if begin and end:
        return src_pkg_file[begin.start() + 1: end]
    else:
        return None
        #raise Exception("Couldn't extract version "
        #                "from {0}".format(src_pkg_file))


def get_module_release_path(module, src_pkg_file, prefer_maven_coords=False):
    """
    returns the relative path where the source package of this module will be
    unpacked
    :param module:
    :return:
    """

    group_id, artifact_id, version = extract_coordinates_from_pom(module)
    
    d = ""
    sd = module.subsystem
    if prefer_maven_coords and group_id is not None:
        sd = group_id
    if sd:
        if sd.startswith('org.gcube.'):
            sd = sd[10:]
        d += os.sep + sd.replace('.', os.sep)

    cd = module.name
    if prefer_maven_coords and artifact_id is not None:
        cd = artifact_id
    if cd.startswith('org.gcube.'):
        cd = cd[10:]
        if cd.startswith(sd):
            cd = cd[len(sd) + 1:]
    d += os.sep + cd

    v = extract_version_from_src_pkg(src_pkg_file)
    if v is None or prefer_maven_coords:
        v = version

    d += os.sep + v

    return d


# src_package name -> module_name dict
processed_src_pkgs = {}

# module_name -> release path dict
release_paths = {}


def create_github_release(release_path, modules):

    global processed_src_pkgs, release_paths

    processed_src_pkgs = {}
    release_paths = {}

    modules_names = modules.keys()
    modules_names.sort(cmp=modules_cmp)

    print("\n>>> Creating GitHub release folder")

    for m in modules_names:
        create_module_github_release(modules[m], release_path)


def create_module_github_release(module, release_folder):

        # 1) search the source package inside the module directory
        if not os.path.exists(module.checkout_folder):
                print("  WARNING: checkout folder for {0} does "
                      "not exist".format(module.name))
                return

        src_pkg_file = None

        # in case of multimodule projects, search only on the target dir of the
        # parent. In fact, the source package of the parent contains also the
        # source code of the submodules, so it is enough
        target_dir = module.checkout_folder + os.sep + 'target'

        if os.path.exists(target_dir):
            for file in os.listdir(target_dir):
                    if file.endswith(SRC_PACKAGE_SUFFIX):
                            src_pkg_file = file
                            break
                
        # 1.1) if source package is not found, exit
        if not src_pkg_file:
                print("  WARNING: source package "
                      "not found for {0}".format(module.name))
                return

        # 1.2) if the source package is already processed, it means that the
        # same package (same name) has been already found in another module.
        # This happens when multiple components have the same checkout location
        # (e.g. stubs and service components). We do not re-process it, only
        # map the release path of this component to the same release path
        # of the other component
        if src_pkg_file in processed_src_pkgs:
                print("  WARNING: duplicate source package found in {0}. "
                      "Using source package of {1}".format(
                          module.name, processed_src_pkgs[src_pkg_file]))
                release_paths[module.name] = \
                    release_paths[processed_src_pkgs[src_pkg_file]]
                return
        
        try:
            unpack_folder = get_module_release_path(
                module, src_pkg_file, prefer_maven_coords=False)
            processed_src_pkgs[src_pkg_file] = module.name
            release_paths[module.name] = unpack_folder

            # using closing because tarfile does not support the with statement
            # in Python < 2.7
            with closing(tarfile.open(
                    target_dir + os.sep + src_pkg_file)) as tar:
                tar.extractall(release_folder + os.sep + unpack_folder)

        except Exception as e:
            print("  ERROR: Error computing module release path "
                  "for {0} ({1})".format(module.name, e))
            return

        handle_multimodule_readme(module,
                                  release_folder + os.sep + unpack_folder)


def handle_multimodule_readme(module, output_folder):
    """if the component is a multimodule service (e.g. tree-manager)
       the distro folder is in the reactor module and shared by all
       modules (and also by the reactor), but the README that we want
       is the one filtered with the properties in the service module
       (for instance description is often defined also for service module).
       This function puts the README of the service module source package
       in the github release
       """

    pom_file = module.checkout_folder + os.sep + 'pom.xml'
    
    if not os.path.isfile(pom_file):
        # print("  WARNING: pom.xml not found for {0}".format(module.name))
        return (None, None, None)

    xmlroot = ElementTree.parse(pom_file).getroot()
    
    # find the namespace
    m = re.match('\{.*\}', xmlroot.tag)
    ns = m.group(0) if m else ''
    
    pom_modules = xmlroot.find(ns+'modules')

    if pom_modules is None:
        # not a multimodule project.
        return

    # select the service module
    service_module = None
    for m in pom_modules.getiterator(ns+'module'):
        if m.text.endswith('service'):
            service_module = m.text
            break

    if service_module is None:
        print("  WARNING: service submodule "
              "not found for {0}".format(module.name))
        return

    target_dir = module.checkout_folder + os.sep +\
        service_module + os.sep + 'target'

    src_pkg_file = None

    if os.path.exists(target_dir):
        for file in os.listdir(target_dir):
            if file.endswith(SRC_PACKAGE_SUFFIX):
                src_pkg_file = file
                break

    if src_pkg_file is None:
        print("  WARNING: source package not found "
              "in submodule {0} of {1}".format(service_module, module.name))
        return

    try:
        # using closing because tarfile does not support the with statement
        # in Python < 2.7
        with closing(tarfile.open(
                target_dir + os.sep + src_pkg_file)) as tar:
            tar.extract('README', path=output_folder)

    except Exception as e:
        print("  ERROR: Error copying README from submodule "
              "{0} of {1} ({2})".format(service_module, module.name, e))
        return


def extract_coordinates_from_pom(module):
    pom_file = module.checkout_folder + os.sep + 'pom.xml'
    
    if not os.path.isfile(pom_file):
        # print("  WARNING: pom.xml not found for {0}".format(module.name))
        return (None, None, None)

    xmlroot = ElementTree.parse(pom_file).getroot()
    
    # find the namespace
    m = re.match('\{.*\}', xmlroot.tag)
    ns = m.group(0) if m else ''

    gid = xmlroot.find(ns+'groupId').text
    aid = xmlroot.find(ns+'artifactId').text
    ver = xmlroot.find(ns+'version').text

    return (gid, aid, ver)


def modules_cmp(m1, m2):
    """Sort modules

    We sort modules before going through the list and create 
    GitHub release folders. Multiple modules can have the
    same src_pkg_file (e.g. multimodule Maven projects with
    two separate ETICS modules for service and stubs).
    The creation of release folders create a release folder
    for the src_pkg_file using the name of the *first* module
    in which that src_pkg_file is found. This function is 
    used to sort the modules so that more meaningful names
    comes first in the list
    E.g.
        softwaregateway > softwaregateway-stubs
        softwaregateway > softwaregateway-[anything]
        tree-manager-service > tree-manager-stubs
    """

    if m1.endswith('stubs'):
        return 1
    if m2.endswith('stubs'):
        return -1
    
    return len(m1) - len(m2)

if __name__ == '__main__':

    
    if len(sys.argv) < 4:
        print("Usage: {0} <distribution-report>  <build-type>"
              "<github-release-path>".format(sys.argv[0]))
        sys.exit(1)

    distribution_report_file = sys.argv[1]
    build_type = sys.argv[2]
    release_folder = sys.argv[3]

    if not os.path.exists(release_folder):
        print("Release folder {0} does not exists. Please create it "
              "before invoking {1}".format(release_folder, sys.argv[0]))
        sys.exit(1)

    load_env()
    workspace_dir = os.environ['ETICS_WORKSPACE']

    # 1) load modules, remove non gcube and non components
    build_status = BuildStatus(
            reportsLocation=workspace_dir + "/reports/", verbose=True)
    modules = {}

    # TODO use new getAllModules (since client version 0.27)
    for m in build_status.getModules():
        mod = Module(m, workspace_dir, build_status._getProjectConfigName())
        if mod.project != 'org.gcube' or mod.type != 'component':
            continue
        modules[m['name']] = mod

    # 2) load distribution report
    dr = DistributionReport(distribution_report_file)

    # 3) update svnpath
    update_distribution_svnpath(dr, modules.values())

    # 4) if this is a release build, we also create the github release source
    # code and update the distribution report with github paths
    if build_type == 'release':
        create_github_release(release_folder, modules)
        update_distribution_githubpath(dr, release_paths)
        
