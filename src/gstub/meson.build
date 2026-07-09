srcs = [
	'com/google/android/gms/providerinstaller/ProviderInstallerImpl.java',
]
java_args = [
    '-bootclasspath', bootclasspath,
    '-source', '1.8', '-target', '1.8',
    '-encoding', 'UTF-8',
    '-Xlint:-deprecation', # we implement deprecated APIs
    '-h', join_paths(dir_base, 'src/api-impl-jni/generated_headers')
]

ant = find_program('ant', required: false)

if ant.found()
    gstub_jar = custom_target('gstub',
                              output: 'gstub.jar',
                              command: [ant,
                                        '-buildfile', files('build.xml'),
                                        '-Dbasedir=' + builddir_base,
                                        '-Dsrcdir=' + meson.current_source_dir(),
                                        '-Dincludes=' + ','.join(srcs),
                                        '-Djarfile=@OUTPUT@',
                                        '-Dbuild=@PRIVATE_DIR@',
                                        '-Djavaargs=' + ' '.join(java_args + ['-classpath', hax_jar.full_path()]),
                              ],
                              depends: [hax_jar],
                              depend_files: srcs + ['build.xml'],
    )
else
    gstub_jar = jar('gstub', srcs,
                    link_with: [hax_jar],
                    java_args: java_args,)
endif
