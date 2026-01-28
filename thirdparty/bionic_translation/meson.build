project('android_translation_layer', ['c', 'cpp'], default_options: ['b_lundef=false', 'b_asneeded=false'])

cc = meson.get_compiler('c')
libm_dep = cc.find_library('m', required : false)

c_args_common = [
	'-fPIC',
	'-D_GNU_SOURCE',
]

config = declare_dependency(
	sources: [
		'shared/config/config.c'
	],
	compile_args: [
		c_args_common,
	],
	include_directories: [
		'shared/config',
	]
)

wrapper = static_library('wrapper',
	sources: [
		'wrapper/wrapper.c'
	],
	pic: true,
	c_args: [
		c_args_common,
		'-fvisibility=hidden'
	]
)

dl_bio = shared_library('dl_bio',
	sources: [
		'linker/dlfcn.c',
		'linker/linker.c',
		'linker/linker_environ.c',
		'linker/linker_tls.c',
		'linker/rt.c',
		'linker/strlcpy.c'
	],
	version: '0.0.1', # this is not just a simple API shim, this is a shim dynamic linker with patches to aid us in our goals; it's possible, if unlikely, that the ABI will change
	install: true,
	dependencies: [
		dependency('dl'),
		dependency('egl'),
		dependency('libelf'),
		dependency('libunwind', version : '>=1.8.0'), # needed by libc_bio anyway, so might as well use it here
		config,
	],
	link_with: [
		wrapper
	],

	c_args: [
		c_args_common,
		'-D_LARGEFILE64_SOURCE',
		'-DLINKER_DEBUG=1',
		'-DVERBOSE_FUNCTIONS=1',
		'-Wno-parentheses',
	],
	link_args: [
		'-Wl,--no-as-needed',
		'-lpthread' # dependency('threads') doesn't express that we specifically need pthreads
	]
)

pthread_bio = shared_library('pthread_bio',
	sources: [
		'pthread_wrapper/libpthread.c'
	],
	soversion: 0, # we're shimming a stable ABI; this should never need to change
	install: true,
	dependencies: [
		dependency('dl'),
	],
	c_args: [
		c_args_common,
		'-fno-exceptions',
	],
	link_args: [
		'-Wl,--no-as-needed', # this really shouldn't be needed, since symbols from libpthread are used, but somehow it is
		'-lpthread' # dependency('threads') doesn't express that we specifically need pthreads
	]
)

libc_bio_hardcoded_symbols = [
	# glibc on most arches and all the ones that we support, and musl on all arches that
	# *it* supports, define FE_DFL_ENV as -1; bionic defines it as &__fe_dfl_env */
	'-Wl,--defsym,__fe_dfl_env=-1'
]

# meson doesn't let us just check for 32bit in general
if (host_machine.cpu_family() == 'arm' or host_machine.cpu_family() == 'x86' or get_option('c_link_args').contains('-m32'))
	# This symbol looks annoying to support properly, and likely no actual app will ever use it.
	# It is however needed by the testsuite, so hopefully this will make sure to let us know if
	# any actual app tries to use it. (the 0th page should cause a segfault, which in the worst
	# case of the app swallowing it will still be caught by gdb)
	libc_bio_hardcoded_symbols += '-Wl,--defsym,__bionic_brk=0x123'
endif

# libc_bio.so - c_bio looks weird, but remember that 'lib' will be prepended automatically
c_bio = shared_library('c_bio',
	sources: [
		'libc/libc.c',
		'libc/libc-chk.c',
		'libc/libc-math.c',
		'libc/libc-misc.c',
		'libc/libc-musl.c',
		'libc/libc-signal.c',
		'libc/libc-stdio.c',
		'libc/libc-sha1.c',
		'libc/libc-open-overrides.c',
	],
	soversion: 0, # we're shimming a stable ABI; this should never need to change
	install: true,
	dependencies: [
		dependency('libbsd'),
		dependency('libunwind'), # see comments on https://reviews.llvm.org/D78787; for 32bit arm we may need more fun
		libm_dep, # TODO: have separate libm_bio? (note: on musl this is a noop)
	],
	link_with: [
		wrapper,
		pthread_bio
	],
	c_args: [
		c_args_common,
		'-DPAGESIZE=4096',
	],
	link_args: [
		libc_bio_hardcoded_symbols,
		'-Wl,-wrap,_IO_file_xsputn',
		'-Wl,--no-as-needed', # needed to put stuff in DT_NEEDED that we don't necessarily directly use (sadly this doesn't apply to the link_with libraries, since they are specified before this on the cmdline)
		pthread_bio.full_path() # as-needed is the default on some platforms, and the explicit no-as-needed above only applies to libraries specified after it
	]
)

stdcpp_bio = shared_library('stdc++_bio',
	sources: [
		'libstdc++_standalone/new.cpp',
		'libstdc++_standalone/__cxa_pure_virtual.cpp',
		'libstdc++_standalone/__cxa_guard.cpp'
	],
	soversion: 0, # this tiny library's stable ABI will definitely never change, it's deprecated; this should never need to change
	install: true,
	include_directories: [
		'libstdc++_standalone/include/'
	],
	cpp_args: [
		'--std=c++23'
	]
)

install_data(
	sources: [
		'cfg.d/bionic_translation.cfg'
	],
	install_dir: get_option('datadir') / 'bionic_translation/cfg.d'
)

subdir('tests')
