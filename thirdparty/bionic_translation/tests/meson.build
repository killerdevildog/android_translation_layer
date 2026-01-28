test_runner = executable('test_runner', 'test_runner.c', '../main_executable/bionic_compat.c',
                         link_with: dl_bio, export_dynamic: true)

sh  = find_program('sh')

_run_in_bwrap = meson.current_source_dir() / '_run_in_bwrap.sh'
get_expected_to_pass = meson.current_source_dir() / 'get_expected_to_pass.sh'
testsuite_dir = meson.current_source_dir() / 'CtsBionicTestCases/'
expected_to_pass_dir = meson.current_source_dir() / 'expected_to_pass/'

libc = ''
glibc_test_code = '''#include <stdlib.h>
#ifdef __GLIBC__
// __GLIBC__ defined, using glibc
#else
#error "__GLIBC__ not defined, not using glibc"
#endif'''
if cc.compiles(glibc_test_code)
	libc = 'glibc'
else
	# assume musl
	libc = 'musl'
endif

arch = host_machine.cpu_family()

test_cmd = ' '.join([_run_in_bwrap, test_runner.full_path(), testsuite_dir, '--gtest_filter=`sh '+get_expected_to_pass+' '+libc+' '+arch+' '+expected_to_pass_dir+'`'])

test('bionic_tests_from_cts',
	sh,
	args: [ '-c', test_cmd ],
	depends: [
		test_runner,
		pthread_bio,
		c_bio,
		stdcpp_bio,
	],
	protocol: 'gtest',
	timeout: 3 * 60, # typically < 30
	env: [
		'BUILDDIR_PATH=' + meson.project_build_root(),
		'LINKER_IGNORE_UNKNOWN_RELOCS='
	],
)
