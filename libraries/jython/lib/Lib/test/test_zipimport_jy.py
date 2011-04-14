import unittest
import sys
import java.lang.Package

from test import test_support

class SyspathZipimportTest(unittest.TestCase):
    def setUp(self):
        self.orig_path = sys.path
        sys.path.insert(0, test_support.findfile("syspath_import.jar"))

        # TODO confirm that package is unloaded via a phantom ref or something like that
        
    def tearDown(self):
        sys.path = self.orig_path

    def test_load_class_from_syspath_zip(self):
        from syspathonly import Syspath
        self.assertEquals(Syspath.staticCall(), "result")

    def test_package_defined(self):
        from syspathonly import Syspath
        package = Syspath().class.package
        self.assert_(isinstance(package, java.lang.Package))
        self.assertEquals(package.name, 'syspathonly')

    def test_load_pkg_from_syspath(self):
        import syspathpkg
        self.assertEquals(syspathpkg.__name__, 'syspathpkg')
        self.assert_('syspath_import.jar' in syspathpkg.__file__)
        from syspathpkg import module
        self.assertEquals(module.__name__, 'syspathpkg.module')

def test_main():
    test_support.run_unittest(SyspathZipimportTest)

if __name__ == "__main__":
    test_main()